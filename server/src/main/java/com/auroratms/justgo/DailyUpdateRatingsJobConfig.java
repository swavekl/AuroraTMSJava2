package com.auroratms.justgo;

import java.util.TimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DailyUpdateRatingsJobConfig {

    // Naming the Job
    @Bean
    public JobDetail dailyTaskJobDetail() {
        return JobBuilder.newJob(DailyUpdateRatingsJob.class)
                .withIdentity("DailyUpdateRatingsJob", "dailyJobsGroup") // Custom Job Name & Group Name
                .withDescription("Weekday job running at 10 PM Denver time")
                .storeDurably() // Keeps the job in DB even if no trigger points to it
                .build();
    }

    // Naming and Scheduling the Trigger
    @Bean
    public Trigger dailyTaskTrigger(JobDetail dailyTaskJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(dailyTaskJobDetail)
                .withIdentity("dailyTaskTrigger", "dailyTriggersGroup") // Custom Trigger Name & Group Name
                .withSchedule(CronScheduleBuilder
                        .cronSchedule("0 0 22 ? * MON-FRI")
                        .inTimeZone(TimeZone.getTimeZone("America/Denver")))
                .build();
    }
}
