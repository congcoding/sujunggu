package com.sujunggu.web;

import com.sujunggu.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class SchedulerController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SchedulerService schedulerService;

    // cron : 초(0-59) 분(0-59) 시간(0-23) 일(1-31) 월(1-12) 요일(0-7 일월화수목금토)
    // 0 * * * * * : 매 0초에 실행 (1분 주기)
    // 0 0 * * * * : 매 0분에 실행 (1시간 주기)
    @Scheduled(cron = "0 0 * * * *")
    public void schedulingHourly() throws IOException, InterruptedException {

        // 크롤링
        logger.info("==================== 크롤링 시작 ====================");
        schedulerService.crawling();
        logger.info("==================== 크롤링 끝 ====================");

        // 이메일 발송
        logger.info("==================== Hourly 이메일 발송 시작 ====================");
        schedulerService.sendMailByType('h');
        logger.info("==================== Hourly 이메일 발송 끝 ====================");
    }

    @Scheduled(cron = "0 30 18 * * *")
    public void schedulingDaily() {

        // 이메일 발송
        logger.info("==================== Daily 이메일 발송 시작 ====================");
        schedulerService.sendMailByType('d');
        logger.info("==================== Daily 이메일 발송 끝 ====================");
    }

}
