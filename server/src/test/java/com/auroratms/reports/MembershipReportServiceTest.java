package com.auroratms.reports;

import com.auroratms.AbstractServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class MembershipReportServiceTest extends AbstractServiceTest {

    @Autowired
    private MembershipReportService membershipReportService;

    @Test
    public void testReportGeneration() {
        String reportFilename = membershipReportService.generateMembershipReport(153);
        File reportFile = new File(reportFilename);
        assertTrue(reportFile.exists(),"report file not created");

        long length = reportFile.length();
        assertTrue(length > 100, "wrong length of report file");
    }

    @Test
    public void testApplicationsGeneration() {
        String reportFilename = membershipReportService.generateMembershipApplications(153);
        File reportFile = new File(reportFilename);
        assertTrue(reportFile.exists(),"report file not created");

        long length = reportFile.length();
        assertTrue(length > 100, "wrong length of report file");
    }
}
