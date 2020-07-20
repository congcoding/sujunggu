package com.sujunggu.web;

import com.sujunggu.service.UserService;
import com.sujunggu.web.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    // 메인 페이지
    @GetMapping("/")
    public String index() {
        return "/index";
    }

    // 회원가입 페이지
    @GetMapping("/user/signup")
    public String dispSignup() {
        return "/signup";
    }

    // 회원가입 처리
    @PostMapping("/user/signup")
    public String execSignup(UserDto userDto) {
        userService.joinUser(userDto);
        return "/signup";
    }

    // 이메일 인증 처리
    @GetMapping("/user/active")
    public String execActive(UserDto userDto) {
        userService.updateActive(userDto);
        return "/active";
    }

    // 비밀번호 찾기 페이지
    @GetMapping("/user/find-password")
    public String dispFindPassword() { return "/findpassword"; }

    // 비밀번호 찾기
    @PostMapping("/user/send-temp-password")
    public String execSendTempPassword(String email) {
        String tempPassword = userService.updateTempPassword(email);
        userService.sendTempPassword(email, tempPassword);
        return "/findpassword";
    }

    // 접근 거부 페이지
    @GetMapping("/user/denied")
    public String dispDenied() {
        return "/denied";
    }

}