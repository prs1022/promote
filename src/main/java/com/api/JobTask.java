package com.api;

import lombok.Data;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@ComponentScan
@EnableScheduling
@Data
public class JobTask {
    private boolean flag = false;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron = "0/2 * * * * *")
    public void testCron() {
        try {
            System.out.println("work begin." + dateFormat.format(Calendar.getInstance().getTime()));
            Thread.sleep(4000);
            System.out.println("定时任务");
            System.out.println("work end," + dateFormat.format(Calendar.getInstance().getTime()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
