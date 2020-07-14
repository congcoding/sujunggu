package com.sujunggu.domain.department;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@NoArgsConstructor
@Entity
public class Department {

    @Id // PK 필드
    private String address;

    private String college;
    private String major;

    @Builder // 생성자 대신 빌더 클래스 사용
    public Department(String address, String college, String major) {
        this.address = address;
        this.college = college;
        this.major = major;
    }
}
