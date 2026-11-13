package com.auroratms.users.emailverification.cleanupjob;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/** Registers the durable Quartz job and its daily 02:00 UTC trigger. */
@Configuration
public class AccountVerificationCleanupJobConfig {

    /** Creates the persistent identity for the daily cleanup job. */
    @Bean
    public JobDetail accountVerificationCleanupJobDetail() {
        return JobBuilder.newJob(AccountVerificationCleanupJob.class)
                .withIdentity("AccountVerificationCleanupJob", "dailyJobsGroup")
                .withDescription("Daily cleanup of expired and retained account verification tokens")
                .storeDurably()
                .build();
    }

    /** Schedules cleanup once per day at 02:00 UTC. */
    @Bean
    public Trigger accountVerificationCleanupTrigger(
            @Qualifier("accountVerificationCleanupJobDetail") JobDetail accountVerificationCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(accountVerificationCleanupJobDetail)
                .withIdentity("accountVerificationCleanupTrigger", "dailyTriggersGroup")
                .withSchedule(CronScheduleBuilder
                        .cronSchedule("0 0 2 * * ?")
                        .inTimeZone(TimeZone.getTimeZone("UTC")))
                .build();
    }
}
