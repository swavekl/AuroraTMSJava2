package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.event.DrawMethod;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DivisionDrawsGeneratorTest extends AbstractDrawsGeneratorTest {

    @Test
    public void testDivisionDraw() {
        TournamentEventEntity tournamentEventEntity = new TournamentEventEntity();
        tournamentEventEntity.setId(55L);
        tournamentEventEntity.setPlayersPerGroup(8);
        tournamentEventEntity.setPlayersToSeed(0);
        tournamentEventEntity.setPlayersToAdvance(0);
        tournamentEventEntity.setDrawMethod(DrawMethod.DIVISION); // division

        List<TournamentEventEntry> eventEntries = this.makeTournamentEntriesList();

        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = this.makePlayerDrawInfos();

        // usually there is only one 1 event in giant round robin tournament
        List<DrawItem> existingDraws = Collections.emptyList();
        IDrawsGenerator generator = DrawGeneratorFactory.makeGenerator(tournamentEventEntity, DrawType.ROUND_ROBIN);
        List<DrawItem> drawItemsList = generator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDraws);
        assertEquals ("wrong size of drawItemsList", eventEntries.size(), drawItemsList.size());

        int [] expectedGroupCounts = {8, 8, 7};
        int [] actualGroupCounts = {0, 0, 0};
        for (DrawItem drawItem : drawItemsList) {
            int groupNum = drawItem.getGroupNum();
            actualGroupCounts[groupNum - 1] = actualGroupCounts[groupNum - 1] + 1;
        }
        assertArrayEquals("wrong counts of players in groups", expectedGroupCounts, actualGroupCounts);
    }
}
