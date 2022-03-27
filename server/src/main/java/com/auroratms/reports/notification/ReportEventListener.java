package com.auroratms.reports.notification;

import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubService;
import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.reports.*;
import com.auroratms.reports.notification.event.TournamentReportsProcessingEvent;
import com.auroratms.tournamentprocessing.TournamentProcessingRequest;
import com.auroratms.tournamentprocessing.TournamentProcessingRequestDetail;
import com.auroratms.tournamentprocessing.TournamentProcessingRequestService;
import com.auroratms.tournamentprocessing.TournamentProcessingRequestStatus;
import com.auroratms.usatt.UsattPersonnelService;
import com.auroratms.users.UserRoles;
import com.auroratms.utils.EmailService;
import com.auroratms.utils.filerepo.FileInfo;
import com.auroratms.utils.filerepo.FileRepositoryException;
import com.auroratms.utils.filerepo.FileRepositoryFactory;
import com.auroratms.utils.filerepo.IFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.FileCopyUtils;

import javax.mail.MessagingException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Listener for requests to generate reports
 */
@Component
@Slf4j
@Transactional  // place @Transactional here to avoid Lazy loading exception
public class ReportEventListener {

    @Autowired
    private TournamentProcessingRequestService tournamentProcessingRequestService;

    @Autowired
    private MembershipReportService membershipReportService;

    @Autowired
    private TournamentReportService tournamentReportService;

    @Autowired
    private ResultsReportService resultsReportService;

    @Autowired
    private PlayerListReportService playerListReportService;

    @Autowired
    private FileRepositoryFactory fileRepositoryFactory;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private ClubService clubService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UsattPersonnelService usattPersonnelService;

    @Value("${client.host.url}")
    private String clientHostUrl;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(TournamentReportsProcessingEvent event) {
        log.info("Handing tournament reports event for request id " + event.getTournamentProcessingRequestId());
        // run this task as system principal so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                switch (event.getEventType()) {
                    case GenerateReports:
                        generateReports(event);
                        break;

                    case Submit:
                        submitReports(event);
                        break;
                }
            }
        };
        task.execute();
    }

    private void generateReports(TournamentReportsProcessingEvent event) {
        try {
            log.info("Starting reports generation for user " + event.getCurrentUserName());
            long start = System.currentTimeMillis();
            TournamentProcessingRequest tournamentProcessingRequest =
                    tournamentProcessingRequestService.findById(event.getTournamentProcessingRequestId());
            generateReports(tournamentProcessingRequest, event.getCurrentUserName());

            // save request with paths to reports
            tournamentProcessingRequestService.save(tournamentProcessingRequest);

            long duration = System.currentTimeMillis() - start;
            log.info("Reports generation completed in " + duration + " ms");
        } catch (ReportGenerationException e) {
            log.error("Error generating request", e);
        }
    }

    /**
     *
     * @param tournamentProcessingRequest
     * @param currentUserName
     * @throws ReportGenerationException
     */
    private void generateReports(TournamentProcessingRequest tournamentProcessingRequest, String currentUserName) throws ReportGenerationException {
        String profileByLoginId = userProfileService.getProfileByLoginId(currentUserName);
        UserProfile preparerUserProfile = userProfileService.getProfile(profileByLoginId);

        UserProfileExt userProfileExt = userProfileExtService.getByProfileId(profileByLoginId);
        Long clubFk = userProfileExt.getClubFk();

        ClubEntity clubEntity = clubService.findById(clubFk);
        String clubName = clubEntity.getClubName();

        // generate reports
        long tournamentId = tournamentProcessingRequest.getTournamentId();
        String membershipReportPath = this.membershipReportService.generateMembershipReport(tournamentId);
        String applicationsReportPath  = this.membershipReportService.generateMembershipApplications(tournamentId);
        String playerListReportPath = this.playerListReportService.generateReport(tournamentId);
        String resultsReportPath = this.resultsReportService.generateReport(tournamentId);
        TournamentReportGenerationResult result = this.tournamentReportService.generateReport(
                tournamentProcessingRequest.getTournamentId(),
                tournamentProcessingRequest.getCcLast4Digits(),
                tournamentProcessingRequest.getRemarks(),
                preparerUserProfile, clubName);

        String tournamentReportPath = result.getReportFilename();
        int amountToPay = (int) (result.getGrandTotalDue() * 100);

        // save them to repository
        Date createdOn = new Date();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(createdOn);
        String repositoryFolder = "tournament-processing-" + timestamp;
        String [] reportFilePaths = {membershipReportPath, applicationsReportPath, playerListReportPath, resultsReportPath, tournamentReportPath};
        Map<String, String> fileToRepoURLMap = copyReportsToRepository(reportFilePaths, repositoryFolder);

        // find the detail that needs filling and save repository URLs in it
        List<TournamentProcessingRequestDetail> details = tournamentProcessingRequest.getDetails();
        for (TournamentProcessingRequestDetail detail : details) {
            if (detail.getCreatedOn() == null) {
                detail.setCreatedOn(createdOn);
                detail.setCreatedByProfileId(preparerUserProfile.getUserId());
                detail.setPathMembershipList(fileToRepoURLMap.get(membershipReportPath));
                detail.setPathApplications(fileToRepoURLMap.get(applicationsReportPath));
                detail.setPathPlayerList(fileToRepoURLMap.get(playerListReportPath));
                detail.setPathMatchResults(fileToRepoURLMap.get(resultsReportPath));
                detail.setPathTournamentReport(fileToRepoURLMap.get(tournamentReportPath));
                detail.setAmountToPay(amountToPay);
            }
        }
    }

    /**
     * Copies the files into repository
     * @param reportFilePaths
     * @param repositoryFolder
     * @return
     * @throws ReportGenerationException
     */
    private Map<String, String> copyReportsToRepository(String[] reportFilePaths, String repositoryFolder) throws ReportGenerationException {
        Map<String, String> fileToRepoURL = new HashMap<>();

        IFileRepository fileRepository = this.fileRepositoryFactory.getFileRepository();
        for (String reportFilePath : reportFilePaths) {
            try {
                File reportFile = new File(reportFilePath);
                String filename = reportFile.getName();
                InputStream repInputStream = new FileInputStream(reportFile);
                String repositoryURL = fileRepository.save(repInputStream, filename, repositoryFolder);
                fileToRepoURL.put(reportFilePath, repositoryURL);
                reportFile.delete();
            } catch (FileNotFoundException | FileRepositoryException e) {
                log.error("Error copying report to repository", e);
                throw new ReportGenerationException("Error generating reports", e);
            }
        }
        return fileToRepoURL;
    }

    ///////////////////////////////////////////////////////////////////

    private void submitReports(TournamentReportsProcessingEvent event) {
        try {
            TournamentProcessingRequest tournamentProcessingRequest =
                    tournamentProcessingRequestService.findById(event.getTournamentProcessingRequestId());
            sendEmail(tournamentProcessingRequest, event.getCurrentUserName());
            // state has changed save it
            tournamentProcessingRequestService.save(tournamentProcessingRequest);
        } catch (ReportGenerationException e) {
            log.error("Error submitting reports for request " + event.getTournamentProcessingRequestId(), e);
        }
    }

    /**
     * Sends email to the proper recipient about the status change
     * @param tournamentProcessingRequest
     * @param currentUserName
     */
    private void sendEmail(TournamentProcessingRequest tournamentProcessingRequest, String currentUserName) {
        try {
            UserProfile usattClubManager = this.usattPersonnelService.getPersonInRole(UserRoles.USATTClubManagers);
            Map<String, Object> templateModel = new HashMap<>();
            String associationAdminEmail = null;
            if (usattClubManager != null) {
                String associationAdminName = usattClubManager.getFirstName() + " " + usattClubManager.getLastName();
                String associationAdminFirstName = usattClubManager.getFirstName();
                associationAdminEmail = usattClubManager.getEmail();
                templateModel.put("associationAdminName", associationAdminName);
                templateModel.put("associationAdminFirstName", associationAdminFirstName);
                templateModel.put("associationAdminEmail", associationAdminEmail);
            } else {
                log.error("Unable to find USATT club manager profile");
                return;
            }

            // https://gateway-pc:4200/processing/detail/10
            String processingRequestUrl = clientHostUrl + "/processing/detail/" + tournamentProcessingRequest.getId();
            templateModel.put("processingRequestUrl", processingRequestUrl);

            String strStatus = "";
            List<String> attachmentRepoURLs = new ArrayList<>();
            TournamentProcessingRequestStatus status = TournamentProcessingRequestStatus.New;
            List<TournamentProcessingRequestDetail> details = tournamentProcessingRequest.getDetails();
            for (TournamentProcessingRequestDetail detail : details) {
                if (detail.getStatus() == TournamentProcessingRequestStatus.Submitting) {
                    status = TournamentProcessingRequestStatus.Submitted;
                    strStatus = status.toString().toLowerCase();
                    detail.setStatus(status);
                    if (detail.getPathTournamentReport() != null) {
                        attachmentRepoURLs.add(detail.getPathTournamentReport());
                    }
                    if (detail.getPathPlayerList() != null) {
                        attachmentRepoURLs.add(detail.getPathPlayerList());
                    }
                    if (detail.getPathMembershipList() != null) {
                        attachmentRepoURLs.add(detail.getPathMembershipList());
                    }
                    if (detail.getPathApplications() != null) {
                        attachmentRepoURLs.add(detail.getPathApplications());
                    }
                    if (detail.getPathMatchResults() != null) {
                        attachmentRepoURLs.add(detail.getPathMatchResults());
                    }
                    break;
                } else if (detail.getStatus() == TournamentProcessingRequestStatus.Paid) {
                    status = detail.getStatus();
                    strStatus = status.toString().toLowerCase();
                    break;
                }
            }
            String subject = "Tournament Processing Request " + strStatus;

            String template = null;
            switch (status) {
                case Submitted:
                    template = "tournament-processing/tpr-submitted.html";
                    break;
                case Paid:
                    template = "tournament-processing/tpr-paid.html";
                    break;
            }

            if (status == TournamentProcessingRequestStatus.Submitted ||
                    status == TournamentProcessingRequestStatus.Paid) {
                // send email to TD
                String profileByLoginId = userProfileService.getProfileByLoginId(currentUserName);
                UserProfile preparerUserProfile = userProfileService.getProfile(profileByLoginId);
                String clubAdminEmail = preparerUserProfile.getEmail();
                String clubAdminName = preparerUserProfile.getFirstName() + " " + preparerUserProfile.getLastName();

                templateModel.put("clubAdminEmail", clubAdminEmail);
                templateModel.put("clubAdminName", clubAdminName);

                // create a temporary directory where we will get copies of attachments
                // this is necessary if the file repository is remote
                String tempDir = System.getenv("TEMP");
                tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
                tempDir += File.separator + "attachments" + File.separator + System.currentTimeMillis();
                File tempDirFile = new File(tempDir);
                tempDirFile.mkdirs();

                log.info("Copying report files to be used as attachments");
                // send email with reports as attachments
                List<String> attachmentFilenames = new ArrayList<>();
                IFileRepository fileRepository = this.fileRepositoryFactory.getFileRepository();
                for (String attachmentsRepoURL : attachmentRepoURLs) {
                    try {
                        String attachmentRepoPath = attachmentsRepoURL.substring(attachmentsRepoURL.indexOf("path=") + "path=".length());
                        FileInfo fileInfo = fileRepository.read(attachmentRepoPath);
                        File outputFile = new File(tempDir, fileInfo.getFilename());
                        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                        FileCopyUtils.copy(fileInfo.getFileInputStream(), fileOutputStream);
                        // copy file into local temp path
                        attachmentFilenames.add(outputFile.getAbsolutePath());
                    } catch (FileRepositoryException | IOException e) {
                        log.error("Unable to copy file from repository " + attachmentsRepoURL, e);
                    }
                }

                log.info("Sending email with attachments");
                emailService.sendHtmlMessageUsingThymeleafTemplateWithAttachments(clubAdminEmail, clubAdminEmail,
                        subject, template, templateModel, attachmentFilenames);

                log.info("Deleting attachment files");
                // delete the files
                for (String attachmentFilename : attachmentFilenames) {
                    try {
                        boolean deleteOk = new File(attachmentFilename).delete();
//                        System.out.println(attachmentFilename + " deleteOk = " + deleteOk);
                    } catch (Exception e) {
                        log.error("Error cleaning up attachment files");
                    }
                }
                boolean tempDirDeleteOk = tempDirFile.delete();
//                System.out.println("tempDirDeleteOk = " + tempDirDeleteOk);
            }
        } catch (MessagingException e) {
            log.error("Unable to send email ", e);
        }
    }

}
