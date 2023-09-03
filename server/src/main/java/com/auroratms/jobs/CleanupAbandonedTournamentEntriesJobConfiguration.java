package com.auroratms.jobs;

import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * Configuration for removing entries without any events and payments/refunds
 */
@Configuration
public class CleanupAbandonedTournamentEntriesJobConfiguration {

    @Bean(name = "cleanupAbandonedTournamentEntriesJobDetail")
    public JobDetailFactoryBean cleanupAbandonedTournamentEntriesJobDetail() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(CleanupAbandonedTournamentEntriesJob.class);
        jobDetailFactory.setName("CleanupAbandonedTournamentEntriesJob");
        jobDetailFactory.setDescription("Invoke Cleanup Abandoned Tournament Entries Job...");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    @Bean (name = "cleanupAbandonedTournamentEntriesTrigger")
    public SimpleTriggerFactoryBean cleanupAbandonedTournamentEntriesTrigger(
            @Qualifier("cleanupAbandonedTournamentEntriesJobDetail") JobDetail job) {
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        trigger.setJobDetail(job);
        trigger.setRepeatInterval(60 * 60 * 1000);  // every hour
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setStartDelay(60 * 1000);
        return trigger;
    }
}
