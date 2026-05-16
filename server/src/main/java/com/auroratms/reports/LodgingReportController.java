package com.auroratms.reports;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * Controller for managing and downloading lodging reports.
 */
@RestController
@RequestMapping("/api/reports")
@Slf4j
public class LodgingReportController {

    @Autowired
    private LodgingReportService lodgingReportService;

    /**
     * Generates and downloads the lodging report for a specific tournament.
     * * @param tournamentId the ID of the tournament
     * @return PDF file as an attachment stream
     */
    @GetMapping("/lodging/{tournamentId}")
    public ResponseEntity<Resource> downloadlodgingReport(@PathVariable long tournamentId) {
        try {
            log.info("Request received to generate lodging report for tournament: {}", tournamentId);

            // Generate the PDF and get the absolute canonical path
            String filePath = lodgingReportService.generateReport(tournamentId);

            if (filePath == null) {
                log.error("Report generation failed, file path is null for tournament: {}", tournamentId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            File file = new File(filePath);
            if (!file.exists()) {
                log.error("Generated report file does not exist at path: {}", filePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Resource resource = new FileSystemResource(file);

            // Set up headers to prompt a file download dialog in the browser
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);

        } catch (Exception e) {
            log.error("Error occurred while generating/downloading lodging report for tournament: " + tournamentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
