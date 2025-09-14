package com.auroratms.reports;

import com.auroratms.AbstractServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class PlayerListReportServiceTest extends AbstractServiceTest {

    @Autowired
    private PlayerListReportService playerListReportService;

    @Test
    public void testReportGeneration() {
        String reportFilename = playerListReportService.generateReport(153);
        File reportFile = new File(reportFilename);
        assertTrue(reportFile.exists(),"report file not created");

        long length = reportFile.length();
        assertTrue(length > 100, "wrong length of report file");
    }
}
