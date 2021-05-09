package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.event.DrawMethod;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class SingleEliminationDrawsGeneratorTest extends AbstractDrawsGeneratorTest {

    @Test
    public void testGroupOf4() {
        TournamentEventEntity tournamentEventEntity = new TournamentEventEntity();
        tournamentEventEntity.setId(55L);
        tournamentEventEntity.setPlayersPerGroup(4);
        tournamentEventEntity.setPlayersToSeed(0);
        tournamentEventEntity.setPlayersToAdvance(1);
        tournamentEventEntity.setDrawMethod(DrawMethod.SNAKE);

        // make RR round entries
        List<TournamentEventEntry> eventEntries = this.makeTournamentEntriesList();
        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = this.makePlayerDrawInfos();
        List<DrawItem> existingDrawItems = Collections.emptyList();
        IDrawsGenerator rrRoundGenerator = DrawGeneratorFactory.makeGenerator(tournamentEventEntity, DrawType.ROUND_ROBIN);
        List<DrawItem> rrDrawItems = rrRoundGenerator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
        assertEquals("wrong RR draws", 23, rrDrawItems.size());

        // pick top n players to advance and get their entries
        List<TournamentEventEntry> seEventEntries = this.makeSeEntriesFromRREntries(rrDrawItems, eventEntries, tournamentEventEntity, entryIdToPlayerDrawInfo);
        assertEquals("wrong number of advancing player entries", 6, seEventEntries.size());

        // make SE draws
        IDrawsGenerator seRoundGenerator = DrawGeneratorFactory.makeGenerator(tournamentEventEntity, DrawType.SINGLE_ELIMINATION);
        assertNotNull("SE round generator is null", seRoundGenerator);

        List<DrawItem> seDrawItems = seRoundGenerator.generateDraws(seEventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
        assertEquals("wrong number of SE entries", 8, seDrawItems.size());

        int i = 0;
        int [] expectedSeedNumbers           = {1, 8, 5, 4, 3, 6, 7, 2};
        int [] expectedByes = { 0, 1, 0, 0, 0, 0, 2, 0};
        for (DrawItem drawItem : seDrawItems) {
            int seedNumber = drawItem.getGroupNum();
            int expectedSeedNum = expectedSeedNumbers[i];
            assertEquals("wrong seed number", expectedSeedNum, seedNumber);

            int expectedBye = expectedByes[i];
            if (expectedBye != 0) {
                assertEquals("wrong bye ", expectedBye, drawItem.getByeNum());
            }
            i++;
        }
    }

    /**
     *
     * @param rrDrawItems
     * @param rrEventEntries
     * @param tournamentEventEntity
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    private List<TournamentEventEntry> makeSeEntriesFromRREntries(List<DrawItem> rrDrawItems,
                                                                  List<TournamentEventEntry> rrEventEntries,
                                                                  TournamentEventEntity tournamentEventEntity,
                                                                  Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        List<TournamentEventEntry> seEventEntries = new ArrayList<>();
        int playersToAdvance = tournamentEventEntity.getPlayersToAdvance();
        for (DrawItem rrDrawItem : rrDrawItems) {
            if (rrDrawItem.getPlaceInGroup() <= playersToAdvance) {
                String playerId = rrDrawItem.getPlayerId();
                TournamentEventEntry eventEntry = makeEventEntry(playerId, rrEventEntries, entryIdToPlayerDrawInfo);
                if (eventEntry != null) {
                    seEventEntries.add(eventEntry);
                }
            }
        }

        return seEventEntries;
    }

    private TournamentEventEntry makeEventEntry(String playerId,
                                                List<TournamentEventEntry> rrEventEntries,
                                                Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        for (Map.Entry<Long, PlayerDrawInfo> playerDrawInfoEntry : entryIdToPlayerDrawInfo.entrySet()) {
            PlayerDrawInfo playerDrawInfo = playerDrawInfoEntry.getValue();
            if (playerDrawInfo.getProfileId().equals(playerId)) {
                Long entryId = playerDrawInfoEntry.getKey();
                for (TournamentEventEntry rrEventEntry : rrEventEntries) {
                    if (rrEventEntry.getTournamentEntryFk() == entryId) {
                        return rrEventEntry;
                    }
                }
            }
        }
        return null;
    }
}
