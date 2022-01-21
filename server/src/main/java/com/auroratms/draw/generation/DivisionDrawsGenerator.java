package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.*;

/**
 * Generator for draws for events like giant round robin in groups of 8
 * Sort players from highest to lowest rating and divide them into groups of 8
 */
public class DivisionDrawsGenerator extends AbstractDrawsGenerator implements IDrawsGenerator {

    public DivisionDrawsGenerator(TournamentEvent tournamentEvent) {
        super(tournamentEvent);
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

    /**
     * @param eventEntries
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    private List<DrawItem> placePlayersInGroups(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        int numEnteredPlayers = eventEntries.size();
        int playersPerGroup = this.tournamentEvent.getPlayersPerGroup();
        int playersToSeed = this.tournamentEvent.getPlayersToSeed();

        // make the draw
        List<DrawItem> drawItemList = new ArrayList<>(eventEntries.size());
        long eventFk = this.tournamentEvent.getId();

        // place players into groups
        int groupNum = 1;
        int rowNum = 1;
        for (int i = 0; i < numEnteredPlayers; i++) {
            TournamentEventEntry tournamentEventEntry = eventEntries.get(i);
            Long entryId = tournamentEventEntry.getTournamentEntryFk();
            PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(entryId);
            if (playerDrawInfo != null) {
                DrawItem drawItem = makeDrawItem(eventFk, groupNum, rowNum, playerDrawInfo, DrawType.ROUND_ROBIN, entryId);
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
