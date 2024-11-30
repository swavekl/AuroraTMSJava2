package com.auroratms.reports;

import com.auroratms.draw.DrawType;
import com.auroratms.event.AgeRestrictionType;
import com.auroratms.event.GenderRestriction;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.EligibilityRestriction;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service for generating ranking report for state, regional or national championships
 * Ranking system is explained here: https://www.usatt.org/ranking-system-explained
 * The report has the following format in CSV file
 * <p>
 * TournamentID,MemberID,LastName,FirstName,DOB,Gender,EventCategory,FinishingPosition
 * 38,31426,Friend,Chance,8/24/1991,M,MS,1st
 * 38,269633,Azrak,Kareem,7/24/2009,M,MS,2nd
 * 38,1179267,Kar,Joydeep,11/22/1982,M,MS,3-4
 * 38,230301,Nieto,Jorge,9/10/1978,M,MS,3-4
 * 38,64892,Lie,Jan,12/16/1962,M,MS,5-8
 * 38,54553,Tannehill,Soren,4/14/1979,M,MS,5-8
 */
@Service
@Slf4j
@Transactional
public class RankingReportService {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    /**
     * Generates report
     *
     * @param tournamentId              this tournament id in our database
     * @param rankingReportTournamentId USATT assigned tournament id for their processing
     * @return
     */
    public String generateReport(long tournamentId, int rankingReportTournamentId) {
        String reportFilename = null;
        FileWriter fileWriter = null;
        try {
            // create report file path
            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            File reportFile = new File(tempDir + File.separator + "ranking-" + tournamentId + ".csv");
            reportFilename = reportFile.getCanonicalPath();
            log.info("Writing ranking report for tournament " + tournamentId);
            log.info("to " + reportFilename);

            // write header
            fileWriter = new FileWriter(reportFile);
            fileWriter.write("TournamentID,MemberID,LastName,FirstName,DOB,Gender,EventCategory,FinishingPosition\n");

            Collection<TournamentEvent> rankedEvents = getRankedEvents(tournamentId);

            for (TournamentEvent tournamentEvent : rankedEvents) {
                String eventCode = getEventCode(tournamentEvent);
                List<ReportLineInfo> reportLineInfos = generateEventReportLineInfos(tournamentEvent);
                for (ReportLineInfo reportLineInfo : reportLineInfos) {
                    String reportLine = String.format("%d,%d,%s,%s,%s,%s,%s\n",
                            rankingReportTournamentId, reportLineInfo.memberId, reportLineInfo.fullName,
                            reportLineInfo.dateOfBirth, reportLineInfo.gender, eventCode, reportLineInfo.strRank);
                    if (!reportLine.isEmpty()) {
                        fileWriter.write(reportLine);
                    }
                }
                fileWriter.flush();
            }

            log.info("Finished player ranking report");

        } catch (IOException e) {
            log.error("Unable to create ranking report ", e);
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
     * Gets abbreviated event code
     * <p>
     * Adult Men’s Singles -> MS
     * Adult Women’s Singles -> WS
     * Junior Boy’s Singles* (U19, U17, U15, U13, U11) -> B19, B17 etc.
     * Junior Girl’s Singles* (U19, U17, U15, U13, U11) -> G19, G17 etc.
     * Senior Men’s Singles* (O30, O40, O50, O60, O65, O70, O75, O80, O85) -> M30, M40, etc.
     * Senior Women’s Singles* (O30, O40, O50, O60, O65, O70, O75, O80, O85) -> W30, W40 etc.
     *
     * @param tournamentEvent
     * @return
     */
    private String getEventCode(TournamentEvent tournamentEvent) {
        String eventCode = tournamentEvent.getName();
        GenderRestriction genderRestriction = tournamentEvent.getGenderRestriction();
        if (tournamentEvent.getAgeRestrictionType() != AgeRestrictionType.NONE) {
            if (tournamentEvent.getMaxPlayerAge() > 0) {
                String genderIndicator = genderRestriction == GenderRestriction.MALE ? "B" : "G";
                eventCode = genderIndicator + tournamentEvent.getMaxPlayerAge();
            } else if (tournamentEvent.getMinPlayerAge() > 0) {
                String genderIndicator = genderRestriction == GenderRestriction.MALE ? "M" : "W";
                eventCode = genderIndicator + tournamentEvent.getMinPlayerAge();
            }
        } else if (genderRestriction != GenderRestriction.NONE) {
            eventCode = genderRestriction == GenderRestriction.MALE ? "MS" : "WS";
        }

        return eventCode;
    }

    /**
     * Gets the event which need to appear on this report
     *
     * @param tournamentId
     * @return
     */
    private Collection<TournamentEvent> getRankedEvents(long tournamentId) {
        Collection<TournamentEvent> allTournamentEvents = this.tournamentEventEntityService.list(tournamentId, Pageable.unpaged());
        Tournament tournament = this.tournamentService.getByKey(tournamentId);
        Collection<TournamentEvent> rankedEvents = Collections.emptyList();
        if (!tournament.getConfiguration().getEligibilityRestriction().equals(EligibilityRestriction.OPEN)) {
            rankedEvents = allTournamentEvents;
        } else {
            rankedEvents = new ArrayList<>();
            for (TournamentEvent tournamentEvent : allTournamentEvents) {
                if (tournamentEvent.getName().equals("Open Singles") || tournamentEvent.getName().equals("Womens")) {
//                if (!tournamentEvent.getEligibilityRestriction().equals(EligibilityRestriction.OPEN)) {
                    rankedEvents.add(tournamentEvent);
                }
            }
        }
        log.info("Found " + rankedEvents.size() + " ranked events");
        return rankedEvents;
    }

    /**
     * Builds report lines for one event
     *
     * @param tournamentEvent
     * @return
     */
    private List<ReportLineInfo> generateEventReportLineInfos(TournamentEvent tournamentEvent) {
        log.info("Generating ranking for event " + tournamentEvent.getName());
        List<ReportLineInfo> reportLineInfos = new ArrayList<>();
        List<MatchCard> singleEliminationEventMatchCards = matchCardService.findAllForEventAndDrawTypeWithPlayerMap(tournamentEvent.getId(), DrawType.SINGLE_ELIMINATION);
        for (MatchCard matchCard : singleEliminationEventMatchCards) {
            addMatchCardReportInfos(matchCard, reportLineInfos);
        }

        // sort 1st through nth place
        reportLineInfos.sort(Comparator.comparing(ReportLineInfo::getRound)
                .thenComparing(ReportLineInfo::getRank));

        List<MatchCard> roundRobinEventMatchCards = matchCardService.findAllForEventAndDrawTypeWithPlayerMap(tournamentEvent.getId(), DrawType.ROUND_ROBIN);
        for (MatchCard matchCard : roundRobinEventMatchCards) {
            addMatchCardReportInfos(matchCard, reportLineInfos);
        }

        fillPlayerInformation(reportLineInfos);

        log.info("Done generating ranking for event");

        return reportLineInfos;
    }

    /**
     * Gets membership id for each player
     *
     * @param reportLineInfos
     */
    private void fillPlayerInformation(List<ReportLineInfo> reportLineInfos) {
        // collect unique profile ids
        List<String> profileIds = new ArrayList<>();
        for (ReportLineInfo reportLineInfo : reportLineInfos) {
            if (StringUtils.isNotEmpty(reportLineInfo.profileId)) {
                profileIds.add(reportLineInfo.profileId);
            }
        }

        // get extended user profile to fill membership id
        Map<String, UserProfileExt> userProfileExtMap = this.userProfileExtService.findByProfileIds(profileIds);
        for (ReportLineInfo reportLineInfo : reportLineInfos) {
            if (StringUtils.isNotEmpty(reportLineInfo.profileId)) {
                UserProfileExt userProfileExt = userProfileExtMap.get(reportLineInfo.profileId);
                reportLineInfo.memberId = userProfileExt.getMembershipId();
            } else {
                reportLineInfo.memberId = 0;
            }
        }

        // fill gender and date of birth
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
        Collection<UserProfile> userProfiles = this.userProfileService.listByProfileIds(profileIds);
        for (UserProfile userProfile : userProfiles) {
            for (ReportLineInfo reportLineInfo : reportLineInfos) {
                if (StringUtils.equals(reportLineInfo.profileId, userProfile.getUserId())) {
                    reportLineInfo.gender = userProfile.getGender().equals("Male") ? "M" : "F";
                    Date dateOfBirth = userProfile.getDateOfBirth();
                    reportLineInfo.dateOfBirth = dateFormat.format(dateOfBirth);
                }
            }
        }
    }

    /**
     * @param matchCard
     * @param reportLineInfos
     */
    private void addMatchCardReportInfos(MatchCard matchCard, List<ReportLineInfo> reportLineInfos) {
        Map<Integer, String> playerRankingsAsMap = matchCard.getPlayerRankingsAsMap();
        Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
        for (Integer rank : playerRankingsAsMap.keySet()) {
            ReportLineInfo reportLineInfo = new ReportLineInfo();
            String profileId = playerRankingsAsMap.get(rank);
            String playerName = profileIdToNameMap.get(profileId); // last name, first name
            playerName = (playerName != null) ? playerName : ",";
            playerName = playerName.replace(", ", ",");
            reportLineInfo.profileId = profileId;
            reportLineInfo.fullName = playerName;
            reportLineInfo.round = matchCard.getRound();
            if (matchCard.getRound() == 2) {  // round of 2 i.e. finals or 3rd & 4th place
                List<Match> matches = matchCard.getMatches();
                if (matches.size() == 1) {
                    Match match = matches.get(0);
                    if (match.getMatchNum() == 2) {  // 3rd and 4th place match
                        reportLineInfo.round = 4;
                    }
                }
            }
            reportLineInfo.rank = rank;
            reportLineInfo.strRank = getStrRank(reportLineInfo.round, reportLineInfo.rank, matchCard.getDrawType());
            addReportLineInfo(reportLineInfo, reportLineInfos, matchCard.getDrawType());
        }
    }

    /**
     * Adds report line info with the highest place
     *
     * @param reportLineInfoToAdd
     * @param reportLineInfos
     * @param drawType
     */
    private void addReportLineInfo(ReportLineInfo reportLineInfoToAdd, List<ReportLineInfo> reportLineInfos, DrawType drawType) {
        Iterator<ReportLineInfo> iterator = reportLineInfos.iterator();
        boolean add = false;
        boolean foundAlready = false;
        while (iterator.hasNext()) {
            ReportLineInfo reportLineInfo = iterator.next();
            if (StringUtils.equals(reportLineInfo.getProfileId(), reportLineInfoToAdd.getProfileId())) {
                foundAlready = true;
                if (drawType == DrawType.SINGLE_ELIMINATION) {
                    if (reportLineInfo.getRound() >= reportLineInfoToAdd.getRound()) {
                        reportLineInfos.remove(reportLineInfo);  // remove earlier round rank
                    }
                    add = true;
                }
                break;
            }
        }

        if (drawType == DrawType.SINGLE_ELIMINATION  && !foundAlready) {
            add = true;
        } else if (drawType == DrawType.ROUND_ROBIN && reportLineInfoToAdd.getRank() == 1) {
            if (!foundAlready) {
                add = true;
            }
        }

        if (add) {
            reportLineInfos.add(reportLineInfoToAdd);
        }
    }

    /**
     * Gets a textual representation of a rank
     *
     * @param round    round of 2, 4, 8, 16 etc.
     * @param rank     rank within this group
     * @param drawType single elimination or round robin
     * @return
     */
    private String getStrRank(int round, int rank, DrawType drawType) {
        String strRank = "";
        if (drawType == DrawType.SINGLE_ELIMINATION) {
            if (round == 2) {
                strRank = (rank == 1) ? "1st" : "2nd";
            } else {// e.g. 3-4, 5-8, 9-16 etc.
                int startRank = (round / 2) + 1;
                int endRank = round;
                strRank = String.format("%d-%d", startRank, endRank);
            }
        } else {
            strRank = "QA-1";
        }
        return strRank;
    }

    /**
     * holds information for each report line
     * TournamentID,MemberID,LastName,FirstName,DOB,Gender,EventCategory,FinishingPosition
     * 38,31426,Friend,Chance,8/24/1991,M,MS,1st
     */
    private static class ReportLineInfo {
        String profileId;
        String fullName;
        long memberId;
        String dateOfBirth;
        String gender;
        int round;
        int rank;
        // 1st, 2nd, 3 - 4 place etc.
        String strRank;

        @Override
        public String toString() {
            return "ReportLineInfo{" +
                    "profileId='" + profileId + '\'' +
                    ", fullName='" + fullName + '\'' +
                    ", memberId=" + memberId +
                    ", dateOfBirth='" + dateOfBirth + '\'' +
                    ", gender='" + gender + '\'' +
                    ", round=" + round +
                    ", rank=" + rank +
                    ", strRank='" + strRank + '\'' +
                    '}';
        }

        int getRound() {
            return this.round;
        }

        int getRank() {
            return this.rank;
        }

        String getProfileId() {
            return profileId;
        }
    }


}
