package com.auroratms.reports.notification;

import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubService;
import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.reports.*;
import com.auroratms.reports.notification.event.TournamentReportsGenerateEvent;
import com.auroratms.tournamentprocessing.TournamentProcessingRequest;
import com.auroratms.tournamentprocessing.TournamentProcessingRequestDetail;
import com.auroratms.tournamentprocessing.TournamentProcessingRequestService;
import com.auroratms.utils.filerepo.FileRepositoryException;
import com.auroratms.utils.filerepo.FileRepositoryFactory;
import com.auroratms.utils.filerepo.IFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(TournamentReportsGenerateEvent event) {
        log.info("Generating reports for request id " + event.getTournamentProcessingRequestId());
        // run this task as system principal so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                handleEventInternal(event);
            }
        };
        task.execute();
    }

    /**
     *  @param event
     *
     */
    private void handleEventInternal(TournamentReportsGenerateEvent event) {
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
        String tournamentReportPath = this.tournamentReportService.generateReport(
                tournamentProcessingRequest.getTournamentId(),
                tournamentProcessingRequest.getCcLast4Digits(),
                tournamentProcessingRequest.getRemarks(),
                preparerUserProfile, clubName);

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
                detail.setPathMembershipList(fileToRepoURLMap.get(membershipReportPath));
                detail.setPathApplications(fileToRepoURLMap.get(applicationsReportPath));
                detail.setPathPlayerList(fileToRepoURLMap.get(playerListReportPath));
                detail.setPathMatchResults(fileToRepoURLMap.get(resultsReportPath));
                detail.setPathTournamentReport(fileToRepoURLMap.get(tournamentReportPath));
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
}
