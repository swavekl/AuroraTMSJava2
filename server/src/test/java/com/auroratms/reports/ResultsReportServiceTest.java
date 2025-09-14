package com.auroratms.reports;

import com.auroratms.AbstractServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class ResultsReportServiceTest extends AbstractServiceTest {

    @Autowired
    private ResultsReportService resultsReportService;

    @Test
    public void testReportGeneration() {
        String reportFilename = resultsReportService.generateReport(153);
        File reportFile = new File(reportFilename);
        assertTrue(reportFile.exists(),"report file not created");

        long length = reportFile.length();
        assertTrue(length > 100, "wrong length of report file");
    }
}
