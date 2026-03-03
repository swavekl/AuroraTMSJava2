package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.generation.singleelim.SingleEliminationEntriesConverter;
import com.auroratms.event.*;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.doubles.DoublesPair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DoublesSingleEliminationDrawsGeneratorTest extends AbstractDoublesDrawsGeneratorTest {

    @Test
    public void testDraws () {
        testDrawGeneration(0, 4);
    }

    private void testDrawGeneration(int numTeamsToSeed, int expectedGroups) {
        TournamentEvent tournamentEvent = makeTournamentEventEntity(numTeamsToSeed, true);

        List<TournamentEventEntry> eventEntries = makeDoublesTournamentEntriesList(153L, tournamentEvent.getId());

        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = makeDoublesPlayerDrawInfos();

        List<DoublesPair> doublesPairList = makeDoublesPairs(tournamentEvent);

        // first event draw
        List<DrawItem> existingDrawItems = new ArrayList<>();

        TournamentRoundsConfiguration roundsConfiguration = tournamentEvent.getRoundsConfiguration();
        List<TournamentEventRound> rounds = roundsConfiguration.getRounds();
        TournamentEventRound rrRound = rounds.get(0);
        TournamentEventRoundDivision rrDivision = rrRound.getDivisions().get(0);

        IDrawsGenerator rrRoundGenerator = DrawGeneratorFactory.makeGenerator(tournamentEvent, rrRound, rrDivision);
        ((DoublesSnakeDrawsGenerator)rrRoundGenerator).setDoublesPairs(doublesPairList);

        List<DrawItem> rrDrawItems = rrRoundGenerator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
        assertEquals(14, rrDrawItems.size(), "wrong number of draws");

        // now test the single elimination draws generation
        // pick top n teams to advance and get their entries
        List<TournamentEventEntry> seEventEntries = SingleEliminationEntriesConverter.generateSEEventEntriesFromDraws(
                rrDrawItems, eventEntries, tournamentEvent, entryIdToPlayerDrawInfo);
        assertEquals(8, seEventEntries.size(), "wrong number of advancing player entries");

        TournamentEventRound seRound = rounds.get(1);
        TournamentEventRoundDivision seDivision = seRound.getDivisions().get(0);

        // make SE draws
        IDrawsGenerator seRoundGenerator = DrawGeneratorFactory.makeGenerator(tournamentEvent, seRound, seDivision);
        assertNotNull(seRoundGenerator, "SE rrRound generator is null");
        ((DoublesSingleEliminationDrawsGenerator)seRoundGenerator).setDoublesPairs(doublesPairList);

        List<DrawItem> seDrawItems = seRoundGenerator.generateDraws(seEventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
        assertEquals(4, seDrawItems.size(), "wrong number of SE entries");
    }
}
