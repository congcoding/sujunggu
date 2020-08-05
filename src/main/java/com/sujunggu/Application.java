package com.sujunggu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync // Async 활성화
@EnableScheduling // Scheduling 활성화
@EnableJpaAuditing // JPA Auditing 활성화
@SpringBootApplication
public class Application { // 스프링 부트의 자동 설정, 스프링 Bean 읽기와 생성을 모두 자동으로 설정
    public static void main(String[] args) { // 프로젝트의 메인 클래스 (@SpringBootApplication이 있는 위치부터 설정을 읽어가기 때문에 항상 프로젝트의 최상단에 위치해야 함)
        SpringApplication.run(Application.class, args); // 내장 WAS(Tomcat) 실행
    }
}
