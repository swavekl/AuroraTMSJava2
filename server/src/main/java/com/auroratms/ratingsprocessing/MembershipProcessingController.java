package com.auroratms.ratingsprocessing;

import com.auroratms.ratingsprocessing.exception.ProcessingException;
import com.auroratms.server.errorhandling.ApiError;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/membershipsprocessing")
@PreAuthorize("isAuthenticated() and hasAuthority('Admins')")
@Transactional
public class MembershipProcessingController {

    private static final Logger logger = LoggerFactory.getLogger(MembershipProcessingController.class);

    @Autowired
    private UsattDataService usattDataService;

    @Autowired
    private FileRepositoryFactory fileRepositoryFactory;

    private Runnable processingTask;

    private MembershipsProcessorStatus membershipsProcessorStatus;


    @GetMapping("")
    public ResponseEntity<MembershipsProcessorStatus> processRatingsFile (@RequestParam String membershipsFile) throws ProcessingException {
        if (processingTask != null) {
            logger.info ("Processing is in progress");
            return ResponseEntity.internalServerError().build();
        }
        try {
            membershipsProcessorStatus = new MembershipsProcessorStatus();
            membershipsProcessorStatus.phase = "Copying membership file";
            membershipsProcessorStatus.startTime = System.currentTimeMillis();
            membershipsProcessorStatus.endTime = membershipsProcessorStatus.startTime;

            // get the file from repository
            IFileRepository fileRepository = this.fileRepositoryFactory.getFileRepository();
            FileInfo fileInfo = fileRepository.read(membershipsFile);

            // copy the file locally, because Slurper can't read from input stream - it may be remote in cloud storage
            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            tempDir += File.separator + "memberships";
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
                        membershipsProcessorStatus.phase = "Reading memberships file";
                        // update memberships in the usatt player list
                        List<UsattPlayerRecord> usattPlayerRecords = usattDataService.readMembershipFile(filename, membershipsProcessorStatus);
                        membershipsProcessorStatus.phase = "Updating memberships";
                        if (usattPlayerRecords.size() > 0) {
                            usattDataService.insertNewMembers(usattPlayerRecords, membershipsProcessorStatus);
                        }
                    } finally {
                        membershipsProcessorStatus.phase = "Finished";
                        membershipsProcessorStatus.endTime = System.currentTimeMillis();
                        processingTask = null;
                        // clean up
                        outputFile.delete();
                        try {
                            fileRepository.deleteByURL(membershipsFile);
                        } catch (FileRepositoryException e) {
                            logger.error(e.getMessage());
                            membershipsProcessorStatus.error = e.getMessage();
                        }
                    }
                }
            };

            Thread thread = new Thread(processingTask);
            thread.start();

        } catch (FileRepositoryException | IOException e) {
            logger.error(e.getMessage());
            throw new ProcessingException("Error opening files", e);
        }
        return ResponseEntity.ok(membershipsProcessorStatus);
    }

    @GetMapping("/status")
    public ResponseEntity<MembershipsProcessorStatus> getStatus () {
        if (membershipsProcessorStatus != null) {
            membershipsProcessorStatus.endTime = System.currentTimeMillis();
            logger.info("membershipsProcessorStatus.phase = " + membershipsProcessorStatus.phase);
            return ResponseEntity.ok(membershipsProcessorStatus);
        } else {
            logger.error("Membership processor status requested prematurely");
            return ResponseEntity.badRequest().build();
        }
    }

    @ExceptionHandler( {ProcessingException.class})
    public ResponseEntity<Object> handleExceptions(
            ProcessingException e, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "");
        return new ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.getStatus());
    }
}
