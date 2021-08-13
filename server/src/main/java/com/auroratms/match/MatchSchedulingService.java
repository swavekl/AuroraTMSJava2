package com.auroratms.match;

import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        // schedule single elimination round matches after round robin is scheduled and tables are assigned
        for (TournamentEventEntity event : daysEvents) {
            if (!event.isSingleElimination()) {
                // if there is a single elimination following round robin
                if (event.getPlayersToAdvance() > 0) {
                    scheduleSingleEliminationMatches (totalAvailableTables, event, matrix);
                }
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
            matchCard.setAssignedTables(null);
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
                    TableAvailabilityMatrix.AvailableTableInfo availableTable = matrix.findAvailableTable(event.getStartTime(), durationOnOneTable, currentTableNum, true);
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
//            } else {
//                matchCard.setAssignedTables("N/A");
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

    /**
     * Schedule single elimination round matches
     * @param totalAvailableTables
     * @param event
     * @param matrix
     */
    private void scheduleSingleEliminationMatches(int totalAvailableTables, TournamentEventEntity event, TableAvailabilityMatrix matrix) {
        // schedule the single elimination round matches on the same tables where the round robin round matches were played
        int maxDuration = 0;
        double eventStart = event.getStartTime();
        // range of tables the matches are played on in this event.
        int eventFirstTableNum = totalAvailableTables;
        int eventLastTableNum = 1;
        // find time when the round robin matches end
        List<MatchCard> roundRobinMatchCards = matchCardService.findAllForEventAndDrawType(event.getId(), DrawType.ROUND_ROBIN);
        for (MatchCard matchCard : roundRobinMatchCards) {
            int duration = matchCard.getDuration();
            maxDuration = Math.max(duration, maxDuration);
            String assignedTables = matchCard.getAssignedTables();
            String[] tableNumbers = assignedTables.split(",");
            if (tableNumbers.length > 0) {
                int matchCardFirstTable = Integer.valueOf(tableNumbers[0]);
                eventFirstTableNum = Math.min(eventFirstTableNum, matchCardFirstTable);
                eventLastTableNum = Math.max(eventLastTableNum, matchCardFirstTable);
            }
        }

        // When the round robin round is finished
        double singleEliminationStartTime = eventStart + (TableAvailabilityMatrix.TIME_SLOT_SIZE * Math.floorDiv(maxDuration, TableAvailabilityMatrix.TIME_SLOT_SIZE_INT));
        // start single elimination rounds 30 minutes later but only if there were at least 8 groups
        singleEliminationStartTime += (roundRobinMatchCards.size() >= 8) ? TableAvailabilityMatrix.TIME_SLOT_SIZE : 0;

        // schedule the single elimination round matches on the same tables where the round robin round matches were played
        int currentTableNum = eventFirstTableNum;
        List<MatchCard> singleEliminationMatchCards = matchCardService.findAllForEventAndDrawType(event.getId(), DrawType.SINGLE_ELIMINATION);
        double currentRoundStartTime = singleEliminationStartTime;
        double previousRoundStartTime = currentRoundStartTime;
        int previousRoundDuration = TableAvailabilityMatrix.TIME_SLOT_SIZE_INT;
        int currentRound = event.getMaxEntries();
        boolean firstMatch = true;
//        System.out.println("event = " + event.getName());
        for (MatchCard matchCard : singleEliminationMatchCards) {
            int duration = calculateAllMatchesDuration(matchCard.getNumberOfGames(), 1);
//            System.out.println("matchCard = " + matchCard.getGroupNum() + " assigned tables " + matchCard.getAssignedTables() + " startTime " + matchCard.getStartTime());
//            System.out.println("duration = " + duration);
            // get the first match round and duration
            if (firstMatch) {
                firstMatch = false;
                currentRound = matchCard.getRound();
                previousRoundDuration = duration;
            }

            // all matches in the same round should have the same starting time and duration
            if (matchCard.getRound() != currentRound) {
                currentRound = matchCard.getRound();
                currentRoundStartTime = previousRoundStartTime + (previousRoundDuration / TableAvailabilityMatrix.TIME_SLOT_SIZE_INT) * TableAvailabilityMatrix.TIME_SLOT_SIZE;
                previousRoundDuration = duration;
                // try finding a table starting with the first one that this event is played on
                currentTableNum = eventFirstTableNum;
            }
//            System.out.println("currentRound = " + currentRound + ", currentRoundStartTime = " + currentRoundStartTime + ", currentTableNum = " + currentTableNum);

            // find table with one following the last one
            TableAvailabilityMatrix.AvailableTableInfo availableTable = matrix.findAvailableTable(currentRoundStartTime, duration, currentTableNum, false);
            if (availableTable != null) {
                matrix.markTableAsUnavailable(availableTable.tableNum, availableTable.startTime, duration);
                currentTableNum = availableTable.tableNum + 1;
                // if we fail because we reached the last available table, start from first table for this event again
                currentTableNum = (currentTableNum <= totalAvailableTables) ? currentTableNum : eventFirstTableNum;
//                System.out.println("availableTable.startTime = " + availableTable.startTime + ", availableTable.tableNum = " + availableTable.tableNum);

                // save the latest start number so next round starts after that match
                previousRoundStartTime = Math.max(previousRoundStartTime, availableTable.startTime);
//                System.out.println("previousRoundStartTime = " + previousRoundStartTime);

                String assignedTables = Integer.toString(availableTable.tableNum);
                matchCard.setAssignedTables(assignedTables);
                matchCard.setStartTime(availableTable.startTime);
                matchCard.setDuration(duration);
            } else {
                matchCard.setAssignedTables(null);
                System.out.println("Unable to find table for matchCard = " + matchCard.getGroupNum() + " assigned tables " + matchCard.getAssignedTables() + " startTime " + matchCard.getStartTime());
            }
            matchCardService.save(matchCard);
        }
    }
}
