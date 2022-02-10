package com.auroratms.reports;

import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.match.MatchService;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Results report contains results of individual matches in a comma separated CSV file.  It has the following format
 * EventID,MemNum_W,MemNum_L,Score,Division
 * 0,79210,202393,"9,7,-11,-9,10",Under 1900 RR
 * where
 * MemNum_W stands for membership number of a winner
 * MemNum_L stands for membership number of a loser
 * Division is an event name and round
 */
@Service
@Slf4j
public class ResultsReportService {

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    public String generateReport(long tournamentId) {
        String reportFilename = null;
        FileWriter fileWriter = null;
        try {
            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            File reportFile = new File(tempDir + File.separator + "tournament-results-" + tournamentId + ".csv");
            reportFilename = reportFile.getCanonicalPath();
            log.info("Writing tournament results report for tournament " + tournamentId);
            log.info("to " + reportFilename);
            long start = System.currentTimeMillis();

            // write header
            fileWriter = new FileWriter(reportFile);
            fileWriter.write("EventID,MemNum_W,MemNum_L,Score,Division\n");

            // write results for each event
            int numResults = 0;
            Collection<TournamentEvent> tournamentEvents = tournamentEventEntityService.list(tournamentId, Pageable.unpaged());
            for (TournamentEvent tournamentEvent : tournamentEvents) {
                // doubles events are nor reported
                if (!tournamentEvent.isDoubles()) {
                    Long eventId = tournamentEvent.getId();
                    log.info("Writing results for event " + tournamentEvent.getName());
                    List<MatchCard> allMatchCardsForEvent = matchCardService.findAllForEvent(eventId);

                    // sort them so RR is followed by SE
                    Comparator<MatchCard> comparator = Comparator
                            .comparing(MatchCard::getDrawType)
                            .thenComparing(Comparator.comparing(MatchCard::getRound).reversed())
                            .thenComparing(MatchCard::getGroupNum);
                    Collections.sort(allMatchCardsForEvent, comparator);
//                    System.out.println("Match cards for " + tournamentEvent.getName());
//                    for (MatchCard matchCard : allMatchCardsForEvent) {
//                        System.out.println(matchCard.getDrawType() + " / " + matchCard.getRoundName() + " / group " + matchCard.getGroupNum());
//                    }
//                    System.out.println("---");

                    List<Match> matchesForEvent = matchService.findAllByMatchCardIn(allMatchCardsForEvent);
                    // get player profiles for all players in this event, so we can get their membership ids in one call
                    Set<String> uniqueProfileIdsSet = new HashSet<>();
                    for (Match match : matchesForEvent) {
                        uniqueProfileIdsSet.add(match.getPlayerAProfileId());
                        uniqueProfileIdsSet.add(match.getPlayerBProfileId());
                    }
                    List<String> profileIds = new ArrayList<>(uniqueProfileIdsSet);
                    Map<String, UserProfileExt> userProfileExtMap = userProfileExtService.findByProfileIds(profileIds);
                    for (MatchCard matchCard : allMatchCardsForEvent) {
                        long matchCardId = matchCard.getId();
                        for (Match match : matchesForEvent) {
                            long matchCardId2 = match.getMatchCard().getId();
                            if (matchCardId == matchCardId2) {
                                boolean matchFinished = match.isMatchFinished(tournamentEvent.getNumberOfGames(), tournamentEvent.getPointsPerGame());
                                boolean isDefaulted = match.isSideADefaulted() || match.isSideBDefaulted();
                                if (matchFinished && !isDefaulted) {
                                    String reportLine = generateLine(matchCard, match, tournamentEvent, userProfileExtMap);
                                    if (!reportLine.isEmpty()) {
                                        fileWriter.write(reportLine);
                                        numResults++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            long duration = System.currentTimeMillis() - start;
            log.info("Finished writing report with " + numResults + " match results in " + duration);

        } catch (IOException e) {
            log.error("Unable to create report for tournament " + tournamentId, e);
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
     * Gets a text for row of report
     *
     * @param matchCard
     * @param match
     * @param tournamentEvent
     * @param userProfileExtMap
     * @return
     */
    private String generateLine(MatchCard matchCard, Match match, TournamentEvent tournamentEvent, Map<String, UserProfileExt> userProfileExtMap) {
        UserProfileExt playerAUserProfileExt = userProfileExtMap.get(match.getPlayerAProfileId());
        UserProfileExt playerBUserProfileExt = userProfileExtMap.get(match.getPlayerBProfileId());
        boolean playerAIsMatchWinner = match.isMatchWinner(match.getPlayerAProfileId(), tournamentEvent.getNumberOfGames(), tournamentEvent.getPointsPerGame());
        Long winnerMembershipId = (playerAIsMatchWinner) ? playerAUserProfileExt.getMembershipId() : playerBUserProfileExt.getMembershipId();
        Long loserMembershipId = (!playerAIsMatchWinner) ? playerAUserProfileExt.getMembershipId() : playerBUserProfileExt.getMembershipId();
        String compactResult = match.getCompactResult(tournamentEvent.getNumberOfGames(), tournamentEvent.getPointsPerGame());
        String drawType = (matchCard.getDrawType() == DrawType.ROUND_ROBIN) ? "RR" : "SE";
        String roundName = (matchCard.getDrawType() == DrawType.ROUND_ROBIN) ? "" : matchCard.getRoundName();
        String division = String.format("%s %s %s ", tournamentEvent.getName(), drawType, roundName);
        return String.format("0,%d,%d,\"%s\",%s\n", winnerMembershipId, loserMembershipId, compactResult, division);
    }
}
