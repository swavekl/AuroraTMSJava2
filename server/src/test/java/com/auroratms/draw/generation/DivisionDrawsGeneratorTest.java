package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.event.DrawMethod;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventRound;
import com.auroratms.event.TournamentEventRoundDivision;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DivisionDrawsGeneratorTest extends AbstractDrawsGeneratorTest {

    @Test
    public void testDivisionDraw() {
        TournamentEvent tournamentEvent = new TournamentEvent();
        tournamentEvent.setId(55L);
        tournamentEvent.setPlayersPerGroup(8);
        tournamentEvent.setPlayersToSeed(0);
        tournamentEvent.setPlayersToAdvance(0);
        tournamentEvent.setDrawMethod(DrawMethod.DIVISION); // division

        TournamentEventRound round = new TournamentEventRound();
        round.setRoundName("Giant Round Robin");
        round.setSingleElimination(false);
        round.setDay(1);
        round.setStartTime(9.0d);
        round.setOrdinalNum(1);

        TournamentEventRoundDivision division = new TournamentEventRoundDivision();
        division.setDivisionName("Division 1");
        division.setDrawMethod(DrawMethod.DIVISION);
        division.setPlayersPerGroup(8);
        round.setDivisions(Collections.singletonList(division));

        List<TournamentEventEntry> eventEntries = this.makeTournamentEntriesList();

        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = this.makePlayerDrawInfos();

        // usually there is only one 1 event in giant round robin tournament
        List<DrawItem> existingDraws = Collections.emptyList();
        IDrawsGenerator generator = DrawGeneratorFactory.makeGenerator(tournamentEvent, round, division);
        List<DrawItem> drawItemsList = generator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDraws);
        assertEquals (eventEntries.size(), drawItemsList.size(), "wrong size of drawItemsList");

        int [] expectedGroupCounts = {8, 8, 7};
        int [] actualGroupCounts = {0, 0, 0};
        for (DrawItem drawItem : drawItemsList) {
            int groupNum = drawItem.getGroupNum();
            actualGroupCounts[groupNum - 1] = actualGroupCounts[groupNum - 1] + 1;
        }
        assertArrayEquals(expectedGroupCounts, actualGroupCounts, "wrong counts of players in groups");
    }
}
