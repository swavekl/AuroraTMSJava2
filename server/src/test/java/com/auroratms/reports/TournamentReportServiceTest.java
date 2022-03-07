package com.auroratms.reports;

import com.auroratms.AbstractServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class TournamentReportServiceTest extends AbstractServiceTest {

    @Autowired
    private TournamentReportService tournamentReportService;

    @Test
    public void testReportGeneration () {
        String reportFilename = tournamentReportService.generateReport(153L, "3217", "Something to remark");

        File reportFile = new File(reportFilename);
        assertTrue("report file not created",reportFile.exists());

        long length = reportFile.length();
        assertTrue("wrong length of report file", length > 100);
    }
}
