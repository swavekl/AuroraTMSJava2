package com.auroratms.results;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawService;
import com.auroratms.draw.DrawType;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventConfiguration;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.match.MatchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@Transactional
public class TournamentResultsService {

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private DrawService drawService;

    /**
     * Gets all events in the tournament and their status - i.e. completed or not and who got which place
     * @param tournamentId
     * @return
     */
    public List<EventResultStatus> listEventResultsStatus(long tournamentId) {
        Collection<TournamentEvent> tournamentEvents =
                this.tournamentEventEntityService.list(tournamentId, Pageable.unpaged());
        List<EventResultStatus> eventResultStatusList = new ArrayList<>(tournamentEvents.size());
        for (TournamentEvent tournamentEvent : tournamentEvents) {
            TournamentEventConfiguration configuration = tournamentEvent.getConfiguration();
            Map<Integer, String> finalPlayerRankings =
                    configuration.getFinalPlayerRankings();
            EventResultStatus eventResultStatus = new EventResultStatus();
            eventResultStatus.setEventId(tournamentEvent.getId());
            eventResultStatus.setEventName(tournamentEvent.getName());
            boolean play3rd4thPlace = tournamentEvent.isPlay3rd4thPlace();
            eventResultStatus.setPlay3rd4thPlace(play3rd4thPlace);
            eventResultStatusList.add(eventResultStatus);
            boolean resultsAvailable = (finalPlayerRankings != null && !finalPlayerRankings.isEmpty());
            eventResultStatus.setResultsAvailable(resultsAvailable);
            if (resultsAvailable) {
                String firstPlacePlayer = finalPlayerRankings.get(1);
                eventResultStatus.setFirstPlacePlayer(firstPlacePlayer);
                String secondPlacePlayer = finalPlayerRankings.get(2);
                eventResultStatus.setSecondPlacePlayer(secondPlacePlayer);
                String thirdPlacePlayer = finalPlayerRankings.get(3);
                eventResultStatus.setThirdPlacePlayer(thirdPlacePlayer);
                String fourthPlacePlayer = finalPlayerRankings.get(4);
                eventResultStatus.setFourthPlacePlayer(fourthPlacePlayer);
            }
        }
        // get a list of tournament events
        return eventResultStatusList;
    }

    /**
     * Gets aggregated results for one event
     * @param eventId
     * @return
     */
    public List<EventResults> getEventResults(long eventId) {
        TournamentEvent tournamentEvent = this.tournamentEventEntityService.get(eventId);
        List<MatchCard> matchCards = this.matchCardService.findAllForEvent(eventId);
        this.matchCardService.fillPlayerIdToNameMapForAllMatches(matchCards);
        List<EventResults> resultsList = new ArrayList<>(matchCards.size());
        List<EventResults> seResultsList = new ArrayList<>(matchCards.size());
        Map<String, String> matchToCompactNotationResultMap = new HashMap<>();
        for (MatchCard matchCard : matchCards) {
            boolean singleElimination = (matchCard.getDrawType() == DrawType.SINGLE_ELIMINATION);
            EventResults eventResults = new EventResults();
            eventResults.setSingleElimination(singleElimination);
            eventResults.setGroupNumber(matchCard.getGroupNum());
            eventResults.setRound(matchCard.getRound());
            List<PlayerResults> playerResultsList = getPlayerResults(matchCard, tournamentEvent);
            eventResults.setPlayerResultsList(playerResultsList);
            if (singleElimination) {
                List<Match> matches = matchCard.getMatches();
                if (matches.size() == 1) {
                    Match match = matches.get(0);
                    String compactResult = match.getCompactResult(matchCard.getNumberOfGames(), tournamentEvent.getPointsPerGame());
                    String matchKey = String.format("%d:%d", matchCard.getRound(), matchCard.getGroupNum());
                    matchToCompactNotationResultMap.put(matchKey, compactResult);
                }
                seResultsList.add(eventResults);
            } else {
                resultsList.add(eventResults);
            }
        }

        // enrich and organized SE results so it is easy to draw them
        if (!seResultsList.isEmpty()) {
            enhanceSEResultsList(tournamentEvent, seResultsList, resultsList);
            for (EventResults eventResults : seResultsList) {
                List<PlayerResults> playerResultsList = eventResults.getPlayerResultsList();
                if (playerResultsList.size() == 2) {
                    PlayerResults playerOneResults = playerResultsList.get(0);
                    PlayerResults playerTwoResults = playerResultsList.get(1);
                    MatchResult matchResult = null;
                    if (playerOneResults.getRank() == 1) {
                        for (MatchResult result : playerOneResults.getMatchResults()) {
                            if (result.getPlayerALetter() != result.getPlayerBLetter()) {
                                matchResult = result;
                                break;
                            }
                        }
                    } else if (playerTwoResults.getRank() == 1) {
                        for (MatchResult result : playerTwoResults.getMatchResults()) {
                            if (result.getPlayerALetter() != result.getPlayerBLetter()) {
                                matchResult = result;
                                break;
                            }
                        }
                    }
                    if (matchResult != null) {
                        String matchKey = String.format("%d:%d", eventResults.getRound(), eventResults.getGroupNumber());
                        String compactMatchResult = matchToCompactNotationResultMap.get(matchKey);
                        matchResult.setCompactMatchResult(compactMatchResult);
                    }
                }
            }
            resultsList.addAll(seResultsList);
        }

        return resultsList;
    }

    /**
     * Adds byes to single elimination results so we can draw a full bracket
     *
     * @param tournamentEvent tournament event
     * @param seResultsList list of single elimination round events to add to
     * @param resultsList all results list so we can get full player names from the round robin round
     */
    private void enhanceSEResultsList(TournamentEvent tournamentEvent, List<EventResults> seResultsList, List<EventResults> resultsList) {
        // get SE draw items so we can identify byes
        List<DrawItem> drawItems = drawService.list(tournamentEvent.getId(), DrawType.SINGLE_ELIMINATION);
        // find the first round after RR e.g. round of 16
        int maxRoundOf = 0;
        for (DrawItem drawItem : drawItems) {
            maxRoundOf = Math.max(drawItem.getRound(), maxRoundOf);
        }

        fillSeedNumbers(seResultsList, drawItems, maxRoundOf);

        // find byes in each round
        for (int roundOf = maxRoundOf; roundOf > 1;) {
            List<EventResults> byeResults = getByesForRound(seResultsList, resultsList, drawItems, roundOf);
            if (!byeResults.isEmpty()) {
                seResultsList.addAll(byeResults);
                // this round was of 16 next round is of 8
                roundOf = roundOf / 2;
            } else {
                // this round didn't produce any byes so the next one won't either - let's stop
                break;
            }
        }

        Collections.sort(seResultsList,
                Comparator.comparing(EventResults::getRound).reversed()
                .thenComparing(EventResults::getGroupNumber));
    }

    /**
     * Fills non-bye matches seSeed numbers
     * @param seResultsList
     * @param drawItems
     * @param roundOf
     */
    private void fillSeedNumbers(List<EventResults> seResultsList, List<DrawItem> drawItems, int roundOf) {
        for (EventResults eventResults : seResultsList) {
            if (eventResults.getRound() == roundOf) {
                int groupNumber = eventResults.getGroupNumber();
                List<PlayerResults> playerResultsList = eventResults.getPlayerResultsList();
                if (playerResultsList.size() == 2) {
                    PlayerResults playerAResults = playerResultsList.get(0);
                    fillPlayerSeedNumber(playerAResults, drawItems, roundOf, groupNumber);
                    PlayerResults playerBResults = playerResultsList.get(1);
                    fillPlayerSeedNumber(playerBResults, drawItems, roundOf, groupNumber);
                }
            }
        }
    }

    /**
     * Fills seed number of one player
     * @param playerAResults
     * @param drawItems
     * @param roundOf
     * @param groupNumber
     */
    private void fillPlayerSeedNumber(PlayerResults playerAResults, List<DrawItem> drawItems, int roundOf, int groupNumber) {
        for (DrawItem drawItem : drawItems) {
            if (drawItem.getRound() == roundOf && drawItem.getPlayerId().equals(playerAResults.getProfileId())) {
                playerAResults.setSeSeedNumber(drawItem.getSeSeedNumber());
                break;
            }
        }
    }

    /**
     *
     * @param seResultsList
     * @param resultsList
     * @param drawItems
     * @param roundOf
     * @return
     */
    private List<EventResults> getByesForRound(List<EventResults> seResultsList, List<EventResults> resultsList, List<DrawItem> drawItems, int roundOf) {
        // find all matches for this first round so we can fill the byes
        Set<Integer> existingGroupNums = new HashSet<>();
        for (EventResults eventResults : seResultsList) {
            if (eventResults.getRound() == roundOf) {
                existingGroupNums.add(eventResults.getGroupNumber());
            }
        }

        int roundMatches = roundOf / 2;
        List<EventResults> byeResults = new ArrayList<>(roundMatches - existingGroupNums.size());
        for (int groupNum = 1; groupNum <= roundMatches; groupNum++) {
            boolean found = existingGroupNums.contains(groupNum);
            if (!found) {
                EventResults byeEventResult = new EventResults();
                byeEventResult.setGroupNumber(groupNum);
                byeEventResult.setRound(roundOf);
                byeEventResult.setSingleElimination(true);
                List<PlayerResults> playerResultsList = makePlayerResultsForBye(roundOf, groupNum, drawItems, resultsList);
                byeEventResult.setPlayerResultsList(playerResultsList);
                byeResults.add(byeEventResult);
            }
        }
        return byeResults;
    }

    private List<PlayerResults> makePlayerResultsForBye(int roundOf, int groupNum, List<DrawItem> drawItems, List<EventResults> resultsList) {
        // find the draw item for this round
        List<PlayerResults> playerResultsList = new ArrayList<>(1);
        int minSingleElimLineNum = (groupNum * 2) - 1;
        int maxSingleElimLineNum = groupNum * 2;
        for (DrawItem drawItem : drawItems) {
            if (drawItem.getRound() == roundOf &&
               (drawItem.getSingleElimLineNum() == minSingleElimLineNum || drawItem.getSingleElimLineNum() == maxSingleElimLineNum)) {
                Character letterCode = (drawItem.getSingleElimLineNum() % 2 == 1) ? 'A' : 'B';
                PlayerResults playerResults = new PlayerResults();
                playerResultsList.add(playerResults);
                playerResults.setLetterCode(letterCode);
                playerResults.setSeSeedNumber(drawItem.getSeSeedNumber());
                playerResults.setByeNumber(drawItem.getByeNum());
                playerResults.setProfileId(drawItem.getPlayerId());
                playerResults.setMatchResults(Collections.singletonList(new MatchResult()));
                playerResults.setRating(drawItem.getRating());
                if (drawItem.getByeNum() == 0) {
                    fillPlayerFullNameAndRating(resultsList, playerResults);
                    playerResults.setRank(1);
                } else {
                    // bye line
                    playerResults.setFullName("Bye");
                    playerResults.setRank(2);
                }
            }
        }
        return playerResultsList;
    }

    /**
     *
     * @param resultsList
     * @param playerResultsToFill
     */
    private void fillPlayerFullNameAndRating(List<EventResults> resultsList, PlayerResults playerResultsToFill) {
        boolean found = false;
        String profileId = playerResultsToFill.getProfileId();
        for (EventResults eventResults : resultsList) {
            List<PlayerResults> playerResultsList = eventResults.getPlayerResultsList();
            for (PlayerResults playerResults : playerResultsList) {
                if (playerResults.getProfileId().equals(profileId)) {
                    playerResultsToFill.setRating(playerResults.getRating());
                    playerResultsToFill.setFullName(playerResults.getFullName());
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }
    }

    /**
     *
     * @param matchCard
     * @param tournamentEvent
     * @return
     */
    private List<PlayerResults> getPlayerResults(MatchCard matchCard, TournamentEvent tournamentEvent) {
        List<PlayerResults> playerResultsList = new ArrayList<>();
        try {
            Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
            Map<Integer, String> playerRankings = matchCard.getPlayerRankingsAsMap();
            int numPlayers = profileIdToNameMap.size();
            for (Map.Entry<String, String> entry : profileIdToNameMap.entrySet()) {
                PlayerResults playerResults = new PlayerResults();
                playerResultsList.add(playerResults);
                String profileId = entry.getKey();
                String fullName = entry.getValue();
                playerResults.setProfileId(profileId);
                playerResults.setFullName(fullName);
                playerResults.setMatchResults(new ArrayList<>(numPlayers));   // +1 is for A vs A.
                for (Map.Entry<Integer, String> rankingsEntry : playerRankings.entrySet()) {
                    if (rankingsEntry.getValue().equals(profileId)) {
                        playerResults.setRank(rankingsEntry.getKey());
                    }
                }
            }

            List<Match> matches = matchCard.getMatches();
            for (Match match : matches) {
                String playerAProfileId = match.getPlayerAProfileId();
                String playerBProfileId = match.getPlayerBProfileId();
                Character playerALetter = match.getPlayerALetter();
                Character playerBLetter = match.getPlayerBLetter();
                int playerARating = match.getPlayerARating();
                int playerBRating = match.getPlayerBRating();
                MatchResult matchResult = match.getGamesOnlyResult(matchCard.getNumberOfGames(),
                        tournamentEvent.getPointsPerGame());
                boolean playerAisMatchWinner = match.isMatchWinner(playerAProfileId, matchCard.getNumberOfGames(), tournamentEvent.getPointsPerGame());
                boolean playerBisMatchWinner = match.isMatchWinner(playerBProfileId, matchCard.getNumberOfGames(), tournamentEvent.getPointsPerGame());

                // find the player results
                for (PlayerResults playerResults : playerResultsList) {
                    List<MatchResult> matchResults = playerResults.getMatchResults();
                    if (playerResults.getProfileId().equals(playerAProfileId)) {
                        playerResults.setLetterCode(playerALetter);
                        playerResults.setRating(playerARating);
                        matchResults.add(matchResult);
                        if (playerAisMatchWinner) {
                            playerResults.addNumMatchesWon();
                        } else if (playerBisMatchWinner) {
                            playerResults.addNumMatchesLost();
                        }
                    } else if (playerResults.getProfileId().equals(playerBProfileId)) {
                        playerResults.setLetterCode(playerBLetter);
                        playerResults.setRating(playerBRating);
                        MatchResult reversedMatchResults = matchResult.makeReverse();
                        matchResults.add(reversedMatchResults);
                        if (playerBisMatchWinner) {
                            playerResults.addNumMatchesWon();
                        } else if (playerAisMatchWinner) {
                            playerResults.addNumMatchesLost();
                        }
                    }
                }
            }

            // sort match results by opposing player letter A, B, C etc.
            for (PlayerResults playerResults : playerResultsList) {
                List<MatchResult> matchResults = playerResults.getMatchResults();
                // add empty cell for A vs A, B vs B, etc.
                MatchResult empty = new MatchResult();
                empty.setPlayerALetter(playerResults.getLetterCode());
                empty.setPlayerBLetter(playerResults.getLetterCode());
                matchResults.add(empty);
                Collections.sort(matchResults, Comparator.comparing(MatchResult::getPlayerBLetter));
            }

            // sort players results by player letter
            Collections.sort(playerResultsList, Comparator.comparing(PlayerResults::getLetterCode));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return playerResultsList;
    }
}
