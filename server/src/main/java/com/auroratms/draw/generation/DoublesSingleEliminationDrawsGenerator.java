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

        // generate remaining draws
        int roundOf = (int)Math.pow(2, rounds);
        List<DrawItem> remainingRoundsDrawItems = addDrawItemsForLaterRounds(roundOf, drawItems);
        drawItems.addAll(remainingRoundsDrawItems);

        return drawItems;
    }

    /**
     *
     * @return
     */
    private List<DrawItem> addDrawItemsForLaterRounds(int firstRoundOf, List<DrawItem> firstRoundDrawItems) {
        List<DrawItem> laterRoundsDrawItems = new ArrayList<>(firstRoundOf);
        int additionalRounds = (int) (Math.log(firstRoundOf / 2) / Math.log(2));
        for (int round = additionalRounds; round >= 1; round--) {
            int roundOf = (int) Math.pow(2, round);
            for (int playerNum = 0; playerNum < roundOf; playerNum++) {
                int groupNum = 1 + Math.floorDiv(playerNum, 2);
                int placeInGroup = (playerNum % 2 == 0) ? 1 : 2;
                int thisPlayerSingleElimLineNumber = playerNum + 1;

                // determine if any player gets a bye from the matches in the previous round which feed into this match
                // so we can propagate this player's player id to this round
                // but only in the second round not 3rd or fourth
                boolean teamAGetsBye = false;
                boolean teamBGetsBye = false;
                DrawItem teamADrawItem = null;
                DrawItem teamBDrawItem = null;
                if (roundOf == firstRoundOf / 2) {
                    int advancingPlayerSingleElimLineNumber = -1;
                    for (DrawItem drawItem : firstRoundDrawItems) {
                        // previous round group line 1 & 2 go to line #1 in this round,
                        // lines 3 & 4 to line # 2 in this round and so on
                        int targetLineNum = (int) Math.ceil((double)drawItem.getSingleElimLineNum() / 2);
                        if (targetLineNum == thisPlayerSingleElimLineNumber) {
                            // this draw item is for bye, find out if the player who gets a bye is A or B
                            if (drawItem.getByeNum() != 0) {
                                // odd line number is a bye e.g. 1, 3 then it's the player on the next line i.e. B who advances
                                teamBGetsBye = ((drawItem.getSingleElimLineNum() % 2) == 1);
                                if (teamBGetsBye) {
                                    advancingPlayerSingleElimLineNumber = drawItem.getSingleElimLineNum() + 1;
                                    break;
                                }
                                // even line number is a bye 2, 4, then it's the player on the previous line i.e. A who advances
                                teamAGetsBye = ((drawItem.getSingleElimLineNum() % 2) == 0);
                                if (teamAGetsBye) {
                                    advancingPlayerSingleElimLineNumber = drawItem.getSingleElimLineNum() - 1;
                                    break;
                                }
                            }
                        }
                    }

                    // now find the player from this line number
                    if (advancingPlayerSingleElimLineNumber != -1) {
                        for (DrawItem drawItem : firstRoundDrawItems) {
                            if (drawItem.getSingleElimLineNum() == advancingPlayerSingleElimLineNumber) {
                                if (teamAGetsBye) {
                                    teamADrawItem = drawItem;
                                }
                                if (teamBGetsBye) {
                                    teamBDrawItem = drawItem;
                                }
                                break;
                            }
                        }
                    }
                }

                PlayerDrawInfo teamPlayerDrawInfo = new PlayerDrawInfo();
                if (teamAGetsBye && teamADrawItem != null) {
                    teamPlayerDrawInfo.setPlayerName(teamADrawItem.getPlayerName());
                    teamPlayerDrawInfo.setProfileId(teamADrawItem.getPlayerId());
                    teamPlayerDrawInfo.setRating(teamADrawItem.getRating());
                } else if (teamBGetsBye && teamBDrawItem != null) {
                    teamPlayerDrawInfo.setPlayerName(teamBDrawItem.getPlayerName());
                    teamPlayerDrawInfo.setProfileId(teamBDrawItem.getPlayerId());
                    teamPlayerDrawInfo.setRating(teamBDrawItem.getRating());
                } else {
                    teamPlayerDrawInfo.setPlayerName(DrawItem.TBD_PROFILE_ID);
                    teamPlayerDrawInfo.setProfileId(DrawItem.TBD_PROFILE_ID);
                    teamPlayerDrawInfo.setRating(0);
                }
                DrawItem drawItem = makeDrawItem(tournamentEventEntity.getId(), groupNum, placeInGroup,
                        teamPlayerDrawInfo, DrawType.SINGLE_ELIMINATION, 0L);
                drawItem.setSingleElimLineNum(thisPlayerSingleElimLineNumber);
                drawItem.setRound(roundOf);
                drawItem.setByeNum(0);
                laterRoundsDrawItems.add(drawItem);
            }
        }

        if (tournamentEventEntity.isPlay3rd4thPlace()) {
            PlayerDrawInfo tbdPlayerDrawInfo = new PlayerDrawInfo();
            tbdPlayerDrawInfo.setPlayerName(DrawItem.TBD_PROFILE_ID);
            tbdPlayerDrawInfo.setProfileId(DrawItem.TBD_PROFILE_ID);

            DrawItem drawItem3 = makeDrawItem(tournamentEventEntity.getId(), 2, 1,
                    tbdPlayerDrawInfo, DrawType.SINGLE_ELIMINATION, 0L);
            drawItem3.setSingleElimLineNum(3);
            drawItem3.setRound(2);
            laterRoundsDrawItems.add(drawItem3);

            DrawItem drawItem4 = makeDrawItem(tournamentEventEntity.getId(), 2, 2,
                    tbdPlayerDrawInfo, DrawType.SINGLE_ELIMINATION, 0L);
            drawItem4.setSingleElimLineNum(4);
            drawItem4.setRound(2);
            laterRoundsDrawItems.add(drawItem4);
        }

        return laterRoundsDrawItems;
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
