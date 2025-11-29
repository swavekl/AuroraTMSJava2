package com.auroratms.utils.filerepo;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("api/filerepository")
@PreAuthorize("isAuthenticated()")
public class FileRepositoryController {

    private static final Logger logger = LoggerFactory.getLogger(FileRepositoryController.class);

    @Autowired
    private FileRepositoryFactory fileRepositoryFactory;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile multipartFile,
                                         @RequestParam String storagePath) {
        try {
            IFileRepository fileRepository = fileRepositoryFactory.getFileRepository();
            InputStream inputStream = multipartFile.getInputStream();
            String repositoryUrl = fileRepository.save(inputStream, multipartFile.getOriginalFilename(), storagePath);
            return ResponseEntity.created(new URI(repositoryUrl)).build();
        } catch (FileRepositoryException | URISyntaxException | IOException e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Downloads requested file located at fileName in the
     *
     * @param path relative fileName to a file in repository
     * @return
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String path) {
        try {
            IFileRepository fileRepository = fileRepositoryFactory.getFileRepository();
            FileInfo fileInfo = fileRepository.read(path);
            FileInputStream fileInputStream = fileInfo.fileInputStream;
            if (fileInputStream != null) {
                InputStreamResource resource = new InputStreamResource(fileInputStream);
                long length = fileInfo.fileSize;
                String filename = fileInfo.filename;
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
            } else {
                logger.error("repository file at path '" + path + "' not found");
                return ResponseEntity.badRequest().build();
            }
        } catch (FileRepositoryException e) {
            logger.error("Unable to read file from repository", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for viewing PDFs with blank entry form stored in repository in a new browser tab
     * @param path
     * @return
     */
    @GetMapping("/viewpdf") // Renamed endpoint to be more semantic
    public ResponseEntity<byte[]> viewPdf(@RequestParam String path) {
        try {
            IFileRepository fileRepository = fileRepositoryFactory.getFileRepository();
            FileInfo fileInfo = fileRepository.read(path);
            FileInputStream fileInputStream = fileInfo.fileInputStream;

            if (fileInputStream != null) {
                // 1. Convert the FileInputStream to a byte array
                // Requires Apache Commons IO or similar utility
                byte[] fileBytes = IOUtils.toByteArray(fileInputStream);

                // Ensure the stream is closed after reading
                fileInputStream.close();

                HttpHeaders httpHeaders = new HttpHeaders();

                // 2. *** MANDATORY CHANGE: Set Content-Type to PDF ***
                httpHeaders.setContentType(MediaType.APPLICATION_PDF);

                // 3. *** MANDATORY CHANGE: REMOVE 'attachment' header ***
                // We keep the cache headers for security/freshness
                httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
                httpHeaders.add("Pragma", "no-cache");
                httpHeaders.add("Expires", "0");

                // Return the byte array. The client (JS fetch) handles the rest.
                return ResponseEntity.ok()
                        .headers(httpHeaders)
                        .contentLength(fileBytes.length)
                        // 4. *** MANDATORY CHANGE: Return byte[] instead of Resource ***
                        .body(fileBytes);
            } else {
                logger.error("repository file at path '" + path + "' not found");
                return ResponseEntity.badRequest().build();
            }
        } catch (FileRepositoryException e) {
            logger.error("Unable to read file from repository", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Catch IO and other exceptions from IOUtils or close()
            logger.error("Error processing file for viewing", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles(@RequestParam String path) {
        try {
            IFileRepository fileRepository = fileRepositoryFactory.getFileRepository();
            List<String> fileListUrls = fileRepository.list(path);
            return ResponseEntity.ok(fileListUrls);
        } catch (FileRepositoryException e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("")
    public ResponseEntity<Void> deleteFiles(@RequestParam String path) {
        try {
            IFileRepository fileRepository = fileRepositoryFactory.getFileRepository();
            fileRepository.delete(path);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
