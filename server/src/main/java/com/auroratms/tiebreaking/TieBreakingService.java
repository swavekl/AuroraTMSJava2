package com.auroratms.tiebreaking;

import com.auroratms.draw.DrawService;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.tiebreaking.model.GroupTieBreakingInfo;
import com.auroratms.tiebreaking.model.MatchStatus;
import com.auroratms.tiebreaking.model.PlayerMatchResults;
import com.auroratms.tiebreaking.model.PlayerTieBreakingInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Service for computing who got which place in a group
 */
@Service
@Transactional
public class TieBreakingService {

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private DrawService drawService;

    @Autowired
    private PrizeAwardingService prizeAwardingService;

    /**
     * Computes ranking and causes players to advance to next round
     *
     * @param matchCardId
     */
    public GroupTieBreakingInfo rankAndAdvancePlayers(Long matchCardId) {
        MatchCard matchCard = matchCardService.getMatchCardWithPlayerProfiles(matchCardId);

        // get number of games and points per game
        long eventFk = matchCard.getEventFk();
        TournamentEvent tournamentEvent = tournamentEventEntityService.get(eventFk);
        int pointsPerGame = tournamentEvent.getPointsPerGame();
        int numberOfGames = matchCard.getNumberOfGames();

        GroupTieBreakingInfo groupTieBreakingInfo = rankPlayers(matchCard, pointsPerGame, numberOfGames);

        // advance players
        advancePlayers(matchCard, tournamentEvent, groupTieBreakingInfo);

        return groupTieBreakingInfo;
    }

    /**
     * Computes ranking
     *
     * @param matchCardId
     * @return
     */
    public GroupTieBreakingInfo rankAndExplain(Long matchCardId) {
        MatchCard matchCard = matchCardService.getMatchCardWithPlayerProfiles(matchCardId);

        // get number of games and points per game
        long eventFk = matchCard.getEventFk();
        TournamentEvent tournamentEvent = tournamentEventEntityService.get(eventFk);
        int pointsPerGame = tournamentEvent.getPointsPerGame();
        int numberOfGames = matchCard.getNumberOfGames();

        return rankPlayers(matchCard, pointsPerGame, numberOfGames);
    }

    /**
     * @param matchCard
     * @param pointsPerGame points per game e.g. 11 or 21
     * @param numberOfGames
     * @return
     */
    public GroupTieBreakingInfo rankPlayers(MatchCard matchCard, int pointsPerGame, int numberOfGames) {
        Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
        int numPlayers = profileIdToNameMap.size();
        List<Character> allPlayerCodes = new ArrayList<>(numPlayers);
        for (int i = 0; i < numPlayers; i++) {
            allPlayerCodes.add((char) ('A' + i));
        }
        Map<String, Character> profileIdToLetterCodeMap = makeProfileIdToLetterCodeMap(matchCard.getMatches());

        GroupTieBreakingInfo groupTieBreakingInfo = new GroupTieBreakingInfo();
        groupTieBreakingInfo.setProfileIdToNameMap(profileIdToNameMap);
        List<PlayerTieBreakingInfo> playerTieBreakingInfoList = new ArrayList<>();
        groupTieBreakingInfo.setPlayerTieBreakingInfoList(playerTieBreakingInfoList);
        for (String playerProfileId : profileIdToNameMap.keySet()) {
            Character playerCode = profileIdToLetterCodeMap.get(playerProfileId);
            PlayerTieBreakingInfo playerTieBreakingInfo = new PlayerTieBreakingInfo(playerProfileId, playerCode, allPlayerCodes);
            playerTieBreakingInfoList.add(playerTieBreakingInfo);
        }
        // sort by letter from A through D
        Collections.sort(playerTieBreakingInfoList, new Comparator<PlayerTieBreakingInfo>() {
            @Override
            public int compare(PlayerTieBreakingInfo ptbi1, PlayerTieBreakingInfo ptbi2) {
                int player1Code = ptbi1.getPlayerCode();
                int player2Code = ptbi2.getPlayerCode();
                return Integer.compare(player1Code, player2Code);
            }
        });

        // build a matrix of match results for tie breaking
        buildMatrix(matchCard, profileIdToLetterCodeMap, playerTieBreakingInfoList, pointsPerGame, numberOfGames);

        // perform tie breaking and record second pass tie-breaking results in this map
        Map<String, List<PlayerTieBreakingInfo>> nWayTieMap = new TreeMap<>();
        groupTieBreakingInfo.setNWayTieBreakingInfosMap(nWayTieMap);
        performTieBreakingProcedure(groupTieBreakingInfo.getPlayerTieBreakingInfoList(), true, pointsPerGame, nWayTieMap);

        return groupTieBreakingInfo;
    }

    /**
     * Performs tie breaking procedure for the set of player matches
     *
     * @param playerTieBreakingInfoList
     * @param firstLevel
     * @param pointsPerGame             points per game e.g. 11 or 21
     * @param nWayTieMap                collect n-way tie results here
     */
    private void performTieBreakingProcedure(List<PlayerTieBreakingInfo> playerTieBreakingInfoList,
                                             boolean firstLevel,
                                             int pointsPerGame,
                                             Map<String, List<PlayerTieBreakingInfo>> nWayTieMap) {
        // compute match points
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            calculateMatchPoints(playerTieBreakingInfo);
        }

        // see if there are any tie-breaks
        // build a sorted map of match points to player codes who have the same number of match points
        Map<Integer, List<Character>> matchPointsToLetterCodes = new TreeMap<>(Collections.reverseOrder());
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            int matchPoints = playerTieBreakingInfo.getMatchPoints();
            List<Character> playerCodes = matchPointsToLetterCodes.computeIfAbsent(matchPoints, k -> new ArrayList<>());
            playerCodes.add(playerTieBreakingInfo.getPlayerCode());
        }

        // assign ranks based on match points
        int rank = 1;
        for (Integer matchPoints : matchPointsToLetterCodes.keySet()) {
            List<Character> playerCodes = matchPointsToLetterCodes.get(matchPoints);
            // assign the same rank to all players with same number of match points
            for (Character playerCode : playerCodes) {
                PlayerTieBreakingInfo playerTieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerCode);
                if (playerTieBreakingInfo != null) {
                    playerTieBreakingInfo.setRank(rank);
                }
            }
            // skip rank number by players with the same match point
            rank += playerCodes.size();
        }

        // determine if we have any ties.  if not every player has a distinct number of match points then we have a tie somewhere
        boolean hasTies = (playerTieBreakingInfoList.size() != matchPointsToLetterCodes.size());
        if (hasTies) {
            if (firstLevel) {
                // for each number of match points that have more than one player with that many MPs resolve the tie
                for (Integer matchPoints : matchPointsToLetterCodes.keySet()) {
                    List<Character> playerLetterCodes = matchPointsToLetterCodes.get(matchPoints);
                    if (playerLetterCodes.size() > 1) {
                        resolveNWayTie(playerLetterCodes, playerTieBreakingInfoList, pointsPerGame, nWayTieMap);
                    }
                }
            } else {
                // levels after first one follow this procedure.
                hasTies = determineRankBasedOnGamesRatio(playerTieBreakingInfoList);
                if (hasTies) {
                    hasTies = determineRankBasedOnPointsRatio(playerTieBreakingInfoList, pointsPerGame);
                    if (hasTies) {
                        // determine by lot
                        determineByLot(playerTieBreakingInfoList);
                    }
                }
            }
        }
    }

    /**
     * Determines ranks based on points won vs lost ratio
     *
     * @param playerTieBreakingInfoList matches to consider for calculating ratios
     * @param pointsPerGame             points per game e.g. 11 or 21
     */
    private boolean determineRankBasedOnPointsRatio(List<PlayerTieBreakingInfo> playerTieBreakingInfoList,
                                                    int pointsPerGame) {
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            calculatePointsWonToLost(playerTieBreakingInfo, pointsPerGame);
        }

        // consider points won / lost ratio
        // put players with the same ratio in list and then sorted descending map by ratio
        Map<Float, List<Character>> pointsRatioToPlayerCodeMap = new TreeMap<>(Collections.reverseOrder());
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
//            System.out.println("player " + playerTieBreakingInfo.getPlayerCode() +
//                    " => W / L = " + playerTieBreakingInfo.getPointsWon() + " / " + playerTieBreakingInfo.getPointsLost());
            float pointsRatio = ((float) playerTieBreakingInfo.getPointsWon() / (float) playerTieBreakingInfo.getPointsLost());
            List<Character> playerCodes = pointsRatioToPlayerCodeMap.computeIfAbsent(pointsRatio, k -> new ArrayList<>());
            playerCodes.add(playerTieBreakingInfo.getPlayerCode());
            pointsRatioToPlayerCodeMap.put(pointsRatio, playerCodes);
        }

        // assign ranks
        int rank = 1;
        for (Float ratio : pointsRatioToPlayerCodeMap.keySet()) {
            List<Character> playerCodes = pointsRatioToPlayerCodeMap.get(ratio);
            if (playerCodes.size() == 1) {
                char playerCode = playerCodes.get(0);
                PlayerTieBreakingInfo playerTieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerCode);
                playerTieBreakingInfo.setRank(rank);
            }
            rank += playerCodes.size();
        }

        // see if there are any ties based on this ratio
        // if every player has a different ratio then we don't have any ties
        boolean hasTies = (playerTieBreakingInfoList.size() != pointsRatioToPlayerCodeMap.size());

        return hasTies;
    }

    /**
     * Determines ranks based on games won to lost ratio
     *
     * @param playerTieBreakingInfoList list of match infos to use for tiebreaking
     * @return
     */
    private boolean determineRankBasedOnGamesRatio(List<PlayerTieBreakingInfo> playerTieBreakingInfoList) {
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            calculateGamesWonToLostRatio(playerTieBreakingInfo);
        }
        // consider games won / lost ratio
        // put them in the sorted map
        Map<Float, List<Character>> gamesRatioToPlayerCodeMap = new TreeMap<>(Collections.reverseOrder());
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            float gamesRatio = ((float) playerTieBreakingInfo.getGamesWon() / (float) playerTieBreakingInfo.getGamesLost());
            List<Character> playerCodes = gamesRatioToPlayerCodeMap.computeIfAbsent(gamesRatio, k -> new ArrayList<>());
            playerCodes.add(playerTieBreakingInfo.getPlayerCode());
            gamesRatioToPlayerCodeMap.put(gamesRatio, playerCodes);
        }

        // assign ranks based on games ratio
        int rank = 1;
        for (Float ratio : gamesRatioToPlayerCodeMap.keySet()) {
            List<Character> playerCodes = gamesRatioToPlayerCodeMap.get(ratio);
            if (playerCodes.size() == 1) {
                char playerCode = playerCodes.get(0);
                PlayerTieBreakingInfo playerTieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerCode);
                playerTieBreakingInfo.setRank(rank);
            }
            rank += playerCodes.size();
        }
        // if every player has a different ratio then we don't have any ties
        boolean hasTies = (playerTieBreakingInfoList.size() != gamesRatioToPlayerCodeMap.size());
        return hasTies;
    }

    /**
     * Calculates match points and puts them in players tie breaking info
     *
     * @param playerTieBreakingInfo
     */
    private void calculateMatchPoints(PlayerTieBreakingInfo playerTieBreakingInfo) {
        char playerCode = playerTieBreakingInfo.getPlayerCode();
//        System.out.println("calculateMatchPoints for player " + playerCode);
        List<PlayerMatchResults> allPlayerMatchResults = playerTieBreakingInfo.getAllPlayerMatchResults();
        int totalMatchPoints = 0;
        for (PlayerMatchResults matchResult : allPlayerMatchResults) {
            if (matchResult.getOpponentCode() != playerCode) {
                MatchStatus matchStatus = matchResult.getMatchStatus();
                if (matchStatus != null) {
                    int matchPoints = (matchStatus == MatchStatus.WIN) ? 2 :
                            (matchStatus == MatchStatus.LOSS ? 1 : 0);
//                    System.out.print(" + " + matchPoints);
                    totalMatchPoints += matchPoints;
                }
            }
        }
//        System.out.println("\ncalculateMatchPoints for player " + playerCode + " totalMatchPoints = " + totalMatchPoints);
        playerTieBreakingInfo.setMatchPoints(totalMatchPoints);
    }

    /**
     * @param playerTieBreakingInfo
     */
    public void calculateGamesWonToLostRatio(PlayerTieBreakingInfo playerTieBreakingInfo) {
        int gamesWon = 0;
        int gamesLost = 0;
        List<PlayerMatchResults> allPlayerMatchResults = playerTieBreakingInfo.getAllPlayerMatchResults();
        for (PlayerMatchResults playerMatchResult : allPlayerMatchResults) {
            List<Integer> gameScores = playerMatchResult.getGameScores();
            // matches played against the same player are null e.g. A vs A, B vs B etc.
            if (gameScores != null) {
                for (Integer gameScore : gameScores) {
                    if (gameScore >= 0) {
                        gamesWon++;
                    } else {
                        gamesLost++;
                    }
                }
            }
        }
        playerTieBreakingInfo.setGamesWon(gamesWon);
        playerTieBreakingInfo.setGamesLost(gamesLost);
    }

    /**
     * @param playerTieBreakingInfo
     * @param pointsPerGame         points per game e.g. 11 or 21
     */
    public void calculatePointsWonToLost(PlayerTieBreakingInfo playerTieBreakingInfo, int pointsPerGame) {
        int pointsWon = 0;
        int pointsLost = 0;
        int deuceStartsAtPoints = pointsPerGame - 1;
        List<PlayerMatchResults> allPlayerMatchResults = playerTieBreakingInfo.getAllPlayerMatchResults();
        for (PlayerMatchResults playerMatchResult : allPlayerMatchResults) {
            List<Integer> gameScores = playerMatchResult.getGameScores();
            // matches played against the same player are null e.g. A vs A, B vs B etc.
            if (gameScores != null) {
//                System.out.println(playerTieBreakingInfo.getPlayerCode() + " vs " + playerMatchResult.getOpponentCode());
                for (Integer gameScore : gameScores) {
                    int thisPlayerPoints = 0;
                    int opponentPoints = 0;
                    boolean notDeuces = Math.abs(gameScore) < deuceStartsAtPoints;
                    if (gameScore >= 0) {
                        thisPlayerPoints = notDeuces ? pointsPerGame : (gameScore + 2);
                        opponentPoints = gameScore;
                    } else {
                        gameScore = Math.abs(gameScore);

                        thisPlayerPoints = gameScore;
                        opponentPoints = notDeuces ? pointsPerGame : (gameScore + 2);
                    }
//                    System.out.println(thisPlayerPoints + " : " + opponentPoints);
                    pointsWon += thisPlayerPoints;
                    pointsLost += opponentPoints;
                }
            }
//            System.out.println("--------");
        }
        playerTieBreakingInfo.setPointsWon(pointsWon);
        playerTieBreakingInfo.setPointsLost(pointsLost);
    }

    /**
     * @param playerLetterCodes
     * @param playerTieBreakingInfoList
     * @param pointsPerGame             points per game e.g. 11 or 21
     * @param nWayTieMap
     */
    private void resolveNWayTie(List<Character> playerLetterCodes,
                                List<PlayerTieBreakingInfo> playerTieBreakingInfoList,
                                int pointsPerGame,
                                Map<String, List<PlayerTieBreakingInfo>> nWayTieMap) {
        // extract matches only between players who are tied
        List<PlayerTieBreakingInfo> tiedPlayersTieBreakingInfoList = extractTiedPlayersTieBreakingInfo(playerLetterCodes, playerTieBreakingInfoList);
        // do the tie breaking
        performTieBreakingProcedure(tiedPlayersTieBreakingInfoList, false, pointsPerGame, nWayTieMap);

        for (PlayerTieBreakingInfo tiedPlayerTieBreakingInfo : tiedPlayersTieBreakingInfoList) {
            char playerCode = tiedPlayerTieBreakingInfo.getPlayerCode();
            // compute the final rank and set it
            int tieResolvingRank = tiedPlayerTieBreakingInfo.getRank();
            PlayerTieBreakingInfo playerTieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerCode);
            int rank = playerTieBreakingInfo.getRank();
            rank += (tieResolvingRank - 1);
            playerTieBreakingInfo.setRank(rank);
        }

        // save in the map to allow easy
        String tieBreakingListKey = String.format("%d) %d-way tie breaking between players %s",
                (nWayTieMap.size() + 1), playerLetterCodes.size(), playerLetterCodes);
        nWayTieMap.put(tieBreakingListKey, tiedPlayersTieBreakingInfoList);
    }

    /**
     * Creates a matrix of player matches for players whose letter codes are in the playerLetterCodes list
     *
     * @param playerLetterCodes         player codes to extract matches for
     * @param playerTieBreakingInfoList
     * @return
     */
    private List<PlayerTieBreakingInfo> extractTiedPlayersTieBreakingInfo(List<Character> playerLetterCodes, List<PlayerTieBreakingInfo> playerTieBreakingInfoList) {
        Map<String, Character> profileIdToLetterCodeMap = new HashMap();
        // find the matches only between the players who are tied
        List<PlayerTieBreakingInfo> tiedPlayersTieBreakingInfoList = new ArrayList<>();
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            char playerCode = playerTieBreakingInfo.getPlayerCode();
            if (playerLetterCodes.contains(playerCode)) {
                String playerProfileId = playerTieBreakingInfo.getPlayerProfileId();
                profileIdToLetterCodeMap.put(playerProfileId, playerCode);
                // create a separate info
                PlayerTieBreakingInfo reducedPlayerTieBreakingInfo =
                        new PlayerTieBreakingInfo(playerProfileId, playerCode, playerLetterCodes);

                // get match results for matches against this subset of opponents
                List<PlayerMatchResults> allPlayerMatchResults = playerTieBreakingInfo.getAllPlayerMatchResults();
                for (PlayerMatchResults playerMatchResult : allPlayerMatchResults) {
                    char opponentCode = playerMatchResult.getOpponentCode();
                    if (playerLetterCodes.contains(opponentCode) && opponentCode != playerCode) {
                        reducedPlayerTieBreakingInfo.setMatchResult(opponentCode,
                                playerMatchResult.getGameScores(),
                                playerMatchResult.getMatchStatus());
                    }
                }
                tiedPlayersTieBreakingInfoList.add(reducedPlayerTieBreakingInfo);
            }
        }
        return tiedPlayersTieBreakingInfoList;
    }

    /**
     * Builds a martix of all players matches
     *
     * @param matchCard
     * @param profileIdToLetterCodeMap
     * @param playerTieBreakingInfoList
     * @param pointsPerGame             points per game e.g. 11 or 21
     * @param numberOfGames             number of games in a match 3, 5, or 7
     */
    private void buildMatrix(MatchCard matchCard, Map<String, Character> profileIdToLetterCodeMap, List<PlayerTieBreakingInfo> playerTieBreakingInfoList, int pointsPerGame, int numberOfGames) {
        List<Match> matches = matchCard.getMatches();
        for (Match match : matches) {
            String playerAProfileId = match.getPlayerAProfileId();
            String playerBProfileId = match.getPlayerBProfileId();
            boolean isPlayerAMatchWinner = isMatchWinner(playerAProfileId, match, numberOfGames, pointsPerGame);
            boolean isPlayerBMatchWinner = isMatchWinner(playerBProfileId, match, numberOfGames, pointsPerGame);
            MatchStatus playerAMatchStatus = (isPlayerAMatchWinner ? MatchStatus.WIN : (!match.isSideADefaulted()) ? MatchStatus.LOSS : MatchStatus.NOT_PLAYED);
            MatchStatus playerBMatchStatus = (isPlayerBMatchWinner ? MatchStatus.WIN : (!match.isSideBDefaulted()) ? MatchStatus.LOSS : MatchStatus.NOT_PLAYED);
            Character playerACode = profileIdToLetterCodeMap.get(playerAProfileId);
            Character playerBCode = profileIdToLetterCodeMap.get(playerBProfileId);
            List<Integer> winnerCompactMatchNotation = createCompactMatchNotation(match, numberOfGames, isPlayerAMatchWinner);
            List<Integer> loserCompactMatchNotation = createOppositeCompactMatchNotation(winnerCompactMatchNotation);
            PlayerTieBreakingInfo playerATieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerACode);
            PlayerTieBreakingInfo playerBTieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerBCode);
            List<Integer> playerAGamesList = (isPlayerAMatchWinner) ? winnerCompactMatchNotation : loserCompactMatchNotation;
            List<Integer> playerBGamesList = (isPlayerBMatchWinner) ? winnerCompactMatchNotation : loserCompactMatchNotation;
            playerATieBreakingInfo.setMatchResult(playerBCode, playerAGamesList, playerAMatchStatus);
            playerBTieBreakingInfo.setMatchResult(playerACode, playerBGamesList, playerBMatchStatus);
        }
    }

    /**
     * @param winnerCompactMatchNotation
     * @return
     */
    public List<Integer> createOppositeCompactMatchNotation(List<Integer> winnerCompactMatchNotation) {
        List<Integer> loserCompactMatchNotation = new ArrayList<>(winnerCompactMatchNotation.size());
        for (Integer gamePoints : winnerCompactMatchNotation) {
            loserCompactMatchNotation.add(gamePoints * -1);
        }
        return loserCompactMatchNotation;
    }

    /**
     * Determines if specified player won this match
     *
     * @param profileId     player profile id to determine if he won
     * @param match         match to determine winner for
     * @param numberOfGames number of games in a match 3, 5, or 7
     * @param pointsPerGame points per game e.g. 11 or 21
     * @return
     */
    public boolean isMatchWinner(String profileId,
                                 Match match,
                                 int numberOfGames,
                                 int pointsPerGame) {
        int numGamesWonByA = 0;
        int numGamesWonByB = 0;
        for (int i = 0; i < numberOfGames; i++) {
            int playerAGameScore = 0;
            int playerBGameScore = 0;
            switch (i) {
                case 0:
                    playerAGameScore = match.getGame1ScoreSideA();
                    playerBGameScore = match.getGame1ScoreSideB();
                    break;
                case 1:
                    playerAGameScore = match.getGame2ScoreSideA();
                    playerBGameScore = match.getGame2ScoreSideB();
                    break;
                case 2:
                    playerAGameScore = match.getGame3ScoreSideA();
                    playerBGameScore = match.getGame3ScoreSideB();
                    break;
                case 3:
                    playerAGameScore = match.getGame4ScoreSideA();
                    playerBGameScore = match.getGame4ScoreSideB();
                    break;
                case 4:
                    playerAGameScore = match.getGame5ScoreSideA();
                    playerBGameScore = match.getGame5ScoreSideB();
                    break;
                case 5:
                    playerAGameScore = match.getGame6ScoreSideA();
                    playerBGameScore = match.getGame6ScoreSideB();
                    break;
                case 6:
                    playerAGameScore = match.getGame7ScoreSideA();
                    playerBGameScore = match.getGame7ScoreSideB();
                    break;
            }

            if (playerAGameScore >= pointsPerGame && playerBGameScore < playerAGameScore) {
                numGamesWonByA++;
            } else if (playerBGameScore >= pointsPerGame && playerAGameScore < playerBGameScore) {
                numGamesWonByB++;
            }
        }
        // in best of 3 need to win 2 games, best of 5 need to win 3, best of 7 need to win 4
        int minimumNumberOfGamesToWin = (numberOfGames == 3) ? 2 : ((numberOfGames == 5) ? 3 : 4);
        if (profileId.equals(match.getPlayerAProfileId())) {
            return (numGamesWonByA == minimumNumberOfGamesToWin) || (match.isSideBDefaulted() && !match.isSideADefaulted());
        } else {
            return (numGamesWonByB == minimumNumberOfGamesToWin) || (match.isSideADefaulted() && !match.isSideBDefaulted());
        }
    }

    /**
     * Gets a list of match game scores in compact way e.g. 7, -8, 9, 6
     *
     * @param match
     * @param numberOfGames
     * @param sideAWon
     * @return
     */
    public List<Integer> createCompactMatchNotation(Match match, int numberOfGames, boolean sideAWon) {
        List<Integer> gamesList = new ArrayList<>(numberOfGames);
        for (int i = 0; i < numberOfGames; i++) {
            int playerAGameScore = 0;
            int playerBGameScore = 0;
            switch (i) {
                case 0:
                    playerAGameScore = match.getGame1ScoreSideA();
                    playerBGameScore = match.getGame1ScoreSideB();
                    break;
                case 1:
                    playerAGameScore = match.getGame2ScoreSideA();
                    playerBGameScore = match.getGame2ScoreSideB();
                    break;
                case 2:
                    playerAGameScore = match.getGame3ScoreSideA();
                    playerBGameScore = match.getGame3ScoreSideB();
                    break;
                case 3:
                    playerAGameScore = match.getGame4ScoreSideA();
                    playerBGameScore = match.getGame4ScoreSideB();
                    break;
                case 4:
                    playerAGameScore = match.getGame5ScoreSideA();
                    playerBGameScore = match.getGame5ScoreSideB();
                    break;
                case 5:
                    playerAGameScore = match.getGame6ScoreSideA();
                    playerBGameScore = match.getGame6ScoreSideB();
                    break;
                case 6:
                    playerAGameScore = match.getGame7ScoreSideA();
                    playerBGameScore = match.getGame7ScoreSideB();
                    break;
            }

            // if we are not at the end of the match
            if (playerAGameScore != 0 || playerBGameScore != 0) {
                int gameScore = Math.min(playerAGameScore, playerBGameScore);
                // side A won the match but lost this game record this game as negative (i.e. lost)
                if (sideAWon) {
                    if (playerAGameScore < playerBGameScore) {
                        gameScore *= -1;
                    }
                } else {
                    // side B won the match, but lost this game
                    if (playerAGameScore > playerBGameScore) {
                        gameScore *= -1;
                    }
                }
                gamesList.add(gameScore);
            }
        }
        return gamesList;
    }

    /**
     * @param matches
     * @return
     */
    private Map<String, Character> makeProfileIdToLetterCodeMap(List<Match> matches) {
        Map<String, Character> profileIdToLetterCodeMap = new HashMap<>();
        for (Match match : matches) {
            profileIdToLetterCodeMap.put(match.getPlayerAProfileId(), match.getPlayerALetter());
            profileIdToLetterCodeMap.put(match.getPlayerBProfileId(), match.getPlayerBLetter());
        }
        return profileIdToLetterCodeMap;
    }

    /**
     * @param playerTieBreakingInfoList
     * @param playerCode
     * @return
     */
    private PlayerTieBreakingInfo getPlayerTieBreakingInfo(List<PlayerTieBreakingInfo> playerTieBreakingInfoList, char playerCode) {
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            if (playerTieBreakingInfo.getPlayerCode() == playerCode) {
                return playerTieBreakingInfo;
            }
        }
        return null;
    }

    /**
     * @param matchCard             Match card which was just entered
     * @param tournamentEvent event information for this match
     * @param groupTieBreakingInfo  tie breaking information
     */
    private void advancePlayers(MatchCard matchCard,
                                TournamentEvent tournamentEvent,
                                GroupTieBreakingInfo groupTieBreakingInfo) {
        // create a JSON representation of the ranking
        // extract player ranking
        List<PlayerTieBreakingInfo> playerTieBreakingInfoList = groupTieBreakingInfo.getPlayerTieBreakingInfoList();
        Map<Integer, String> rankToProfileIdMap = new TreeMap<>();
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            rankToProfileIdMap.put(playerTieBreakingInfo.getRank(), playerTieBreakingInfo.getPlayerProfileId());
        }

        // record player ranking but only for round robin round
//        if (matchCard.getDrawType() == DrawType.ROUND_ROBIN) {
            // convert to a string for saving in match card
            try {
                StringWriter stringWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.writeValue(stringWriter, rankToProfileIdMap);
                String content = stringWriter.toString();
                matchCard.setPlayerRankings(content);
                matchCardService.save(matchCard);
            } catch (IOException e) {
                e.printStackTrace();
            }
//        }

        // round robin match card rounds are 0, single elimination rounds cards are 2, 4, 8 etc,
        // so if this is the last round i.e. 2 there is nowhere to advance to
        // todo unless we advance to another event
        if (matchCard.getRound() > 2 || matchCard.getRound() == 0) {
            // collect player ratings into map of profile id to rating
            Map<String, Integer> playerProfileToRatingMap = new HashMap<>();
            List<Match> matches = matchCard.getMatches();
            for (Match match : matches) {
                playerProfileToRatingMap.put(match.getPlayerAProfileId(), match.getPlayerARating());
                playerProfileToRatingMap.put (match.getPlayerBProfileId(), match.getPlayerBRating());
            }
//            Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
//            for (Map.Entry<String, String> entry : profileIdToNameMap.entrySet()) {
//                System.out.println(entry.getKey()+ " => " + entry.getValue());
//            }
            drawService.advancePlayers(matchCard.getDrawType(), matchCard.getGroupNum(), matchCard.getRound(),
                    tournamentEvent, rankToProfileIdMap, playerProfileToRatingMap);
        }

        // store the final player rankings and names so we can distribute prize money and trophies
        prizeAwardingService.processCompletedMatchCard(matchCard, tournamentEvent);
    }

    /**
     * Determines places by a lot
     * @param playerTieBreakingInfoList
     */
    private void determineByLot(List<PlayerTieBreakingInfo> playerTieBreakingInfoList) {
        int someInt = new Random().nextInt();
        System.out.println("someInt = " + someInt);
        boolean isHead = (someInt % 2 == 0);
        System.out.println("isHead = " + isHead);
        // heads - 1st player wins, tails 2nd player wins
        int playerIndex = 0;
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            if (playerIndex == 0) {
                int rank = (isHead) ? 1 : 2;
                playerTieBreakingInfo.setRank(rank);
            } else {
                int rank = (isHead) ? 2 : 1;
                playerTieBreakingInfo.setRank(rank);
            }
            playerIndex++;
        }
    }
}
