package com.sujunggu.web.dto;

import com.sujunggu.domain.user.User;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserDto {

    private String email;
    private String password;
    private String subscription;
    private char period;
    private String active;

    public User toEntity() {
        return User.builder()
                .email(email)
                .password(password)
                .subscription(subscription)
                .period(period)
                .active(active)
                .build();
    }

    @Builder
    public UserDto(String email, String password, String subscription, char period, String active) {
        this.email = email;
        this.password = password;
        this.subscription = subscription;
        this.period = period;
        this.active = active;
    }

}
