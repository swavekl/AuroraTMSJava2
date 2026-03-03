package com.auroratms.event;

import java.util.List;

/**
 * A class which will hide the fact that we are migrating some match configuration
 * from the tournament event to the round and division
 */
public class TournamentEventConfigAdapter {
    private TournamentEvent tournamentEvent;
    private TournamentEventRound eventRound;
    private TournamentEventRoundDivision roundDivision;

    /**
     * Constructor
     * @param tournamentEvent the tournament event containing the round configurations
     * @param roundOrdinalNumber the ordinal number of the round to use
     * @param divisionIdx the index of the division to use
     */
    public TournamentEventConfigAdapter(TournamentEvent tournamentEvent, int roundOrdinalNumber, int divisionIdx) {
        this.tournamentEvent = tournamentEvent;
        this.eventRound = findEventRound(tournamentEvent, roundOrdinalNumber);
        this.roundDivision = findRoundDivision(eventRound, divisionIdx);
    }

    /**
     * Finds and returns the tournament event round for the specified round ordinal number
     * within the provided tournament event.
     *
     * @param tournamentEvent the tournament event containing the round configurations
     * @param roundOrdinalNumber the ordinal number of the round to find
     * @return the tournament event round matching the given ordinal number, or null if no match is found
     */
    private TournamentEventRound findEventRound(TournamentEvent tournamentEvent, int roundOrdinalNumber) {
        TournamentRoundsConfiguration roundsConfiguration = tournamentEvent.getRoundsConfiguration();
        if (roundsConfiguration != null) {
            List<TournamentEventRound> rounds = roundsConfiguration.getRounds();
            return rounds.stream()
                    .filter(round -> round.getOrdinalNum() == roundOrdinalNumber)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Retrieves a specific TournamentthisDivision from the given TournamentEventRound
     * that matches the provided division index.
     *
     * @param tournamentEventRound the TournamentEventRound containing the divisions to search
     * @param divisionIdx the index of the division to find
     * @return the TournamentEventRoundDivision that matches the given division index,
     *         or null if no such division exists
     */
    private TournamentEventRoundDivision findRoundDivision(TournamentEventRound tournamentEventRound, int divisionIdx) {
        if (tournamentEventRound == null) {
            return null;
        }
        return tournamentEventRound.getDivisions().stream()
                .filter(division -> division.getDivisionIdx() == divisionIdx)
                .findFirst()
                .orElse(null);
    }

    public TournamentEventRound getEventRound() {
        return eventRound;
    }

    public TournamentEventRoundDivision getRoundDivision() {
        return roundDivision;
    }

    /**
     * Gets the number of points to play per game
     *
     * @return
     */
    public int getPointsPerGame() {
        if (roundDivision != null) {
            return roundDivision.getPointsPerGame();
        } else {
            return tournamentEvent.getPointsPerGame();
        }
    }

    /**
     * Gets a number of games to play in a given round robin round as configured
     *
     * @return
     */
    public int getNumberOfGames() {
        if (roundDivision != null) {
            return roundDivision.getNumberOfGames();
        } else {
            return tournamentEvent.getNumberOfGames();
        }
    }

    /**
     * Gets a number of games to play in a given single elimination round as configured
     *
     * @param roundOf            round of 64, 32 etc.
     * @return
     */
    public int getSENumberOfGames(int roundOf) {
        int numGames = 5;
        numGames = switch (roundOf) {
            // finals & 3rd/4th place
            case 2 ->
                    roundDivision == null ? tournamentEvent.getNumberOfGamesSEFinals() : roundDivision.getNumberOfGamesSEFinals();
            case 4 ->
                    roundDivision == null ? tournamentEvent.getNumberOfGamesSESemiFinals() : roundDivision.getNumberOfGamesSESemiFinals();
            case 8 ->
                    roundDivision == null ? tournamentEvent.getNumberOfGamesSEQuarterFinals() : roundDivision.getNumberOfGamesSEQuarterFinals();
            default ->
                    roundDivision == null ? tournamentEvent.getNumberOfGamesSEPlayoffs() : roundDivision.getNumberOfGamesSEPlayoffs();
        };

        // for uninitialized
        if (numGames == 0) {
            numGames = 5;
        }
        return numGames;
    }

    /**
     * Gets starting time.  If configured to be later in the round then use the round numbers
     * This is for events that span multiple days.
     *
     * @return
     */
    public double getStartTime() {
        // if they pushed the round day and start time further, use these parameters
        double startTime = tournamentEvent.getStartTime();
        if (eventRound != null) {
            startTime = Math.max(eventRound.getStartTime(), startTime);
        }
        return startTime;
    }

    /**
     * Gets day.  If configured to be later in the round then use the round numbers
     *
     * @return
     */
    public int getDay() {
        // if they pushed the round day and start time further, use these parameters
        int day = tournamentEvent.getDay();
        if (eventRound != null) {
            day = Math.max(eventRound.getDay(), day);
        }
        return day;
    }

    /**
     * Gets if there should be a match to decide the 3rd and 4th place
     *
     * @return
     */
    public boolean isPlay3rd4thPlace() {
        if (roundDivision != null) {
            return roundDivision.isPlay3rd4thPlace();
        } else {
            return tournamentEvent.isPlay3rd4thPlace();
        }
    }

    /**
     * Gets number of players to advance - usually 1 or 2, but could be all
     *
     * @return
     */
    public int getPlayersToAdvance() {
        if (roundDivision != null) {
            return roundDivision.getPlayersToAdvance();
        } else {
            return tournamentEvent.getPlayersToAdvance();
        }
    }
}
