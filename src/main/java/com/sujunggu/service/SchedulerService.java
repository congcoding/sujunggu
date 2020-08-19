package com.sujunggu.service;

import com.sujunggu.domain.board.Board;
import com.sujunggu.domain.board.BoardRepository;
import com.sujunggu.domain.department.Department;
import com.sujunggu.domain.department.DepartmentRepository;
import com.sujunggu.domain.post.Post;
import com.sujunggu.domain.post.PostPK;
import com.sujunggu.domain.post.PostRepository;
import com.sujunggu.domain.user.User;
import com.sujunggu.domain.user.UserRepository;
import com.sujunggu.web.dto.PostCrawlingDto;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SchedulerService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final AsyncService asyncService;

    private int crawlingCount[] = {0, 0};
    private int mailCount = 0;

    public int[] crawling() throws IOException {
        crawlingCount[0] = 0;
        crawlingCount[1] = 0;
        mailCount = 0;
        List<Board> boardList = boardRepository.findAll();

        for (Board b : boardList) {
            String url = "https://www.sungshin.ac.kr/" + b.getAddress()+ "/" + b.getBoardNo() + "/subview.do";
            crawlingByBoardNo(url, b.getBoardNo());
        }
        return crawlingCount;
    }

    @Transactional
    public void crawlingByBoardNo(String url, int boardNo) throws IOException {

        Document doc = Jsoup.connect(url).get();

        Elements postNoElements = doc.select("._artclTdNum");
        Elements titleElements = doc.select("._artclTdTitle > a > strong");
        Elements addressElements = doc.select("._artclTdTitle > a");

        for (int i = 0; i < postNoElements.size(); i++) {
            if ((postNoElements.get(i).html()).contains("span")) {
                continue; // span 태그가 있으면 상단공지로 지정된 경우라서 중복되므로 continue
            }

            PostPK pk = new PostPK(boardNo, Integer.parseInt(postNoElements.get(i).text())); // pk 생성
            Post p = postRepository.findById(pk).orElse(null); // pk를 이용해서 조회한 결과가 있으면 p에 저장하고, 없으면 p에 null 저장

            PostCrawlingDto postCrawlingDto = PostCrawlingDto.builder() // 크롤링한 데이터를 이용해서 postCrawlingDto 생성
                    .postPK(pk)
                    .title(titleElements.get(i).text())
                    .address(addressElements.get(i).attr("href"))
                    .build();

            if (p == null) {
                postRepository.save(postCrawlingDto.toEntity()); // 새로운 글이면 save
                crawlingCount[0]++;
            }
            else if (!((p.getTitle()).equals(postCrawlingDto.getTitle())) || !((p.getAddress()).equals(postCrawlingDto.getAddress()))) {
                p.updateTitle(postCrawlingDto.getTitle()); // 제목이 변경됐을 경우 update
                p.updateAddress(postCrawlingDto.getAddress());
                postRepository.save(p);
                crawlingCount[1]++;
            }
        }
    }

    public int sendMailByType(char type) {
        List<User> userList = new ArrayList<>();
        List<Post> postList = new ArrayList<>();

        if (type == 'h') { // 구독주기가 1시간인 경우
            userList = userRepository.findUserHourly();
            postList = postRepository.findByModifiedDateGreaterThan(LocalDateTime.now().minusMinutes(70));
        }
        else if (type == 'd') { // 구독주기가 1일인 경우
            userList = userRepository.findUserDaily();
            postList = postRepository.findByModifiedDateGreaterThan(LocalDateTime.now().minusMinutes(1450));
        }

        for (User u : userList) {
            List<String> subscriptionList = Arrays.asList(u.getSubscription().split(",")); // 구독중인 게시판 리스트
            List<Post> sendPostList = new ArrayList<>(); // 이메일로 보낼 포스트 리스트
            for (Post p : postList) {
                int boardNo = p.getPostPK().getBoardNo();
                if (subscriptionList.contains(Integer.toString(boardNo))) {
                    sendPostList.add(p); // 구독중인 게시판의 Post면 sendPostList에 추가
                }
            }
            if (!sendPostList.isEmpty()) { // sendPostList가 비어있지 않으면 이메일 발송
                asyncService.sendMail(u, sendPostList);
                mailCount++;
            }
        }
        return mailCount;
    }


}
