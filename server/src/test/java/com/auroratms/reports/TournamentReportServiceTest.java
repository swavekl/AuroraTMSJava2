package com.auroratms.reports;

import com.auroratms.AbstractServiceTest;
import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

import static org.junit.Assert.assertTrue;

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

        String reportFilename = tournamentReportService.generateReport(153L, "3217", remarks, preparerUserProfile, clubName);

        File reportFile = new File(reportFilename);
        assertTrue("report file not created",reportFile.exists());

        long length = reportFile.length();
        assertTrue("wrong length of report file", length > 100);
    }
}
