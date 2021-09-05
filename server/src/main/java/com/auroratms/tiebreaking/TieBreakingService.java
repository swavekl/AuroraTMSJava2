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
        Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
        int numPlayers = profileIdToNameMap.size();
        Map<String, Character> profileIdToLetterCodeMap = makeProfileIdToLetterCodeMap(matchCard.getMatches());

        GroupTieBreakingInfo groupTieBreakingInfo = new GroupTieBreakingInfo();
        List<PlayerTieBreakingInfo> playerTieBreakingInfoList = new ArrayList<>();
        groupTieBreakingInfo.setPlayerTieBreakingInfoList(playerTieBreakingInfoList);
        for (String playerProfileId : profileIdToNameMap.keySet()) {
            Character playerCode = profileIdToLetterCodeMap.get(playerProfileId);
            PlayerTieBreakingInfo playerTieBreakingInfo = new PlayerTieBreakingInfo(playerProfileId, numPlayers, playerCode);
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

        // get number of games and points
        long eventFk = matchCard.getEventFk();
        TournamentEventEntity tournamentEventEntity = tournamentEventEntityService.get(eventFk);
        int pointsPerGame = tournamentEventEntity.getPointsPerGame();
        int numberOfGames = matchCard.getNumberOfGames();

        buildMatrix(matchCard, profileIdToLetterCodeMap, playerTieBreakingInfoList, pointsPerGame, numberOfGames);

        performTieBreakingProcedure(groupTieBreakingInfo, profileIdToLetterCodeMap);

        return groupTieBreakingInfo;
    }

    private void performTieBreakingProcedure(GroupTieBreakingInfo groupTieBreakingInfo, Map<String, Character> profileIdToLetterCodeMap) {
        // compute match points
        List<PlayerTieBreakingInfo> playerTieBreakingInfoList = groupTieBreakingInfo.getPlayerTieBreakingInfoList();
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            calculateMatchPoints(playerTieBreakingInfo);
        }

        // see if there are any tie-breaks
        // build a sorted map of match points to player codes who have that many match points
        Map<Integer, List<Character>> matchPointsToLetterCodes = new TreeMap<Integer, List<Character>>(Collections.reverseOrder());
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            int matchPoints = playerTieBreakingInfo.getMatchPoints();
            List<Character> playerCodes = matchPointsToLetterCodes.get(matchPoints);
            if (playerCodes == null) {
                playerCodes = new ArrayList<>();
                matchPointsToLetterCodes.put(matchPoints, playerCodes);
            }
            playerCodes.add(playerTieBreakingInfo.getPlayerCode());
        }

        // make a first pass assigning ranks assuming there are some players not requiring tie-breaks
        int rank = 1;
        for (Integer matchPoints : matchPointsToLetterCodes.keySet()) {
            List<Character> playerLetterCodes = matchPointsToLetterCodes.get(matchPoints);
            for (Character playerLetterCode : playerLetterCodes) {
                String foundPlayerProfileId = null;
                for (String playerProfileId : profileIdToLetterCodeMap.keySet()) {
                    if (profileIdToLetterCodeMap.get(playerProfileId).equals(playerLetterCode)) {
                        foundPlayerProfileId = playerProfileId;
                        break;
                    }
                }
                PlayerTieBreakingInfo playerTieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, foundPlayerProfileId);
                playerTieBreakingInfo.setRank(rank);
            }
            rank += playerLetterCodes.size();
        }
    }

    private void calculateMatchPoints(PlayerTieBreakingInfo playerTieBreakingInfo) {
        char playerCode = playerTieBreakingInfo.getPlayerCode();
        List<PlayerMatchResults> allPlayerMatchResults = playerTieBreakingInfo.getAllPlayerMatchResults();
        int matchPoints = 0;
        for (PlayerMatchResults matchResult : allPlayerMatchResults) {
            if (matchResult.getOpponentCode() != playerCode) {
                MatchStatus matchStatus = matchResult.getMatchStatus();
                matchPoints += (matchStatus == MatchStatus.WIN) ? 2 :
                        (matchStatus == MatchStatus.LOSS ? 1 : 0);
            }
        }
        playerTieBreakingInfo.setMatchPoints(matchPoints);
    }

    private void buildMatrix(MatchCard matchCard, Map<String, Character> profileIdToLetterCodeMap, List<PlayerTieBreakingInfo> playerTieBreakingInfoList, int pointsPerGame, int numberOfGames) {
        List<Match> matches = matchCard.getMatches();
        for (Match match : matches) {
            String playerAProfileId = match.getPlayerAProfileId();
            String playerBProfileId = match.getPlayerBProfileId();
            boolean isPlayerAMatchWinner = isMatchWinner(playerAProfileId, match, numberOfGames, pointsPerGame);
            boolean isPlayerBMatchWinner = isMatchWinner(playerBProfileId, match, numberOfGames, pointsPerGame);
            MatchStatus playerAMatchStatus = (isPlayerAMatchWinner ? MatchStatus.WIN : (!match.isSideADefaulted()) ? MatchStatus.LOSS : MatchStatus.NOT_PLAYED);
            MatchStatus playerBMatchStatus = (isPlayerBMatchWinner ? MatchStatus.WIN : (!match.isSideBDefaulted()) ? MatchStatus.LOSS : MatchStatus.NOT_PLAYED);
            PlayerTieBreakingInfo playerATieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerAProfileId);
            PlayerTieBreakingInfo playerBTieBreakingInfo = getPlayerTieBreakingInfo(playerTieBreakingInfoList, playerBProfileId);
            Character playerAOpponentLetterCode = profileIdToLetterCodeMap.get(playerBProfileId);
            Character playerBOpponentLetterCode = profileIdToLetterCodeMap.get(playerAProfileId);
            List<Integer> playerAGamesList = makePlayerGamesList(match, numberOfGames, pointsPerGame, playerAMatchStatus);
            List<Integer> playerBGamesList = makePlayerGamesList(match, numberOfGames, pointsPerGame, playerBMatchStatus);
            playerATieBreakingInfo.setMatchResult(playerAOpponentLetterCode, playerAGamesList, playerAMatchStatus);
            playerBTieBreakingInfo.setMatchResult(playerBOpponentLetterCode, playerBGamesList, playerBMatchStatus);
        }
    }

    /**
     * @param profileId
     * @param match
     * @param numberOfGames
     * @param pointsPerGame
     * @return
     */
    private boolean isMatchWinner(String profileId,
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
    private List<Integer> makePlayerGamesList(Match match, int numberOfGames, int pointsPerGame, MatchStatus playerMatchStatus) {
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
            int gameScore = 0;
            if (playerMatchStatus == MatchStatus.WIN) {
                // player won this match
                if (playerAGameScore >= pointsPerGame && playerBGameScore < playerAGameScore) {
                    // player A won this game so record how many points given up in this game - positive number
                    gameScore = playerBGameScore;
                    gamesList.add(gameScore);
                } else if (playerBGameScore >= pointsPerGame && playerAGameScore < playerBGameScore) {
                    // player A lost this game record how many points won in this game as negative number
                    gameScore = -1 * playerAGameScore;
                    gamesList.add(gameScore);
                }
            } else if (playerMatchStatus == MatchStatus.LOSS) {
                // player lost this match
                if (playerAGameScore >= pointsPerGame && playerBGameScore < playerAGameScore) {
                    // player A won this game so record how many points given up in this game as negative number
                    gameScore = -1 * playerBGameScore;
                    gamesList.add(gameScore);
                } else if (playerBGameScore >= pointsPerGame && playerAGameScore < playerBGameScore) {
                    // player A lost this game record how many points won in this game as positive number
                    gameScore = playerAGameScore;
                    gamesList.add(gameScore);
                }
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
     * @param playerBProfileId
     * @return
     */
    private PlayerTieBreakingInfo getPlayerTieBreakingInfo(List<PlayerTieBreakingInfo> playerTieBreakingInfoList, String playerBProfileId) {
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            if (playerTieBreakingInfo.getPlayerProfileId().equals(playerBProfileId)) {
                return playerTieBreakingInfo;
            }
        }
        return null;
    }
}
