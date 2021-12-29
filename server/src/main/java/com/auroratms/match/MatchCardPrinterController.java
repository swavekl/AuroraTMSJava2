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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.List;

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

    @GetMapping("/matchcard/download")
    public ResponseEntity<Resource> download(@RequestParam List<Long> matchCardIds) {
        try {
            String filename = (matchCardIds.size() == 1)
                    ? matchCardPrinterService.getMatchCardAsPDF(matchCardIds.get(0))
                    : matchCardPrinterService.getMultipleMatchCardsAsPDF(matchCardIds);
            // convert to base64 so printJS library can accept it
            convertToBase64(filename);
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

    /**
     * Converts pdf file into base64 encoded file
     * @param filename
     * @throws IOException
     */
    private void convertToBase64(String filename) throws IOException {
        // rename the file
        File original = new File(filename);
        String renamedFilename = filename + ".b64";
        File renamedFile = new File(renamedFilename);
        boolean b = original.renameTo(renamedFile);
        try (OutputStream os = java.util.Base64.getEncoder().wrap(new FileOutputStream(filename));
             FileInputStream fis = new FileInputStream(renamedFilename)) {
            byte[] bytes = new byte[1024];
            int read;
            while ((read = fis.read(bytes)) > -1) {
                os.write(bytes, 0, read);
            }
            renamedFile.delete();
        }
    }
}
