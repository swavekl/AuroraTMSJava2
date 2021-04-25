package com.auroratms.draw.generation;

import com.auroratms.draw.Draw;
import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.*;

/**
 * Generator for draws for events like giant round robin in groups of 8
 * Sort players from highest to lowest rating and divide them into groups of 8
 */
public class DivisionDrawsGenerator implements IDrawsGenerator {
    private TournamentEventEntity tournamentEventEntity;

    public DivisionDrawsGenerator(TournamentEventEntity tournamentEventEntity) {
        this.tournamentEventEntity = tournamentEventEntity;
    }

    @Override
    public List<Draw> generateDraws(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo, List<Draw> existingDraws) {

        // sort players in this event by rating from highest to lowest
        Collections.sort(eventEntries, new Comparator<TournamentEventEntry>() {
            @Override
            public int compare(TournamentEventEntry tee1, TournamentEventEntry tee2) {
                long tournamentEntryFk1 = tee1.getTournamentEntryFk();
                long tournamentEntryFk2 = tee2.getTournamentEntryFk();
                PlayerDrawInfo pdi1 = entryIdToPlayerDrawInfo.get(tournamentEntryFk1);
                PlayerDrawInfo pdi2 = entryIdToPlayerDrawInfo.get(tournamentEntryFk2);
                int rating1 = pdi1.getRating();
                int rating2 = pdi2.getRating();
                return Integer.compare(rating2, rating1);
            }
        });

        List<Draw> drawList = placePlayersInGroups(eventEntries, entryIdToPlayerDrawInfo);

        return drawList;
    }

    /**
     *
     * @param eventEntries
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    private List<Draw> placePlayersInGroups(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        int numEnteredPlayers = eventEntries.size();
        int playersPerGroup = this.tournamentEventEntity.getPlayersPerGroup();
        int playersToSeed = this.tournamentEventEntity.getPlayersToSeed();

        // players to seed are like a group with only one player
        double dNumGroups = ((double) (numEnteredPlayers - playersToSeed) / (double) playersPerGroup);
        int numGroups = (int) Math.ceil(dNumGroups);

        // make the draw
        List<Draw> drawList = new ArrayList<>(eventEntries.size());
        long eventFk = this.tournamentEventEntity.getId();

        // place players into groups
        int groupNum = 1;
        int rowNum = 1;
        for (int i = 0; i < numEnteredPlayers; i++) {
            TournamentEventEntry tournamentEventEntry = eventEntries.get(i);
            Long entryId = tournamentEventEntry.getTournamentEntryFk();
            PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(entryId);
            if (playerDrawInfo != null) {
                Draw draw = new Draw();
                draw.setEventFk(eventFk);
                draw.setDrawType(DrawType.ROUND_ROBIN);
                draw.setPlayerId(playerDrawInfo.getProfileId());
                draw.setGroupNum(groupNum);
                draw.setPlaceInGroup(rowNum);
                drawList.add(draw);
            }

            // start filling a new group if this one is full
            if (rowNum == playersPerGroup) {
                rowNum = 1;
                groupNum++;
            } else {
                rowNum++;
            }
        }

        return drawList;
    }
}
