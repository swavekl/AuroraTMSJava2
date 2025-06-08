package com.auroratms.tiebreaking;

import com.auroratms.draw.DrawType;
import com.auroratms.event.DrawMethod;
import com.auroratms.event.PrizeInfo;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
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
import java.util.stream.Collectors;

import static com.auroratms.draw.DrawItem.TBD_PROFILE_ID;

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
        if (prizeInfoList == null || prizeInfoList.size() == 0) {
            // if they did not configure prizes make some default ones for trophies only
            prizeInfoList = new ArrayList<>(3);
            prizeInfoList.add(new PrizeInfo("A", 1, 0, 0, true));
            prizeInfoList.add(new PrizeInfo("A", 2, 0, 0, true));
            prizeInfoList.add(new PrizeInfo("A", 3, 4, 0, true));
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
            processRoundRobinEvent(tournamentEvent, prizeInfoList, matchCard);
        }
    }

    private void processRoundRobinEvent(TournamentEvent tournamentEvent, List<PrizeInfo> prizeInfoList, MatchCard matchCard) {
        // find unique division names and sort them
        Map<Integer, String> groupNumToDivisionNameMap = groupNumToDivisionNameMap(prizeInfoList);

        Map<Integer, String> finalPlayerRankings = new HashMap<>();
        Map<Integer, String> playerRankingsMap = this.getPlayerRankingsMap(matchCard.getPlayerRankings());
        Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
        if (playerRankingsMap != null && profileIdToNameMap != null) {
            int groupNum = matchCard.getGroupNum();
            String division = groupNumToDivisionNameMap.get(groupNum);
            int maxPlace = getMaxPlaceForDivision(prizeInfoList, division);
            for (int place = 1; place <= maxPlace; place++) {
                String strPlace = Integer.toString(place);
                String profileId = playerRankingsMap.get(strPlace);
                String playerName = profileIdToNameMap.get(profileId);
                if (playerName != null) {
                    finalPlayerRankings.put(place, playerName);
                }
            }
        }

        if (finalPlayerRankings.size() > 0) {
            tournamentEvent.getConfiguration().setFinalPlayerRankings(finalPlayerRankings);
            this.tournamentEventEntityService.update(tournamentEvent);
        }
    }

    private int getMaxPlaceForDivision(List<PrizeInfo> prizeInfoList, String division) {
        int maxPlace = 1;
        List<PrizeInfo> prizeInfosForDivision = prizeInfoList
                .stream()
                .filter(c -> c.getDivision().equals(division))
                .sorted(Comparator.comparing(PrizeInfo::getAwardedForPlace))
                .collect(Collectors.toList());
        for (PrizeInfo prizeInfo : prizeInfosForDivision) {
            maxPlace = Math.max(maxPlace, prizeInfo.getAwardedForPlace());
            maxPlace = Math.max(maxPlace, prizeInfo.getAwardedForPlaceRangeEnd());
        }
        return maxPlace;
    }

    private Map<Integer, String> groupNumToDivisionNameMap(List<PrizeInfo> prizeInfoList) {
        SortedSet<String> divisions = new TreeSet<>();
        for (PrizeInfo prizeInfo : prizeInfoList) {
            divisions.add(prizeInfo.getDivision());
        }

        // build map translating group number to division name
        // assume division A is group 1, B is group 2, etc.
        Map<Integer, String> groupNumToDivisionNameMap = new HashMap<>();
        int group = 1;
        for (String division : divisions) {
            groupNumToDivisionNameMap.put(group, division);
            group++;
        }
        return groupNumToDivisionNameMap;
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
                } else {
                    // if there is no 4th player to play a match for 3rd nd 4th place, so there is no player rankings map
                    // just get this player's profile id and put him/her in the 3rd place
                    if (roundToAssignPrizes == 2 && tournamentEvent.isPlay3rd4thPlace() && matchCard.getGroupNum() == 2) {
                        List<Match> matches = matchCard.getMatches();
                        if (!matches.isEmpty()) {
                            Match match = matches.get(0);
                            String playerAProfileId = match.getPlayerAProfileId();
                            if (TBD_PROFILE_ID.equals(playerAProfileId)) {
                                List<MatchCard> matchCardsForRoundOf4 = getMatchCardsForRound(seMatchCards, 4);
                                // when a player gets a bye there is no match card for it.
                                // the match in a round of 4 feeding into this match is in group 1 but if we only see a match in group 2
                                // there won't be a result from match 1 so we shouldn't wait, just assign 3 place
                                if (matchCardsForRoundOf4.size() == 1) {
                                    MatchCard onlyMatchCard = matchCardsForRoundOf4.get(0);
                                    if (onlyMatchCard.getGroupNum() == 2) {
                                        String playerBProfileId = match.getPlayerBProfileId();
                                        finalPlayerRankings.put(3, playerBProfileId);
                                    }
                                }
                            }
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
