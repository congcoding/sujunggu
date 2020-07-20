package com.sujunggu.domain.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class User {

    @Id
    private String email;

    private String password;

    @Column(columnDefinition = "TEXT")
    private String subscription;

    @Column(columnDefinition = "CHAR", length = 1)
    private char period;

    private String active;

    @Builder
    public User(String email, String password, String subscription, char period, String active) {
        this.email = email;
        this.password = password;
        this.subscription = subscription;
        this.period = period;
        this.active = active;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateActive(String active) {
        this.active = active;
    }
}
