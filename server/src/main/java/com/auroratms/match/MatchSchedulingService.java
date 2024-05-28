package com.auroratms.match;

import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.exception.MatchSchedulingException;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for generating automatic schedule of matches for tournament events
 */
@Service
@Transactional
public class MatchSchedulingService {

    private static final Logger log = LoggerFactory.getLogger(MatchSchedulingService.class);
    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private TournamentService tournamentService;

    /**
     * Generates schedule for all events for the specified day by assigning tables to all round robin
     * and single elimination rounds
     *
     * @param tournamentId tournament id
     * @param day          day of the tournament to generate schedule for
     * @return all match cards for this day with table numbers and starting times assigned
     */
    public List<MatchCard> generateScheduleForDay(long tournamentId, int day) throws MatchSchedulingException {
        // make a matrix for marking which time slots are not available as we are filling up the schedule
        Tournament tournament = this.tournamentService.getByKey(tournamentId);
        int totalAvailableTables = tournament.getConfiguration().getNumberOfTables();
        TableAvailabilityMatrix matrix = new TableAvailabilityMatrix(totalAvailableTables);
        // generate them
        List<TournamentEvent> daysEvents = this.tournamentEventEntityService.listDaysEvents(tournamentId, day);
        int startingTableNumber = 1;
        double previousEventStartTime = 8.0d;
        for (TournamentEvent event : daysEvents) {
            log.info ("Scheduling matches for event " + event.getName());
            // start scheduling this event's matches from the 1st table if it is starting later
            if (event.getStartTime() != previousEventStartTime) {
                startingTableNumber = 1;
            }
            previousEventStartTime = event.getStartTime();
            if (!event.isSingleElimination()) {
                startingTableNumber = scheduleRoundRobinMatches(startingTableNumber, totalAvailableTables, event, matrix);
            } else {
                scheduleSingleEliminationMatches(totalAvailableTables, event, matrix);
            }
        }
        log.info("Finished scheduling first round matches");
        // schedule single elimination round matches after round robin is scheduled and tables are assigned
        for (TournamentEvent event : daysEvents) {
            if (!event.isSingleElimination()) {
                // if there is a single elimination following round robin
                if (event.getPlayersToAdvance() > 0) {
                    scheduleSingleEliminationMatches(totalAvailableTables, event, matrix);
                }
            }
        }
        log.info("Finished scheduling second round matches");
        // get them
        return this.matchCardService.findAllForTournamentAndDay(tournamentId, day);
    }

    /**
     * Generates schedule just for the specified match cards to preserve possibly hand adjusted schedule for all other events
     * for that day.
     *
     * @param tournamentId tournament id
     * @param day          day in which these events take place
     * @param matchCardIds match cards to generate schedule for
     * @return
     */
    public List<MatchCard> generateScheduleForMatchCards(long tournamentId, int day, List<Long> matchCardIds) throws MatchSchedulingException {
        Tournament tournament = this.tournamentService.getByKey(tournamentId);
        int totalAvailableTables = tournament.getConfiguration().getNumberOfTables();
        TableAvailabilityMatrix matrix = new TableAvailabilityMatrix(totalAvailableTables);

        // fill the table availability matrix with all other match cards for that day
        // so that we know which tables are available.
        List<MatchCard> allTodaysMatchCards = this.matchCardService.findAllForTournamentAndDay(tournamentId, day);
        Set<Long> eventIdsToFix = new HashSet<>();
        for (MatchCard matchCard : allTodaysMatchCards) {
            if (!matchCardIds.contains(matchCard.getId())) {
                String strAssignedTables = matchCard.getAssignedTables();
                String[] assignedTables = strAssignedTables.split(",");
                int duration = matchCard.getDuration();
                for (int i = 0; i < assignedTables.length; i++) {
                    Integer assignedTable = Integer.parseInt(assignedTables[i]);
                    matrix.markTableAsUnavailable(assignedTable, matchCard.getStartTime(), duration);
                }
            } else {
                eventIdsToFix.add(matchCard.getEventFk());
            }
        }

        // get list of event definitions that the match cards are for
        List<TournamentEvent> eventsToFix = new ArrayList<>(eventIdsToFix.size());
        for (Long eventId : eventIdsToFix) {
            TournamentEvent tournamentEvent = this.tournamentEventEntityService.get(eventId);
            eventsToFix.add(tournamentEvent);
        }

        // schedule all match cards for these events - both RR and SE type
        int startingTableNumber = 1;
        double previousEventStartTime = 8.0d;
        for (TournamentEvent event : eventsToFix) {
            // start scheduling this event's matches from the 1st table if it is starting later
            if (event.getStartTime() != previousEventStartTime) {
                startingTableNumber = 1;
            }
            previousEventStartTime = event.getStartTime();
            if (!event.isSingleElimination()) {
                startingTableNumber = scheduleRoundRobinMatches(startingTableNumber, totalAvailableTables, event, matrix);
            } else {
                scheduleSingleEliminationMatches(totalAvailableTables, event, matrix);
            }
        }

        // schedule single elimination round matches after round robin is scheduled and tables are assigned
        for (TournamentEvent event : eventsToFix) {
            if (!event.isSingleElimination()) {
                // if there is a single elimination following round robin
                if (event.getPlayersToAdvance() > 0) {
                    scheduleSingleEliminationMatches(totalAvailableTables, event, matrix);
                }
            }
        }

        // get them
        return this.matchCardService.findAllForTournamentAndDay(tournamentId, day);
    }

    /**
     * Schedules all match cards for specified event, taking into account available tables stored in matrix
     *
     * @param startingTableNumber  table number to start scheduling from
     * @param totalAvailableTables total available tables
     * @param event                event definition
     * @param matrix               matrix of table availability
     * @return next available table number
     */
    private int scheduleRoundRobinMatches(int startingTableNumber, int totalAvailableTables, TournamentEvent event, TableAvailabilityMatrix matrix) throws MatchSchedulingException {
        // calculate number of required tables to play this event if it were completely full
        int numTablesPerGroup = event.getNumTablesPerGroup();
        int maxEntries = event.getMaxEntries();
        int playersPerGroup = event.getPlayersPerGroup();
        int maxNumGroups = maxEntries / playersPerGroup;
        int maxTablesNeeded = maxNumGroups * numTablesPerGroup;
        int playersToAdvance = event.getPlayersToAdvance();

        log.info("++++++ event " + event.getName() + " startTime = " + event.getStartTime());
        log.info("numTablesPerGroup: " + numTablesPerGroup + " playersPerGroup: " + playersPerGroup + " maxNumGroups: " + maxNumGroups + " maxTablesNeeded: " + maxTablesNeeded);

        // assign tables to each match card
        int currentTableNum = startingTableNumber;
        List<MatchCard> allForEvent = matchCardService.findAllForEventAndDrawType(event.getId(), DrawType.ROUND_ROBIN);
        // if we need more tables to play all matches then are available then, push some matches to start later
        // this happens in Giant Round Robin events that schedule play in two parts (morning and afternoon)
        boolean mustStartAtStartTime = ((maxNumGroups * numTablesPerGroup) <= totalAvailableTables);
        for (MatchCard matchCard : allForEvent) {
            matchCard.setAssignedTables(null);
            // can't assign more tables than we have
            if (currentTableNum <= totalAvailableTables) {
                // calculate duration
                int numMatchesToPlay = matchCard.getMatches().size();
                int allMatchesDuration = calculateAllMatchesDuration(event.getNumberOfGames(), numMatchesToPlay, event.getPointsPerGame());
                int tablesToAssign = getNumTablesToAssign(numTablesPerGroup, numMatchesToPlay, playersPerGroup, playersToAdvance);
                log.info ("numMatchesToPlay: " + numMatchesToPlay + " allMatchesDuration: " + allMatchesDuration + " tablesToAssign: " + tablesToAssign);
                // assign tables and mark them as used - one at a time
                String assignedTables = "";
                int durationOnOneTable = Math.floorDiv(allMatchesDuration, tablesToAssign);
                // round up to the nearest 30 minutes, so it is nicely fitting into the matrix which is also 30 minutes based
                if (durationOnOneTable % TableAvailabilityMatrix.TIME_SLOT_SIZE_INT > 0) {
                    int numSlotsNeeded = (durationOnOneTable / TableAvailabilityMatrix.TIME_SLOT_SIZE_INT) + 1;
                    durationOnOneTable = numSlotsNeeded * TableAvailabilityMatrix.TIME_SLOT_SIZE_INT;
                }

                double assignedStartTime = event.getStartTime();
                for (int i = 0; i < tablesToAssign; i++) {
                    TableAvailabilityMatrix.AvailableTableInfo availableTable = matrix.findAvailableTable(event.getStartTime(), durationOnOneTable, currentTableNum, mustStartAtStartTime);
                    if (availableTable != null) {
                        matrix.markTableAsUnavailable(availableTable.tableNum, availableTable.startTime, durationOnOneTable);
                        assignedStartTime = availableTable.startTime;
                        assignedTables += (StringUtils.isEmpty(assignedTables)) ? "" : ",";
                        assignedTables += (availableTable.tableNum);

                        currentTableNum = availableTable.tableNum + 1;
                        // start over from first table if we got to the end
                        if (currentTableNum > totalAvailableTables) {
                            currentTableNum = startingTableNumber;
                        }
                    } else {
                        String message = "Unable to assign table number to group " + matchCard.getGroupNum() + " in '"
                                + event.getName() + "' event round robin round which starts at " + event.getStartTime() +
                                " due to lack of available table time.  You can assign a later starting time to this event or configure more tables to the prior event so it completes sooner.";
                        log.warn(message);
                        throw new MatchSchedulingException(message);
                    }
                }
                log.info("group num = " + matchCard.getGroupNum() + " assignedStartTime = " +assignedStartTime+" assignedTables = " + assignedTables);

                matchCard.setAssignedTables(assignedTables);
                matchCard.setStartTime(assignedStartTime);
                matchCard.setDuration(durationOnOneTable);
            }

            matchCard.setDay(event.getDay());
        }
        log.info ("Saving match cards");
        matchCardService.saveAllAndFlush(allForEvent);
        log.info ("Saved match cards");

        // even when all tables are not used 'reserve' them by skipping unused tables
        return startingTableNumber + maxTablesNeeded;
    }

    /**
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
     * @param numberOfGames
     * @param numMatchesToPlay
     * @param pointsPerGame
     * @return
     */
    private int calculateAllMatchesDuration(int numberOfGames, int numMatchesToPlay, int pointsPerGame) {
        int singleMatchDuration = 0;
        switch (numberOfGames) {
            case 3:
                singleMatchDuration = (pointsPerGame == 11) ? 20 : 25;
                break;
            case 5:
                singleMatchDuration = (pointsPerGame == 11) ? 25 : 30;
                break;
            case 7:
                singleMatchDuration = 40;
                break;
            default:
                singleMatchDuration = 20;
                break;
        }

        return (numMatchesToPlay * singleMatchDuration);
    }

    /**
     * Schedule single elimination round matches
     *
     * @param totalAvailableTables
     * @param event
     * @param matrix
     */
    private void scheduleSingleEliminationMatches(int totalAvailableTables, TournamentEvent event, TableAvailabilityMatrix matrix) {
        // schedule the single elimination round matches on the same tables where the round robin round matches were played
        int maxDuration = 0;
        double eventStart = event.getStartTime();
        // range of tables the matches are played on in this event.
        int eventFirstTableNum = (!event.isSingleElimination()) ? totalAvailableTables : 1;
        int eventLastTableNum = 1;
        log.info("scheduling single elimination matches for " + event.getName());
        // find time when the round robin matches end
        double singleEliminationStartTime = eventStart;
        if (!event.isSingleElimination()) {
            List<MatchCard> roundRobinMatchCards = matchCardService.findAllForEventAndDrawType(event.getId(), DrawType.ROUND_ROBIN);
            for (MatchCard matchCard : roundRobinMatchCards) {
                int duration = matchCard.getDuration();
                maxDuration = Math.max(duration, maxDuration);
                String assignedTables = matchCard.getAssignedTables();
                String[] tableNumbers = assignedTables.split(",");
                for (String tableNumber : tableNumbers) {
                    if (StringUtils.isNotEmpty(tableNumber)) {
                        int iTableNumber = Integer.parseInt(tableNumber);
                        eventFirstTableNum = Math.min(eventFirstTableNum, iTableNumber);
                        eventLastTableNum = Math.max(eventLastTableNum, iTableNumber);
                    }
                }
            }
            log.info("RR round firstTableNum = " + eventFirstTableNum + " lastTableNum = " + eventLastTableNum);
            log.info("maxDuration = " + maxDuration);

            // When the round robin round is finished
            singleEliminationStartTime = eventStart + (TableAvailabilityMatrix.TIME_SLOT_SIZE * Math.floorDiv(maxDuration, TableAvailabilityMatrix.TIME_SLOT_SIZE_INT));
            // start single elimination rounds 30 minutes later but only if there were at least 8 groups
            singleEliminationStartTime += (roundRobinMatchCards.size() >= 8) ? TableAvailabilityMatrix.TIME_SLOT_SIZE : 0;
            log.info("singleEliminationStartTime = " + singleEliminationStartTime);
        }

        // schedule the single elimination round matches on the same tables where the round robin round matches were played
        int currentTableNum = eventFirstTableNum;
        List<MatchCard> singleEliminationMatchCards = matchCardService.findAllForEventAndDrawType(event.getId(), DrawType.SINGLE_ELIMINATION);
        double currentRoundStartTime = singleEliminationStartTime;
        double previousRoundStartTime = currentRoundStartTime;
        int previousRoundDuration = TableAvailabilityMatrix.TIME_SLOT_SIZE_INT;
        int currentRound = event.getMaxEntries();
        boolean firstMatch = true;
        for (MatchCard matchCard : singleEliminationMatchCards) {
            int duration = calculateAllMatchesDuration(matchCard.getNumberOfGames(), 1, event.getPointsPerGame());
            log.info("groupNum = " + matchCard.getGroupNum() + " current assigned tables " + matchCard.getAssignedTables() + " startTime " + matchCard.getStartTime());
            log.info("match duration = " + duration);
            // get the first match round and duration
            if (firstMatch) {
                firstMatch = false;
                currentRound = matchCard.getRound();
                previousRoundDuration = duration;
                log.info("round = " + currentRound + ", currentRoundStartTime = " + currentRoundStartTime + ", currentTableNum = " + currentTableNum);
            }

            // all matches in the same round should have the same starting time and duration
            if (matchCard.getRound() != currentRound) {
                currentRound = matchCard.getRound();
                double numTimeSlots = Math.ceil((double) previousRoundDuration / (double) TableAvailabilityMatrix.TIME_SLOT_SIZE_INT);
                log.info("numTimeSlots = " + numTimeSlots);
                currentRoundStartTime = previousRoundStartTime + ((int) numTimeSlots * TableAvailabilityMatrix.TIME_SLOT_SIZE);
                previousRoundDuration = duration;
                // try finding a table starting with the first one that this event is played on
                currentTableNum = eventFirstTableNum;
                log.info("round = " + currentRound + ", currentRoundStartTime = " + currentRoundStartTime + ", currentTableNum = " + currentTableNum);
            }

            // find table with one following the last one
            TableAvailabilityMatrix.AvailableTableInfo availableTable = matrix.findAvailableTable(currentRoundStartTime, duration, currentTableNum, false);
            if (availableTable != null) {
                matrix.markTableAsUnavailable(availableTable.tableNum, availableTable.startTime, duration);
                currentTableNum = availableTable.tableNum + 1;
                // if we fail because we reached the last available table, start from first table for this event again
                currentTableNum = (currentTableNum <= totalAvailableTables) ? currentTableNum : eventFirstTableNum;

                // save the latest start number so next round starts after that match
                previousRoundStartTime = Math.max(previousRoundStartTime, availableTable.startTime);
                log.info("previousRoundStartTime = " + previousRoundStartTime);

                String assignedTables = Integer.toString(availableTable.tableNum);
                log.info("Assigning startTime = " + availableTable.startTime + ", on table # " + assignedTables + " with duration " + duration);
                matchCard.setAssignedTables(assignedTables);
                matchCard.setStartTime(availableTable.startTime);
                matchCard.setDuration(duration);
            } else {
                matchCard.setAssignedTables(null);
                log.info("Unable to find table for matchCard = " + matchCard.getGroupNum() + " assigned tables " + matchCard.getAssignedTables() + " startTime " + matchCard.getStartTime());
            }
        }
        matchCardService.saveAllAndFlush(singleEliminationMatchCards);
        log.info ("Saved single elimination match cards");
    }

    /**
     * Updates existing match card start time and table numbers only
     *
     * @param matchCards
     */
    public void updateMatches(List<MatchCard> matchCards) {
        for (MatchCard matchCard : matchCards) {
            MatchCard existingMatchCard = matchCardService.getMatchCard(matchCard.getId());
            existingMatchCard.setDay(matchCard.getDay());
            existingMatchCard.setStartTime(matchCard.getStartTime());
            existingMatchCard.setAssignedTables(matchCard.getAssignedTables());
            matchCardService.save(existingMatchCard);
        }
    }

    /**
     * Gets matches to be played on a given day at the specified tournament
     *
     * @param tournamentId
     * @param day
     * @param tableNumber
     * @return
     */
    public List<MatchCard> getScheduleForTable(long tournamentId, int day, int tableNumber) {
        // get all match cards for this tournament and day which use a tableNumber
        List<MatchCard> allForTournamentAndDay = matchCardService.findAllForTournamentAndDayAndAssignedTable(tournamentId, day, tableNumber);

        // since assigned table numbers are stored as a string e.g. '20,21' request for table '2' would also match
        // because we are using CONTAINS query clause
        // filter them by matching assigned table exactly
        List<MatchCard> filteredMatchCards = new ArrayList<>(allForTournamentAndDay.size());
        for (MatchCard matchCard : allForTournamentAndDay) {
            String assignedTables = matchCard.getAssignedTables();
            String[] tableNumbers = assignedTables.split(",");
            for (int i = 0; i < tableNumbers.length; i++) {
                try {
                    int assignedTableNumber = Integer.parseInt(tableNumbers[i]);
                    if (assignedTableNumber == tableNumber) {
                        filteredMatchCards.add(matchCard);
                        break;
                    }
                } catch (NumberFormatException e) {

                }
            }
        }

        // retrieve player names
        for (MatchCard matchChard : filteredMatchCards) {
            Map<String, String> profileIdToNameMap = this.matchCardService.buildProfileIdToNameMap(matchChard.getMatches());
            matchChard.setProfileIdToNameMap(profileIdToNameMap);
        }

        return filteredMatchCards;
    }
}
