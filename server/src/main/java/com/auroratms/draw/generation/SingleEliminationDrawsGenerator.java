package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.draw.generation.singleelim.BracketGenerator;
import com.auroratms.draw.generation.singleelim.BracketLine;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.*;

/**
 * Draws generator for single elimination type of event or later round of round robin event
 */
public class SingleEliminationDrawsGenerator extends AbstractDrawsGenerator implements IDrawsGenerator {

    public SingleEliminationDrawsGenerator(TournamentEventEntity tournamentEventEntity) {
        super(tournamentEventEntity);
    }

    @Override
    public List<DrawItem> generateDraws(List<TournamentEventEntry> eventEntries,
                                        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                        List<DrawItem> existingDrawItems) {
        int numEntries = eventEntries.size();
        BracketGenerator bracketGenerator = new BracketGenerator(numEntries);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        int requiredByes = bracketGenerator.getRequiredByes();

        // place players into the bracket
        return placePlayers(bracketLines, eventEntries, entryIdToPlayerDrawInfo, requiredByes);
    }

    /**
     * @param bracketLines
     * @param eventEntries
     * @param entryIdToPlayerDrawInfo
     * @param requiredByes
     * @return
     */
    private List<DrawItem> placePlayers(BracketLine[] bracketLines,
                                        List<TournamentEventEntry> eventEntries,
                                        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo, int requiredByes) {

        sortEntriesByRating(eventEntries, entryIdToPlayerDrawInfo);

        // separate players into batches according to seed number
        // 3 & 4 are equivalent so batch of 3s
        // 5 - 8 are equivalent so batch of 5s
        // 9 - 16 are equivalent so batch of 9s
        // within the batch players can be placed by spacing them geographically as far as possible
        // to avoid conflicts
        DrawItem[] drawItemsArray = new DrawItem[bracketLines.length];
        int participantsCount = eventEntries.size();
        int rounds = (int) Math.ceil(Math.log(participantsCount) / Math.log(2));
        int previousSublistEnd = 0;
        int playerSeedNum = 1;
        for (int round = 1; round <= rounds; round++) {
            int batchSize = (int) Math.pow(2, round);
            // sublist is starting list inclusive and ending index exclusive
            int sublistStart = previousSublistEnd;
            int subListEnd = sublistStart + batchSize;
            subListEnd = Math.min(subListEnd, eventEntries.size());
//            System.out.println("playerSeedNum = " + playerSeedNum+ " batchSize = " + batchSize + " sublistStart = " + sublistStart + " subListEnd = " + subListEnd);
            List<TournamentEventEntry> entriesSubList = eventEntries.subList(sublistStart, subListEnd);

            placePlayersFromSublist(playerSeedNum, entriesSubList, entryIdToPlayerDrawInfo, bracketLines, drawItemsArray);

            previousSublistEnd = subListEnd;
            playerSeedNum += batchSize;
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
        for (DrawItem drawItem : drawItemsArray) {
            if (drawItem != null) {
                drawItems.add(drawItem);
            } else {
                System.out.println("draw item is null");
            }
        }
        return drawItems;
    }

    /**
     *
     * @param bracketLine
     * @return
     */
    private DrawItem makeByeLine(BracketLine bracketLine) {
        DrawItem drawItem = new DrawItem();
        long eventFk = this.tournamentEventEntity.getId();
        drawItem.setEventFk(eventFk);
        drawItem.setDrawType(DrawType.SINGLE_ELIMINATION);
        drawItem.setGroupNum(bracketLine.getSeedNumber());
        drawItem.setByeNum(bracketLine.getByeSeedNumber());
        drawItem.setPlayerId("");
        return drawItem;
    }

    /**
     * Places players with the same normalized seed number in the array of draw lines
     *
     * @param playerSeedNum           seed number of the first player in the batch
     * @param entriesSubList          subset of entries that needs to be put into draw lines
     * @param entryIdToPlayerDrawInfo map of tournament entry ids to player draw infos
     * @param bracketLines            bracket lines telling us where to place the players with given seed numbers
     * @param drawItemsArray          array of draw items where we add player's draw items
     */
    private void placePlayersFromSublist(int playerSeedNum,
                                         List<TournamentEventEntry> entriesSubList,
                                         Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                         BracketLine[] bracketLines,
                                         DrawItem[] drawItemsArray) {
        long eventFk = this.tournamentEventEntity.getId();
        int currentPlayerSeedNum = playerSeedNum;
        for (TournamentEventEntry eventEntry : entriesSubList) {
            long tEntryId = eventEntry.getTournamentEntryFk();
//            System.out.println("currentPlayerSeedNum = " + currentPlayerSeedNum);
            // find bracket line for this seed
            for (int i = 0; i < bracketLines.length; i++) {
                BracketLine bracketLine = bracketLines[i];
                if (bracketLine.getSeedNumber() == currentPlayerSeedNum) {
//                    System.out.println("found bracket line for seed number at index " + i);
                    PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(tEntryId);
                    if (playerDrawInfo != null) {
                        DrawItem drawItem = makeDrawItem(eventFk, 0, 0,
                                playerDrawInfo,
                                DrawType.SINGLE_ELIMINATION);
                        drawItemsArray[i] = drawItem;

                        drawItem.setGroupNum(currentPlayerSeedNum);

//                        if (currentPlayerSeedNum < 2) {
                            currentPlayerSeedNum++;
//                        }
                    }
                    break;
                }
            }
        }
    }
}
