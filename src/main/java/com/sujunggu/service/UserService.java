package com.sujunggu.service;

import com.sujunggu.domain.board.Board;
import com.sujunggu.domain.board.BoardRepository;
import com.sujunggu.domain.user.User;
import com.sujunggu.domain.user.UserRepository;
import com.sujunggu.domain.user.Role;
import com.sujunggu.web.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public String joinUser(UserDto userDto) {
        User u = userRepository.findByEmail(userDto.getEmail()).orElse(null);

        // 이미 가입된 이메일인 경우 에러 처리
        if (u != null) {
            throw new IllegalArgumentException("입력한 이메일 주소 \"" + userDto.getEmail() + "\"은\n이미 가입된 이메일입니다.");
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword())); // 비밀번호 암호화해서 저장
        userDto.setSubscription(""); // subscription 기본값 ""로 설정
        userDto.setPeriod('d'); // period 기본값 'd'로 설정

        // 인증키 생성 후 저장
        String authKey = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
        userDto.setActive(authKey);

        // user 저장
        userRepository.save(userDto.toEntity()).getEmail();

        return authKey;
    }

    @Transactional
    public String updateAuth(String email) {
        User u = userRepository.findByEmail(email).orElse(null);
        String authKey = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
        u.updateActive(authKey);
        return authKey;
    }

    @Async
    public void sendAuthKey(String email, String authKey) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(msg, true, "UTF-8");

            messageHelper.setSubject("수정구 회원가입 이메일 인증 메일입니다.");
            messageHelper.setText("", "<h3>수정구 회원가입 이메일 인증</h3><a href='https://www.수정구.com/user/active?email=" + email + "&active=" + authKey + "'>인증 링크</a>를 클릭하면 회원가입이 완료됩니다.");
            messageHelper.setTo(email);
            messageHelper.setFrom(new InternetAddress("subsforsujung@gmail.com", "subsforsujung"));
            mailSender.send(msg);
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public boolean updateActive(UserDto userDto) {
        User u = userRepository.findByEmail(userDto.getEmail()).orElseThrow(() -> new IllegalArgumentException("정상적인 접근이 아닙니다."));

        // DB에 저장된 active값과 userDto의 active값이 같으면 active를 "Y"로 update
        if (userDto.getActive().equals(u.getActive())) {
            u.updateActive("Y");
        } else {
            return false;
        }
        return true;
    }

    @Transactional
    public String updateTempPassword(String email) {
        User u = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("입력한 이메일 주소 \"" + email + "\"은\n가입된 이메일이 아닙니다."));

        // 10자리 임시 비밀번호 생성
        String tempPassword = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);

        // 임시 비밀번호 암호화해서 update
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        u.updatePassword(passwordEncoder.encode(tempPassword));

        return tempPassword;
    }

    public void sendTempPassword(String email, String tempPassword) {
        // 임시 비밀번호 이메일로 전송
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(msg, true, "UTF-8");
            messageHelper.setSubject("수정구 임시 비밀번호 안내 메일입니다.");
            messageHelper.setText("임시 비밀번호는 " + tempPassword + " 입니다.\n임시 비밀번호로 로그인 후 비밀번호를 변경해 주세요.");
            messageHelper.setTo(email);
            messageHelper.setFrom(new InternetAddress("subsforsujung@gmail.com", "subsforsujung"));
            mailSender.send(msg);
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void updatePassword(String email, String oldPassword, String newPassword) {
        User u = userRepository.findByEmail(email).orElse(null);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (passwordEncoder.matches(oldPassword, u.getPassword())) {
            u.updatePassword(passwordEncoder.encode(newPassword));
        }
        else {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    public List<Board> getBoardList() {
        List<Board> boardList = boardRepository.findAll();
        return boardList;
    }

    public List<Board> getSubscriptionList(String email) {
        User u = userRepository.findByEmail(email).orElse(null);
        String[] subscriptionArray = u.getSubscription().split(",");
        List<Board> subscriptionList = new ArrayList<>();
        for (String str : subscriptionArray) {
            if (!str.equals(""))
                subscriptionList.add(boardRepository.findOneByBoardNo(Integer.parseInt(str)));
        }
        return subscriptionList;
    }

    public char getPeriod(String email) {
        User u = userRepository.findByEmail(email).orElse(null);
        return u.getPeriod();
    }

    @Transactional
    public void updateSetting(String email, String subscription, char period) {
        User u = userRepository.findByEmail(email).orElse(null);
        u.updateSetting(subscription, period);
    }

    @Transactional
    public void deleteUser(String email) {
        User u = userRepository.findByEmail(email).orElse(null);
        userRepository.delete(u);
    }

    @Override
    public UserDetails loadUserByUsername(String email) { // login-form에서 name="username"으로 요청해야 함

        Optional<User> userEntityWrapper = userRepository.findByEmail(email);
        User u = userEntityWrapper.get();

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (("subsforsujung@gmail.com").equals(email)) { // 이메일이 subsforsujung@gmail.com이면 ADMIN
            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue()));
        } else if ("Y".equals(u.getActive())) { // active값이 "Y"면 USER
            authorities.add(new SimpleGrantedAuthority(Role.USER.getValue()));
        } else { // active값이 "Y"가 아니면 TEMP
            authorities.add(new SimpleGrantedAuthority(Role.TEMP.getValue()));
        }

        // SpringSecurity에서 제공하는 UserDetails를 구현한 User return
        return new org.springframework.security.core.userdetails.User(u.getEmail(), u.getPassword(), authorities);
    }

}
