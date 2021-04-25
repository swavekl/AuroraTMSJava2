package com.auroratms.draw.generation;

import com.auroratms.draw.Draw;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Draws generator for single elimination type of event or later round of round robin event
 */
public class SingleEliminationDrawsGenerator implements IDrawsGenerator {
    private TournamentEventEntity tournamentEventEntity;

    public SingleEliminationDrawsGenerator(TournamentEventEntity tournamentEventEntity) {
        this.tournamentEventEntity = tournamentEventEntity;
    }

    @Override
    public List<Draw> generateDraws(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo, List<Draw> existingDraws) {
        return new ArrayList<>();
    }
}
