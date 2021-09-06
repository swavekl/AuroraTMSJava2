package com.auroratms.tiebreaking;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.tiebreaking.model.GroupTieBreakingInfo;
import com.auroratms.tiebreaking.model.MatchStatus;
import com.auroratms.tiebreaking.model.PlayerMatchResults;
import com.auroratms.tiebreaking.model.PlayerTieBreakingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Computes ranking and causes players to advance to next round
     *
     * @param matchCardId
     */
    public GroupTieBreakingInfo rankAndAdvancePlayers(Long matchCardId) {
        MatchCard matchCard = matchCardService.get(matchCardId);

        // get number of games and points per game
        long eventFk = matchCard.getEventFk();
        TournamentEventEntity tournamentEventEntity = tournamentEventEntityService.get(eventFk);
        int pointsPerGame = tournamentEventEntity.getPointsPerGame();
        int numberOfGames = matchCard.getNumberOfGames();

        return rankAndAdvancePlayersInternal(matchCard, pointsPerGame, numberOfGames);
    }

    /**
     * @param matchCard
     * @param pointsPerGame
     * @param numberOfGames
     * @return
     */
    public GroupTieBreakingInfo rankAndAdvancePlayersInternal(MatchCard matchCard, int pointsPerGame, int numberOfGames) {
        Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
        int numPlayers = profileIdToNameMap.size();
        List<Character> allPlayerCodes = new ArrayList<>(numPlayers);
        for (int i = 0; i < numPlayers; i++) {
            allPlayerCodes.add((char) ('A' + i));
        }
        Map<String, Character> profileIdToLetterCodeMap = makeProfileIdToLetterCodeMap(matchCard.getMatches());

        GroupTieBreakingInfo groupTieBreakingInfo = new GroupTieBreakingInfo();
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


        buildMatrix(matchCard, profileIdToLetterCodeMap, playerTieBreakingInfoList, pointsPerGame, numberOfGames);

        Map<String, List<PlayerTieBreakingInfo>> nWayTieMap = performTieBreakingProcedure(groupTieBreakingInfo.getPlayerTieBreakingInfoList(), true, pointsPerGame);
        groupTieBreakingInfo.setNWayTieBreakingInfoList(nWayTieMap);

        return groupTieBreakingInfo;
    }

    /**
     * Performs tie breaking procedure for the set of player matches
     *
     * @param playerTieBreakingInfoList
     * @param firstLevel
     * @param pointsPerGame
     */
    private Map<String, List<PlayerTieBreakingInfo>> performTieBreakingProcedure(List<PlayerTieBreakingInfo> playerTieBreakingInfoList,
                                                                                 boolean firstLevel,
                                                                                 int pointsPerGame) {
        Map<String, List<PlayerTieBreakingInfo>> nWayTieMap = new HashMap<>();
        // compute match points
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            calculateMatchPoints(playerTieBreakingInfo);
        }

        // see if there are any tie-breaks
        // build a sorted map of match points to player codes who have that many match points
        Map<Integer, List<Character>> matchPointsToLetterCodes = new TreeMap<>(Collections.reverseOrder());
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            int matchPoints = playerTieBreakingInfo.getMatchPoints();
            List<Character> playerCodes = matchPointsToLetterCodes.computeIfAbsent(matchPoints, k -> new ArrayList<>());
            playerCodes.add(playerTieBreakingInfo.getPlayerCode());
        }

        // make a first pass assigning ranks assuming there are some players not requiring tie-breaks
        int rank = 1;
        for (Integer matchPoints : matchPointsToLetterCodes.keySet()) {
            List<Character> playerLetterCodes = matchPointsToLetterCodes.get(matchPoints);
            for (Character playerLetterCode : playerLetterCodes) {
                PlayerTieBreakingInfo playerTieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerLetterCode);
                if (playerTieBreakingInfo != null) {
                    playerTieBreakingInfo.setRank(rank);
                }
            }
            rank += playerLetterCodes.size();
        }

        // determine if we have any ties.  if not every player has a distinct number of match points then we have a tie somewhere
        boolean hasTies = (playerTieBreakingInfoList.size() != matchPointsToLetterCodes.size());
        if (hasTies) {
            if (firstLevel) {
                nWayTieMap = resolveTies(matchPointsToLetterCodes, playerTieBreakingInfoList, pointsPerGame);
            } else {
                hasTies = determineRankBasedOnGamesRatio(playerTieBreakingInfoList);
                if (hasTies) {
                    hasTies = determineRankBasedOnPointsRatio(playerTieBreakingInfoList, pointsPerGame);
                    if (hasTies) {
                        // determine by lot
                    }
                }
            }
        }
        return nWayTieMap;
    }

    /**
     * @param playerTieBreakingInfoList
     * @param pointsPerGame
     */
    private boolean determineRankBasedOnPointsRatio(List<PlayerTieBreakingInfo> playerTieBreakingInfoList,
                                                    int pointsPerGame) {
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            calculatePointsWonToLost(playerTieBreakingInfo, pointsPerGame);
        }
        // consider points won / lost ratio
        // put them in the sorted map
        Map<Float, List<Character>> pointsRatioToPlayerCodeMap = new TreeMap<>(Collections.reverseOrder());
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
//            System.out.println("player " + playerTieBreakingInfo.getPlayerCode() +
//                    " => W / L = " + playerTieBreakingInfo.getPointsWon() + " / " + playerTieBreakingInfo.getPointsLost());
            float pointsRatio = ((float) playerTieBreakingInfo.getPointsWon() / (float) playerTieBreakingInfo.getPointsLost());
            List<Character> playerCodes = pointsRatioToPlayerCodeMap.computeIfAbsent(pointsRatio, k -> new ArrayList<>());
            playerCodes.add(playerTieBreakingInfo.getPlayerCode());
            pointsRatioToPlayerCodeMap.put(pointsRatio, playerCodes);
        }

        // see if there are any ties based on this ratio
        // if every player has a different ratio then we don't have any ties
        boolean hasTies = (playerTieBreakingInfoList.size() != pointsRatioToPlayerCodeMap.size());

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

        return hasTies;
    }

    /**
     * @param playerTieBreakingInfoList
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

        // see if there are any ties based on this ratio
        // if every player has a different ratio then we don't have any ties
        boolean hasTies = (playerTieBreakingInfoList.size() != gamesRatioToPlayerCodeMap.size());

        int rank = 1;
        for (Float ratio : gamesRatioToPlayerCodeMap.keySet()) {
            List<Character> playerCodes = gamesRatioToPlayerCodeMap.get(ratio);
            if (playerCodes.size() == 1) {
                char playerCode = playerCodes.get(0);
                PlayerTieBreakingInfo playerTieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerCode);
                playerTieBreakingInfo.setRank(rank);
            } else {
//                if (playerCodes.size() == 2) {
//                    // two-way tie
//                    resolveTwoWayTie(playerCodes, playerTieBreakingInfoList);
//                } else if (playerCodes.size() > 2) {
//                    // another n-way tie
//                    resolveNWayTie(playerCodes, playerTieBreakingInfoList);
//                }
            }
            rank += playerCodes.size();
        }
        return hasTies;
    }

    /**
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
     * @param pointsPerGame
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
     * Resolves 2 way tie. the winner has a higher rank than looser
     *
     * @param matchPointsToLetterCodes
     * @param playerTieBreakingInfoList
     * @param pointsPerGame
     */
    private Map<String, List<PlayerTieBreakingInfo>> resolveTies(Map<Integer, List<Character>> matchPointsToLetterCodes,
                                                                 List<PlayerTieBreakingInfo> playerTieBreakingInfoList,
                                                                 int pointsPerGame) {
        Map<String, List<PlayerTieBreakingInfo>> nWayTiesMap = new HashMap<>();
        // resolve ties if any
        for (Integer matchPoints : matchPointsToLetterCodes.keySet()) {
            List<Character> playerLetterCodes = matchPointsToLetterCodes.get(matchPoints);
            if (playerLetterCodes.size() > 1) {
                List<PlayerTieBreakingInfo> tiedPlayersTieBreakingInfoList = null;
                if (playerLetterCodes.size() == 2) {
                    // resolve two-way tie
                    tiedPlayersTieBreakingInfoList = resolveTwoWayTie(playerLetterCodes, playerTieBreakingInfoList);
                } else {
                    // resolve 3 or more way tie
                    tiedPlayersTieBreakingInfoList = resolveNWayTie(playerLetterCodes, playerTieBreakingInfoList, pointsPerGame);
                }
                String tieBreakingListKey = String.format("Tie breaking between players %s", playerLetterCodes);
                nWayTiesMap.put(tieBreakingListKey, tiedPlayersTieBreakingInfoList);
            }
        }
        return nWayTiesMap;
    }

    /**
     * @param playerLetterCodes
     * @param playerTieBreakingInfoList
     * @param pointsPerGame
     */
    private List<PlayerTieBreakingInfo> resolveNWayTie(List<Character> playerLetterCodes, List<PlayerTieBreakingInfo> playerTieBreakingInfoList, int pointsPerGame) {
        List<PlayerTieBreakingInfo> tiedPlayersTieBreakingInfoList = extractTiedPlayersTieBreakingInfo(playerLetterCodes, playerTieBreakingInfoList);
        performTieBreakingProcedure(tiedPlayersTieBreakingInfoList, false, pointsPerGame);

        for (PlayerTieBreakingInfo tiedPlayerTieBreakingInfo : tiedPlayersTieBreakingInfoList) {
            char playerCode = tiedPlayerTieBreakingInfo.getPlayerCode();
            // compute the final rank and set it
            int tieResolvingRank = tiedPlayerTieBreakingInfo.getRank();
            PlayerTieBreakingInfo playerTieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerCode);
            int rank = playerTieBreakingInfo.getRank();
            rank += (tieResolvingRank - 1);
            playerTieBreakingInfo.setRank(rank);
        }
        return tiedPlayersTieBreakingInfoList;
    }

    /**
     * @param playerLetterCodes
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
     * @param playerLetterCodes
     * @param playerTieBreakingInfoList
     * @return
     */
    private List<PlayerTieBreakingInfo> resolveTwoWayTie(List<Character> playerLetterCodes, List<PlayerTieBreakingInfo> playerTieBreakingInfoList) {
        Map<Character, Integer> ranks = new HashMap<>();

        // find a match between these 2 players
        char player1 = playerLetterCodes.get(0);
        char player2 = playerLetterCodes.get(1);
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            if (playerTieBreakingInfo.getPlayerCode() == player1) {
                List<PlayerMatchResults> allPlayerMatchResults = playerTieBreakingInfo.getAllPlayerMatchResults();
                for (PlayerMatchResults playerMatchResult : allPlayerMatchResults) {
                    if (playerMatchResult.getOpponentCode() == player2) {
                        MatchStatus matchStatus = playerMatchResult.getMatchStatus();
                        if (matchStatus == MatchStatus.WIN) {
                            ranks.put(player1, 1);
                            ranks.put(player2, 2);
                        } else if (matchStatus == MatchStatus.LOSS) {
                            ranks.put(player2, 1);
                            ranks.put(player1, 2);
                        }
                        break;
                    }
                }
            }
        }

        // compute overall rank and set it
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            for (Character playerCode : ranks.keySet()) {
                if (playerTieBreakingInfo.getPlayerCode() == playerCode) {
                    Integer tieBreakingRank = ranks.get(playerCode);
                    int overallRank = playerTieBreakingInfo.getRank();
                    overallRank += (tieBreakingRank - 1);
                    playerTieBreakingInfo.setRank(overallRank);
                    break;
                }
            }
        }

        // prepare the rank within these 2 players and return it
        List<PlayerTieBreakingInfo> tiedPlayersTieBreakingInfoList = extractTiedPlayersTieBreakingInfo(playerLetterCodes, playerTieBreakingInfoList);
        for (PlayerTieBreakingInfo playerTieBreakingInfo : tiedPlayersTieBreakingInfoList) {
            for (Character playerCode : ranks.keySet()) {
                if (playerTieBreakingInfo.getPlayerCode() == playerCode) {
                    Integer tieBreakingRank = ranks.get(playerCode);
                    playerTieBreakingInfo.setRank(tieBreakingRank);
                }
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
     * @param pointsPerGame
     * @param numberOfGames
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
            List<Integer> winnerCompactMatchNotation = createCompactMatchNotation(match, numberOfGames, pointsPerGame, isPlayerAMatchWinner);
            List<Integer> loserCompactMatchNotation = createOppositeCompactMatchNotation(winnerCompactMatchNotation);
            PlayerTieBreakingInfo playerATieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerACode);
            PlayerTieBreakingInfo playerBTieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerBCode);
            List<Integer> playerAGamesList = (isPlayerAMatchWinner) ? winnerCompactMatchNotation : loserCompactMatchNotation;
            List<Integer> playerBGamesList = (isPlayerBMatchWinner) ? winnerCompactMatchNotation : loserCompactMatchNotation;
            playerATieBreakingInfo.setMatchResult(playerBCode, playerAGamesList, playerAMatchStatus);
            playerBTieBreakingInfo.setMatchResult(playerACode, playerBGamesList, playerBMatchStatus);
        }
    }

    public List<Integer> createOppositeCompactMatchNotation(List<Integer> winnerCompactMatchNotation) {
        List<Integer> loserCompactMatchNotation = new ArrayList<>(winnerCompactMatchNotation.size());
        for (Integer gamePoints : winnerCompactMatchNotation) {
            loserCompactMatchNotation.add(gamePoints * -1);
        }
        return loserCompactMatchNotation;
    }

    /**
     * @param profileId
     * @param match
     * @param numberOfGames
     * @param pointsPerGame
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
     * @param pointsPerGame
     * @param playerMatchStatus
     * @return
     */
    public List<Integer> createCompactMatchNotation(Match match, int numberOfGames, int pointsPerGame, boolean sideAWon) {
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
}
