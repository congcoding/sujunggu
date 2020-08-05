package com.sujunggu.web;

import com.sujunggu.domain.board.Board;
import com.sujunggu.service.UserService;
import com.sujunggu.web.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;

@Controller
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    // 메인 페이지
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // 회원가입 페이지
    @GetMapping("/user/signup")
    public String dispSignup() {
        return "signup";
    }

    // 회원가입 처리
    @PostMapping("/user/signup")
    public String execSignup(UserDto userDto) {
        userService.joinUser(userDto);
        return "signup";
    }

    // 이메일 인증 처리
    @GetMapping("/user/active")
    public String execActive(UserDto userDto) {
        userService.updateActive(userDto);
        return "active";
    }

    // 비밀번호 찾기 페이지
    @GetMapping("/user/find-password")
    public String dispFindPassword() { return "findpassword"; }

    // 비밀번호 찾기
    @PostMapping("/user/send-temp-password")
    public String execSendTempPassword(String email) {
        String tempPassword = userService.updateTempPassword(email);
        userService.sendTempPassword(email, tempPassword);
        return "findpassword";
    }

    // 구독 설정 페이지
    @GetMapping("/user/setting")
    public String dispSetting(Principal principal, Model model) {
        List<Board> boardList = userService.getBoardList();
        List<Board> subscriptionList = userService.getSubscriptionList(principal.getName());
        char period = userService.getPeriod(principal.getName());
        model.addAttribute("boardList", boardList);
        model.addAttribute("subscriptionList", subscriptionList);
        model.addAttribute("period", period);
        return "setting";
    }

    // 구독 설정 변경
    @PutMapping("/user/setting")
    public String execSetting(Principal principal, String subscription, char period) {
        userService.updateSetting(principal.getName(), subscription, period);
        return "index";
    }

    // 회원 탈퇴
    @DeleteMapping("/user/signout")
    public String execSignout(Principal principal) {
        SecurityContextHolder.clearContext(); // 회원탈퇴시 SecurityContext clear
        userService.deleteUser(principal.getName());
        return "index";
    }

    // 접근 거부 페이지
    @GetMapping("/user/denied")
    public String dispDenied() {
        return "denied";
    }

}