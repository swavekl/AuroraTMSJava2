package com.auroratms.draw.generation;

import com.auroratms.draw.DrawType;
import com.auroratms.event.DrawMethod;
import com.auroratms.event.TournamentEvent;

/**
 * Makes the generator for event and round
 */
public class DrawGeneratorFactory {

    public static IDrawsGenerator makeGenerator(TournamentEvent tournamentEvent, DrawType drawType) {
        IDrawsGenerator generator = null;
        if (drawType == DrawType.ROUND_ROBIN) {
            DrawMethod drawMethod = tournamentEvent.getDrawMethod();
            switch (drawMethod) {
                case SNAKE:
                    if (!tournamentEvent.isDoubles()) {
                        generator = new SnakeDrawsGenerator(tournamentEvent);
                    } else {
                        generator = new DoublesSnakeDrawsGenerator(tournamentEvent);
                    }
                    break;
                case DIVISION:
                    generator = new DivisionDrawsGenerator(tournamentEvent);
                    break;
                case BY_RECORD:
                default:
                    break;
            }
        } else {
            // single elimination
            if (!tournamentEvent.isDoubles()) {
                generator = new SingleEliminationDrawsGenerator(tournamentEvent);
            } else {
                generator = new DoublesSingleEliminationDrawsGenerator(tournamentEvent);
            }
        }
        return generator;
    }
}
