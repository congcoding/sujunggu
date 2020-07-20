package com.sujunggu.service;

import com.sujunggu.domain.user.User;
import com.sujunggu.domain.user.UserRepository;
import com.sujunggu.domain.user.Role;
import com.sujunggu.web.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public String joinUser(UserDto userDto) {
        User u = userRepository.findByEmail(userDto.getEmail()).orElse(null);

        // 이미 가입된 이메일인 경우 에러 처리
        if (u != null) {
            throw new IllegalArgumentException("입력한 이메일 주소 \"" + userDto.getEmail() + "\"은\n이미 가입된 이메일입니다.");
        }

        // 비밀번호 암호화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // 인증키 생성
        String authKey = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
        userDto.setActive(authKey);

        // 인증키 이메일 발송
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(msg, true, "UTF-8");

            messageHelper.setSubject("수정구 이메일 인증 링크 안내 메일입니다.");
            messageHelper.setText("", "<a href='http://3.34.222.66/user/active?email=" + userDto.getEmail() + "&active=" + authKey + "'>인증 링크</a>를 클릭하면 회원가입이 완료됩니다.");
            messageHelper.setTo(userDto.getEmail());
            msg.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(userDto.getEmail()));
            mailSender.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return userRepository.save(userDto.toEntity()).getEmail();
    }

    @Transactional
    public UserDetails updateActive(UserDto userDto) {
        User u = userRepository.findByEmail(userDto.getEmail()).orElseThrow(() -> new IllegalArgumentException("정상적인 접근이 아닙니다."));

        // DB에 저장된 active값과 userDto의 active값이 같으면 active를 "Y"로 update
        if (userDto.getActive().equals(u.getActive())) {
            u.updateActive("Y");
        } else {
            throw new IllegalArgumentException("정상적인 접근이 아닙니다.");
        }

        return loadUserByUsername(userDto.getEmail());
    }

    @Transactional
    public String updateTempPassword(String email) {
        User u = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("입력한 이메일 주소 \"" + email + "\"은\n 가입된 이메일이 아닙니다."));

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
            messageHelper.setText("임시 비밀번호는 " + tempPassword + " 입니다.");
            messageHelper.setTo(email);
            msg.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(email));
            mailSender.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
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
