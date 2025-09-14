package com.auroratms.reports;

import com.auroratms.AbstractServiceTest;
import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TournamentReportServiceTest extends AbstractServiceTest {

    @Autowired
    private TournamentReportService tournamentReportService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private ClubService clubService;

    @Test
    public void testReportGeneration () {
        String profileByLoginId = userProfileService.getProfileByLoginId("swaveklorenc@gmail.com");
        UserProfile preparerUserProfile = userProfileService.getProfile(profileByLoginId);

        UserProfileExt userProfileExt = userProfileExtService.getByProfileId(profileByLoginId);
        Long clubFk = userProfileExt.getClubFk();

        ClubEntity clubEntity = clubService.findById(clubFk);
        String clubName = clubEntity.getClubName();

        String remarks = "Please use the card on file";

        TournamentReportGenerationResult result = tournamentReportService.generateReport(153L, "3217", remarks, preparerUserProfile, clubName);
        String reportFilename = result.getReportFilename();

        File reportFile = new File(reportFilename);
        assertTrue(reportFile.exists(),"report file not created");

        long length = reportFile.length();
        assertTrue(length > 100, "wrong length of report file");

        double grandTotalDue = result.getGrandTotalDue();
        assertEquals(217.5d, grandTotalDue, 0.0d, "wrong amount due");
    }
}
