package com.auroratms.justgo;

import com.auroratms.AbstractServiceTest;
import org.junit.jupiter.api.Test;
import org.quartz.impl.JobExecutionContextImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class DailyUpdateRatingsJobTest extends AbstractServiceTest {

    @Autowired
    private DailyUpdateRatingsJob job;

    @Test
    public void testJob() {
        job.syncAllRatings();
    }
}
