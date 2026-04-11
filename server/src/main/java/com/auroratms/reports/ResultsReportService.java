package com.auroratms.reports;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.match.MatchService;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Results report contains results of individual matches in a Excel spreadsheet.
 * EventID,MemNum_W,MemNum_L,Score,Division
 * 0,79210,202393,"9,7,-11,-9,10",Under 1900 RR
 * where
 * MemNum_W stands for membership number of a winner
 * MemNum_L stands for membership number of a loser
 * Division is an event name and round
 */
@Service
@Slf4j
@Transactional
public class ResultsReportService {

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private UsattDataService usattDataService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    public String generateReport(long tournamentId) {
        String reportFilename = null;
        // Use XSSFWorkbook for .xlsx format
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            File reportFile = new File(tempDir + File.separator + "tournament-results-" + tournamentId + ".xlsx");
            reportFilename = reportFile.getCanonicalPath();

            // Create the two required sheets
            XSSFSheet resultsSheet = workbook.createSheet("Match Results");
            XSSFSheet ratingsSheet = workbook.createSheet("Estimated Ratings");

            // Initialize Headers
            writeHeaders(resultsSheet, new String[]{"Winner Name", "Winner Membership#", "Loser Name", "Loser Membership#", "Scores", "Event"});
            writeHeaders(ratingsSheet, new String[]{"Name", "Membership#", "Est Rating"});

            // get all player entries
            List<TournamentEntry> allTournamentEntries = tournamentEntryService.listForTournament(tournamentId);
            // get player profile ids
            List<String> allProfileIds = allTournamentEntries.stream().map(TournamentEntry::getProfileId).toList();

            // get map of player profile ids to user profile ext
            Map<String, UserProfileExt> profileIdsToProfileMap = userProfileExtService.findByProfileIds(allProfileIds);

            // find all player full names and who is not rated yet
            Map<Long, String> membershipIdToFullName = new HashMap<>();
            Set<Long> unratedPlayersMembershipIds = new HashSet<>();
            if (profileIdsToProfileMap != null && !profileIdsToProfileMap.isEmpty()) {
                List<Long> allPlayersMembershipIds = profileIdsToProfileMap.values().stream()
                        .map(UserProfileExt::getMembershipId)
                        .toList();

                // fetch all player records by membership id
                List<UsattPlayerRecord> playerRecords = this.usattDataService.findAllByMembershipIdIn(allPlayersMembershipIds);
                playerRecords.forEach(pr -> {
                    // fetch full name
                    String fullName = pr.getFirstName() + " " + pr.getLastName();
                    membershipIdToFullName.put(pr.getMembershipId(), fullName);

                    // record membership id if not rated yet
                    if (pr.getLastTournamentPlayedDate() == null) {
                        unratedPlayersMembershipIds.add(pr.getMembershipId());
                    }
                });
            }

            // write match results in worksheet 1
            int resultsRowNum = 1;
            Collection<TournamentEvent> tournamentEvents = tournamentEventEntityService.list(tournamentId, Pageable.unpaged());
            for (TournamentEvent tournamentEvent : tournamentEvents) {
                if (!tournamentEvent.isDoubles()) {
                    List<MatchCard> matchCards = matchCardService.findAllForEvent(tournamentEvent.getId());
                    List<Match> matches = matchService.findAllByMatchCardIn(matchCards);

                    for (Match match : matches) {
                        MatchCard matchCard = match.getMatchCard();
                        if (match.isMatchFinished(matchCard.getNumberOfGames(), tournamentEvent.getPointsPerGame())
                                && !match.isSideADefaulted() && !match.isSideBDefaulted()) {

                            assert profileIdsToProfileMap != null;
                            boolean added = addMatchRow(resultsSheet, resultsRowNum, match, matchCard, tournamentEvent,
                                    profileIdsToProfileMap,
                                    membershipIdToFullName);
                            if (added) {
                                resultsRowNum++;
                            }
                        }
                    }
                }
            }

            if (!unratedPlayersMembershipIds.isEmpty()) {
                // get user profiles of unrated players
                List<UserProfileExt> unratedPlayerProfiles = profileIdsToProfileMap.values().stream()
                        .filter(userProfileExt -> unratedPlayersMembershipIds.contains(userProfileExt.getMembershipId()))
                        .toList();

                // get tournament entries of unrated players
                List<@NonNull String> unratedPlayerProfileIds = unratedPlayerProfiles.stream().map(UserProfileExt::getProfileId).toList();
                List<TournamentEntry> unratedPlayerTournamentEntries = allTournamentEntries.stream()
                        .filter(tournamentEntry -> unratedPlayerProfileIds.contains(tournamentEntry.getProfileId()))
                        .toList();

                int ratingsRowNum = 1;
                // Fill Estimated Ratings sheet (assuming Est Rating might be manually filled or added to UserProfileExt)
                for (UserProfileExt userProfileExt : unratedPlayerProfiles) {
                    Long membershipId = userProfileExt.getMembershipId();
                    String fullName = membershipIdToFullName.get(membershipId);
                    Optional<TournamentEntry> first = unratedPlayerTournamentEntries.stream()
                            .filter(tournamentEntry -> tournamentEntry.getProfileId().equals(userProfileExt.getProfileId()))
                            .findFirst();
                    int estimatedRating = 0;
                    if (first.isPresent()) {
                        estimatedRating = first.get().getSeedRating();
                    }

                    Row row = ratingsSheet.createRow(ratingsRowNum++);
                    row.createCell(0).setCellValue(fullName);
                    row.createCell(1).setCellValue(membershipId);
                    row.createCell(2).setCellValue(estimatedRating);
                }
            }

            try (FileOutputStream fileOut = new FileOutputStream(reportFile)) {
                workbook.write(fileOut);
            }
            log.info("Excel report generated at: " + reportFilename);

        } catch (IOException e) {
            log.error("Excel generation failed", e);
        }
        return reportFilename;
    }

    private void writeHeaders(XSSFSheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
    }

    /**
     *
     * @param sheet
     * @param rowNum
     * @param match
     * @param matchCard
     * @param event
     * @param profileMap
     * @param membershipIdToFullName
     * @return true if match was added to the sheet, false otherwise
     */
    private boolean addMatchRow(XSSFSheet sheet,
                                int rowNum,
                                Match match,
                                MatchCard matchCard,
                                TournamentEvent event,
                                Map<String, UserProfileExt> profileMap,
                                Map<Long, String> membershipIdToFullName) {
        UserProfileExt playerA = profileMap.get(match.getPlayerAProfileId());
        UserProfileExt playerB = profileMap.get(match.getPlayerBProfileId());

        if (playerA != null && playerB != null) {
            boolean aWins = match.isMatchWinner(playerA.getProfileId(), matchCard.getNumberOfGames(), event.getPointsPerGame());
            UserProfileExt winner = aWins ? playerA : playerB;
            UserProfileExt loser = aWins ? playerB : playerA;

            String winnerFullName = membershipIdToFullName.get(winner.getMembershipId());
            String loserFullName = membershipIdToFullName.get(loser.getMembershipId());
            winnerFullName = !StringUtils.isEmpty(winnerFullName) ? winnerFullName : "";
            loserFullName = !StringUtils.isEmpty(loserFullName) ? loserFullName : "";
            String matchResult = match.getCompactResult(matchCard.getNumberOfGames(), event.getPointsPerGame());
            String eventAndRound = event.getName() + " - " + matchCard.getRoundName();

            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(winnerFullName);
            row.createCell(1).setCellValue(winner.getMembershipId());
            row.createCell(2).setCellValue(loserFullName);
            row.createCell(3).setCellValue(loser.getMembershipId());
            row.createCell(4).setCellValue(matchResult);
            row.createCell(5).setCellValue(eventAndRound);
            return true;
        }
        return false;
    }
}

