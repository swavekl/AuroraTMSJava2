package com.auroratms.ratingsprocessing;

import com.auroratms.ratingsprocessing.notification.RatingsProcessingEventPublisher;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.auroratms.utils.filerepo.FileInfo;
import com.auroratms.utils.filerepo.FileRepositoryException;
import com.auroratms.utils.filerepo.FileRepositoryFactory;
import com.auroratms.utils.filerepo.IFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated() and hasAuthority('Admins')")
@Transactional
public class RatingsProcessingController {

    private static final Logger logger = LoggerFactory.getLogger(RatingsProcessingController.class);

    @Autowired
    private UsattDataService usattDataService;

    @Autowired
    private FileRepositoryFactory fileRepositoryFactory;

    @Autowired
    private RatingsProcessingEventPublisher ratingsProcessingEventPublisher;

    private Runnable processingTask;

    private RatingsProcessorStatus ratingsProcessorStatus = new RatingsProcessorStatus();

    @GetMapping("/ratingsprocessing")
    public ResponseEntity<RatingsProcessorStatus> processRatingsFile (@RequestParam String ratingsFile) {
        if (processingTask != null) {
            logger.info ("Processing is in progress");
            return ResponseEntity.internalServerError().build();
        }
        try {
            ratingsProcessorStatus = new RatingsProcessorStatus();
            ratingsProcessorStatus.phase = "Copying ratings file";
            ratingsProcessorStatus.startTime = System.currentTimeMillis();
            ratingsProcessorStatus.endTime = ratingsProcessorStatus.startTime;

            // get the file from repository
            IFileRepository fileRepository = this.fileRepositoryFactory.getFileRepository();
            FileInfo fileInfo = fileRepository.read(ratingsFile);

            // copy the file locally, because Slurper can't read from input stream - it may be remote in cloud storage
            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            tempDir += File.separator + "ratings";
            File tempDirFile = new File(tempDir);
            tempDirFile.mkdirs();

            File outputFile = new File(tempDir, fileInfo.getFilename());
            outputFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            FileCopyUtils.copy(fileInfo.getFileInputStream(), fileOutputStream);

            String filename = outputFile.getCanonicalPath();

            processingTask = new Runnable() {

                @Transactional
                @Override
                public void run() {
                    try {
                        ratingsProcessorStatus.phase = "Reading ratings file";
                        // update ratings in the usatt player list
                        List<UsattPlayerRecord> usattPlayerRecords = usattDataService.readAllPlayersFromFile(filename, ratingsProcessorStatus);
                        ratingsProcessorStatus.phase = "Updating ratings";
                        if (usattPlayerRecords.size() > 0) {
                            usattDataService.insertPlayerData(usattPlayerRecords, ratingsProcessorStatus);
                        }
                        ratingsProcessorStatus.phase = "Ratings updated";

                        // clean up
                        outputFile.delete();
                        fileRepository.deleteByURL(ratingsFile);
                        ratingsProcessorStatus.phase = "Finished";
                    } catch (FileRepositoryException e) {
                        logger.error(e.getMessage());
                        ratingsProcessorStatus.error = e.getMessage();
                    } finally {
                        ratingsProcessorStatus.endTime = System.currentTimeMillis();
                        processingTask = null;

                        // initiate updates to seed and eligibility ratings in all future tournaments
                        ratingsProcessingEventPublisher.publishRatingsProcessingEndEvent();
                    }
                }
            };

            Thread thread = new Thread(processingTask);
            thread.start();

        } catch (FileRepositoryException | IOException e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(ratingsProcessorStatus);
    }

    @GetMapping("/ratingsprocessing/status")
    public ResponseEntity<RatingsProcessorStatus> getStatus () {
        if (ratingsProcessorStatus != null) {
            ratingsProcessorStatus.endTime = System.currentTimeMillis();
            logger.info("ratingsProcessorStatus.phase = " + ratingsProcessorStatus.phase);
            if (ratingsProcessorStatus.error == null) {
                return ResponseEntity.ok(ratingsProcessorStatus);
            } else {
                return ResponseEntity.internalServerError().body(ratingsProcessorStatus);
            }
        } else {
            logger.error("Ratings processor status requested prematurely");
            return ResponseEntity.badRequest().build();
        }
    }
}
