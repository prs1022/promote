package com.api;

import lombok.Data;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@ComponentScan
@EnableScheduling
@Data
public class JobTask {
    private boolean flag = false;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private org.slf4j.Logger logger = LoggerFactory.getLogger(JobTask.class);

    @Scheduled(cron = "0/2 * * * * *")
    public void testCron() {
        try {
            logger.info("work begin." + dateFormat.format(Calendar.getInstance().getTime()));
            Thread.sleep(4000);
            logger.info("定时任务");
            logger.info("work end," + dateFormat.format(Calendar.getInstance().getTime()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
