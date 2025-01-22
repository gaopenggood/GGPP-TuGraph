package com.ggpp.tugraph.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@EnableScheduling
@Component
@Slf4j
public class MainTask {

    @Scheduled(cron = "0 * * * * *")
    public void cleanOtmp() {
        log.info("执行定时器任务："+new Date());
    }
}
