package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.event.DrawMethod;
import com.auroratms.event.TournamentEvent;
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
        TournamentEvent tournamentEvent = new TournamentEvent();
        tournamentEvent.setId(55L);
        tournamentEvent.setPlayersPerGroup(4);
        tournamentEvent.setPlayersToSeed(numPlayersToSeed);
        tournamentEvent.setPlayersToAdvance(1);
        tournamentEvent.setDrawMethod(DrawMethod.SNAKE);

        List<TournamentEventEntry> eventEntries = makeTournamentEntriesList();

        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = makePlayerDrawInfos ();

        // first event draw
        List<DrawItem> existingDrawItems = new ArrayList<>();

        SnakeDrawsGenerator generator = new SnakeDrawsGenerator(tournamentEvent);
        List<DrawItem> drawItems = generator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
        assertEquals("wrong number of draws", 23, drawItems.size());

        Set<Integer> uniqueGroups = new HashSet<>();
        for (DrawItem drawItem : drawItems) {
            uniqueGroups.add(drawItem.getGroupNum());
        }
        assertEquals("wrong number of groups", expectedGroups, uniqueGroups.size());
    }


}
