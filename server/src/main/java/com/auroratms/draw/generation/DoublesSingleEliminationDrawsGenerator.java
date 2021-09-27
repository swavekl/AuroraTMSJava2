package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.draw.generation.singleelim.BracketGenerator;
import com.auroratms.draw.generation.singleelim.BracketLine;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.doubles.DoublesPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DoublesSingleEliminationDrawsGenerator extends AbstractDoublesDrawsGenerator implements IDrawsGenerator {

    public DoublesSingleEliminationDrawsGenerator(TournamentEventEntity tournamentEventEntity) {
        super(tournamentEventEntity);
    }

    @Override
    public List<DrawItem> generateDraws(List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo, List<DrawItem> existingDrawItems) {
        int numEntries = eventEntries.size() / 2; // doubles teams have 2 players entries
        BracketGenerator bracketGenerator = new BracketGenerator(numEntries);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        int requiredByes = bracketGenerator.getRequiredByes();

        return placeTeams(bracketLines, eventEntries, entryIdToPlayerDrawInfo, requiredByes);
    }

    /**
     * @param bracketLines
     * @param eventEntries
     * @param entryIdToPlayerDrawInfo
     * @param requiredByes
     * @return
     */
    private List<DrawItem> placeTeams(BracketLine[] bracketLines, List<TournamentEventEntry> eventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo, int requiredByes) {
        DrawItem[] drawItemsArray = new DrawItem[bracketLines.length];

        int participantsCount = eventEntries.size() / 2;
        int rounds = (int) Math.ceil(Math.log(participantsCount) / Math.log(2));
        int previousSublistEnd = 0;
        int playerSeedNum = 1;
        for (int round = 1; round <= rounds; round++) {
            // batch of players to fetch and place
            // 1 & 2 so 2
            // 3 & 4 so 2
            // 5 - 8 so 4
            // 9 -16 so 8 and so on
            int power = (round <= 2) ? 1 : round - 1;
            int batchSize = (int) Math.pow(2, power) * 2;
//            System.out.println("batchSize = " + batchSize + " round = " + round);
            // sublist is starting list inclusive and ending index exclusive
            int sublistStart = previousSublistEnd;
            int subListEnd = sublistStart + batchSize;
            subListEnd = Math.min(subListEnd, eventEntries.size());
//            System.out.println("playerSeedNum = " + playerSeedNum+ " batchSize = " + batchSize + " sublistStart = " + sublistStart + " subListEnd = " + subListEnd);
            List<TournamentEventEntry> entriesSubList = eventEntries.subList(sublistStart, subListEnd);

            placeTeamFromSublist(playerSeedNum, entriesSubList, entryIdToPlayerDrawInfo, bracketLines, drawItemsArray);

            previousSublistEnd = subListEnd;
            playerSeedNum += (batchSize / 2);
        }

        // place byes if any
        if (requiredByes > 0) {
            for (int i = 0; i < bracketLines.length; i++) {
                BracketLine bracketLine = bracketLines[i];
                if (bracketLine.isBye() && bracketLine.getByeSeedNumber() <= requiredByes) {
                    drawItemsArray[i] = makeByeLine(bracketLine);
                }
            }
        }

        // convert to draw items list
        List<DrawItem> drawItems = new ArrayList<>(bracketLines.length);
        int singleElimLineNum = 1;
        for (DrawItem drawItem : drawItemsArray) {
            if (drawItem != null) {
                drawItem.setSingleElimLineNum(singleElimLineNum);
                drawItem.setRound(bracketLines.length);
                drawItems.add(drawItem);
                singleElimLineNum++;
            } else {
                System.out.println("draw item is null");
            }
        }

        // todo - generate remaining draws
        List<DrawItem> remainingRoundsDrawItems = generateRemainingRoundsDrawItems();
        drawItems.addAll(remainingRoundsDrawItems);

        return drawItems;
    }

    /**
     *
     * @return
     */
    private List<DrawItem> generateRemainingRoundsDrawItems() {
        List<DrawItem> drawItemList = new ArrayList<>();

        return drawItemList;

    }

    /**
     *
     * @param playerSeedNum
     * @param entriesSubList
     * @param entryIdToPlayerDrawInfo
     * @param bracketLines
     * @param drawItemsArray
     */
    private void placeTeamFromSublist(int playerSeedNum,
                                      List<TournamentEventEntry> entriesSubList,
                                      Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                      BracketLine[] bracketLines,
                                      DrawItem[] drawItemsArray) {
        long eventFk = this.tournamentEventEntity.getId();
        int currentPlayerSeedNum = playerSeedNum;
        boolean firstPlayer = true;
        for (TournamentEventEntry eventEntry : entriesSubList) {
            if (firstPlayer) {
                Long eventEntryId = eventEntry.getId();
                // find bracket line for this seed
                for (int i = 0; i < bracketLines.length; i++) {
                    BracketLine bracketLine = bracketLines[i];
                    if (bracketLine.getSeedNumber() == currentPlayerSeedNum) {
                        DoublesPair doublesPair = findDoublesPair(eventEntryId);
                        if (doublesPair != null) {
                            DrawItem drawItem = makeDoublesDrawItem(doublesPair, eventFk, 0, 0,
                                    entryIdToPlayerDrawInfo, entriesSubList, DrawType.SINGLE_ELIMINATION);
                            if (drawItem != null) {
                                drawItemsArray[i] = drawItem;
                                int groupNum = 0;
                                if (doublesPair.getPlayerAEventEntryFk() == eventEntryId) {
                                    PlayerDrawInfo playerADrawInfo = getPlayerDrawInfo(doublesPair.getPlayerAEventEntryFk(), entriesSubList, entryIdToPlayerDrawInfo);
                                    groupNum = playerADrawInfo.getRRGroupNum();
                                } else {
                                    PlayerDrawInfo playerBDrawInfo = getPlayerDrawInfo(doublesPair.getPlayerBEventEntryFk(), entriesSubList, entryIdToPlayerDrawInfo);
                                    groupNum = playerBDrawInfo.getRRGroupNum();
                                }
                                drawItem.setGroupNum(groupNum);
                                drawItem.setSeSeedNumber(currentPlayerSeedNum);
                                currentPlayerSeedNum++;
                                break;
                            }
                        }
                    }
                }
            }
            firstPlayer = !firstPlayer;
        }
    }

    /**
     *
     * @param eventEntryId
     * @return
     */
    private DoublesPair findDoublesPair(long eventEntryId) {
        for (DoublesPair doublesPair : doublesPairsForEvent) {
            if (doublesPair.getPlayerAEventEntryFk() == eventEntryId ||
                doublesPair.getPlayerBEventEntryFk() == eventEntryId) {
                return doublesPair;
            }
        }
        return null;
    }
}
