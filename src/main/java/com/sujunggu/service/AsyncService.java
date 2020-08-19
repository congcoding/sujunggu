package com.sujunggu.service;

import com.sujunggu.domain.board.Board;
import com.sujunggu.domain.board.BoardRepository;
import com.sujunggu.domain.department.Department;
import com.sujunggu.domain.department.DepartmentRepository;
import com.sujunggu.domain.post.Post;
import com.sujunggu.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AsyncService {

    private final BoardRepository boardRepository;
    private final DepartmentRepository departmentRepository;
    private final JavaMailSender mailSender;

    @Async
    public void sendMail(User u, List<Post> sendPostList) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(msg, true, "UTF-8");

            messageHelper.setSubject("[수정구] 구독중인 게시판에 공지사항 " + sendPostList.size() + "건이 등록/수정되었습니다.");

            String html = "구독중인 게시판에 올라온 새로운 글과 수정된 글은 아래와 같습니다. 클릭하면 해당 글로 이동합니다.<br /><br />";
            for (Post p : sendPostList) {
                Board b = boardRepository.findOneByBoardNo(p.getPostPK().getBoardNo());
                Department d = departmentRepository.findOneByAddress(b.getAddress());
                String title = "";
                if (p.getCreatedDate() != p.getModifiedDate()) { // 수정된 글일 경우
                    title = "[수정][" + d.getMajor() + "-" + b.getName() + "] " + p.getTitle();
                }
                else {
                    title = "[신규][" + d.getMajor() + "-" + b.getName() + "] " + p.getTitle();
                }

                html += "<a href='https://www.sungshin.ac.kr" + p.getAddress() + "'>" + title + "</a><br />";
            }
            html += "<br />구독 게시판, 구독 주기 변경은 <a href='https://www.수정구.com'>www.수정구.com</a>에서 가능합니다.";

            messageHelper.setText("", html);
            messageHelper.setTo(u.getEmail());
            msg.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(u.getEmail()));
            mailSender.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
