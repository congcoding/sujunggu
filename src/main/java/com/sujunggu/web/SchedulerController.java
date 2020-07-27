package com.sujunggu.web;

import com.sujunggu.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class SchedulerController {

    private final SchedulerService schedulerService;

    // cron : 초(0-59) 분(0-59) 시간(0-23) 일(1-31) 월(1-12) 요일(0-7 일월화수목금토)
    // 0 * * * * * : 매 0초에 실행 (1분 주기)
    // 0 0 * * * * : 매 0분에 실행 (1시간 주기)
    @Scheduled(cron = "0 0 * * * *")
    public void schedulingHourly() throws IOException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        // 게시판 크롤링
        System.out.println("크롤링 시작 시각 : " + sdf.format(new Date()));
        int crawlingCount[] = schedulerService.crawling();
        System.out.println("크롤링 끝난 시각 : " + sdf.format(new Date()));
        System.out.println("크롤링 결과 : 추가(" + crawlingCount[0] + "건), 수정(" + crawlingCount[1] + "건)");

        // 이메일 발송
        System.out.println("이메일 발송 시작 시각 : " + sdf.format(new Date()));
        int mailCount = schedulerService.sendMailByType('h');
        System.out.println("이메일 발송 끝난 시각 : " + sdf.format(new Date()));
        System.out.println("Hourly 이메일 발송 결과 : " + mailCount + "건)");
    }

    @Scheduled(cron = "0 30 18 * * *")
    public void schedulingDaily() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        // 이메일 발송
        System.out.println("이메일 발송 시작 시각 : " + sdf.format(new Date()));
        int mailCount = schedulerService.sendMailByType('h');
        System.out.println("이메일 발송 끝난 시각 : " + sdf.format(new Date()));
        System.out.println("Daily 이메일 발송 결과 : " + mailCount + "건)");
    }
}
