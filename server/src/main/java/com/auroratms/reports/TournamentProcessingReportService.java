package com.auroratms.reports;

import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamentprocessing.TournamentProcessingRequest;
import com.auroratms.tournamentprocessing.TournamentProcessingRequestDetail;
import com.auroratms.users.UserRolesHelper;
import com.auroratms.utils.filerepo.FileRepositoryException;
import com.auroratms.utils.filerepo.FileRepositoryFactory;
import com.auroratms.utils.filerepo.IFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
 * Service for generating all reports
 */
@Service
@Slf4j
public class TournamentProcessingReportService {

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

    public void generateReports(TournamentProcessingRequest tournamentProcessingRequest) throws ReportGenerationException {
        String currentUserName = UserRolesHelper.getCurrentUsername();

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
        String remarks = "my remarks";
        String card4Digits = "1234";
        String tournamentReportPath = this.tournamentReportService.generateReport(tournamentProcessingRequest.getTournamentId(), card4Digits, remarks, preparerUserProfile, clubName);

        // save them to repository
        Date createdOn = new Date();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(createdOn);
        String repositoryFolder = "tournament-processing-" + timestamp;
        String [] reportFilePaths = {membershipReportPath, applicationsReportPath, playerListReportPath, resultsReportPath, tournamentReportPath};
        Map<String, String> fileToRepoURLMap = copyReportsToRepository(reportFilePaths, repositoryFolder);

        // find the detail that needs filling and save repository URLs in it
        List<TournamentProcessingRequestDetail> details = tournamentProcessingRequest.getDetails();
        for (TournamentProcessingRequestDetail detail : details) {
            if (detail.getId() == null) {
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
