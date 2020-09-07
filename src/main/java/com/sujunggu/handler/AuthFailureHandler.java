package com.sujunggu.handler;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) {

        // 가입된 이메일이 아닐 때
        if(exception instanceof InternalAuthenticationServiceException) {
            throw new IllegalArgumentException("가입된 이메일이 아닙니다.");
        }

        // 비밀번호가 일치하지 않을 때
        if(exception instanceof BadCredentialsException) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
    }
}
