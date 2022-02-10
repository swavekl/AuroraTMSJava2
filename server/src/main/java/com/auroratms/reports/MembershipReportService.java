package com.auroratms.reports;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamententry.MembershipType;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Report listing who bought which membership level.  The report has the following format
 *
 * Membership#,LastName,FirstName,DOB,Email,Gender,Address#1,City,State,Zip,Phone#,Citizenship,RepresentingCountry,ProductID,YearCount
 * 224026,Albulescu,Julian,11/08/2003,julianalbulescu1@gmail.com,M,2730 Ridge Road,Highland Park,IL,60035,847-602-0191,,,56,1,
 */
@Service
@Slf4j
public class MembershipReportService {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    // 11/08/2003 - mm/dd/yyyy
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public String generateReport (long tournamentId) {
        String reportFilename = null;
        FileWriter fileWriter = null;
        try {
            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            File reportFile = new File(tempDir + File.separator + "usatt-players-" + tournamentId + ".csv");
            reportFilename = reportFile.getCanonicalPath();
            log.info("Writing membership report for tournament " + tournamentId);
            log.info("to " + reportFilename);

            // write header
            fileWriter = new FileWriter(reportFile);
            fileWriter.write("Membership#,LastName,FirstName,DOB,Email,Gender,Address#1,City,State,Zip,Phone#,Citizenship,RepresentingCountry,ProductID,YearCount\n");

            // get all tournament entries for this tournament and collect player profiles ids
            // of players who bought some type of membership
            List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournament(tournamentId);
            Set<String> uniqueProfileIdsSet = new HashSet<>();
            Map<String, MembershipType> profileIdToPurchasedMembershipTypesMap = new HashMap<>();
            for (TournamentEntry tournamentEntry : tournamentEntries) {
                MembershipType membershipOption = tournamentEntry.getMembershipOption();
                if (membershipOption != MembershipType.NO_MEMBERSHIP_REQUIRED) {
                    uniqueProfileIdsSet.add(tournamentEntry.getProfileId());
                    profileIdToPurchasedMembershipTypesMap.put(tournamentEntry.getProfileId(), membershipOption);
                }
            }

            // get profile information for those who bought it
            List<String> profileIds = new ArrayList<>(uniqueProfileIdsSet);
            Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIds);
            List<UserProfile> userProfileList = new ArrayList<>(userProfiles);
            Comparator<UserProfile> comparator = Comparator.comparing(UserProfile::getLastName)
                    .thenComparing(UserProfile::getFirstName);
            Collections.sort(userProfileList, comparator);

            // get profile id to membership id map
            Map<String, UserProfileExt> profileIdToUserExtProfileMap = userProfileExtService.findByProfileIds(profileIds);

            for (UserProfile userProfile : userProfileList) {
                String playerProfileId = userProfile.getUserId();
                MembershipType membershipType = profileIdToPurchasedMembershipTypesMap.get(playerProfileId);
                UserProfileExt userProfileExt = profileIdToUserExtProfileMap.get(playerProfileId);
                String reportLine = makeReportLine(membershipType, userProfile, userProfileExt);
                if (!reportLine.isEmpty()) {
                    fileWriter.write(reportLine);
                }
            }
            log.info("Finished player membership report for " + userProfileList.size() + " players");

        } catch (IOException e) {
            log.error("Unable to create membership report ", e);
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return reportFilename;
    }

    /**
     * Creates report line
     * Membership#,LastName,FirstName,DOB,Email,Gender,Address#1,City,State,Zip,Phone#,Citizenship,RepresentingCountry,ProductID,YearCount
     * 224026,Albulescu,Julian,11/08/2003,julianalbulescu1@gmail.com,M,2730 Ridge Road,Highland Park,IL,60035,847-602-0191,,,56,1,
     *
     * @param membershipType
     * @param userProfile
     * @param userProfileExt
     * @return
     */
    private String makeReportLine(MembershipType membershipType, UserProfile userProfile, UserProfileExt userProfileExt) {
        Long membershipId = userProfileExt.getMembershipId();

        String formattedDOB = dateFormat.format(userProfile.getDateOfBirth());
        String gender = (userProfile.getGender().equals("Male")) ? "M" : "F";
        String countryCode = userProfile.getCountryCode();
        String citizenship = countryCode.equals("US") ? "" : "FN";
        String representingCountry = countryCode.equals("US") ? "" : countryCode;
        int productId = getProductId(membershipType);
        int yearCount = 1;

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%d\n", membershipId,
                userProfile.getLastName(), userProfile.getFirstName(), formattedDOB, userProfile.getEmail(), gender,
                userProfile.getStreetAddress(), userProfile.getCity(), userProfile.getState(), userProfile.getZipCode(), userProfile.getMobilePhone(),
                citizenship, representingCountry, productId, yearCount);
    }

    /**
     * Gets USATT product id
     * @param membershipType
     * @return
     */
    private int getProductId(MembershipType membershipType) {
        int productId = 0;
        switch(membershipType) {
            case TOURNAMENT_PASS_ADULT:
                productId = 25;
                break;
            case TOURNAMENT_PASS_JUNIOR:
                productId = 26;
                break;
            case BASIC_PLAN:
                productId = 27;
                break;
            case PRO_PLAN:
                productId = 28;
                break;
            case LIFETIME:
                productId = 29;
                break;
        }
        return productId;
    }
}
