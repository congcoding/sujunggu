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
    @Scheduled(cron = "0 11 * * * *")
    public void scheduling() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date();
        String strDate = sdf.format(now);
        System.out.println("크롤링 시작 시각 : " + strDate);

        schedulerService.crawling();

        // schedulerService.sendMail();

        now = new Date();
        strDate = sdf.format(now);
        System.out.println("크롤링 끝난 시각 : " + strDate);
    }
}
