package com.ggpp.tugraph.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.util.Date;

@EnableScheduling
@Component
@Slf4j
public class MainTask {

    private boolean DAYLY = true;

    private boolean WEEKLY = false;

    @Scheduled(cron = "0 10 0 * * *")
    public void formatterAttachData2DocList() {
        this.getFrequency();
        if(DAYLY) {
            LocalDateTime now = LocalDateTime.now();
            // 获取前一天的日期
            LocalDateTime start = now.minusDays(1).with(LocalTime.MIN); // 前一天0点00分
            LocalDateTime end = now.minusDays(1).with(LocalTime.MAX);   // 前一天23点59分59秒


            log.info("执行定时器任务："+new Date());
        }

    }

    @Scheduled(cron = "0 20 0 ? * SAT")
    public void formatterAttacjData2DocList2() {
        if(WEEKLY) {
            log.info("执行定时器任务："+new Date());
        }
    }

    private void getFrequency() {
        //TODO 从系统配置中查询定时器执行频率
        DAYLY = true;
        WEEKLY = false;
    }
}
