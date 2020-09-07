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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class SchedulerService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JavaMailSender mailSender;

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Value("${portal.id}")
    private String PORTAL_ID;

    @Value("${portal.pwd}")
    private String PORTAL_PWD;

    @Value("${selenium.driver.id}")
    private String DRIVER_ID;

    @Value("${selenium.driver.path}")
    private String DRIVER_PATH;

    private int postCreatedCount;
    private int postModifiedCount;

    public void crawling() throws IOException, InterruptedException {
        postCreatedCount = 0;
        postModifiedCount = 0;

        // 학과별 게시판 크롤링
        logger.info("==================== 학과별 게시판 크롤링 시작 ====================");
        List<Board> boardList = boardRepository.findAll();
        for (Board b : boardList) {
            String url = "https://www.sungshin.ac.kr/" + b.getAddress()+ "/" + b.getBoardNo() + "/subview.do";
            crawlingByBoardNo(url, b.getBoardNo());
        }
        logger.info("학과별 게시판 크롤링 결과 : 신규(" + postCreatedCount + "건), 수정(" + postModifiedCount + "건)");
        logger.info("==================== 학과별 게시판 크롤링 끝 ====================");

        postCreatedCount = 0;
        postModifiedCount = 0;

        logger.info("==================== 포탈 크롤링 시작 ====================");
        crawlingPortal("https://portal.sungshin.ac.kr/portal/ssu/menu/notice/ssuboard02.page", 8656); // 학부학사 크롤링
        crawlingPortal("https://portal.sungshin.ac.kr/portal/ssu/menu/notice/ssuboard10.page", 9616); // 학부장학 크롤링
        logger.info("포탈 게시판 크롤링 결과 : 신규(" + postCreatedCount + "건), 수정(" + postModifiedCount + "건)");
        logger.info("==================== 포탈 크롤링 끝 ====================");

    }

    @Transactional
    public void crawlingByBoardNo(String url, int boardNo) throws IOException {

        Document doc = Jsoup.connect(url).timeout(20000).get();

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
                logger.info("[신규 크롤링] " + postCrawlingDto.toEntity().toString());
                postCreatedCount++;
            }
            else if (!((p.getTitle()).equals(postCrawlingDto.getTitle())) || !((p.getAddress()).equals(postCrawlingDto.getAddress()))) {
                logger.info("[수정 크롤링 : 전] " + p.toString());
                p.updateTitle(postCrawlingDto.getTitle()); // 제목이 변경됐을 경우 update
                p.updateAddress(postCrawlingDto.getAddress()); // 주소가 변경됐을 경우 update
                logger.info("[수정 크롤링 : 후] " + postCrawlingDto.toEntity().toString());
                postRepository.save(p);
                postModifiedCount++;
            }
        }
    }

    public void crawlingPortal(String url, int boardNo) throws IOException, InterruptedException {

        // 변수 설정
        System.setProperty(DRIVER_ID, DRIVER_PATH);

        // 크롬옵션 설정
        ChromeOptions options = new ChromeOptions();
        options.setCapability("ignoreProtectedModeSettings", true);
        options.addArguments("headless"); //창 안 보이게 하는 옵션
        options.addArguments("--disable-dev-shm-usage"); // linux 성능 개선 옵션
        options.addArguments("--no-sandbox"); // linux 성능 개선 옵션
        options.addArguments("lang=ko"); // 한글 옵션
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(180, TimeUnit.SECONDS); // 페이지 로딩 시간 3분까지 기다리게 하기

        // 포탈 로그인
        driver.get("https://portal.sungshin.ac.kr/sso/login.jsp");
        WebElement inputId = driver.findElement(By.id("loginId_mobile"));
        WebElement inputPwd = driver.findElement(By.id("loginPwd_mobile"));
        WebElement btnLogin = driver.findElements(By.className("login_btn")).get(0);
        inputId.sendKeys(PORTAL_ID);
        inputPwd.sendKeys(PORTAL_PWD);
        btnLogin.click();
        Thread.sleep(3000);

        // 학부학사 iframe으로 이동
        driver.get(url);
        Thread.sleep(3000);
        driver.switchTo().frame(driver.findElement(By.id("IframePortlet_" + Integer.toString(boardNo))));
        Thread.sleep(3000);

        // 게시글 크롤링
        List<WebElement> aList = driver.findElements(By.cssSelector("table[class='list web'] > tbody > tr > td[class='L inLi'] > div > a"));

        for (int i = 0; i < 10; i++) {
            String title = aList.get(i).getAttribute("title");
            String postNo = aList.get(i).getAttribute("onclick").substring(39, 48);

            PostPK pk = new PostPK(boardNo, Integer.parseInt(postNo)); // pk 생성
            Post p = postRepository.findById(pk).orElse(null); // pk를 이용해서 조회한 결과가 있으면 p에 저장하고, 없으면 p에 null 저장

            PostCrawlingDto postCrawlingDto = PostCrawlingDto.builder() // 크롤링한 데이터를 이용해서 postCrawlingDto 생성
                    .postPK(pk)
                    .title(title)
                    .address("")
                    .build();

            if (p == null) {
                postRepository.save(postCrawlingDto.toEntity()); // 새로운 글이면 save
                logger.info("[신규 크롤링] " + postCrawlingDto.toEntity().toString());
                postCreatedCount++;
            }
            else if (!((p.getTitle()).equals(postCrawlingDto.getTitle()))) {
                logger.info("[수정 크롤링 : 전] " + p.toString());
                p.updateTitle(postCrawlingDto.getTitle()); // 제목이 변경됐을 경우 update
                logger.info("[수정 크롤링 : 후] " + postCrawlingDto.toEntity().toString());
                postRepository.save(p);
                postModifiedCount++;
            }
        }
        driver.quit();
    }

    public MimeMessagePreparator[] getPreparatorArrayByType(char type) {
        List<User> userList = new ArrayList<>();
        List<Post> postList = new ArrayList<>();

        if (type == 'h') { // 구독주기가 1시간인 경우
            userList = userRepository.findUserHourly();
            postList = postRepository.findByModifiedDateGreaterThan(LocalDateTime.now().minusMinutes(10));
        }
        else if (type == 'd') { // 구독주기가 1일인 경우
            userList = userRepository.findUserDaily();
            postList = postRepository.findByModifiedDateGreaterThan(LocalDateTime.now().minusMinutes(1450));
        }

        List<MimeMessagePreparator> preparatorList = new ArrayList<>();
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
                setPreparatorList(preparatorList, u, sendPostList);
            }
        }
        return preparatorList.toArray(new MimeMessagePreparator[preparatorList.size()]);
    }

    public void setPreparatorList(List preparatorList, User u, List<Post> sendPostList) {
        preparatorList.add(new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage msg) throws Exception {
                MimeMessageHelper messageHelper = new MimeMessageHelper(msg, true, "UTF-8");
                messageHelper.setSubject("[수정구] 구독중인 게시판에 공지사항 " + sendPostList.size() + "건이 등록/수정되었습니다.");

                String html = "구독중인 게시판에 올라온 새로운 글과 수정된 글은 아래와 같습니다<br />";
                html += "(학과별 공지사항의 경우 클릭하면 해당 글로 이동하고, 포탈 공지사항은 포탈로 이동합니다.)<br /><br />";
                for (Post p : sendPostList) {
                    Board b = boardRepository.findOneByBoardNo(p.getPostPK().getBoardNo());
                    Department d = departmentRepository.findOneByAddress(b.getAddress());
                    String title = "";
                    if (p.getCreatedDate().getHour() != p.getModifiedDate().getHour()) { // 수정된 글 경우
                        title = "[수정][" + d.getMajor() + "-" + b.getName() + "] " + p.getTitle();
                    }
                    else { // 새로운 글인 경우
                        title = "[신규][" + d.getMajor() + "-" + b.getName() + "] " + p.getTitle();
                    }

                    if ("".equals(p.getAddress())) { // 포탈 게시글인 경우
                        html += "<a href='https://portal.sungshin.ac.kr/portal'>" + title + "</a><br />";
                    }
                    else { // 학과별 게시글인 경우
                        html += "<a href='https://www.sungshin.ac.kr" + p.getAddress() + "'>" + title + "</a><br />";
                    }
                }
                html += "<br />구독 게시판, 구독 주기 변경은 <a href='https://www.수정구.com'>www.수정구.com</a>에서 가능합니다.";

                messageHelper.setText(html, true);
                messageHelper.setTo(u.getEmail());
            }
        });
    }

    @Async
    public void sendMail(MimeMessagePreparator[] preparatorArray) {
        mailSender.send(preparatorArray);
        logger.info("==================== 이메일 발송 끝 (" + preparatorArray.length + "건) ====================");
    }
}
