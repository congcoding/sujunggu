package com.sujunggu.service;

import com.sujunggu.domain.board.Board;
import com.sujunggu.domain.board.BoardRepository;
import com.sujunggu.domain.post.Post;
import com.sujunggu.domain.post.PostPK;
import com.sujunggu.domain.post.PostRepository;
import com.sujunggu.web.dto.PostCrawlingDto;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SchedulerService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;

    public void crawling() throws IOException {
        List<Board> boardList = boardRepository.findAll();

        for (Board b : boardList) {
            String url = "https://www.sungshin.ac.kr/" + b.getAddress()+ "/" + b.getBoardNo() + "/subview.do";
            crawlingByBoardNo(url, b.getBoardNo());
        }
    }

    @Transactional
    public void crawlingByBoardNo(String url, int boardNo) throws IOException {

        Document doc = Jsoup.connect(url).get();

        Elements postNoElements = doc.select("._artclTdNum");
        Elements titleElements = doc.select("._artclTdTitle strong");
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
            }
            else if (!((p.getTitle()).equals(postCrawlingDto.getTitle()))) {
                p.update(postCrawlingDto.getTitle()); // 제목이 변경됐을 경우 update
            }
        }
    }
}
