package com.auroratms.users.emailverification.cleanupjob;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/** Registers the durable Quartz job and its hourly UTC trigger. */
@Configuration
public class AccountVerificationCleanupJobConfig {

    /** Creates the persistent identity for the cleanup job. */
    @Bean
    public JobDetail accountVerificationCleanupJobDetail() {
        return JobBuilder.newJob(AccountVerificationCleanupJob.class)
                .withIdentity("AccountVerificationCleanupJob", "dailyJobsGroup")
                .withDescription("Hourly cleanup of abandoned accounts and account verification tokens")
                .storeDurably()
                .build();
    }

    /** Schedules cleanup at the start of every UTC hour. */
    @Bean
    public Trigger accountVerificationCleanupTrigger(
            @Qualifier("accountVerificationCleanupJobDetail") JobDetail accountVerificationCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(accountVerificationCleanupJobDetail)
                .withIdentity("accountVerificationCleanupTrigger", "dailyTriggersGroup")
                .withSchedule(CronScheduleBuilder
                        .cronSchedule("0 0 * * * ?")
                        .inTimeZone(TimeZone.getTimeZone("UTC")))
                .build();
    }

    /** Updates only this persisted job and trigger when its definition changes. */
    @Bean
    public ApplicationRunner updateAccountVerificationCleanupSchedule(
            Scheduler scheduler,
            @Qualifier("accountVerificationCleanupJobDetail") JobDetail jobDetail,
            @Qualifier("accountVerificationCleanupTrigger") Trigger desiredTrigger) {
        return args -> {
            scheduler.addJob(jobDetail, true);
            Trigger existingTrigger = scheduler.getTrigger(desiredTrigger.getKey());
            if (existingTrigger == null) {
                scheduler.scheduleJob(desiredTrigger);
            } else if (!hasSameSchedule(existingTrigger, desiredTrigger)) {
                scheduler.rescheduleJob(desiredTrigger.getKey(), desiredTrigger);
            }
        };
    }

    private boolean hasSameSchedule(Trigger existingTrigger, Trigger desiredTrigger) {
        if (existingTrigger instanceof CronTrigger existingCron && desiredTrigger instanceof CronTrigger desiredCron) {
            return existingCron.getCronExpression().equals(desiredCron.getCronExpression())
                    && existingCron.getTimeZone().getID().equals(desiredCron.getTimeZone().getID());
        }
        return false;
    }
}
