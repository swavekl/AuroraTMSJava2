package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.draw.generation.singleelim.BracketGenerator;
import com.auroratms.draw.generation.singleelim.BracketLine;
import com.auroratms.draw.generation.singleelim.EntrySorter;
import com.auroratms.draw.generation.singleelim.GeographicalDistanceCalculator;
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

        boolean geographicalSeparation = true;
        if (!geographicalSeparation) {
            sortEntriesByRating(eventEntries, entryIdToPlayerDrawInfo);
        } else {
            EntrySorter sorter = new EntrySorter(entryIdToPlayerDrawInfo);
            eventEntries = sorter.sortEntries(eventEntries, requiredByes);
        }

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
            // batch of players to fetch and place
            // 1 & 2 so 2
            // 3 & 4 so 2
            // 5 - 8 so 4
            // 9 -16 so 8 and so on
            int power = (round <= 2) ? 1 : round - 1;
            int batchSize = (int) Math.pow(2, power);
//            System.out.println("batchSize = " + batchSize + " round = " + round);
            // sublist is starting list inclusive and ending index exclusive
            int sublistStart = previousSublistEnd;
            int subListEnd = sublistStart + batchSize;
            subListEnd = Math.min(subListEnd, eventEntries.size());
//            System.out.println("playerSeedNum = " + playerSeedNum+ " batchSize = " + batchSize + " sublistStart = " + sublistStart + " subListEnd = " + subListEnd);
            List<TournamentEventEntry> entriesSubList = eventEntries.subList(sublistStart, subListEnd);

            if (!geographicalSeparation) {
                placePlayersFromSublist(playerSeedNum, entriesSubList, entryIdToPlayerDrawInfo, bracketLines, drawItemsArray);
            } else {
                placePlayersFromSublistSeparateGeographically(playerSeedNum, entriesSubList, entryIdToPlayerDrawInfo, bracketLines, drawItemsArray, batchSize);
            }

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
        int singleElimLineNum = 1;
        for (DrawItem drawItem : drawItemsArray) {
            if (drawItem != null) {
                drawItem.setSingleElimLineNum(singleElimLineNum);
                drawItems.add(drawItem);
                singleElimLineNum++;
            } else {
                System.out.println("draw item is null");
            }
        }
        return drawItems;
    }

    /**
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
            long entryId = eventEntry.getTournamentEntryFk();
//            System.out.println("currentPlayerSeedNum = " + currentPlayerSeedNum);
            // find bracket line for this seed
            for (int i = 0; i < bracketLines.length; i++) {
                BracketLine bracketLine = bracketLines[i];
                if (bracketLine.getSeedNumber() == currentPlayerSeedNum) {
//                    System.out.println("found bracket line for seed number at index " + i);
                    PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(entryId);
                    if (playerDrawInfo != null) {
                        DrawItem drawItem = makeDrawItem(eventFk, 0, 0,
                                playerDrawInfo,
                                DrawType.SINGLE_ELIMINATION, entryId);
                        drawItemsArray[i] = drawItem;

                        drawItem.setGroupNum(currentPlayerSeedNum);

                        currentPlayerSeedNum++;
                    }
                    break;
                }
            }
        }
    }

    /**
     * Places players with the same normalized seed number in the array of draw lines
     *
     * @param playerSeedNum           seed number of the first player in the batch
     * @param entriesSubList          subset of entries that needs to be put into draw lines
     * @param entryIdToPlayerDrawInfo map of tournament entry ids to player draw infos
     * @param bracketLines            bracket lines telling us where to place the players with given seed numbers
     * @param drawItemsArray          array of draw items where we add player's draw items
     * @param numSections             number of sections that the players in the entriesSublist are to be placed in
     */
    private void placePlayersFromSublistSeparateGeographically(int playerSeedNum,
                                                               List<TournamentEventEntry> entriesSubList,
                                                               Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                                               BracketLine[] bracketLines,
                                                               DrawItem[] drawItemsArray,
                                                               int numSections) {
        long eventFk = this.tournamentEventEntity.getId();
        int currentPlayerSeedNum = playerSeedNum;
//        System.out.println("================================");
//        System.out.println("entriesSubList.size = " + entriesSubList.size());
        for (TournamentEventEntry eventEntry : entriesSubList) {
            long entryId = eventEntry.getTournamentEntryFk();
            PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(entryId);
            if (playerDrawInfo != null) {
                int i = 0;
                if (playerSeedNum == 1) {
                    // place players 1 & 2 into first / last lines because there is no one in the list yet
                    i = (currentPlayerSeedNum == 1) ? 0 : drawItemsArray.length - 1;
                } else {
                    // calculate the spot with highest separation e.g. club is 1, city is 2, state is 3 etc.
                    // the higher number the
                    i = calculateHighestSeparationSpot(drawItemsArray, numSections, playerDrawInfo, entryIdToPlayerDrawInfo, bracketLines, playerSeedNum);
                }
                DrawItem drawItem = makeDrawItem(eventFk, 0, 0,
                        playerDrawInfo,
                        DrawType.SINGLE_ELIMINATION, entryId);
                drawItemsArray[i] = drawItem;

                drawItem.setGroupNum(currentPlayerSeedNum);

                currentPlayerSeedNum++;
            }
        }
    }

    /**
     * @param drawItemsArray
     * @param numSections
     * @param playerDrawInfo
     * @param entryIdToPlayerDrawInfo
     * @param bracketLines
     * @param playerSeedNum
     * @return
     */
    private int calculateHighestSeparationSpot(DrawItem[] drawItemsArray,
                                               int numSections,
                                               PlayerDrawInfo playerDrawInfo,
                                               Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                               BracketLine[] bracketLines,
                                               int playerSeedNum) {
        // map of section start index to section end index to avoid

        this.dumpDraw(drawItemsArray, bracketLines);

        // find the sections with players from same geography and avoid those sections of the draw when placing this player
        Map<Integer, Integer> sectionsToAvoid = sectionsToAvoid(playerDrawInfo, drawItemsArray, entryIdToPlayerDrawInfo);
        int maxDistanceIndex = getMaxDistanceIndex(drawItemsArray, numSections, playerDrawInfo, entryIdToPlayerDrawInfo, bracketLines, sectionsToAvoid);
        // if unable to place player by avoiding sections try to place him anywhere (don't avoid any sections)
        if (maxDistanceIndex == -1) {
            maxDistanceIndex = getMaxDistanceIndex(drawItemsArray, numSections, playerDrawInfo, entryIdToPlayerDrawInfo, bracketLines, new HashMap<>());
        }
        if (maxDistanceIndex == -1) {
            throw new RuntimeException("unable to calculate distance for player " + playerDrawInfo.getPlayerName());
        }
        System.out.println("placing player " + playerDrawInfo.getPlayerName() + " at index " + maxDistanceIndex); // + ", maxDistance = " + maxDistance);
        System.out.println("===================================");

        return maxDistanceIndex;
    }

    private int getMaxDistanceIndex (DrawItem[] drawItemsArray,
                                     int numSections,
                                     PlayerDrawInfo playerDrawInfo,
                                     Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                     BracketLine[] bracketLines,
                                     Map<Integer, Integer> sectionsToAvoid) {

    int maxDistanceIndex = -1;
    int maxDistance = 0;
//        System.out.println("--------------------------------------------------------------------");
//        String playerState = playerDrawInfo.getState();
//        System.out.println("playerDrawInfo.getPlayerName() = " + playerDrawInfo.getPlayerName() + " \tfrom " + playerState + " seedNum " + playerSeedNum);

    //        System.out.println("numSections = " + numSections);
//        System.out.println("drawItemsArray.length = " + drawItemsArray.length);
    int sectionSize = drawItemsArray.length / numSections;
//        boolean start = true;
//        System.out.println("sectionSize = " + sectionSize);
    // divide draws array into num sections

    //        for (int i = 0; i < numSections; i++) {
    // in 8 sections - first fill the 0, 2, 4, 6 then odd number sections 1, 3, 5, 7
    int[] sectionsToVisit = createSectionsVisitOrder(numSections);
        for (int i = 0; i < sectionsToVisit.length; i++) {
            int sectionNumber = sectionsToVisit[i];
//            System.out.println("sectionNumber = " + sectionNumber);
            boolean start = ((sectionNumber % 2) == 0);
//            System.out.println("start = " + start);
            // alternate getting players from beginning of the section, then the end, then beginning and so on
            int sectionStart = sectionNumber * sectionSize;
            int sectionEnd = sectionStart + sectionSize - 1;
            int existingPlayerPosition = (start) ? sectionStart : (sectionStart + sectionSize - 1);
            int newPlayerPosition = (!start) ? sectionStart : (sectionStart + sectionSize - 1);
//            start = !start;

            if (avoidSection(sectionStart, sectionEnd, sectionsToAvoid)) {
//                System.out.println("avoided section sectionStart = " + sectionStart +" sectionEnd = " + sectionEnd);
                continue;
            }

            boolean isAlreadyTaken = (drawItemsArray[newPlayerPosition] != null);
            boolean isBye = bracketLines[newPlayerPosition].isBye();
//            System.out.println("isAlreadyTaken = " + isAlreadyTaken);
//            System.out.println("isBye = " + isBye);
            if (!isBye && !isAlreadyTaken) {
                System.out.println("testing sectionNumber " + sectionNumber);
                if (existingPlayerPosition < drawItemsArray.length) {
                    DrawItem drawItem = drawItemsArray[existingPlayerPosition];
                    if (drawItem != null) {
                        long entryId = drawItem.getEntryId();
                        PlayerDrawInfo placedPlayerDrawInfo = entryIdToPlayerDrawInfo.get(entryId);
                        if (placedPlayerDrawInfo != null) {
                            System.out.println("existingPlayerPosition = " + existingPlayerPosition + " player " + placedPlayerDrawInfo.getPlayerName() + " \tfrom " + placedPlayerDrawInfo.getState());
                            System.out.println("newPlayerPosition      = " + newPlayerPosition);
                            // check if placed player is from the same club or city/state
                            GeographicalDistanceCalculator calculator = new GeographicalDistanceCalculator();
                            int distance = calculator.getDistance(placedPlayerDrawInfo, playerDrawInfo);
//                            System.out.println("distance = " + distance);
                            // from same club, city state or state
                            if (distance > maxDistance) {
                                maxDistance = distance;
                                maxDistanceIndex = newPlayerPosition;
                            }
                        }
                    }
                }
            }
        }
        return maxDistanceIndex;
    }

    /**
     * Creates order in which sections should be visited for best sprading of players
     *
     * @param numSections number of sections to visit
     * @return array of section indexes
     */
    private int[] createSectionsVisitOrder(int numSections) {
        // in 8 sections - first visit the 0, 2, 4, 6 section
        int[] sectionsToVisit = new int[numSections];
        int currentSpot = 0;
        for (int i = 0; i < numSections; i++) {
            if ((i % 2) == 0) {
                sectionsToVisit[currentSpot] = i;
                currentSpot++;
            }
        }
        // then odd number sections 1, 3, 5, 7 in reverse order e.g. 7, 5, 3, 1
        for (int i = 0; i < numSections; i++) {
//            if ((i % 2) > 0) {
//                sectionsToVisit[currentSpot] = i;
//                currentSpot++;
//            }
            if (((numSections - i) % 2) > 0) {
//                System.out.println("reverse i = " + (numSections - i));
                sectionsToVisit[currentSpot] = (numSections - i);
                currentSpot++;
            }
        }
//        System.out.print(sectionsToVisit.length + " sectionsToVisit = [");
//        for (int i = 0; i < sectionsToVisit.length; i++) {
//            int sectionNumber = sectionsToVisit[i];
//            System.out.print(sectionNumber + ", ");
//        }
//        System.out.println("]");
        return sectionsToVisit;
    }

    /**
     * Test if this section should be avoided because it contains a player from this geogrpahy already
     *
     * @param sectionStart    section start index (0 based)
     * @param sectionEnd      section end index
     * @param sectionsToAvoid map of section start index to end indexes of sections to avoid
     * @return
     */
    private boolean avoidSection(int sectionStart, int sectionEnd, Map<Integer, Integer> sectionsToAvoid) {
        boolean avoid = false;
        for (Map.Entry<Integer, Integer> sectionToAvoidStartEnd : sectionsToAvoid.entrySet()) {
            Integer avoidStart = sectionToAvoidStartEnd.getKey();
            Integer avoidEnd = sectionToAvoidStartEnd.getValue();
            if (sectionStart >= avoidStart && sectionEnd <= avoidEnd) {
                avoid = true;
                break;
            }
        }
        return avoid;
    }

    /**
     * Calculate how many sections we should test considering the nth player from the same geography being placed in draw already
     * E.g. if there is only one player from same geography already placed then consider 2 sections (i.e. halfves) - on eof them wil not
     * have a player from this geography
     * if there are 2 or 3 players from same geography then 4 sections will allow to place the next player,
     * 4 - 7 players then 8 sections,  Number of sections are powers of 2
     *
     * @param playerDrawInfo this player draw information which includes geography
     * @param drawItemsArray array of items for size
     * @return
     */
    private int calculateNumSectionsForTesting(PlayerDrawInfo playerDrawInfo, DrawItem[] drawItemsArray) {
        int numSectionsToTest = 1;
        String playerState = playerDrawInfo.getState();
        if (playerState != null) {
            int nthPlayerFromGeography = 1;  // count this player as 1
            for (DrawItem drawItem : drawItemsArray) {
                if (drawItem != null) {
                    nthPlayerFromGeography += (playerState.equals(drawItem.getState())) ? 1 : 0;
                }
            }
//        System.out.println("nthPlayerFromGeography = " + nthPlayerFromGeography);

            for (int power = 1; power < 6; power++) {
                int pwr = (int) Math.pow(2, power);
                if (nthPlayerFromGeography <= pwr) {
                    numSectionsToTest = pwr;
                    break;
                }
            }
        } else {
            // let's try every section
            numSectionsToTest = drawItemsArray.length / 2;
        }
        return numSectionsToTest;
    }

    /**
     * Find sections which should be avoided because they contain player from same geography
     *
     * @param playerDrawInfo
     * @param drawItemsArray
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    private Map<Integer, Integer> sectionsToAvoid(PlayerDrawInfo playerDrawInfo, DrawItem[] drawItemsArray, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        // calculate number of sections to test if there is already a player from that same club or city/state
        int numSectionsToTest = calculateNumSectionsForTesting(playerDrawInfo, drawItemsArray);
        Map<Integer, Integer> sectionsToAvoid = new HashMap<>();

        int sectionSize = drawItemsArray.length / numSectionsToTest;
//        System.out.println("numSectionsToTest = " + numSectionsToTest);
        // now test which sections should be avoided since they have players from this geography
        for (int i = 0; i < numSectionsToTest; i++) {
            int sectionStart = i * sectionSize;
            int sectionEnd = sectionStart + sectionSize - 1;
//            System.out.println("Start, End = " + sectionStart + ", " + sectionEnd);
            for (int j = sectionStart; j <= sectionEnd; j++) {
                DrawItem drawItem = drawItemsArray[j];
                if (drawItem != null) {
                    long entryId = drawItem.getEntryId();
                    PlayerDrawInfo placedPlayerDrawInfo = entryIdToPlayerDrawInfo.get(entryId);
                    GeographicalDistanceCalculator calculator = new GeographicalDistanceCalculator();
                    int distance = calculator.getDistance(placedPlayerDrawInfo, playerDrawInfo);
//                    System.out.println(placedPlayerDrawInfo.getPlayerName() + " state " + placedPlayerDrawInfo.getState() + " dist " + distance);
                    // same club = 1, same city/state 2 or 3
                    if (distance <= 3) {
                        sectionsToAvoid.put(sectionStart, sectionEnd);
                        break;
                    }
                }
            }
        }
//        System.out.println("Sections to avoid -- Begin");
//        for (Map.Entry<Integer, Integer> range : sectionsToAvoid.entrySet()) {
//            int sectionStart = range.getKey();
//            int sectionEnd = range.getValue();
//            System.out.println("Start, End = " + sectionStart + ", " + sectionEnd);
//        }
//        System.out.println("Sections to avoid -- End");
        return sectionsToAvoid;
    }

}
