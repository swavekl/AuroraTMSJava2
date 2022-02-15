package com.auroratms.reports;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.match.MatchService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service for producing report with a list of players who actually played at the tournament
 */
@Service
@Slf4j
public class PlayerListReportService {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private MatchService matchService;

    // 11/08/2003 - mm/dd/yyyy
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public String generateReport(long tournamentId) {
        String reportFilename = null;
        FileWriter fileWriter = null;
        try {
            // get data and write it out
            List<PlayerReportInfo> playerReportInfos = prepareReportData(tournamentId);

            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            File reportFile = new File(tempDir + File.separator + "player-list-" + tournamentId + ".csv");
            reportFilename = reportFile.getCanonicalPath();
            log.info("Writing player list report for tournament " + tournamentId);
            log.info("to " + reportFilename);

            // write header
            fileWriter = new FileWriter(reportFile);
            fileWriter.write("Count,USATT#,Name,Rating,State,Zip,Sex,Birthdate,Expires\n");

            for (PlayerReportInfo info : playerReportInfos) {
                String fullName = info.lastName + ", " + info.firstName;
                String reportLine = String.format("%d,%d,\"%s\",%d,%s,%s,%s,%s,%s\n", info.playerNumber, info.membershipId, fullName,
                        info.rating, info.state, info.zipCode, info.gender, info.dateOfBirth, info.membershipExpirationDate);
                fileWriter.write(reportLine);
            }

            log.info("Finished player list report for " + playerReportInfos.size() + " players");

        } catch (IOException e) {
            log.error("Unable to create player list report ", e);
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
     *
     * @param tournamentId
     * @return
     */
    private List<PlayerReportInfo> prepareReportData (long tournamentId) {
        Set<String> uniqueProfileIdsSet = new HashSet<>();
        // get all events in this tournament
        Collection<TournamentEvent> allEvents = this.tournamentEventEntityService.list(tournamentId, Pageable.unpaged());
        for (TournamentEvent tournamentEvent : allEvents) {
            // doubles matches results are not sent to USATT so players who only played in doubles events are not counted?
            if (!tournamentEvent.isDoubles()) {
                Long eventId = tournamentEvent.getId();
                // get all match cards for this event
                log.info("Writing results for event " + tournamentEvent.getName());
                List<MatchCard> allMatchCardsForEvent = matchCardService.findAllForEvent(eventId);

                // sort them so RR is followed by SE
                Comparator<MatchCard> comparator = Comparator
                        .comparing(MatchCard::getDrawType)
                        .thenComparing(Comparator.comparing(MatchCard::getRound).reversed())
                        .thenComparing(MatchCard::getGroupNum);
                Collections.sort(allMatchCardsForEvent, comparator);
                List<Match> matchesForEvent = matchService.findAllByMatchCardIn(allMatchCardsForEvent);
                for (Match match : matchesForEvent) {
                    if (match.isMatchFinished(tournamentEvent.getNumberOfGames(), tournamentEvent.getPointsPerGame())) {
                        if (!match.isSideADefaulted()) {
                            uniqueProfileIdsSet.add(match.getPlayerAProfileId());
                        }
                        if (!match.isSideBDefaulted()) {
                            uniqueProfileIdsSet.add(match.getPlayerBProfileId());
                        }
                    }
                }
            }
        }

        // get profile information for those players who played at least one match
        List<String> profileIds = new ArrayList<>(uniqueProfileIdsSet);
        // get profile id to membership id map
        Map<String, UserProfileExt> profileIdToUserExtProfileMap = userProfileExtService.findByProfileIds(profileIds);

        Map<String, PlayerReportInfo> profileIdToReportInfoMap = new HashMap<>(profileIds.size());

        // sort players alphabetically by last name first name
        Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIds);
        List<UserProfile> userProfileList = new ArrayList<>(userProfiles);
        Comparator<UserProfile> comparator = Comparator.comparing(UserProfile::getLastName)
                .thenComparing(UserProfile::getFirstName);
        Collections.sort(userProfileList, comparator);

        int playerNumber = 0;

        for (UserProfile userProfile : userProfileList) {
            String playerProfileId = userProfile.getUserId();
            PlayerReportInfo playerReportInfo = new PlayerReportInfo();
            playerReportInfo.playerNumber = ++playerNumber;
            playerReportInfo.firstName = userProfile.getFirstName();
            playerReportInfo.lastName = userProfile.getLastName();
            playerReportInfo.gender = (userProfile.getGender().equals("Male")) ? "M" : "F";
            Date dateOfBirth = userProfile.getDateOfBirth();
            playerReportInfo.dateOfBirth = (dateOfBirth != null) ? dateFormat.format(dateOfBirth) : "";

            String countryCode = userProfile.getCountryCode();
            String state = countryCode.equals("US") ? userProfile.getState() : "FN";
            playerReportInfo.state = (state != null) ? state : "";

            playerReportInfo.zipCode = (userProfile.getZipCode() != null) ? userProfile.getZipCode() : "";

            Date membershipExpirationDate = userProfile.getMembershipExpirationDate();
            playerReportInfo.membershipExpirationDate = (membershipExpirationDate != null) ? dateFormat.format(membershipExpirationDate) : "";

            UserProfileExt userProfileExt = profileIdToUserExtProfileMap.get(playerProfileId);
            playerReportInfo.membershipId = userProfileExt.getMembershipId();

            profileIdToReportInfoMap.put(playerProfileId, playerReportInfo);
        }

        // get tournament entry with tournament rating
        List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournament(tournamentId);
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            // if player played
            PlayerReportInfo playerReportInfo = profileIdToReportInfoMap.get(tournamentEntry.getProfileId());
            if (playerReportInfo != null) {
                playerReportInfo.rating = tournamentEntry.getEligibilityRating();
            }
        }

        // sort after extraction from the map
        List<PlayerReportInfo> sortedPlayerReportInfos = new ArrayList<>(profileIdToReportInfoMap.values());
        Comparator<PlayerReportInfo> comparator2 = Comparator.comparing(PlayerReportInfo::getLastName)
                .thenComparing(PlayerReportInfo::getFirstName);
        Collections.sort(sortedPlayerReportInfos, comparator2);

        return sortedPlayerReportInfos;
    }

    // Class for holding report data for one row
    private class PlayerReportInfo {
        int playerNumber;
        String firstName;
        String lastName;
        String gender;
        long membershipId; // 0 if not a member yet
        int rating;  // rating for this tournament
        String state;
        String zipCode;
        String dateOfBirth;
        String membershipExpirationDate;

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }
}
