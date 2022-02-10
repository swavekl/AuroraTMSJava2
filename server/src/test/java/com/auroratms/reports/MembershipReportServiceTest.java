package com.auroratms.reports;

import com.auroratms.AbstractServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

import static org.junit.Assert.assertTrue;

@Transactional
public class MembershipReportServiceTest extends AbstractServiceTest {

    @Autowired
    private MembershipReportService membershipReportService;

    @Test
    public void testReportGeneration() {
        String reportFilename = membershipReportService.generateReport(153);
        File reportFile = new File(reportFilename);
        assertTrue("report file not created",reportFile.exists());

        long length = reportFile.length();
        assertTrue("wrong length of report file", length > 100);
    }
}
