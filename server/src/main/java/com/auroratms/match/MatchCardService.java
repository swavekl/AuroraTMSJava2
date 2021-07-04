package com.auroratms.match;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawService;
import com.auroratms.draw.DrawType;
import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing march cards which contain individual matches
 */
@Service
@Transactional
public class MatchCardService {

    @Autowired
    private MatchCardRepository matchCardRepository;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private DrawService drawService;

    public MatchCardService() {
    }

    /**
     * Generates match cards and matches for all groups in this event
     *
     * @param eventId  event id
     * @param drawType draw type - Round Robin or single elimination
     */
    public void generateMatchCardsForEvent(long eventId, DrawType drawType) {
        List<DrawItem> eventDrawItems = drawService.list(eventId, drawType);
        TournamentEventEntity tournamentEventEntity = tournamentEventEntityService.get(eventId);
        if (drawType == DrawType.ROUND_ROBIN) {
            generateRoundRobinMatchCards(eventDrawItems, tournamentEventEntity);
        } else if (drawType == DrawType.SINGLE_ELIMINATION) {
            generateSingleEliminationCards(eventDrawItems, tournamentEventEntity);
        }
    }

    /**
     * Generates round robin phase match cards
     *
     * @param eventDrawItems
     * @param tournamentEventEntity
     */
    private void generateRoundRobinMatchCards(List<DrawItem> eventDrawItems, TournamentEventEntity tournamentEventEntity) {
        int currentGroupNum = 0;
        // separate group draws into their own lists
        List<DrawItem> groupDrawItems = new ArrayList<>();
        Map<Integer, List<DrawItem>> groupNumDrawItemsMap = new HashMap<>();
        for (DrawItem drawItem : eventDrawItems) {
            if (currentGroupNum != drawItem.getGroupNum()) {
                currentGroupNum = drawItem.getGroupNum();
                groupDrawItems = new ArrayList<>();
                groupNumDrawItemsMap.put(currentGroupNum, groupDrawItems);
            }
            groupDrawItems.add(drawItem);
        }

        /**
         * Generate matches for each group
         */
        int playersToAdvance = tournamentEventEntity.getPlayersToAdvance();

        Map<Character, String> mapPlayerCodeToProfileId = new HashMap<>();
        for (Integer groupNumber : groupNumDrawItemsMap.keySet()) {
            groupDrawItems = groupNumDrawItemsMap.get(groupNumber);
            int playersDrawnIntoGroup = groupDrawItems.size();
            mapPlayerCodeToProfileId.clear();
            // if this group consists only of a seeded player then don't create score sheets
            if (playersDrawnIntoGroup > 1) {
                List<MatchOpponents> matchesInOrder = MatchOrderGenerator.generateOrderOfMatches(playersDrawnIntoGroup, playersToAdvance);

                // collect players information
                for (DrawItem drawItem : groupDrawItems) {
                    int placeInGroup = drawItem.getPlaceInGroup();
                    Character playerCode = (char) ('A' + (placeInGroup - 1));
                    mapPlayerCodeToProfileId.put(playerCode, drawItem.getPlayerId());
                }

                // convert match order into matches
                MatchCard matchCard = new MatchCard();
                matchCard.setEventFk(tournamentEventEntity.getId());
                matchCard.setGroupNum(groupNumber);
                matchCard.setDrawType(DrawType.ROUND_ROBIN);
                matchCard.setNumberOfGames(tournamentEventEntity.getNumberOfGames());
                List<Match> matches = new ArrayList<>();

                int matchNumber = 0;
                for (MatchOpponents matchOpponents : matchesInOrder) {
                    if (matchOpponents.playerA != MatchOrderGenerator.BYE &&
                            matchOpponents.playerB != MatchOrderGenerator.BYE) {
                        String playerAProfileId = mapPlayerCodeToProfileId.get(matchOpponents.playerA);
                        String playerBProfileId = mapPlayerCodeToProfileId.get(matchOpponents.playerB);
                        Match match = new Match();
                        match.setMatchCard(matchCard);
                        match.setMatchNum(++matchNumber);
                        match.setRound(0);
                        match.setPlayerAProfileId(playerAProfileId);
                        match.setPlayerBProfileId(playerBProfileId);
                        match.setSideADefaulted(false);
                        match.setSideBDefaulted(false);
                        matches.add(match);
                    }
                }
                matchCard.setMatches(matches);
                this.matchCardRepository.save(matchCard);
            }
        }
    }

    /**
     * @param eventDrawItems
     * @param tournamentEventEntity
     */
    private void generateSingleEliminationCards(List<DrawItem> eventDrawItems, TournamentEventEntity tournamentEventEntity) {
        // round of is a number of players in a round e.g. 64, 32, etc.
        int roundOf = eventDrawItems.size();
        // in round of 64 players there will be 32 matches to play
        // draw items are for each player so in S.E. phase we need to pair them up to create a match
        int matchesToPlay = roundOf / 2;

        int numberOfGames = getNumberOfGames(tournamentEventEntity, roundOf);
        for (int matchNum = 0; matchNum < matchesToPlay; matchNum++) {
            int startIndex = matchNum * 2;
            DrawItem playerADrawItem = eventDrawItems.get(startIndex);
            DrawItem playerBDrawItem = eventDrawItems.get(startIndex + 1);
            String playerAProfileId = playerADrawItem.getPlayerId();
            String playerBProfileId = playerBDrawItem.getPlayerId();

            if (!StringUtils.isEmpty(playerAProfileId) && !StringUtils.isEmpty(playerBProfileId)) {

                // create match card with one match
                MatchCard matchCard = new MatchCard();
                matchCard.setEventFk(tournamentEventEntity.getId());
                matchCard.setGroupNum(0);
                matchCard.setNumberOfGames(numberOfGames);
                matchCard.setDrawType(DrawType.SINGLE_ELIMINATION);

                List<Match> matches = new ArrayList<>();
                Match match = new Match();
                match.setMatchCard(matchCard);
                match.setMatchNum(matchNum);
                match.setRound(roundOf);
                match.setPlayerAProfileId(playerAProfileId);
                match.setPlayerBProfileId(playerBProfileId);
                match.setSideADefaulted(false);
                match.setSideBDefaulted(false);

                matches.add(match);
                matchCard.setMatches(matches);
                this.matchCardRepository.save(matchCard);
            }
        }
    }

    /**
     * Gets number of games to play in a given round as configured in event
     *
     * @param tournamentEventEntity event configuration
     * @param roundOf round of 64, 32 etc.
     * @return
     */
    private int getNumberOfGames(TournamentEventEntity tournamentEventEntity, int roundOf) {
        int numGames = 5;
        switch (roundOf) {
            // finals & 3rd/4th place
            case 2:
                numGames = tournamentEventEntity.getNumberOfGamesSEFinals();
                break;
            case 4:
                numGames = tournamentEventEntity.getNumberOfGamesSESemiFinals();
                break;
            case 8:
                numGames = tournamentEventEntity.getNumberOfGamesSEQuarterFinals();
                break;
            default:
                numGames = tournamentEventEntity.getNumberOfGamesSEPlayoffs();
                break;
        }
        // for uninitialized
        if (numGames == 0) {
            numGames = 5;
        }
        return numGames;
    }

    /**
     * Updates match cards keeping already entered match results
     *
     * @param eventId
     * @param drawType
     */
    public void updateMatchCardsForEvent(long eventId, DrawType drawType) {
        List<DrawItem> eventDrawItems = drawService.list(eventId, drawType);
        TournamentEventEntity tournamentEventEntity = tournamentEventEntityService.get(eventId);
        if (drawType == DrawType.ROUND_ROBIN) {
//            updateRoundRobinMatchCards (eventId);
        } else if (drawType == DrawType.SINGLE_ELIMINATION) {
            updateSingleEliminationCards(eventDrawItems, tournamentEventEntity);
        }
    }

    private void updateSingleEliminationCards(List<DrawItem> eventDrawItems, TournamentEventEntity tournamentEventEntity) {
        List<MatchCard> matchCards = this.findAllForEventAndDrawType(tournamentEventEntity.getId(), DrawType.SINGLE_ELIMINATION);
    }

    public MatchCard getMatchCard(long eventId, int groupNum) {
        return this.matchCardRepository.findMatchCardByEventFkAndGroupNum(eventId, groupNum)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find match card"));
    }

    public MatchCard get(long id) {
        return this.matchCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find match card"));
    }

    public long save(MatchCard matchCard) {
        MatchCard saved = this.matchCardRepository.save(matchCard);
        return saved.getId();
    }

    public List<MatchCard> findAllForEventAndDrawType(long evenId, DrawType drawType) {
        return this.matchCardRepository.findMatchCardByEventFkAndDrawTypeOrderByGroupNum(evenId, drawType);
    }

    public void deleteAllForEvent(long evenId, DrawType drawType) {
        List<MatchCard> matchCardByEventFk = this.matchCardRepository.findMatchCardByEventFkAndDrawTypeOrderByGroupNum(evenId, drawType);
        for (MatchCard matchCard : matchCardByEventFk) {
            this.matchCardRepository.delete(matchCard);
        }
    }

    public void delete(long eventId, DrawType drawType, int groupNum) {
        MatchCard matchCard = new MatchCard();
        matchCard.setEventFk(eventId);
        matchCard.setGroupNum(groupNum);
        matchCard.setDrawType(drawType);
        this.matchCardRepository.delete(matchCard);
    }

}
