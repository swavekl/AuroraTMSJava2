package com.auroratms.draw.generation;

import com.auroratms.draw.DrawType;
import com.auroratms.draw.generation.teams.TeamsDivisionDrawsGenerator;
import com.auroratms.event.*;

/**
 * Makes the generator for event and round
 */
public class DrawGeneratorFactory {

    /**
     * Creates an instance of {@link IDrawsGenerator} based on the provided tournament event, draw type,
     * and draw method. It determines the appropriate draw generator implementation to use.
     *
     * @param tournamentEvent the tournament event for which the draws are to be generated
     * @return an instance of {@link IDrawsGenerator} appropriate for the specified configuration,
     * or null if no generator is applicable
     */
    public static IDrawsGenerator makeGenerator(TournamentEvent tournamentEvent,
                                                TournamentEventRound round,
                                                TournamentEventRoundDivision division) {
        IDrawsGenerator generator = null;
        DrawMethod drawMethod = division.getDrawMethod();
        switch (drawMethod) {
            case SNAKE:
                if (tournamentEvent.getEventEntryType() == EventEntryType.TEAM) {

                } else {
                    if (!tournamentEvent.isDoubles()) {
                        generator = new SnakeDrawsGenerator(tournamentEvent, round, division);
                    } else {
                        generator = new DoublesSnakeDrawsGenerator(tournamentEvent, round, division);
                    }
                }
                break;
            case DIVISION:
                if (tournamentEvent.getEventEntryType() == EventEntryType.TEAM) {
                    generator = new TeamsDivisionDrawsGenerator(tournamentEvent, round, division, null);
                } else {
                    generator = new DivisionDrawsGenerator(tournamentEvent, round, division);
                }
                break;

            case SINGLE_ELIMINATION:
                if (tournamentEvent.getEventEntryType() == EventEntryType.TEAM) {
                    // todo -
                } else {
                    if (!tournamentEvent.isDoubles()) {
                        generator = new SingleEliminationDrawsGenerator(tournamentEvent, round, division);
                    } else {
                        generator = new DoublesSingleEliminationDrawsGenerator(tournamentEvent, round, division);
                    }
                }
                break;

            case BY_RECORD:
            default:
                break;
        }
        return generator;
    }
}
