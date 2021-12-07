package com.auroratms.utils.filerepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("api/filerepository")
@PreAuthorize("isAuthenticated()")
public class FileRepositoryController {

    private static final Logger logger = LoggerFactory.getLogger(FileRepositoryController.class);

    @Autowired
    private FileRepositoryFactory fileRepositoryFactory;

    @Value("${spring.servlet.multipart.location}")
    private String uploadFileLocation;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile multipartFile,
                                                   @RequestParam("storagePath") String storagePath) {
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
     * Downloads requested file located at path in the
     *
     * @param path relative path to a file in repository
     * @return
     */
    @GetMapping("/download/{path}")
    public ResponseEntity<Resource> download(@PathVariable String path) {
        try {
            IFileRepository fileRepository = fileRepositoryFactory.getFileRepository();
            FileInputStream fileInputStream = fileRepository.read(path);
            InputStreamResource resource = new InputStreamResource(fileInputStream);
            File file = resource.getFile();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
            httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
            httpHeaders.add("Pragma", "no-cache");
            httpHeaders.add("Expires", "0");
            return ResponseEntity.ok()
                    .headers(httpHeaders)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/list/{path}")
    public ResponseEntity<List<String>> listFiles(@PathVariable String path) {
        IFileRepository fileRepository = fileRepositoryFactory.getFileRepository();
        List<String> fileListUrls = fileRepository.list(path);
        return ResponseEntity.ok(fileListUrls);
    }

    @DeleteMapping("{path}")
    public ResponseEntity<Void> deleteFiles(@PathVariable String path) {
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
