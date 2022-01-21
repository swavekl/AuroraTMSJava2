package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.doubles.DoublesPair;

import java.util.*;

/**
 * Generator for draws for a doubles event
 */
public class DoublesSnakeDrawsGenerator extends AbstractDoublesDrawsGenerator implements IDrawsGenerator {

    public DoublesSnakeDrawsGenerator(TournamentEvent tournamentEvent) {
        super(tournamentEvent);
    }

    @Override
    public List<DrawItem> generateDraws(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo, List<DrawItem> existingDrawItems) {
        List<DrawItem> drawItemsList = placeTeamsInGroups(eventEntries, entryIdToPlayerDrawInfo, this.doublesPairsForEvent);

        sortDrawItemsByPlaceInGroup(drawItemsList);

        return drawItemsList;
    }

    /**
     *
     * @param eventEntries
     * @param entryIdToPlayerDrawInfo
     * @param doublesPairsForEvent
     * @return
     */
    private List<DrawItem> placeTeamsInGroups(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo, List<DoublesPair> doublesPairsForEvent) {
        int numEnteredTeams = doublesPairsForEvent.size();
        int teamsPerGroup = this.tournamentEvent.getPlayersPerGroup();
        int teamsToSeed = this.tournamentEvent.getPlayersToSeed();

        // players to seed are like a group with only one player
        double dNumGroups = ((double) (numEnteredTeams - teamsToSeed) / (double) teamsPerGroup);
        int numGroups = (int) Math.ceil(dNumGroups);
        numGroups += teamsToSeed;

        // make the draw
        List<DrawItem> drawItemList = new ArrayList<>(doublesPairsForEvent.size());
        long eventFk = this.tournamentEvent.getId();
        int rowNum = 1;
        int groupNum = 1;
        // direction of placing players into groups - snaking left to right then R to L, then L to R and so on
        boolean leftToRight = true;

        // place seeded teams into groups if any
        for (int i = 0; i < teamsToSeed; i++) {
            DoublesPair doublesPair = doublesPairsForEvent.get(i);
            DrawItem drawItem = makeDoublesDrawItem(doublesPair, eventFk, groupNum, rowNum,
                    entryIdToPlayerDrawInfo, eventEntries, DrawType.ROUND_ROBIN);
            if (drawItem != null) {
                drawItemList.add(drawItem);
            }
            groupNum++;
        }

        // place the rest of the teams in the remaining full groups
        for (int i = teamsToSeed; i < doublesPairsForEvent.size(); i++) {
            DoublesPair doublesPair = doublesPairsForEvent.get(i);
            DrawItem drawItem = makeDoublesDrawItem(doublesPair, eventFk, groupNum, rowNum,
                    entryIdToPlayerDrawInfo, eventEntries, DrawType.ROUND_ROBIN);
            if (drawItem != null) {
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
                if (groupNum == teamsToSeed + 1) {
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
