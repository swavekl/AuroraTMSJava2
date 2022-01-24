package com.auroratms.tiebreaking;

import com.auroratms.draw.DrawType;
import com.auroratms.event.DrawMethod;
import com.auroratms.event.PrizeInfo;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@Transactional
public class PrizeAwardingService {

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    /**
     * @param matchCard
     * @param tournamentEvent
     */
    public void processCompletedMatchCard(MatchCard matchCard, TournamentEvent tournamentEvent) {
        List<PrizeInfo> prizeInfoList = tournamentEvent.getConfiguration().getPrizeInfoList();
        if (prizeInfoList == null) {
            log.error("Prizes are not configured for event " + tournamentEvent.getName());
            return;
        }
        // check if completed round is for the round where we award money or trophies
        if (tournamentEvent.getDrawMethod() == DrawMethod.SNAKE) {
            if (matchCard.getDrawType() == DrawType.SINGLE_ELIMINATION) {
                // round of 2 (finals) or of 4 (semifinals) or 8
                int maxPlaceWithPrize = 2;
                for (PrizeInfo prizeInfo : prizeInfoList) {
                    maxPlaceWithPrize = Math.max(maxPlaceWithPrize, prizeInfo.getAwardedForPlace());
                    if (prizeInfo.getAwardedForPlaceRangeEnd() != 0) {
                        maxPlaceWithPrize = Math.max(maxPlaceWithPrize, prizeInfo.getAwardedForPlaceRangeEnd());
                    }
                }

                // round of this match card result impacts places with prize money or trophies
                int round = matchCard.getRound();
                int maxRoundWithPrize = (int) Math.pow(2, Math.ceil(Math.log(maxPlaceWithPrize)));
                if (round <= maxRoundWithPrize) {
                    processSingleEliminationEvent(tournamentEvent, maxRoundWithPrize);
                }
            }
        } else if (tournamentEvent.getDrawMethod() == DrawMethod.DIVISION) {
            // giant round robin has only one round
            processRoundRobinEvent(tournamentEvent);
        }
    }

    private void processRoundRobinEvent(TournamentEvent tournamentEvent) {
        // todo - finish RR
    }

    /**
     * @param tournamentEvent
     * @param maxRoundWithPrizes
     * @return
     */
    private Map<Integer, String> processSingleEliminationEvent(TournamentEvent tournamentEvent, int maxRoundWithPrizes) {
        // get all match cards for this event single elimination round
        List<MatchCard> seMatchCards = this.matchCardService.findAllForEventAndDrawType(tournamentEvent.getId(), DrawType.SINGLE_ELIMINATION);
        // process them in this order round of 8, round of 4 (semifinals), round of 2 (i.e. finals)
        int roundToAssignPrizes = maxRoundWithPrizes;
        Map<Integer, String> finalPlayerRankings = new TreeMap<>();
        do {
            // collect player rankings so far by building a map of rank to player profile ids
            List<MatchCard> matchCardsForRound = getMatchCardsForRound(seMatchCards, roundToAssignPrizes);
            int playerPlace = roundToAssignPrizes;
            for (MatchCard matchCard : matchCardsForRound) {
                Map<Integer, String> playerRankingsMap = this.getPlayerRankingsMap(matchCard.getPlayerRankings());
                if (playerRankingsMap != null) {
                    System.out.println("playerRankingsMap = " + playerRankingsMap);
                    // higher rounds we only need one player - the looser
                    if (roundToAssignPrizes > 4) {
                        // places 5 - 8 are taking looser
                        String loosingPlayerProfileId = playerRankingsMap.get(Integer.toString(2));
                        finalPlayerRankings.put(playerPlace, loosingPlayerProfileId);
                    } else if (roundToAssignPrizes == 4) {
                        if (!tournamentEvent.isPlay3rd4thPlace()) {
                            // choose looser of this match for semifinalist prize i.e. 3 - 4 th place
                            String loosingPlayerProfileId = playerRankingsMap.get(Integer.toString(2));
                            finalPlayerRankings.put(playerPlace, loosingPlayerProfileId);
                        }
                    } else {
                        // finals plus 3rd and 4th place
                        if (tournamentEvent.isPlay3rd4thPlace() && matchCard.getGroupNum() == 2) {
                            // match for 3rd and 4th
                            String loosingPlayerProfileId = playerRankingsMap.get(Integer.toString(2));
                            finalPlayerRankings.put(4, loosingPlayerProfileId);

                            String winningPlayerProfileId = playerRankingsMap.get(Integer.toString(1));
                            finalPlayerRankings.put(3, winningPlayerProfileId);
                        } else {
                            // final 1st and 2nd place
                            String loosingPlayerProfileId = playerRankingsMap.get(Integer.toString(2));
                            finalPlayerRankings.put(2, loosingPlayerProfileId);

                            String winningPlayerProfileId = playerRankingsMap.get(Integer.toString(1));
                            finalPlayerRankings.put(1, winningPlayerProfileId);
                        }
                    }
                }
                playerPlace--;
            }
            roundToAssignPrizes = roundToAssignPrizes / 2;
        } while (roundToAssignPrizes >= 2);

        // persist final rankings
        if (finalPlayerRankings.size() > 0) {
            Map<Integer, String> finalPlayerRankingsWithNames = convertProfileIdsToNamesMap(finalPlayerRankings);
            tournamentEvent.getConfiguration().setFinalPlayerRankings(finalPlayerRankingsWithNames);
            this.tournamentEventEntityService.update(tournamentEvent);
        }

        return finalPlayerRankings;
    }

    /**
     *
     * @param finalPlayerRankings
     * @return
     */
    private Map<Integer, String> convertProfileIdsToNamesMap(Map<Integer, String> finalPlayerRankings) {
        Map<String, String> profileIdsToNamesMap = matchCardService.convertPlayerRankingsToPlayerNamesMap(finalPlayerRankings);
        Map<Integer, String> finalPlayerRankingsWithNames = new TreeMap<>();
        for (Map.Entry<Integer, String> entry : finalPlayerRankings.entrySet()) {
            Integer place = entry.getKey();
            String playerProfileId = entry.getValue();
            String fullPlayerName = profileIdsToNamesMap.get(playerProfileId);
            finalPlayerRankingsWithNames.put(place, fullPlayerName);
        }
        return finalPlayerRankingsWithNames;
    }

    /**
     * @param seMatchCards
     * @param roundToAssignPrizes
     * @return
     */
    private List<MatchCard> getMatchCardsForRound(List<MatchCard> seMatchCards, int roundToAssignPrizes) {
        List<MatchCard> matchCardsForRound = new ArrayList<>();

        for (MatchCard matchCard : seMatchCards) {
            if (matchCard.getRound() == roundToAssignPrizes) {
                matchCardsForRound.add(matchCard);
            }
        }

        Collections.sort(matchCardsForRound, new Comparator<MatchCard>() {
            @Override
            public int compare(MatchCard o1, MatchCard o2) {
                return Integer.compare(o1.getGroupNum(), o2.getGroupNum());
            }
        });
        return matchCardsForRound;
    }

    /**
     * @param playerRankings
     * @return
     */
    private Map<Integer, String> getPlayerRankingsMap(String playerRankings) {
        Map<Integer, String> playerRankingsMap = null;
        if (playerRankings != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                playerRankingsMap = mapper.readValue(playerRankings, TreeMap.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return playerRankingsMap;
    }
}
