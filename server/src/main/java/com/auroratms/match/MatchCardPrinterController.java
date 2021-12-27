package com.auroratms.match;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;

/**
 * Service for printing match cards
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
@Slf4j
public class MatchCardPrinterController {

    @Autowired
    private MatchCardPrinterService matchCardPrinterService;

    @GetMapping("/matchcard/download/{matchCardId}")
    public ResponseEntity<Resource> download(@PathVariable Long matchCardId) {
        try {
            String filename = matchCardPrinterService.getMatchCardAsPDF(matchCardId);
            File file = new File(filename);
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamResource resource = new InputStreamResource(fileInputStream);
            long length = file.length();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
            httpHeaders.add("Pragma", "no-cache");
            httpHeaders.add("Expires", "0");
            return ResponseEntity.ok()
                    .headers(httpHeaders)
                    .contentLength(length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            log.error("Unable to read file from repository", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
