package com.auroratms.draw;

import com.auroratms.draw.generation.*;
import com.auroratms.draw.notification.DrawsEventPublisher;
import com.auroratms.draw.notification.event.DrawAction;
import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.doubles.DoublesPair;
import com.auroratms.tournamentevententry.doubles.DoublesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service for draws functions
 */
@Service
@Transactional
public class DrawService {

    private DrawRepository drawRepository;

    private DoublesService doublesService;

    private DrawsEventPublisher drawsEventPublisher;

    public DrawService(DrawRepository drawRepository,
                       DoublesService doublesService,
                       DrawsEventPublisher drawsEventPublisher) {
        this.drawRepository = drawRepository;
        this.doublesService = doublesService;
        this.drawsEventPublisher = drawsEventPublisher;
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
                                        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        List<DrawItem> drawItemList = Collections.emptyList();
        try {
            IDrawsGenerator generator = DrawGeneratorFactory.makeGenerator(tournamentEvent, drawType);
            if (generator != null) {
                if (tournamentEvent.isDoubles()) {
                    // pass doubles pairs to generator
                    List<DoublesPair> doublesPairsForEvent = this.doublesService.findDoublesPairsForEvent(tournamentEvent.getId());
                    if (generator instanceof DoublesSnakeDrawsGenerator) {
                        ((DoublesSnakeDrawsGenerator)generator).setDoublesPairs(doublesPairsForEvent);
                    } else if (generator instanceof DoublesSingleEliminationDrawsGenerator) {
                        ((DoublesSingleEliminationDrawsGenerator)generator).setDoublesPairs(doublesPairsForEvent);
                    }
                }
                drawItemList = generator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
            }
            // save the list
            if (drawItemList.size() > 0) {
                this.drawRepository.saveAll(drawItemList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        this.drawsEventPublisher.publishEvent(tournamentEvent.getId(), DrawAction.GENERATED, drawType, null);

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
        this.drawsEventPublisher.publishEvent(savedItem.getEventFk(), DrawAction.UPDATED, savedItem.getDrawType(), updatedDrawItems);
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
            this.drawsEventPublisher.publishEvent(drawItem.getEventFk(), DrawAction.UPDATED, drawItem.getDrawType(), drawItems);
        }
    }

    /**
     *
     * @param eventId
     * @param drawType
     */
    public void deleteDraws(long eventId, DrawType drawType) {
        this.drawRepository.deleteAllByEventFkAndDrawType(eventId, drawType);
        this.drawsEventPublisher.publishEvent(eventId, DrawAction.DELETED, drawType, null);
    }

    /**
     *
     * @param matchDrawType
     * @param matchGroupNum
     * @param matchRound
     * @param tournamentEvent
     * @param rankToProfileIdMap
     * @param playerProfileToRatingMap
     */
    public void advancePlayers(DrawType matchDrawType,
                               int matchGroupNum,
                               int matchRound,
                               TournamentEvent tournamentEvent,
                               Map<Integer, String> rankToProfileIdMap,
                               Map<String, Integer> playerProfileToRatingMap) {
        List<DrawItem> seDrawItems = this.list(tournamentEvent.getId(), DrawType.SINGLE_ELIMINATION);

        if (matchDrawType == DrawType.ROUND_ROBIN) {
            // find the first round of the single elimination round
            int firstSeRoundOf = 1;
            for (DrawItem seDrawItem : seDrawItems) {
                firstSeRoundOf = Math.max(seDrawItem.getRound(), firstSeRoundOf);
            }
            // first round of SE round
            int placeRankNum = 1;
            // update draws
            for (DrawItem drawItem : seDrawItems) {
                if (drawItem.getRound() == firstSeRoundOf &&
                        drawItem.getGroupNum() == matchGroupNum) {
                    String playerProfileId = rankToProfileIdMap.get(placeRankNum);
                    int rating = playerProfileToRatingMap.get(playerProfileId);
                    System.out.println("DrawService advancing player " + playerProfileId + " with rating " + rating + " from round robin round of " + drawItem.getRound() + " group " + drawItem.getGroupNum());
                    // if changed player who advanced then update draws
//                    if (!playerProfileId.equals(drawItem.getPlayerId())) {
                    drawItem.setRating(rating);
                    drawItem.setPlayerId(playerProfileId);
                    // todo - need to pass which draw items changed
                    // otherwise the round matches are erased.
                    System.out.println("Updating drawItem " + drawItem);
                    this.updateDraws(Arrays.asList(drawItem));
//                    }

                    DrawItem nextRoundDrawItem = advanceToSecondRoundIfBye(drawItem, seDrawItems, firstSeRoundOf);
                    if (nextRoundDrawItem != null) {
                        System.out.println("Updating draw in the next round for player who got a bye " + nextRoundDrawItem);
                        nextRoundDrawItem.setPlayerId(playerProfileId);
                        nextRoundDrawItem.setRating(rating);
                        this.updateDraws(Arrays.asList(nextRoundDrawItem));
                    }

                    // todo - where do we place the second and nth player who advances
                    placeRankNum++;
                    if (placeRankNum > tournamentEvent.getPlayersToAdvance()) {
                        break;
                    }
                }
            }
        } else {
            // next round has half the players
            int roundOf = matchRound / 2;
            // later round - i.e. draw item may or may not exist for it - if score is corrected it will exist
            // if the later rounds (you lose you are out) so we only need one player
            String playerProfileId = rankToProfileIdMap.get(1);
            if (playerProfileId != null) {
                int rating = playerProfileToRatingMap.get(playerProfileId);
                // group 1 & 2 winners, go to this round's group 1, group 3 & 4 winners into group 2, 5 & 6 to 3 etc.
                int groupNum = (int) Math.ceil((double) matchGroupNum / 2);
                // there are only two places in this group - 1 or 2
                int placeInGroup = (matchGroupNum % 2 == 1) ? 1 : 2;
                int singleElimLineNumber = ((groupNum - 1) * 2) + placeInGroup;
                updateDrawItem(tournamentEvent.getId(), roundOf, playerProfileId, rating, groupNum, placeInGroup, singleElimLineNumber);
            }

            // last round but if we play for 3rd and 4th place update draw item for that as well
            // with the player who lost
            if (tournamentEvent.isPlay3rd4thPlace() && roundOf == 2) {
                String losingPlayerProfileId = rankToProfileIdMap.get(2);
                // make sure this player can play
                int rating = (losingPlayerProfileId != null) ? playerProfileToRatingMap.get(losingPlayerProfileId) : 0;
                // 3 & 4th place match is group 2 in round of 2
                int groupNum = 2;
                // there are only two places in this group
                int placeInGroup = (matchGroupNum % 2 == 1) ? 1 : 2;
                int singleElimLineNumber = ((groupNum - 1) * 2) + placeInGroup; // i.e. 3 or 4
                updateDrawItem(tournamentEvent.getId(), roundOf, losingPlayerProfileId, rating, groupNum, placeInGroup, singleElimLineNumber);
            }
        }
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
     * @param eventFk
     * @param roundOf
     * @param playerProfileId
     * @param rating
     * @param groupNum
     * @param placeInGroup
     * @param singleElimLineNumber
     */
    private void updateDrawItem(long eventFk, int roundOf, String playerProfileId, int rating, int groupNum, int placeInGroup, int singleElimLineNumber) {
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
        drawItem.setPlayerId(playerProfileId);
        drawItem.setRating(rating);
        this.save(drawItem);
    }
}
