package com.auroratms.draw.generation;

import com.auroratms.draw.DrawType;
import com.auroratms.event.DrawMethod;
import com.auroratms.event.TournamentEventEntity;

/**
 * Makes the generator for event and round
 */
public class DrawGeneratorFactory {

    public static IDrawsGenerator makeGenerator(TournamentEventEntity tournamentEventEntity, DrawType drawType) {
        IDrawsGenerator generator = null;
        if (drawType == DrawType.ROUND_ROBIN) {
            DrawMethod drawMethod = tournamentEventEntity.getDrawMethod();
            switch (drawMethod) {
                case SNAKE:
                    generator = new SnakeDrawsGenerator(tournamentEventEntity);
                    break;
                case DIVISION:
                    generator = new DivisionDrawsGenerator(tournamentEventEntity);
                    break;
                case BY_RECORD:
                default:
                    break;
            }
        } else {
            // single elimination
            generator = new SingleEliminationDrawsGenerator(tournamentEventEntity);
        }
        return generator;
    }
}
