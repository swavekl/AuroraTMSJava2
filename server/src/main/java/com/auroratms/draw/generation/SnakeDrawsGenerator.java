package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.*;

/**
 * Generator for draws using snake method - typical draws for round robin phase in groups of 4
 */
public class SnakeDrawsGenerator extends AbstractDrawsGenerator implements IDrawsGenerator {

    public SnakeDrawsGenerator(TournamentEventEntity tournamentEventEntity) {
        super(tournamentEventEntity);
    }

    /**
     * Generates draws based on existing draws and player information
     *
     * @param eventEntries            entries into the event for which we are generating a draw
     * @param entryIdToPlayerDrawInfo information about players who entered the event (state, club, rating etc)
     * @param existingDrawItems       draws to other events for conflict resolution
     * @return draws for this event
     */
    @Override
    public List<DrawItem> generateDraws(List<TournamentEventEntry> eventEntries,
                                        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                        List<DrawItem> existingDrawItems) {

        // sort players in this event by rating from highest to lowest
        sortEntriesByRating(eventEntries, entryIdToPlayerDrawInfo);

        // place players into groups
        List<DrawItem> drawItemList = placePlayersInGroups(eventEntries, entryIdToPlayerDrawInfo);

        // sort draw so players in a group are listed together for easy display
        sortDrawItemsByPlaceInGroup(drawItemList);

        return drawItemList;
    }

    /**
     * Makes a draw
     *
     * @param eventEntries            entries into this event
     * @param entryIdToPlayerDrawInfo information about players who entered
     */
    private List<DrawItem> placePlayersInGroups(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        int numEnteredPlayers = eventEntries.size();
        int playersPerGroup = this.tournamentEventEntity.getPlayersPerGroup();
        int playersToSeed = this.tournamentEventEntity.getPlayersToSeed();

        // players to seed are like a group with only one player
        double dNumGroups = ((double) (numEnteredPlayers - playersToSeed) / (double) playersPerGroup);
        int numGroups = (int) Math.ceil(dNumGroups);
        numGroups += playersToSeed;

        // make the draw
        List<DrawItem> drawItemList = new ArrayList<>(eventEntries.size());
        long eventFk = this.tournamentEventEntity.getId();
        int rowNum = 1;
        int groupNum = 1;
        // direction of placing players into groups - snaking left to right then R to L, then L to R and so on
        boolean leftToRight = true;

        // place seeded players into groups if any
        for (int i = 0; i < playersToSeed; i++) {
            TournamentEventEntry tournamentEventEntry = eventEntries.get(i);
            Long entryId = tournamentEventEntry.getTournamentEntryFk();
            PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(entryId);
            if (playerDrawInfo != null) {
                DrawItem drawItem = makeDrawItem(eventFk, groupNum, rowNum, playerDrawInfo, DrawType.ROUND_ROBIN);
                drawItemList.add(drawItem);
            }
            groupNum++;
        }

        // place the rest of the players in the remaining full groups
        for (int i = playersToSeed; i < eventEntries.size(); i++) {
            TournamentEventEntry tournamentEventEntry = eventEntries.get(i);
            Long entryId = tournamentEventEntry.getTournamentEntryFk();
            PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(entryId);
            if (playerDrawInfo != null) {
                DrawItem drawItem = makeDrawItem(eventFk, groupNum, rowNum, playerDrawInfo, DrawType.ROUND_ROBIN);
                drawItemList.add(drawItem);
            }

            if (leftToRight) {
                if (groupNum == numGroups) {
                    // change direction and advance to next row
                    leftToRight = false;
                    rowNum++;
                } else {
                    groupNum++;
                }
            } else {
                // right to left
                if (groupNum == playersToSeed + 1) {
                    // change direction and advance to the next row
                    leftToRight = true;
                    rowNum++;
                } else {
                    groupNum--;
                }
            }
        }

        return drawItemList;
    }

    /**
     * Sort items so items in the same group are together
     *
     * @param drawItemList draw item list
     */
    private void sortDrawItemsByPlaceInGroup(List<DrawItem> drawItemList) {
        Collections.sort(drawItemList, new Comparator<DrawItem>() {
            @Override
            public int compare(DrawItem drawItem1, DrawItem drawItem2) {
                if (drawItem1.getGroupNum() == drawItem2.getGroupNum()) {
                    return Integer.compare(drawItem1.getPlaceInGroup(), drawItem2.getPlaceInGroup());
                } else {
                    return Integer.compare(drawItem1.getGroupNum(), drawItem2.getGroupNum());
                }
            }
        });

//System.out.println("Sorted draw list [groupNum, placeInGroup]");
//int previousGroupNum = 0;
//for (DrawItem drawItem : drawItemList) {
//    if (drawItem.getGroupNum() != previousGroupNum) {
//        System.out.println("----------");
//        previousGroupNum = drawItem.getGroupNum();
//    }
//    System.out.println(drawItem.getGroupNum() + ", " + drawItem.getPlaceInGroup() + ", " + drawItem.getPlayerName() + ", " + drawItem.getRating() + ", " + drawItem.getClubName() + ", " + drawItem.getState());
//}
//System.out.println("----------");
    }
}
