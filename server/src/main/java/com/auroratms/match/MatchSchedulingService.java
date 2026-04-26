package com.auroratms.match;

import com.auroratms.draw.DrawType;
import com.auroratms.event.*;
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
import java.util.stream.Stream;

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
     * Clears match schedule for all events in a day
     * @param tournamentId
     * @param day
     * @return
     */
    public List<MatchCard> clearScheduleForDay(long tournamentId, int day) {
        List<TournamentEvent> daysEvents = this.tournamentEventEntityService.listDaysEvents(tournamentId, day);
        for (TournamentEvent event : daysEvents) {
            List<MatchCard> eventMatchCards = matchCardService.findAllForEvent(event.getId());
            for (MatchCard matchCard : eventMatchCards) {
                matchCard.setAssignedTables(null);
            }
            matchCardService.saveAllAndFlush(eventMatchCards);
        }

        return matchCardService.findAllForTournamentAndDay(tournamentId, day);
    }


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
        Comparator<TournamentEvent> comparator = Comparator
                .comparing(TournamentEvent::getStartTime);
        Collections.sort(daysEvents, comparator);

        int maximumRoundOrdinalNumber = findMaximumRoundOrdinalNumber(daysEvents);
        for (int ordinalNumber = 1; ordinalNumber <= maximumRoundOrdinalNumber; ordinalNumber++) {
            log.info("Scheduling round " + ordinalNumber + " matches");
            for (TournamentEvent event : daysEvents) {
                // start scheduling this event's matches from the 1st table if it is starting later
                if (event.getStartTime() != previousEventStartTime) {
                    startingTableNumber = 1;
                }
                previousEventStartTime = event.getStartTime();
                TournamentRoundsConfiguration roundsConfiguration = event.getRoundsConfiguration();
                if (roundsConfiguration != null) {
                    List<TournamentEventRound> rounds = roundsConfiguration.getRounds();
                    for (TournamentEventRound round : rounds) {
                        if (round.getOrdinalNum() != ordinalNumber) {
                            continue;
                        }
                        log.info("Scheduling round " + ordinalNumber + " matches for event " + event.getName());
                        List<TournamentEventRoundDivision> divisions = round.getDivisions();
                        for (TournamentEventRoundDivision division : divisions) {
                            boolean isSingleElimination = round.isSingleElimination();
                            DrawType drawType = (isSingleElimination) ? DrawType.SINGLE_ELIMINATION : DrawType.ROUND_ROBIN;
                            List<MatchCard> divisionMatchCards = matchCardService.findAllForEventAndDrawTypeAndRoundAndDivision(
                                    event.getId(), drawType, round.getOrdinalNum(), division.getDivisionIdx());
                            if (!divisionMatchCards.isEmpty()) {
                                if (isSingleElimination) {
                                    scheduleSingleEliminationMatches(totalAvailableTables, event, round, division, matrix);
                                } else {
                                    startingTableNumber = scheduleRoundRobinMatches(startingTableNumber, totalAvailableTables, event, round, division, matrix);
                                }
                            }
                        }
                    }
                } else {
                    log.warn("No rounds configuration found for event " + event.getName());
                }
            }
        }

        log.info("Finished scheduling second round matches");
        // get them
        return this.matchCardService.findAllForTournamentAndDay(tournamentId, day);
    }

    /**
     * Finds the maxium round number in the specified list of events
     * @param events
     * @return
     */
    private int findMaximumRoundOrdinalNumber(List<TournamentEvent> events) {
        int maxOrdinalNumber = 0;
        for (TournamentEvent event : events) {
            TournamentRoundsConfiguration roundsConfiguration = event.getRoundsConfiguration();
            if (roundsConfiguration == null) {
                log.error("No rounds configuration found for event " + event.getName());
                continue;
            }
            int ordinalNumber = roundsConfiguration.getRounds().stream()
                    .map(TournamentEventRound::getOrdinalNum)
                    .max(Integer::compareTo)
                    .orElse(0);
            maxOrdinalNumber = Math.max(maxOrdinalNumber, ordinalNumber);
        }
        return maxOrdinalNumber;
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

        // get all events played on this day at once
        List<TournamentEvent> dayTournamentEvents = this.tournamentEventEntityService.listDaysEvents(tournamentId, day);
        // make a map of id to name for easy lookup
        Map<Long, String> eventIdToNameMap = new HashMap<>();
        dayTournamentEvents.forEach(event -> {eventIdToNameMap.put(event.getId(), event.getName());});
        // fill the table availability matrix with all other match cards for that day
        // so that we know which tables are available.
        List<MatchCard> allTodaysMatchCards = this.matchCardService.findAllForTournamentAndDay(tournamentId, day);
        Set<Long> eventIdsToFix = new HashSet<>();
        List<MatchCard> matchCardsToFix = new ArrayList<>();
        for (MatchCard matchCard : allTodaysMatchCards) {
            if (!matchCardIds.contains(matchCard.getId())) {
                String strAssignedTables = matchCard.getAssignedTables();
                String eventName = eventIdToNameMap.get(matchCard.getEventFk());
                log.info("Marking assigned tables " + strAssignedTables + " for event '" + eventName + "' as unavailable");
                String[] assignedTables = strAssignedTables.split(",");
                int duration = matchCard.getDuration();
                for (int i = 0; i < assignedTables.length; i++) {
                    Integer assignedTable = Integer.parseInt(assignedTables[i]);
                    matrix.markTableAsUnavailable(assignedTable, matchCard.getStartTime(), duration);
                }
            } else {
                eventIdsToFix.add(matchCard.getEventFk());
                matchCardsToFix.add(matchCard);
            }
        }

        // get list of event definitions that the match cards are for
        List<TournamentEvent> eventsToFix = dayTournamentEvents.stream()
                .filter(event -> {return eventIdsToFix.contains(event.getId());})
                .toList();
        for (TournamentEvent tournamentEvent : eventsToFix) {
            log.info("Need to fix match cards for event " + tournamentEvent.getName() + " eventId: " + tournamentEvent.getId());
        }

        log.info("State of table occupation before assignment");
        matrix.prettyPrint();

        // schedule all match cards for these events - both RR and SE type
        int startingTableNumber = 1;
        double previousEventStartTime = 8.0d;
        int maximumRoundOrdinalNumber = findMaximumRoundOrdinalNumber(eventsToFix);
        for (int ordinalNumber = 1; ordinalNumber <= maximumRoundOrdinalNumber; ordinalNumber++) {
            for (TournamentEvent event : eventsToFix) {
                // start scheduling this event's matches from the 1st table if it is starting later
                if (event.getStartTime() != previousEventStartTime) {
                    startingTableNumber = 1;
                }
                previousEventStartTime = event.getStartTime();

                TournamentRoundsConfiguration roundsConfiguration = event.getRoundsConfiguration();
                if (roundsConfiguration != null) {
                    List<TournamentEventRound> rounds = roundsConfiguration.getRounds();
                    for (TournamentEventRound round : rounds) {
                        if (round.getOrdinalNum() != ordinalNumber) {
                            continue;
                        }

                        log.info("Scheduling match cards for event " + event.getName() + " roundOrdinalNumber: " + round.getOrdinalNum());
                        List<TournamentEventRoundDivision> divisions = round.getDivisions();
                        for (TournamentEventRoundDivision division : divisions) {
                            long matchCardCount = matchCardsToFix.stream()
                                    .filter(matchCard -> matchCard.getEventFk() == event.getId())
                                    .filter(matchCard -> matchCard.getRoundOrdinalNumber() == round.getOrdinalNum())
                                    .filter(matchCard -> matchCard.getDivisionIdx() == division.getDivisionIdx())
                                    .count();
                            log.info("Found " + matchCardCount + " in round " + round.getRoundName() + " division " + division.getDivisionName() + " in event " + event.getName());
                            if (matchCardCount > 0) {
                                if (round.isSingleElimination()) {
                                    scheduleSingleEliminationMatches(totalAvailableTables, event, round, division, matrix);
                                } else {
                                    startingTableNumber = scheduleRoundRobinMatches(startingTableNumber, totalAvailableTables, event, round, division, matrix);
                                }
                            }
                        }
                    }
                }
            }
        }


        // get them
        return this.matchCardService.findAllForTournamentAndDay(tournamentId, day);
    }

    /**
     * Schedules all match cards for specified event and round, taking into account available tables stored in matrix
     *
     * @param startingTableNumber  table number to start scheduling from
     * @param totalAvailableTables total available tables
     * @param event                event definition
     * @param round                 event round
     * @param division              round division
     * @param matrix               matrix of table availability
     * @return next available table number
     */
    private int scheduleRoundRobinMatches(int startingTableNumber,
                                          int totalAvailableTables,
                                          TournamentEvent event,
                                          TournamentEventRound round,
                                          TournamentEventRoundDivision division,
                                          TableAvailabilityMatrix matrix) throws MatchSchedulingException {
        // calculate number of required tables to play this event if it were completely full
        TournamentEventConfigAdapter adapter = new TournamentEventConfigAdapter(event, round.getOrdinalNum(), division.getDivisionIdx());
        int numTablesPerGroup = adapter.getNumTablesPerGroup();
        int maxEntries = event.getMaxEntries();
        maxEntries = (maxEntries == 0) ? 16 : maxEntries;
        maxEntries = (event.isDoubles()) ? maxEntries / 2 : maxEntries;
        int playersPerGroup = adapter.getPlayersPerGroup();
        int maxNumGroups = maxEntries / playersPerGroup;
        int maxTablesNeeded = maxNumGroups * numTablesPerGroup;
        int playersToAdvance = adapter.getPlayersToAdvance();
        int numberOfGames = adapter.getNumberOfGames();
        int pointsPerGame = adapter.getPointsPerGame();
        double roundStartTime = adapter.getStartTime();
        log.info("-----------------------------------------------------------------------");
        log.info("Scheduling round robin matches for " + event.getName() + " round: " + round.getRoundName() + " division: " + division.getDivisionName() + " startTime: " + roundStartTime);
        log.info("-----------------------------------------------------------------------");
        log.info("numTablesPerGroup: " + numTablesPerGroup + " playersPerGroup: " + playersPerGroup + " maxNumGroups: " + maxNumGroups + " maxTablesNeeded: " + maxTablesNeeded);

        // assign tables to each match card
        int currentTableNum = startingTableNumber;
        List<MatchCard> divisionMatchCards = matchCardService.findAllForEventAndDrawTypeAndRoundAndDivision(
                event.getId(), DrawType.ROUND_ROBIN, round.getOrdinalNum(), division.getDivisionIdx());
        // if we need more tables to play all matches then are available then, push some matches to start later
        // this happens in Giant Round Robin events that schedule play in two parts (morning and afternoon)
        boolean mustStartAtStartTime = ((maxNumGroups * numTablesPerGroup) <= totalAvailableTables);
        for (MatchCard matchCard : divisionMatchCards) {
            log.info("Looking for table for group " + matchCard.getGroupNum());
            matchCard.setAssignedTables(null);
            // can't assign more tables than we have
            if (currentTableNum <= totalAvailableTables) {
                // calculate duration
                int numMatchesToPlay = matchCard.getMatches().size();
                int allMatchesDuration = calculateAllMatchesDuration(numberOfGames, numMatchesToPlay, pointsPerGame);
                int tablesToAssign = getNumTablesToAssign(numTablesPerGroup, numMatchesToPlay, playersPerGroup, playersToAdvance);
                log.info ("Found numMatchesToPlay: " + numMatchesToPlay + " allMatchesDuration: " + allMatchesDuration + " tablesToAssign: " + tablesToAssign);
                // assign tables and mark them as used - one at a time
                String assignedTables = "";
                int durationOnOneTable = Math.floorDiv(allMatchesDuration, tablesToAssign);
                // round up to the nearest 30 minutes, so it is nicely fitting into the matrix which is also 30 minutes based
                if (durationOnOneTable % TableAvailabilityMatrix.TIME_SLOT_SIZE_INT > 0) {
                    int numSlotsNeeded = (durationOnOneTable / TableAvailabilityMatrix.TIME_SLOT_SIZE_INT) + 1;
                    durationOnOneTable = numSlotsNeeded * TableAvailabilityMatrix.TIME_SLOT_SIZE_INT;
                }

                double assignedStartTime = roundStartTime;
                for (int i = 0; i < tablesToAssign; i++) {
                    TableAvailabilityMatrix.AvailableTableInfo availableTable = matrix.findAvailableTable(roundStartTime, durationOnOneTable, currentTableNum, mustStartAtStartTime);
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
                                + event.getName() + "' event " + round.getRoundName() + " RR round " + division.getDivisionName() + " which starts at " + roundStartTime +
                                " due to lack of available table time.  You can assign a later starting time to this event or configure more tables to the prior event so it completes sooner.";
                        log.warn(message);
                        matrix.prettyPrint();
                        throw new MatchSchedulingException(message);
                    }
                }
                log.info("Assigned tables = " + assignedTables + " assignedStartTime = " + assignedStartTime + " for group " + matchCard.getGroupNum());
                log.info("--------------------------------------");

                matchCard.setAssignedTables(assignedTables);
                matchCard.setStartTime(assignedStartTime);
                matchCard.setDuration(durationOnOneTable);
            }

            matchCard.setDay(event.getDay());
        }
        log.info ("Saving match cards");
        matchCardService.saveAllAndFlush(divisionMatchCards);
        log.info ("Saved match cards");

        // even when all tables are not used 'reserve' them by skipping unused tables
        return startingTableNumber; // + maxTablesNeeded;
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
     * @param round
     * @param division
     * @param matrix
     */
    private void scheduleSingleEliminationMatches(int totalAvailableTables,
                                                  TournamentEvent event,
                                                  TournamentEventRound round,
                                                  TournamentEventRoundDivision division,
                                                  TableAvailabilityMatrix matrix) {
        // schedule the single elimination round matches on the same tables where the round robin round matches were played
        int maxDuration = 0;
        TournamentEventConfigAdapter adapter = new TournamentEventConfigAdapter(event, round.getOrdinalNum(), division.getDivisionIdx());
        double eventStart = adapter.getStartTime();
        // range of tables the matches are played on in this event.
        log.info("-----------------------------------------------------------------------");
        log.info("Scheduling matches for '" + event.getName() + "' round: '" + round.getRoundName() + "' division: '" + division.getDivisionName() + "' startTime: " + eventStart);
        log.info("-----------------------------------------------------------------------");
        log.info("Finding on which tables the RR round matches were played to keep them similar and when they end");
        // find time when the prior round matches end
        int previousDivisionIdx = division.getPreviousDivisionIdx();
        int previousRoundOrdinalNumber = round.getOrdinalNum() - 1;
        DrawType previousRoundDrawType = getPreviousRoundDrawType(event, previousRoundOrdinalNumber);
        int eventFirstTableNum = (!round.isSingleElimination()) ? totalAvailableTables : 1;
        int eventLastTableNum = 1;
        List<MatchCard> previousRoundMatchCards = matchCardService.findAllForEventAndDrawTypeAndRoundAndDivision(event.getId(), previousRoundDrawType, previousRoundOrdinalNumber, previousDivisionIdx);
        for (MatchCard matchCard : previousRoundMatchCards) {
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
        log.info("Prior round firstTableNum = " + eventFirstTableNum + " lastTableNum = " + eventLastTableNum);
        log.info("maxDuration = " + maxDuration);

        // When the round robin round is finished
        double previousRoundEndTime = eventStart +
                (TableAvailabilityMatrix.TIME_SLOT_SIZE * Math.floorDiv(maxDuration, TableAvailabilityMatrix.TIME_SLOT_SIZE_INT));
        // start single elimination rounds 30 minutes later but only if there were at least 8 groups
        previousRoundEndTime += (previousRoundMatchCards.size() >= 8) ? TableAvailabilityMatrix.TIME_SLOT_SIZE : 0;
        log.info("previousRoundEndTime = " + previousRoundEndTime);

        // schedule this round matches on the same tables where the previous round matches were played
        int currentTableNum = eventFirstTableNum;
        DrawType currentRoundDrawType = (round.isSingleElimination()) ? DrawType.SINGLE_ELIMINATION : DrawType.ROUND_ROBIN;
        List<MatchCard> currentRoundMatchCards = matchCardService.findAllForEventAndDrawTypeAndRoundAndDivision(event.getId(),
                currentRoundDrawType, round.getOrdinalNum(), division.getDivisionIdx());
        double currentRoundStartTime = previousRoundEndTime;
        double previousRoundStartTime = currentRoundStartTime;
        int previousRoundDuration = TableAvailabilityMatrix.TIME_SLOT_SIZE_INT;
        int currentRound = event.getMaxEntries();
        boolean firstMatch = true;
        for (MatchCard matchCard : currentRoundMatchCards) {
            int duration = calculateAllMatchesDuration(matchCard.getNumberOfGames(), 1, adapter.getPointsPerGame());
            log.info("----------------------------------------------------");
            log.info("groupNum = " + matchCard.getGroupNum() + " current assigned tables " + matchCard.getAssignedTables() + " startTime " + matchCard.getStartTime());
            log.info("match duration = " + duration);
            // get the first match round and duration
            if (firstMatch) {
                firstMatch = false;
                currentRound = matchCard.getRound();
                previousRoundDuration = duration;
                log.info("+++ round = " + currentRound + ", currentRoundStartTime = " + currentRoundStartTime + ", currentTableNum = " + currentTableNum);
            }

            // all matches in the same round of (8, 4, or 2) should have the same starting time and duration
            if (matchCard.getRound() != currentRound) {
                currentRound = matchCard.getRound();
                double numTimeSlots = Math.ceil((double) previousRoundDuration / (double) TableAvailabilityMatrix.TIME_SLOT_SIZE_INT);
                log.info("numTimeSlots = " + numTimeSlots);
                currentRoundStartTime = previousRoundStartTime + ((int) numTimeSlots * TableAvailabilityMatrix.TIME_SLOT_SIZE);
                previousRoundDuration = duration;
                // try finding a table starting with the first one that this event is played on
                currentTableNum = eventFirstTableNum;
                log.info("+++ round = " + currentRound + ", currentRoundStartTime = " + currentRoundStartTime + ", currentTableNum = " + currentTableNum);
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
        matchCardService.saveAllAndFlush(currentRoundMatchCards);
        log.info ("Saved single elimination match cards");
    }

    /**
     * Gets previous round draw type
     * @param event
     * @param previousRoundOrdinalNumber
     * @return
     */
    private DrawType getPreviousRoundDrawType(TournamentEvent event, int previousRoundOrdinalNumber) {
        DrawType previousRoundDrawType = DrawType.ROUND_ROBIN;
        TournamentRoundsConfiguration roundsConfiguration = event.getRoundsConfiguration();
        if (roundsConfiguration != null) {
            previousRoundDrawType = roundsConfiguration.getRounds().stream()
                    .filter(tournamentEventRound -> {
                        return tournamentEventRound.getOrdinalNum() == previousRoundOrdinalNumber;
                    })
                    .map(tournamentEventRound -> tournamentEventRound.isSingleElimination() ? DrawType.SINGLE_ELIMINATION : DrawType.ROUND_ROBIN)
                    .findFirst()
                    .orElse(DrawType.ROUND_ROBIN);
        }
        return previousRoundDrawType;
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
