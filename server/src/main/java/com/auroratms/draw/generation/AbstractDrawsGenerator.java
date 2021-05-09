package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Class with common code for generators
 */
public abstract class AbstractDrawsGenerator {

    protected TournamentEventEntity tournamentEventEntity;

    public AbstractDrawsGenerator(TournamentEventEntity tournamentEventEntity) {
        this.tournamentEventEntity = tournamentEventEntity;
    }

    /**
     * Sorts submitted event entries list by player seed rating
     *
     * @param eventEntries            event entries
     * @param entryIdToPlayerDrawInfo player info with rating
     */
    protected void sortEntriesByRating(List<TournamentEventEntry> eventEntries,
                                       Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
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

//        System.out.println("Sorted entries");
//        for (TournamentEventEntry eventEntry : eventEntries) {
//            long tournamentEntryFk = eventEntry.getTournamentEntryFk();
//            PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(tournamentEntryFk);
//            System.out.println (String.format("%s => %d", playerDrawInfo.getPlayerName(), playerDrawInfo.getRating()));
//        }
    }

    /**
     * Make a draw item with player information populated
     *
     * @param eventFk
     * @param groupNum
     * @param placeInGroup
     * @param playerDrawInfo
     * @param drawType
     * @return
     */
    protected DrawItem makeDrawItem(long eventFk,
                                    int groupNum,
                                    int placeInGroup,
                                    PlayerDrawInfo playerDrawInfo,
                                    DrawType drawType) {
        DrawItem drawItem = new DrawItem();
        drawItem.setEventFk(eventFk);
        drawItem.setDrawType(drawType);
        drawItem.setPlayerId(playerDrawInfo.getProfileId());
        drawItem.setPlayerName(playerDrawInfo.getPlayerName());
        drawItem.setRating(playerDrawInfo.getRating());
        drawItem.setClubName(playerDrawInfo.getClubName());
        drawItem.setState(playerDrawInfo.getState());
        drawItem.setGroupNum(groupNum);
        drawItem.setPlaceInGroup(placeInGroup);
        return drawItem;
    }
}
