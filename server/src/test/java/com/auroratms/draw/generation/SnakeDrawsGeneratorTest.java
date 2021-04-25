package com.auroratms.draw.generation;

import com.auroratms.draw.Draw;
import com.auroratms.event.DrawMethod;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class SnakeDrawsGeneratorTest extends AbstractDrawsGeneratorTest {

    @Test
    public void testNoSeeding () {
        testDrawGeneration(0, 6);
    }

    @Test
    public void testSeed2Players () {
        testDrawGeneration(3, 8);
    }

    private void testDrawGeneration (int numPlayersToSeed, int expectedGroups) {
        TournamentEventEntity tournamentEventEntity = new TournamentEventEntity();
        tournamentEventEntity.setId(55L);
        tournamentEventEntity.setPlayersPerGroup(4);
        tournamentEventEntity.setPlayersToSeed(numPlayersToSeed);
        tournamentEventEntity.setPlayersToAdvance(1);
        tournamentEventEntity.setDrawMethod(DrawMethod.SNAKE);

        List<TournamentEventEntry> eventEntries = makeTournamentEntriesList();

        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = makePlayerDrawInfos ();

        // first event draw
        List<Draw> existingDraws = new ArrayList<>();

        SnakeDrawsGenerator generator = new SnakeDrawsGenerator(tournamentEventEntity);
        List<Draw> draws = generator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDraws);
        assertEquals("wrong number of draws", 23, draws.size());

        Set<Integer> uniqueGroups = new HashSet<>();
        for (Draw draw : draws) {
            uniqueGroups.add(draw.getGroupNum());
        }
        assertEquals("wrong number of groups", expectedGroups, uniqueGroups.size());
    }


}
