package com.auroratms.draw.generation;

import com.auroratms.draw.Draw;
import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.*;

/**
 * Generator for draws using snake method - typical draws for round robin phase in groups of 4
 */
public class SnakeDrawsGenerator implements IDrawsGenerator {
    private TournamentEventEntity tournamentEventEntity;

    public SnakeDrawsGenerator(TournamentEventEntity tournamentEventEntity) {
        this.tournamentEventEntity = tournamentEventEntity;
    }

    /**
     * Generates draws based on existing draws and player information
     *
     * @param eventEntries            entries into the event for which we are generating a draw
     * @param entryIdToPlayerDrawInfo information about players who entered the event (state, club, rating etc)
     * @param existingDraws           draws to other events for conflict resolution
     * @return draws for this event
     */
    @Override
    public List<Draw> generateDraws(List<TournamentEventEntry> eventEntries,
                                    Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                    List<Draw> existingDraws) {

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

//        System.out.println("Sorted entries");
//        for (TournamentEventEntry eventEntry : eventEntries) {
//            long tournamentEntryFk = eventEntry.getTournamentEntryFk();
//            PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(tournamentEntryFk);
//            System.out.println (String.format("%s => %d", playerDrawInfo.getPlayerName(), playerDrawInfo.getRating()));
//        }

        // places players into groups
        List<Draw> drawList = placePlayersInGroups(eventEntries, entryIdToPlayerDrawInfo);

        // sort draws so players in a group are listed together
        Collections.sort(drawList, new Comparator<Draw>() {
            @Override
            public int compare(Draw draw1, Draw draw2) {
                if (draw1.getGroupNum() == draw2.getGroupNum()) {
                    return Integer.compare(draw1.getPlaceInGroup(), draw2.getPlaceInGroup());
                } else {
                    return Integer.compare(draw1.getGroupNum(), draw2.getGroupNum());
                }
            }
        });

System.out.println("Sorted draw list [groupNum, placeInGroup]");
int previousGroupNum = 0;
for (Draw draw : drawList) {
    if (draw.getGroupNum() != previousGroupNum) {
        System.out.println("----------");
        previousGroupNum = draw.getGroupNum();
    }
    System.out.println(draw.getGroupNum() + ", " + draw.getPlaceInGroup());
}
System.out.println("----------");

        return drawList;
    }

    /**
     * Makes a draw
     *
     * @param eventEntries entries into this event
     * @param entryIdToPlayerDrawInfo information about players who entered
     */
    private List<Draw> placePlayersInGroups(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        int numEnteredPlayers = eventEntries.size();
        int playersPerGroup = this.tournamentEventEntity.getPlayersPerGroup();
        int playersToSeed = this.tournamentEventEntity.getPlayersToSeed();

        // players to seed are like a group with only one player
        double dNumGroups = ((double)(numEnteredPlayers - playersToSeed) / (double)playersPerGroup);
        int numGroups = (int) Math.ceil(dNumGroups);
        numGroups += playersToSeed;

        // make the draw
        List<Draw> drawList = new ArrayList<>(eventEntries.size());
        long eventFk = this.tournamentEventEntity.getId();
        int groupNum = 1;
        // direction of placing players into groups - snaking left to right then R to L, then L to R and so on
        boolean leftToRight = true;

        // place seeded players into groups if any
        for (int i = 0; i < playersToSeed; i++) {
            TournamentEventEntry tournamentEventEntry = eventEntries.get(i);
            Long entryId = tournamentEventEntry.getTournamentEntryFk();
            PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(entryId);
            if (playerDrawInfo != null) {
                Draw draw = new Draw();
                draw.setEventFk(eventFk);
                draw.setDrawType(DrawType.ROUND_ROBIN);
                draw.setPlayerId(playerDrawInfo.getProfileId());
                draw.setGroupNum(groupNum);
                draw.setPlaceInGroup(1);
                drawList.add(draw);
            }
            groupNum++;
        }

        // place the rest of the players in the remaining full groups
        int rowNum = 1;
        for (int i = playersToSeed; i < eventEntries.size(); i++) {
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

        return drawList;
    }
}
