package com.auroratms.match;

import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for generating automatic schedule of matches for tournament events
 */
@Service
public class MatchSchedulingService {

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private TournamentService tournamentService;

    /**
     *  @param tournamentId
     * @param day
     * @return
     */
    public List<MatchCard> generateScheduleForDay (long tournamentId, int day) {
        // make a matrix for marking which time slots are not available as we are filling up the schedule
        Tournament tournament = this.tournamentService.getByKey(tournamentId);
        int totalAvailableTables = tournament.getConfiguration().getNumberOfTables();
        TableAvailabilityMatrix matrix = new TableAvailabilityMatrix(totalAvailableTables);

        // generate them
        List<TournamentEventEntity> daysEvents = this.tournamentEventEntityService.listDaysEvents(tournamentId, day);
        int startingTableNumber = 1;
        double previousEventStartTime = 8.0d;
        for (TournamentEventEntity event : daysEvents) {
            // start scheduling this event's matches from the 1st table if it is starting later
            if (event.getStartTime() != previousEventStartTime) {
                startingTableNumber = 1;
            }
            previousEventStartTime = event.getStartTime();
            if (!event.isSingleElimination()) {
                startingTableNumber = scheduleRoundRobinMatches(startingTableNumber, totalAvailableTables, event, matrix);
            } else {

            }
        }
        // get them
        return this.matchCardService.findAllForTournamentAndDay(tournamentId, day);
    }

    /**
     *
     * @param startingTableNumber
     * @param totalAvailableTables
     * @param event
     * @param matrix
     * @return
     */
    private int scheduleRoundRobinMatches(int startingTableNumber, int totalAvailableTables, TournamentEventEntity event, TableAvailabilityMatrix matrix) {
        // calculate number of required tables to play this event if it were completely full
        int numTablesPerGroup = event.getNumTablesPerGroup();
        int maxEntries = event.getMaxEntries();
        int playersPerGroup = event.getPlayersPerGroup();
        int maxNumGroups = maxEntries / playersPerGroup;
        int maxTablesNeeded = maxNumGroups * numTablesPerGroup;
        int playersToAdvance = event.getPlayersToAdvance();

        // assign tables to each match card
        int currentTableNum = startingTableNumber;
        List<MatchCard> allForEvent = matchCardService.findAllForEventAndDrawType(event.getId(), DrawType.ROUND_ROBIN);
        for (MatchCard matchCard : allForEvent) {
            // can't assign more tables than we have
            if (currentTableNum <= totalAvailableTables) {
                // calculate duration
                int numMatchesToPlay = matchCard.getMatches().size();
                int allMatchesDuration = calculateAllMatchesDuration(event.getNumberOfGames(), numMatchesToPlay);
                int tablesToAssign = getNumTablesToAssign (numTablesPerGroup, numMatchesToPlay, playersPerGroup, playersToAdvance);
                // assign tables and mark them as used - one at a time
                String assignedTables = "";
                int durationOnOneTable = Math.floorDiv(allMatchesDuration, tablesToAssign);
                String name = event.getName();
//                System.out.println("eventName = " + name + " startTime = " + event.getStartTime());
                double assignedStartTime = event.getStartTime();
                for (int i = 0; i < tablesToAssign; i++) {
                    TableAvailabilityMatrix.AvailableTableInfo availableTable = matrix.findAvailableTable(event.getStartTime(), durationOnOneTable, currentTableNum);
                    if (availableTable != null) {
                        matrix.markTableAsUnavailable (availableTable.tableNum, availableTable.startTime, durationOnOneTable);
                        assignedStartTime = availableTable.startTime;
                        assignedTables += (StringUtils.isEmpty(assignedTables)) ? "": ",";
                        assignedTables += (availableTable.tableNum);

                        currentTableNum = availableTable.tableNum + 1;
                    }
                }
//                System.out.println("assignedStartTime = " +assignedStartTime);
//                System.out.println("assignedTables = " + assignedTables);

                matchCard.setAssignedTables(assignedTables);
                matchCard.setStartTime(assignedStartTime);
                matchCard.setDuration(durationOnOneTable);
            } else {
                matchCard.setAssignedTables("N/A");
            }

            matchCard.setDay(event.getDay());
            matchCardService.save(matchCard);
        }

        // even when all tables are not used 'reserve' them by skipping unused tables
        return startingTableNumber + maxTablesNeeded;
    }

    /**
     *
     * @param numTablesPerGroup
     * @param numMatchesToPlay
     * @param playersToAdvance
     * @return
     */
    private int getNumTablesToAssign(int numTablesPerGroup, int numMatchesToPlay, int playersPerGroup, int playersToAdvance) {
        // max matches if group was full
        List<MatchOpponents> matchesInOrder = MatchOrderGenerator.generateOrderOfMatches(playersPerGroup, playersToAdvance);
        int maxMatchesToPlay = matchesInOrder.size();

        int tablesToAssign = numTablesPerGroup;
        if (numMatchesToPlay < maxMatchesToPlay) {
            int matchesPerTable = Math.floorDiv(maxMatchesToPlay, numTablesPerGroup);
            int matchesPlayed = 0;
            tablesToAssign = 0;
            while (matchesPlayed < numMatchesToPlay) {
                tablesToAssign++;
                matchesPlayed += matchesPerTable;
            }
        }

        return tablesToAssign;
    }

    /**
     *
     * @param numberOfGames
     * @param numMatchesToPlay
     * @return
     */
    private int calculateAllMatchesDuration(int numberOfGames, int numMatchesToPlay) {
        int singleMatchDuration = 0;
        switch (numberOfGames) {
            case 3:
                singleMatchDuration = 20;
                break;
            case 5:
                singleMatchDuration = 30;
                break;
            case 7:
                singleMatchDuration = 60;
                break;
            default:
                singleMatchDuration = 20;
                break;
        }

        return (numMatchesToPlay * singleMatchDuration);
    }
}
