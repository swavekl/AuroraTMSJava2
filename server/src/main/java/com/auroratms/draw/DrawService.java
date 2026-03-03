package com.auroratms.draw;

import com.auroratms.draw.conflicts.ConflictFinder;
import com.auroratms.draw.generation.*;
import com.auroratms.draw.notification.DrawsEventPublisher;
import com.auroratms.draw.notification.event.DrawAction;
import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.event.*;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.doubles.DoublesPair;
import com.auroratms.tournamentevententry.doubles.DoublesService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for draws functions
 */
@Service
@Transactional
@Slf4j
public class DrawService {

    private DrawRepository drawRepository;

    private DoublesService doublesService;

    private DrawsEventPublisher drawsEventPublisher;

    private UsattDataService usattDataService;

    private UserProfileExtService userProfileExtService;

    public DrawService(DrawRepository drawRepository,
                       DoublesService doublesService,
                       DrawsEventPublisher drawsEventPublisher,
                       UsattDataService usattDataService,
                       UserProfileExtService userProfileExtService) {
        this.drawRepository = drawRepository;
        this.doublesService = doublesService;
        this.drawsEventPublisher = drawsEventPublisher;
        this.usattDataService = usattDataService;
        this.userProfileExtService = userProfileExtService;
    }

    /**
     *
     * @param tournamentEvent
     * @param drawType
     * @param eventEntries
     * @param existingDrawItems
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    public List<DrawItem> generateDraws(TournamentEvent tournamentEvent,
                                        DrawType drawType,
                                        List<TournamentEventEntry> eventEntries,
                                        List<DrawItem> existingDrawItems,
                                        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                        TournamentEventRound round,
                                        TournamentEventRoundDivision division) {
        List<DrawItem> drawItemList = Collections.emptyList();
        try {
            IDrawsGenerator generator = DrawGeneratorFactory.makeGenerator(tournamentEvent, round, division);
            if (generator != null) {
                if (tournamentEvent.isDoubles()) {
                    // pass doubles pairs to generator
                    List<DoublesPair> doublesPairsForEvent = this.doublesService.findDoublesPairsForEvent(tournamentEvent.getId());
                    if (generator instanceof DoublesSnakeDrawsGenerator drawsGenerator) {
                        drawsGenerator.setDoublesPairs(doublesPairsForEvent);
                    } else if (generator instanceof DoublesSingleEliminationDrawsGenerator drawsGenerator) {
                        drawsGenerator.setDoublesPairs(doublesPairsForEvent);
                    }
                }
                drawItemList = generator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDrawItems);

                if (!tournamentEvent.isDoubles()) {
                    // find the conflicts and record them in draw items
                    ConflictFinder conflictFinder = new ConflictFinder(drawType,
                            entryIdToPlayerDrawInfo, existingDrawItems, tournamentEvent);
                    conflictFinder.identifyConflicts(drawItemList);
                }
            }
            // save the list
            if (!drawItemList.isEmpty()) {
                this.drawRepository.saveAll(drawItemList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        this.drawsEventPublisher.publishEvent(tournamentEvent.getId(), DrawAction.GENERATED, drawType, null,
                round.getOrdinalNum(), division.getDivisionIdx());

        return drawItemList;
    }

    /**
     * List all draw items for one event and draw type
     * @param eventId
     * @param drawType
     * @return
     */
    @Transactional(readOnly = true)
    public List<DrawItem> list(long eventId, DrawType drawType) {
        if (drawType.equals(DrawType.ROUND_ROBIN)) {
            return this.drawRepository.findAllByEventFkAndDrawTypeOrderByGroupNumAscPlaceInGroupAsc(eventId, drawType);
        } else {
            return this.drawRepository.findAllByEventFkAndDrawTypeOrderByRoundDescSingleElimLineNumAsc(eventId, drawType);
        }
    }

    /**
     * List all draw items for one event and draw type
     * @param eventId
     * @param drawType
     * @return
     */
    @Transactional(readOnly = true)
    public List<DrawItem> listForRoundAndDivision(long eventId, DrawType drawType, int roundOrdinalNum, int divisionIdx) {
        if (drawType.equals(DrawType.ROUND_ROBIN)) {
            return this.drawRepository.findAllByEventFkAndDrawTypeAndRoundOrdinalNumberAndDivisionIdxOrderByGroupNumAscPlaceInGroupAsc(eventId, drawType, roundOrdinalNum, divisionIdx);
        } else {
            return this.drawRepository.findAllByEventFkAndDrawTypeAndRoundOrdinalNumberAndDivisionIdxOrderByRoundDescSingleElimLineNumAsc(eventId, drawType, roundOrdinalNum, divisionIdx);
        }
    }

    @Transactional(readOnly = true)
    public List<DrawItem> listAllRoundsForEvent(long eventId) {
        return this.drawRepository.findAllByEventFkOrderByRoundOrdinalNumberAscDivisionIdxAscGroupNumAscPlaceInGroupAsc(eventId);
    }

    @Transactional(readOnly = true)
    public boolean existsByEventFkAndDrawTypeAndRoundAndSingleElimLineNum(long eventId, DrawType drawType, int roundOf, int singleElimLineNum) {
        return this.drawRepository.existsByEventFkAndDrawTypeAndRoundAndSingleElimLineNum(eventId, drawType, roundOf, singleElimLineNum);
    }

    @Transactional(readOnly = true)
    public DrawItem findByEventFkAndDrawTypeAndRoundAndSingleElimLineNum(long eventId, DrawType drawType, int roundOf, int singleElimLineNum) {
        return this.drawRepository.findByEventFkAndDrawTypeAndRoundAndSingleElimLineNum(eventId, drawType, roundOf, singleElimLineNum)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find draw item"));
    }

    @Transactional(readOnly = true)
    public DrawItem get(long drawItemId) {
        return this.drawRepository.findById(drawItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find draw item"));
    }

    @Transactional(readOnly = true)
    public List<DrawItem> listByProfileIdAndEventFkIn(String profileId, List<Long> eventIdList) {
        return this.drawRepository.findAllByPlayerIdContainsAndEventFkIsIn(profileId, eventIdList);
    }

    /**
     *
     * @param eventIds
     * @return
     */
    @Transactional(readOnly = true)
    public List<DrawItem> listAllDrawsForTournament(List<Long> eventIds) {
        return this.drawRepository.findAllByEventFkInOrderByEventFkAscGroupNumAscPlaceInGroupAsc(eventIds);
    }

    /**
     * Saves newly created item
     * @param drawItem
     * @return
     */
    public DrawItem save(DrawItem drawItem) {
        DrawItem savedItem = this.drawRepository.save(drawItem);
        List<DrawItem> updatedDrawItems = Arrays.asList(drawItem);
        this.drawsEventPublisher.publishEvent(savedItem.getEventFk(), DrawAction.UPDATED, savedItem.getDrawType(), updatedDrawItems,
                savedItem.getRoundOrdinalNumber(), savedItem.getDivisionIdx());
        return savedItem;
    }

    /**
     *
     * @param drawItems
     */
    public void updateDraws(List<DrawItem> drawItems) {
        if (drawItems.size() > 0) {
            this.drawRepository.saveAll(drawItems);
            DrawItem drawItem = drawItems.get(0);
            this.drawsEventPublisher.publishEvent(drawItem.getEventFk(), DrawAction.UPDATED, drawItem.getDrawType(), drawItems,
                    drawItem.getRoundOrdinalNumber(), drawItem.getDivisionIdx());
        }
    }

    /**
     *
     * @param eventId
     * @param drawType
     */
    public void deleteDraws(long eventId, DrawType drawType) {
        this.drawRepository.deleteAllByEventFkAndDrawType(eventId, drawType);
        this.drawsEventPublisher.publishEvent(eventId, DrawAction.DELETED, drawType, null, 1, 0);
    }

    /**
     * Advances players from a match card which was just completed
     *
     * @param matchDrawType             current round draw type RR or SE
     * @param matchGroupNum             match group number which was completed
     * @param matchRound                match round number which was completed 0 for single RR, 8, 4, 2 etc for SE
     * @param tournamentEvent           tournament event for which players are being advanced
     * @param rankToProfileIdMap        map of ranking withing a group to profile id 1 -> abc4578273, 2 -> 445783fg59 etc.
     * @param playerProfileToRatingMap  map of player profile to his USATT rating e.g. abc4578273 -> 1755, 445783fg59 -> 1712
     * @param currentRoundOrdinalNumber current round ordinal number
     * @param currentRoundDivisionIdx   current round division index
     */
    public void advancePlayers(DrawType matchDrawType,
                               int matchGroupNum,
                               int matchRound,
                               TournamentEvent tournamentEvent,
                               Map<Integer, String> rankToProfileIdMap,
                               Map<String, Integer> playerProfileToRatingMap,
                               int currentRoundOrdinalNumber,
                               int currentRoundDivisionIdx) {
        // determine the next rounds and divisions, if any, which will take players from this round and division
        TournamentRoundsConfiguration roundsConfiguration = tournamentEvent.getRoundsConfiguration();
        if (roundsConfiguration != null) {
            List<TournamentEventRound> rounds = roundsConfiguration.getRounds();
            int finalRoundOrdinalNum = rounds.size();
            log.info("========================================================================");
            log.info("Advancing players from matchGroupNum in event '" + tournamentEvent.getName() + "'");
            log.info("from results in group " + matchGroupNum + " round of " + matchRound + " in tournament round # " + currentRoundOrdinalNumber + " division " + currentRoundDivisionIdx);
            log.info("========================================================================");

            // create a map of 'round/division' to a map of player rank to player profile ids
            // put the real ranking in this map first
            // each processed round will add to this map and we will keep adding and processing
            // it until there are no more rounds to process
            // this will simulate having real results and let us update later round results
            // so we don't have players who lost still visible in the later rounds
            Map<String, Map<Integer, String>> roundDivToRankItProfileMaps = new HashMap<>();
            String key = currentRoundOrdinalNumber + "/" + currentRoundDivisionIdx + "/" + matchGroupNum;
            log.info("Saving real map of rankings for key " + key + " with " + rankToProfileIdMap.size() + " entries");
            roundDivToRankItProfileMaps.put(key, rankToProfileIdMap);

            // prepare a map of round/div/group rankings so we can detect any shifts in ranking
            Map<String, Map<Integer, String>> currentMapsOfPlayerRankings = createCurrentMapsOfPlayerRankings(
                    tournamentEvent.getId(), rounds, currentRoundOrdinalNumber, currentRoundDivisionIdx);

            // collect all updated draw itesm and delay their update until the end
            List<DrawItem>  drawItemsToUpdate = new ArrayList<>();
            // for all later rounds, pull players from the currently finished round into next round
            while (!roundDivToRankItProfileMaps.isEmpty()) {

                // Snapshot keys so we can safely mutate the backing map (put/remove) during processing
                List<String> keysToProcess = new ArrayList<>(roundDivToRankItProfileMaps.keySet());

                for (String processedRoundsKey : keysToProcess) {
                    Map<Integer, String> priorRoundRankToProfileIdMap =
                            roundDivToRankItProfileMaps.get(processedRoundsKey);

                    // Mark this key as processed no matter what (prevents infinite loops on empty maps)
                    roundDivToRankItProfileMaps.remove(processedRoundsKey);

                    if (priorRoundRankToProfileIdMap == null || priorRoundRankToProfileIdMap.isEmpty()) {
                        continue;
                    }

                    log.info("Got prior round rank to profile id map for round " + processedRoundsKey
                            + " with " + priorRoundRankToProfileIdMap.size() + " entries");

                    String[] keyParts = processedRoundsKey.split("/");
                    int sourceRoundOrdinalNumber = Integer.parseInt(keyParts[0]);
                    int sourceDivisionIdx = Integer.parseInt(keyParts[1]);
                    int sourceGroupNum = Integer.parseInt(keyParts[2]);

                    TournamentEventRound nextRound = rounds.stream()
                            .filter(round -> round.getOrdinalNum() == (sourceRoundOrdinalNumber + 1))
                            .findFirst()
                            .orElse(null);

                    // If we are advancing to later rounds of the last round e.g. from round of 16 to round of 8 use the current round
                    if (nextRound == null) {
                        nextRound = rounds.stream()
                                .filter(round -> round.getOrdinalNum() == sourceRoundOrdinalNumber)
                                .findFirst()
                                .orElse(null);
                    }

                    if (nextRound == null) {
                        continue;
                    }

                    int nextRoundOrdinalNum = nextRound.getOrdinalNum();
                    DrawType nextRoundDrawType = nextRound.isSingleElimination()
                            ? DrawType.SINGLE_ELIMINATION
                            : DrawType.ROUND_ROBIN;

                    log.info("========================================================================");
                    log.info("Processing next round '" + nextRound.getRoundName() + "' # " + nextRoundOrdinalNum + " of type " + nextRoundDrawType);

                    for (TournamentEventRoundDivision division : nextRound.getDivisions()) {
                        log.info("============================  Processing division '" + division.getDivisionName() + "' # " + division.getDivisionIdx() + " =================");

//                        Map<Integer, String> originalRoundRankToProfileMap = currentMapsOfPlayerRankings.get(key);
//                        Map<String, String> replacementMap = createReplacementMap(originalRoundRankToProfileMap, priorRoundRankToProfileIdMap);
//                        if (replacementMap.isEmpty()) {
//                            continue;
//                        }
                        boolean isFinalRound = finalRoundOrdinalNum == nextRoundOrdinalNum;
                        List<DrawItem> divisionDrawItemListToUpdate = processDivision(
                                sourceGroupNum, matchRound, tournamentEvent, playerProfileToRatingMap,
                                nextRoundOrdinalNum, division, sourceRoundOrdinalNumber, sourceDivisionIdx,
                                priorRoundRankToProfileIdMap, nextRoundDrawType, roundDivToRankItProfileMaps,
                                isFinalRound);

                        drawItemsToUpdate.addAll(divisionDrawItemListToUpdate);

                        log.info("roundDivToRankItProfileMaps keys " + roundDivToRankItProfileMaps.keySet() + " size " + roundDivToRankItProfileMaps.size());

                        // only process changes and not beyond final round
//                        if (!thisDivisionRankToProfileIdMap.isEmpty() && finalRoundOrdinalNum != nextRoundOrdinalNum) {
//                            key = nextRoundOrdinalNum + "/" + division.getDivisionIdx();
//                            log.info("Saving generated map of rankings for key " + key
//                                    + " with " + thisDivisionRankToProfileIdMap.size() + " entries");
//                            roundDivToRankItProfileMaps.put(key, thisDivisionRankToProfileIdMap);
//                        }
                    }
                }
            }

            if (!drawItemsToUpdate.isEmpty()) {
                log.info("Updating " + drawItemsToUpdate.size() + " draw items for event " + tournamentEvent.getName());
//                this.drawRepository.saveAll(drawItemsToUpdate);
                this.updateDraws(drawItemsToUpdate);
            }

            log.info("Finished advancing players");
        }
    }


    /**
     * Creates a mapping of player rankings for the current and subsequent rounds of a tournament event.
     * The method generates a map that associates round, division, and group information with the rankings of players
     * in those groups prior to any changes occurring in the current round.
     *
     * @param eventFk                  The ID of the tournament event.
     * @param rounds                   The list of all rounds associated with the tournament event.
     * @param currentRoundOrdinalNumber The ordinal number of the current round where changes are taking place.
     * @param currentRoundDivisionIdx  The index of the division within the current round where changes are taking place.
     * @return A map where the key is a concatenation of round number, division index, and group number, and the value
     *         is a nested map mapping the ranking position within the group to the player's profile ID.
     */
    private Map<String, Map<Integer, String>> createCurrentMapsOfPlayerRankings(long eventFk,
                                                                                List<TournamentEventRound> rounds,
                                                                                int currentRoundOrdinalNumber,
                                                                                int currentRoundDivisionIdx) {
        log.info("preparing group rankings before the advancing takes place for event " + eventFk + " current round ordinal number " + currentRoundOrdinalNumber + " current round division idx " + currentRoundDivisionIdx + "");
        Map<String, Map<Integer, String>> roundDivGroupToRankingMap = new HashMap<>();
        for (TournamentEventRound round : rounds) {
            int roundOrdinalNum = round.getOrdinalNum();
            // skip prior rounds to the round in which the change occurred
            if (roundOrdinalNum < currentRoundOrdinalNumber) {
                continue;
            }

            List<TournamentEventRoundDivision> divisions = round.getDivisions();
            DrawType drawType = round.isSingleElimination()
                    ? DrawType.SINGLE_ELIMINATION
                    : DrawType.ROUND_ROBIN;

            for (TournamentEventRoundDivision division : divisions) {
                // only consider divisions of the current round which changed and
                // any division in the following rounds
                if ((roundOrdinalNum == currentRoundOrdinalNumber && division.getDivisionIdx() == currentRoundDivisionIdx) ||
                     roundOrdinalNum > currentRoundOrdinalNumber) {

                    log.info("Preparing original rankings for round " + roundOrdinalNum + " division " + division.getDivisionIdx() + " of type " + drawType);
                    List<DrawItem> roundDrawItems = listForRoundAndDivision(eventFk, drawType, roundOrdinalNum, division.getDivisionIdx());
                    makeRankingMapForAllGroups(division, roundDrawItems, roundOrdinalNum, roundDivGroupToRankingMap);
                }
            }
        }

        return roundDivGroupToRankingMap;
    }

    /**
     *
     * @param division
     * @param roundDrawItems
     * @param roundOrdinalNum
     * @param roundDivGroupToRankingMap
     */
    private void makeRankingMapForAllGroups(TournamentEventRoundDivision division,
                                            List<DrawItem> roundDrawItems,
                                            int roundOrdinalNum,
                                            Map<String, Map<Integer, String>> roundDivGroupToRankingMap) {
        roundDrawItems.sort(Comparator.comparing(DrawItem::getGroupNum)
                .thenComparing(DrawItem::getPlaceInGroup));

        // create 'before' rankings based on draws
        for (DrawItem drawItem : roundDrawItems) {
            String groupKey = roundOrdinalNum + "/" + division.getDivisionIdx() + "/" + drawItem.getGroupNum();
            Map<Integer, String> groupRankToProfileIdMap = roundDivGroupToRankingMap.computeIfAbsent(groupKey, k -> new HashMap<>());
            groupRankToProfileIdMap.put(drawItem.getPlaceInGroup(), drawItem.getPlayerId());
        }
    }

    /**
     * Creates a map which shows the changes between rankings of players.  So if there was an upset and player ranked
     * in second place actually took the 1st place in the group we will have replacement.
     * oldPlayerProfileA -> newPlayerProfileB and oldPlayerProfileB -> newPlayerProfileA.
     *
     * @param oldRankingMap 1 -> 1st place player profile id 2 -> 2nd place player profile id, etc.
     * @param newRankingMap 1 -> 1st place player profile id 2 -> 2nd place player profile id, etc.
     * @return
     */
    private Map<String, String> createReplacementMap(Map<Integer, String> oldRankingMap, Map<Integer, String> newRankingMap) {
        Map<String, String> replacementMap = new HashMap<>();
        int oldSize = oldRankingMap.size();
        int newSize = newRankingMap.size();
        int ranks = Math.min(oldSize, newSize);
        for (int rank = 1; rank <= ranks; rank++) {
            String oldPlayerProfileId = oldRankingMap.get(rank);
            String newPlayerProfileId = newRankingMap.get(rank);
            if (!oldPlayerProfileId.equals(newPlayerProfileId)) {
                log.info("Found player replacement in rank: " + rank + ", " + oldPlayerProfileId + " -> " + newPlayerProfileId);
                replacementMap.put(oldPlayerProfileId, newPlayerProfileId);
            }
        }

        return replacementMap;
    }

    /**
     * Processes the division for advancing players in a tournament across rounds or divisions.
     * This method handles both within-round transformations (e.g., single elimination match progression)
     * and cross-round progressions (e.g., round-robin to single elimination).
     *
     * @param matchGroupNum                The group number in the match being processed.
     * @param matchRound                   The round number in the match being processed.
     * @param tournamentEvent              The tournament event object containing details about the overall event.
     * @param playerProfileToRatingMap     A map linking player profile IDs to their respective ratings.
     * @param nextRoundOrdinalNum          The ordinal number of the next round in the tournament.
     * @param division                     The current division being processed within the tournament's round.
     * @param sourceRoundOrdinalNumber     The ordinal number of the source round.
     * @param sourceDivisionIdx            The index of the source division from which players are advancing.
     * @param priorRoundRankToProfileIdMap A map linking prior round ranks (e.g., 1, 2, 3) to player profile IDs.
     * @param nextRoundDrawType            The type of draw for the next round (e.g., single elimination, round-robin).
     * @param roundDivGroupToRankingMap    A map that links round-division-group combinations to their rankings.
     * @param isFinalRound
     * @return A list of draw items that need to be updated to reflect the players advancing to the next round or division.
     */
    private List<DrawItem> processDivision(int matchGroupNum, int matchRound,
                                           TournamentEvent tournamentEvent,
                                           Map<String, Integer> playerProfileToRatingMap,
                                           int nextRoundOrdinalNum,
                                           TournamentEventRoundDivision division,
                                           int sourceRoundOrdinalNumber,
                                           int sourceDivisionIdx,
                                           Map<Integer, String> priorRoundRankToProfileIdMap,
                                           DrawType nextRoundDrawType,
                                           Map<String, Map<Integer, String>> roundDivGroupToRankingMap,
                                           boolean isFinalRound) {
        List<DrawItem> drawItemsToBeUpdated = new ArrayList<>();
        // within the same round - i.e. later founds of the single elimination round
        if (nextRoundOrdinalNum == sourceRoundOrdinalNumber) {
            // next round has half the players
            int roundOf = matchRound / 2;
            log.info("Advancing winning player within the same round " + nextRoundOrdinalNum + " division " + division.getDivisionIdx() + " of Single Elimination round from match round of " + matchRound + " to round of " + roundOf + " ================");
            // later round - i.e. draw item may or may not exist for it - if score is corrected it will exist
            // if the later rounds (you lose you are out) so we only need one player
            String playerProfileId = priorRoundRankToProfileIdMap.get(1);
            if (playerProfileId != null) {
                int rating = playerProfileToRatingMap.get(playerProfileId);
                // group 1 & 2 winners, go to this round's group 1, group 3 & 4 winners into group 2, 5 & 6 to 3 etc.
                int groupNum = (int) Math.ceil((double) matchGroupNum / 2);
                // there are only two places in this group - 1 or 2
                int placeInGroup = (matchGroupNum % 2 == 1) ? 1 : 2;
                int singleElimLineNumber = ((groupNum - 1) * 2) + placeInGroup;
                updateDrawItem(tournamentEvent.getId(), roundOf, playerProfileId, rating, groupNum, placeInGroup, singleElimLineNumber, nextRoundOrdinalNum, division.getDivisionIdx());
            }

            // last round but if we play for 3rd and 4th place update draw item for that as well
            // with the player who lost
            boolean play3rd4thPlace = division.isPlay3rd4thPlace();
            if (play3rd4thPlace && roundOf == 2) {
                String losingPlayerProfileId = priorRoundRankToProfileIdMap.get(2);
                // make sure this player can play
                int rating = (losingPlayerProfileId != null) ? playerProfileToRatingMap.get(losingPlayerProfileId) : 0;
                // 3 & 4th place match is group 2 in round of 2
                int groupNum = 2;
                // there are only two places in this group
                int placeInGroup = (matchGroupNum % 2 == 1) ? 1 : 2;
                int singleElimLineNumber = ((groupNum - 1) * 2) + placeInGroup; // i.e. 3 or 4
                log.info("Advancing loosing player to 3 & 4th place match in the same round " + nextRoundOrdinalNum + " division " +
                        division.getDivisionIdx() + " of Single Elimination round from match round of " + matchRound + " to round of " + roundOf + " placeInGroup " + placeInGroup);
                updateDrawItem(tournamentEvent.getId(), roundOf, losingPlayerProfileId, rating, groupNum, placeInGroup, singleElimLineNumber, nextRoundOrdinalNum, division.getDivisionIdx());
            }
        } else {
            // advancing from an earlier round RR -> RR or RR -> SE
            // if this division pulls from the current division, then we need to advance players
            if (division.getPreviousDivisionIdx() == sourceDivisionIdx) {
                log.info("===================== Advancing players from round " + sourceRoundOrdinalNumber + " division " + sourceDivisionIdx + " to round " + nextRoundOrdinalNum + " division " + division.getDivisionIdx() + " ================");
                List<DrawItem> nextRoundDrawItems = listForRoundAndDivision(tournamentEvent.getId(), nextRoundDrawType, nextRoundOrdinalNum, division.getDivisionIdx());
                log.info("Found " + nextRoundDrawItems.size() + " next round draw items for round " + nextRoundOrdinalNum + " division " + division.getDivisionIdx());
                nextRoundDrawItems.forEach(drawItem -> {
                    String msg = (nextRoundDrawType == DrawType.ROUND_ROBIN)
                            ? (", group " + drawItem.getGroupNum() + ", place " + drawItem.getPlaceInGroup())
                            : (", getSingleElimLineNum " + drawItem.getSingleElimLineNum());
                    log.info("Player profile id: " + drawItem.getPlayerId() + msg);
                });

                // determine how many players to advance
                int numPlayersToPull = division.getPreviousRoundPlayerRankingEnd() - division.getPreviousRoundPlayerRanking() + 1;
                log.info("Pulling/Advancing " + numPlayersToPull + " players ranked between " + division.getPreviousRoundPlayerRanking() + " and " + division.getPreviousRoundPlayerRankingEnd());
                int placeRankNum = division.getPreviousRoundPlayerRanking();
                boolean advanceUnratedPlayer = division.isAdvanceUnratedWinner();
                boolean doubles = tournamentEvent.isDoubles();

                // find draw items which need to be updated
                // search by player profile id from the map of replaced plaeyrs
                for (int i = 1; i <= numPlayersToPull; i++) {
                    // we may have to skip an unrated player so adjust placeRankNum
                    placeRankNum = getRankOfPlayerToAdvance(placeRankNum, priorRoundRankToProfileIdMap, advanceUnratedPlayer, doubles);
                    log.info("Searching for drawItem in placeRankNum " + placeRankNum);

                    // get player profile of the player in this rank
                    // check if this player rank changed and needs to be replaced because of an upset or stays the same
                    String advancingPlayerId = priorRoundRankToProfileIdMap.get(placeRankNum);
                    log.info("Advancing player with profile id " + advancingPlayerId);

                    // is this player in this round/division
                    int previousRank = nextRoundDrawItems.stream()
                            .filter(drawItem -> drawItem.getPlayerId().equals(advancingPlayerId))
                            .findFirst()
                            .map(DrawItem::getPlaceInGroup)
                            .orElse(0);
                    log.info("Previous rank is " + previousRank + " current rank is " + placeRankNum);
                    // if ranking has changed we need to replace the player
                    if (placeRankNum != previousRank) {
                        // find the player who is in this group in the next round
                        // get ids of players other than the advancing player from the ranking map
                        // if player ranked #2 is now player ranked #1, gets players 1, 3 & 4
                        List<String> otherProfileIds = priorRoundRankToProfileIdMap.values().stream()
                                .filter(profileId -> !profileId.equals(advancingPlayerId))
                                .toList();
                        log.info("Num other players in the ranking map: " + otherProfileIds.size());
                        // get draw items for players other than the player with the specified rank
                        List<DrawItem> playerToBeReplaced = nextRoundDrawItems.stream()
                                .filter(drawItem -> otherProfileIds.contains(drawItem.getPlayerId()))
                                .toList();
                        // there should be only one player in the next round who was advancing to this round
                        log.info("Found " + playerToBeReplaced.size() + " draw items for player other than the player with rank " + placeRankNum);
                        if (playerToBeReplaced.size() == 1) {
                            DrawItem drawItemToBeReplaced = playerToBeReplaced.get(0);
                            Integer advancingPlayerRating = playerProfileToRatingMap.get(advancingPlayerId);
                            if (advancingPlayerRating == null) {
                                log.info("Advancing player rating is null, getting it from usatt");
                                UserProfileExt userProfileExt = userProfileExtService.getByProfileId(advancingPlayerId);
                                Long membershipId = userProfileExt.getMembershipId();
                                UsattPlayerRecord usattPlayerRecord = usattDataService.getPlayerByMembershipId(membershipId);
                                advancingPlayerRating = usattPlayerRecord.getTournamentRating();
                                log.info("Advancing player rating is " + advancingPlayerRating);
                            }
                            log.info("Updating draw item for player with profile id " + drawItemToBeReplaced.getPlayerId() + " rating " + drawItemToBeReplaced.getRating());
                            log.info("by replacing it by player with profile id     " + advancingPlayerId + " rating " + advancingPlayerRating);
                            drawItemToBeReplaced.setPlayerId(advancingPlayerId);
                            drawItemToBeReplaced.setRating(advancingPlayerRating);
                            drawItemsToBeUpdated.add(drawItemToBeReplaced);
                        } else {
                            log.warn("Found " + playerToBeReplaced.size() + " draw items for players other than the player with rank " + placeRankNum);
                        }
                    }
                    placeRankNum++;
                }

                // now that we have draw items to replace
                if (nextRoundDrawType == DrawType.SINGLE_ELIMINATION) {
                    for (DrawItem drawItem : drawItemsToBeUpdated) {
                        int firstSeRoundOf = nextRoundDrawItems.stream()
                                .mapToInt(DrawItem::getRound)
                                .max()
                                .orElse(1);
                        log.info("Checking if player has a bye for rounds after first round of " + firstSeRoundOf);
                        DrawItem drawItemWithBye = advanceToSecondRoundIfBye(drawItem, nextRoundDrawItems, firstSeRoundOf);
                        if (drawItemWithBye != null) {
                            log.info("Updating draw in the next round for player who got a bye in round " + drawItemWithBye.getRound());
                            drawItemWithBye.setPlayerId(drawItem.getPlayerId());
                            drawItemWithBye.setRating(drawItem.getRating());
                            drawItemsToBeUpdated.add(drawItemWithBye);
                        } else {
                            log.info("No drawItem in next round for player - probably did not get a bye");
                        }
                    }
                }

                if (!isFinalRound && !drawItemsToBeUpdated.isEmpty()) {
                    // make a map of new updated rankings and save it for when we process later rounds
                    log.info("Updating a map of rankings for next round " + nextRoundOrdinalNum + " division " + division.getDivisionIdx() + " all groups ");
                    makeRankingMapForAllGroups(division, nextRoundDrawItems, nextRoundOrdinalNum, roundDivGroupToRankingMap);
                }
            }
        }
        return drawItemsToBeUpdated;
    }

    /**
     * Get the rank of the player to advance, skipping unrated players if event is configured not to advance them
     *
     * @param placeRankNum
     * @param rankToProfileIdMap
     * @param advanceUnratedPlayer
     * @param doubles
     * @return
     */
    private int getRankOfPlayerToAdvance(int placeRankNum, Map<Integer, String> rankToProfileIdMap, boolean advanceUnratedPlayer, boolean doubles) {
        if (!advanceUnratedPlayer && !doubles) {
            boolean isUnrated = false;
            do {
                // find the first 'rated' player starting with this rank
                String playerProfileId = rankToProfileIdMap.get(placeRankNum);
                isUnrated = isPlayerUnrated(playerProfileId);
                if (isUnrated) {
                    // skip to the next player if this one is unrated
                    placeRankNum++;
                }
            } while (isUnrated && placeRankNum < rankToProfileIdMap.size());
        }
        return placeRankNum;
    }

    /**
     * Finds out if the player is unrated
     * @param playerProfileId
     * @return
     */
    private boolean isPlayerUnrated(String playerProfileId) {
        UserProfileExt userProfileExt = userProfileExtService.getByProfileId(playerProfileId);
        Long membershipId = userProfileExt.getMembershipId();
        UsattPlayerRecord usattPlayerRecord = usattDataService.getPlayerByMembershipId(membershipId);
        Date lastTournamentPlayedDate = usattPlayerRecord.getLastTournamentPlayedDate();
        int tournamentRating = usattPlayerRecord.getTournamentRating();

        return (lastTournamentPlayedDate == null || tournamentRating == 0);
    }

    /**
     * Finds the next round draw item if this player gets a bye in this round
     * @param drawItem
     * @param seDrawItems
     * @param firstSeRoundOf
     * @return
     */
    private DrawItem advanceToSecondRoundIfBye(DrawItem drawItem, List<DrawItem> seDrawItems, int firstSeRoundOf) {
        DrawItem nextRoundDrawItem = null;
        // does this player get a bye in this round to which he just advanced
        // find which group this player is in this round
        int thisPlayerSingleElimLineNum = drawItem.getSingleElimLineNum();
        int byeLineNum = ((thisPlayerSingleElimLineNum % 2) == 1) ? thisPlayerSingleElimLineNum + 1 : thisPlayerSingleElimLineNum - 1;
        boolean getsBye = false;
        for (DrawItem seDrawItem : seDrawItems) {
            if (seDrawItem.getRound() == firstSeRoundOf &&
                seDrawItem.getSingleElimLineNum() == byeLineNum) {
                getsBye = seDrawItem.getByeNum() > 0;
                break;
            }
        }

        // find the next round and modify the draw item for this player
        if (getsBye) {
            int nextRound = firstSeRoundOf / 2;
            // find which group this player is in this round
            // group 1 & 2 winners, go to this round's group 1, group 3 & 4 winners into group 2, 5 & 6 to 3 etc.
            int thisGroupNum = (int) Math.ceil((double) drawItem.getSingleElimLineNum() / 2);
            int nextRoundGroupNum = (int) Math.ceil((double) thisGroupNum / 2);
            int placeInGroup = (thisGroupNum % 2 == 1) ? 1 : 2;
            for (DrawItem seDrawItem : seDrawItems) {
                if (seDrawItem.getRound() == nextRound) {
                    if (seDrawItem.getGroupNum() == nextRoundGroupNum &&
                        seDrawItem.getPlaceInGroup() == placeInGroup) {
                        nextRoundDrawItem = seDrawItem;
                        break;
                    }
                }
            }
        }
        return nextRoundDrawItem;
    }

    /**
     * Updates draw item
     *
     * @param eventFk
     * @param roundOf
     * @param playerProfileId
     * @param rating
     * @param groupNum
     * @param placeInGroup
     * @param singleElimLineNumber
     * @param roundOrdinalNum
     * @param divisionIdx
     */
    private void updateDrawItem(long eventFk, int roundOf, String playerProfileId, int rating, int groupNum, int placeInGroup, int singleElimLineNumber, int roundOrdinalNum, int divisionIdx) {
        DrawItem drawItem = new DrawItem();
        drawItem.setRound(roundOf);
        drawItem.setDrawType(DrawType.SINGLE_ELIMINATION);
        drawItem.setGroupNum(groupNum);
        drawItem.setPlaceInGroup(placeInGroup);
        drawItem.setEventFk(eventFk);
        drawItem.setSingleElimLineNum(singleElimLineNumber);
        if (this.existsByEventFkAndDrawTypeAndRoundAndSingleElimLineNum(eventFk, DrawType.SINGLE_ELIMINATION, roundOf, singleElimLineNumber)) {
            drawItem = this.findByEventFkAndDrawTypeAndRoundAndSingleElimLineNum(eventFk, DrawType.SINGLE_ELIMINATION, roundOf, singleElimLineNumber);
        }
        drawItem.setRoundOrdinalNumber(roundOrdinalNum);
        drawItem.setDivisionIdx(divisionIdx);
        drawItem.setPlayerId(playerProfileId);
        drawItem.setRating(rating);
        this.save(drawItem);
    }

    public void deleteDrawItem(DrawItem drawItem) {
        this.drawRepository.delete(drawItem);
//        this.drawsEventPublisher.publishEvent(drawItem.getEventFk(), DrawAction.DELETED, drawItem.getDrawType(), null);
    }
}
