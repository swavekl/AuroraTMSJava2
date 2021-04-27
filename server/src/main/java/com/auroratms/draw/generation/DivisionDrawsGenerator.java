package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
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

    /**
     * Generates draws based on existing draws and player information
     *
     * @param eventEntries            entries into the event for which we are generating a draw
     * @param entryIdToPlayerDrawInfo information about players who entered the event (state, club, rating etc)
     * @param existingDrawItems       draws to other events for conflict resolution
     * @return
     */
    @Override
    public List<DrawItem> generateDraws(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo, List<DrawItem> existingDrawItems) {

        // sort players from highest to lowest rated
        sortEntriesByRating(eventEntries, entryIdToPlayerDrawInfo);

        // place players in groups
        return placePlayersInGroups(eventEntries, entryIdToPlayerDrawInfo);
    }

    private void sortEntriesByRating(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
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
    }

    /**
     * @param eventEntries
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    private List<DrawItem> placePlayersInGroups(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        int numEnteredPlayers = eventEntries.size();
        int playersPerGroup = this.tournamentEventEntity.getPlayersPerGroup();
        int playersToSeed = this.tournamentEventEntity.getPlayersToSeed();

        // make the draw
        List<DrawItem> drawItemList = new ArrayList<>(eventEntries.size());
        long eventFk = this.tournamentEventEntity.getId();

        // place players into groups
        int groupNum = 1;
        int rowNum = 1;
        for (int i = 0; i < numEnteredPlayers; i++) {
            TournamentEventEntry tournamentEventEntry = eventEntries.get(i);
            Long entryId = tournamentEventEntry.getTournamentEntryFk();
            PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(entryId);
            if (playerDrawInfo != null) {
                DrawItem drawItem = new DrawItem();
                drawItem.setEventFk(eventFk);
                drawItem.setDrawType(DrawType.ROUND_ROBIN);
                drawItem.setPlayerId(playerDrawInfo.getProfileId());
                drawItem.setGroupNum(groupNum);
                drawItem.setPlaceInGroup(rowNum);
                drawItemList.add(drawItem);
            }

            // start filling a new group if this one is full
            if (rowNum == playersPerGroup) {
                rowNum = 1;
                groupNum++;
            } else {
                rowNum++;
            }
        }

        return drawItemList;
    }
}
