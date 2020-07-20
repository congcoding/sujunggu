package com.sujunggu.config;

import com.sujunggu.handler.AuthFailureHandler;
import com.sujunggu.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity // Spring Security 사용 (WebSecurityConfigurerAdapter 상속)
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final AuthFailureHandler authFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 암호화 객체
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // static 디렉터리의 하위 파일 목록은 인증 무시
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // 페이지 권한 설정
                .antMatchers("/admin/**").hasRole("ADMIN") // /admin으로 시작하는 경로는 ADMIN 롤을 가진 사용자만 접근 가능
                .antMatchers("/user/setting").hasRole("USER") // /user/setting 경로는 MEMBER 롤을 가진 사용자만 접근 가능
                .antMatchers("/**").permitAll() // 모든 경로에 대해서 권한없이 접근 가능
                .and() // 로그인 설정
                .formLogin() // form기반으로 인증
                .loginPage("/user/login") // 커스텀 로그인 폼 사용
                .defaultSuccessUrl("/") // 로그인이 성공했을 때 이동되는 페이지
                .failureHandler(authFailureHandler) // 로그인 실패 Handler
                .permitAll()
                .and() // 로그아웃 설정
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout")) // /logout이 아닌 다른 URL로 재정의
                .logoutSuccessUrl("/") // 로그아웃이 성공했을 때 이동되는 페이지
                .invalidateHttpSession(true) // HTTP Session 초기화
                .and()
                // 403 예외처리 핸들링
                .exceptionHandling().accessDeniedPage("/user/denied"); // 예외가 발생했을 때(접근권한이 없을 때) 로그인 페이지로 이동
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

}
