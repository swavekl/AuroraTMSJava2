package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.draw.generation.singleelim.SingleEliminationEntriesConverter;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.doubles.DoublesPair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DoublesSingleEliminationDrawsGeneratorTest extends AbstractDoublesDrawsGeneratorTest {

    @Test
    public void testDraws () {
        testDrawGeneration(0, 4);
    }

    private void testDrawGeneration(int numTeamsToSeed, int expectedGroups) {
        TournamentEventEntity tournamentEventEntity = makeTournamentEventEntity(numTeamsToSeed);

        List<TournamentEventEntry> eventEntries = makeDoublesTournamentEntriesList(153L, tournamentEventEntity.getId());

        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = makeDoublesPlayerDrawInfos();

        List<DoublesPair> doublesPairList = makeDoublesPairs(tournamentEventEntity);

        // first event draw
        List<DrawItem> existingDrawItems = new ArrayList<>();

        IDrawsGenerator rrRoundGenerator = DrawGeneratorFactory.makeGenerator(tournamentEventEntity, DrawType.ROUND_ROBIN);
        ((DoublesSnakeDrawsGenerator)rrRoundGenerator).setDoublesPairs(doublesPairList);

        List<DrawItem> rrDrawItems = rrRoundGenerator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
        assertEquals("wrong number of draws", 14, rrDrawItems.size());

        // now test the single elimination draws generation
        // pick top n teams to advance and get their entries
        List<TournamentEventEntry> seEventEntries = SingleEliminationEntriesConverter.generateSEEventEntriesFromDraws(
                rrDrawItems, eventEntries, tournamentEventEntity, entryIdToPlayerDrawInfo);
        assertEquals("wrong number of advancing player entries", 8, seEventEntries.size());

        // make SE draws
        IDrawsGenerator seRoundGenerator = DrawGeneratorFactory.makeGenerator(tournamentEventEntity, DrawType.SINGLE_ELIMINATION);
        assertNotNull("SE round generator is null", seRoundGenerator);
        ((DoublesSingleEliminationDrawsGenerator)seRoundGenerator).setDoublesPairs(doublesPairList);

        List<DrawItem> seDrawItems = seRoundGenerator.generateDraws(seEventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
        assertEquals("wrong number of SE entries", 4, seDrawItems.size());
    }
}
