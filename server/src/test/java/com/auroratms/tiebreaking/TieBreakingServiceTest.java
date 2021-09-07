package com.auroratms.tiebreaking;

import com.auroratms.AbstractServiceTest;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.tiebreaking.model.GroupTieBreakingInfo;
import com.auroratms.tiebreaking.model.MatchStatus;
import com.auroratms.tiebreaking.model.PlayerTieBreakingInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.*;

@Transactional
public class TieBreakingServiceTest extends AbstractServiceTest {

    @Autowired
    private TieBreakingService tieBreakingService;

    @Test
    public void testMakingGamesPointList_A_LosesTo_B() {
        int pointsPerGame = 11;
        int numberOfGames = 5;
        Match match = makeMatch('A', 'B', ArrayUtils.toArray(7, -7, 8, -9, -8), true);

        boolean isAWinner = tieBreakingService.isMatchWinner(makePlayerProfileId('A'), match, numberOfGames, pointsPerGame);
        assertFalse("A should be a loser", isAWinner);
        boolean isBWinner = tieBreakingService.isMatchWinner(makePlayerProfileId('B'), match, numberOfGames, pointsPerGame);
        assertTrue("B should be winner", isBWinner);

        List<Integer> winnerCompactMatchNotation = tieBreakingService.createCompactMatchNotation(match, numberOfGames, pointsPerGame, false);
        List<Integer> loserCompactMatchNotation = tieBreakingService.createOppositeCompactMatchNotation(winnerCompactMatchNotation);
        assertEquals("player A games list wrong", ArrayUtils.toArray(7, -7, 8, -9, -8),
                loserCompactMatchNotation.toArray());
        assertEquals("player B games list wrong", ArrayUtils.toArray(-7, 7, -8, 9, 8),
                winnerCompactMatchNotation.toArray());
    }

    @Test
    public void testMakingGamesPointList_A_LosesTo_B_WithZeroPoints() {
        int pointsPerGame = 11;
        int numberOfGames = 5;
        Match match = makeMatch('A', 'B', ArrayUtils.toArray(7, -7, -8, 0), false);

        boolean isAWinner = tieBreakingService.isMatchWinner(makePlayerProfileId('A'), match, numberOfGames, pointsPerGame);
        assertFalse("A should be a loser", isAWinner);
        boolean isBWinner = tieBreakingService.isMatchWinner(makePlayerProfileId('B'), match, numberOfGames, pointsPerGame);
        assertTrue("B should be winner", isBWinner);

        List<Integer> winnerCompactMatchNotation = tieBreakingService.createCompactMatchNotation(match, numberOfGames, pointsPerGame, false);
        List<Integer> loserCompactMatchNotation = tieBreakingService.createOppositeCompactMatchNotation(winnerCompactMatchNotation);
        assertEquals("player A games list wrong", ArrayUtils.toArray(7, -7, -8, 0),
                loserCompactMatchNotation.toArray());
        assertEquals("player B games list wrong", ArrayUtils.toArray(-7, 7, 8, 0),
                winnerCompactMatchNotation.toArray());
    }

    @Test
    public void testMakingGamesPointList_A_Beats_D() {
        int pointsPerGame = 11;
        int numberOfGames = 5;
        Match match = makeMatch('A', 'D', ArrayUtils.toArray(-9, 9, 4, -9, 7), true);
        boolean isAWinner = tieBreakingService.isMatchWinner(makePlayerProfileId('A'), match, numberOfGames, pointsPerGame);
        assertTrue("A should be a loser", isAWinner);
        boolean isDWinner = tieBreakingService.isMatchWinner(makePlayerProfileId('D'), match, numberOfGames, pointsPerGame);
        assertFalse("D should be winner", isDWinner);

        List<Integer> winnerCompactMatchNotation = tieBreakingService.createCompactMatchNotation(match, numberOfGames, pointsPerGame, true);
        List<Integer> loserCompactMatchNotation = tieBreakingService.createOppositeCompactMatchNotation(winnerCompactMatchNotation);
        assertEquals("player A games list wrong", ArrayUtils.toArray(-9, 9, 4, -9, 7),
                winnerCompactMatchNotation.toArray());
        assertEquals("player D games list wrong", ArrayUtils.toArray(9, -9, -4, 9, -7),
                loserCompactMatchNotation.toArray());

    }

    @Test
    public void testMakingGamesPointList_A_Beats_D_WithZeroPoints() {
        int pointsPerGame = 11;
        int numberOfGames = 5;
        Match match = makeMatch('A', 'D', ArrayUtils.toArray(-9, 9, 4, -9, 0), true);
        boolean isAWinner = tieBreakingService.isMatchWinner(makePlayerProfileId('A'), match, numberOfGames, pointsPerGame);
        assertTrue("A should be a loser", isAWinner);
        boolean isDWinner = tieBreakingService.isMatchWinner(makePlayerProfileId('D'), match, numberOfGames, pointsPerGame);
        assertFalse("D should be winner", isDWinner);

        List<Integer> winnerCompactMatchNotation = tieBreakingService.createCompactMatchNotation(match, numberOfGames, pointsPerGame, true);
        List<Integer> loserCompactMatchNotation = tieBreakingService.createOppositeCompactMatchNotation(winnerCompactMatchNotation);
        assertEquals("player A games list wrong", ArrayUtils.toArray(-9, 9, 4, -9, 0),
                winnerCompactMatchNotation.toArray());
        assertEquals("player D games list wrong", ArrayUtils.toArray(9, -9, -4, 9, 0),
                loserCompactMatchNotation.toArray());

    }

    @Test
    public void testMakingGamesPointList_A_Beats_C_ByDefault() {
        int pointsPerGame = 11;
        int numberOfGames = 5;
        Integer[] unfinishedMatch = {};
        Match match = makeMatch('A', 'C', unfinishedMatch, true, false, true);

        boolean isAWinner = tieBreakingService.isMatchWinner(makePlayerProfileId('A'), match, numberOfGames, pointsPerGame);
        assertTrue("A should be a winner by default", isAWinner);
        boolean isCWinner = tieBreakingService.isMatchWinner(makePlayerProfileId('C'), match, numberOfGames, pointsPerGame);
        assertFalse("C should be loser by default", isCWinner);

        List<Integer> winnerCompactMatchNotation = tieBreakingService.createCompactMatchNotation(match, numberOfGames, pointsPerGame, false);
        List<Integer> loserCompactMatchNotation = tieBreakingService.createOppositeCompactMatchNotation(winnerCompactMatchNotation);
        assertEquals("player A games list wrong", unfinishedMatch,
                loserCompactMatchNotation.toArray());
        assertEquals("player C games list wrong", unfinishedMatch,
                winnerCompactMatchNotation.toArray());

    }

    @Test
    public void testMakingGamesPointList_A_vs_E_MatchNotPlayed() {
        int pointsPerGame = 11;
        int numberOfGames = 5;
        Integer[] unfinishedMatch = {};
        Match match = makeMatch('A', 'E', unfinishedMatch, true, true, true);

        boolean isAWinner = tieBreakingService.isMatchWinner(makePlayerProfileId('A'), match, numberOfGames, pointsPerGame);
        assertFalse("A should NOT be a winner by default", isAWinner);
        boolean isEWinner = tieBreakingService.isMatchWinner(makePlayerProfileId('E'), match, numberOfGames, pointsPerGame);
        assertFalse("C should NOT be loser by default", isEWinner);

        List<Integer> winnerCompactMatchNotation = tieBreakingService.createCompactMatchNotation(match, numberOfGames, pointsPerGame, false);
        List<Integer> loserCompactMatchNotation = tieBreakingService.createOppositeCompactMatchNotation(winnerCompactMatchNotation);
        assertEquals("player A games list wrong", unfinishedMatch,
                loserCompactMatchNotation.toArray());
        assertEquals("player C games list wrong", unfinishedMatch,
                winnerCompactMatchNotation.toArray());

    }

    @Test
    public void testCalculateWonVsLost_A() {
        int pointsPerGame = 11;
        char playerCode = 'A';
        List<Character> allPlayerCodes = Arrays.asList('A', 'B', 'D');
        PlayerTieBreakingInfo playerTieBreakingInfo = new PlayerTieBreakingInfo(makePlayerProfileId(playerCode), playerCode, allPlayerCodes);
        List<Integer> A_vs_B_gamesResults = Arrays.asList(7, -7, 8, -9, -8);
        playerTieBreakingInfo.setMatchResult('B', A_vs_B_gamesResults, MatchStatus.LOSS);
        List<Integer> A_vs_D_gamesResults = Arrays.asList(-9, 9, 4, -9, 7);
        playerTieBreakingInfo.setMatchResult('D', A_vs_D_gamesResults, MatchStatus.WIN);
        tieBreakingService.calculateGamesWonToLostRatio(playerTieBreakingInfo);
        assertEquals("wrong games won calculation", 5, playerTieBreakingInfo.getGamesWon());
        assertEquals("wrong games lost calculation", 5, playerTieBreakingInfo.getGamesLost());
        tieBreakingService.calculatePointsWonToLost(playerTieBreakingInfo, pointsPerGame);
        assertEquals("wrong points won calculation", 97, playerTieBreakingInfo.getPointsWon());
        assertEquals("wrong points lost calculation", 90, playerTieBreakingInfo.getPointsLost());
    }

    @Test
    public void testCalculateWonVsLost_B() {
        int pointsPerGame = 11;
        char playerCode = 'B';
        List<Character> allPlayerCodes = Arrays.asList('A', 'B', 'D');
        PlayerTieBreakingInfo playerTieBreakingInfo = new PlayerTieBreakingInfo(makePlayerProfileId(playerCode), playerCode, allPlayerCodes);
        List<Integer> A_vs_B_gamesResults = Arrays.asList(-7, 7, -8, 9, 8);
        playerTieBreakingInfo.setMatchResult('A', A_vs_B_gamesResults, MatchStatus.WIN);
        List<Integer> A_vs_D_gamesResults = Arrays.asList(7, -10, -9, 9, -11);
        playerTieBreakingInfo.setMatchResult('D', A_vs_D_gamesResults, MatchStatus.LOSS);

        tieBreakingService.calculateGamesWonToLostRatio(playerTieBreakingInfo);
        assertEquals("wrong games won calculation", 5, playerTieBreakingInfo.getGamesWon());
        assertEquals("wrong games lost calculation", 5, playerTieBreakingInfo.getGamesLost());

        tieBreakingService.calculatePointsWonToLost(playerTieBreakingInfo, pointsPerGame);
        assertEquals("wrong points won calculation", 100, playerTieBreakingInfo.getPointsWon());
        assertEquals("wrong points lost calculation", 98, playerTieBreakingInfo.getPointsLost());
    }

    @Test
    public void testCalculateWonVsLost_D() {
        int pointsPerGame = 11;
        char playerCode = 'D';
        List<Character> allPlayerCodes = Arrays.asList('A', 'B', 'D');
        PlayerTieBreakingInfo playerTieBreakingInfo = new PlayerTieBreakingInfo(makePlayerProfileId(playerCode), playerCode, allPlayerCodes);
        List<Integer> A_vs_B_gamesResults = Arrays.asList(9, -9, -4, 9, -7);
        playerTieBreakingInfo.setMatchResult('A', A_vs_B_gamesResults, MatchStatus.LOSS);
        List<Integer> A_vs_D_gamesResults = Arrays.asList(-7, 10, 9, -9, 11);
        playerTieBreakingInfo.setMatchResult('B', A_vs_D_gamesResults, MatchStatus.WIN);
        tieBreakingService.calculateGamesWonToLostRatio(playerTieBreakingInfo);
        assertEquals("wrong games won calculation", 5, playerTieBreakingInfo.getGamesWon());
        assertEquals("wrong games lost calculation", 5, playerTieBreakingInfo.getGamesLost());
        tieBreakingService.calculatePointsWonToLost(playerTieBreakingInfo, pointsPerGame);
        assertEquals("wrong points won calculation", 94, playerTieBreakingInfo.getPointsWon());
        assertEquals("wrong points lost calculation", 103, playerTieBreakingInfo.getPointsLost());
    }

    @Test
    public void testCalculateWonVsLost_B_vs_D_E() {
        int pointsPerGame = 11;
        char playerCode = 'B';
        List<Character> allPlayerCodes = Arrays.asList('B', 'D', 'E');
        PlayerTieBreakingInfo playerTieBreakingInfo = new PlayerTieBreakingInfo(makePlayerProfileId(playerCode), playerCode, allPlayerCodes);
        List<Integer> B_vs_D_gamesResults = Arrays.asList(7, -10, -9, 9, -11);
        playerTieBreakingInfo.setMatchResult('D', B_vs_D_gamesResults, MatchStatus.LOSS);
        List<Integer> B_vs_E_gamesResults = Arrays.asList(-9, 5, 9, 7);
        playerTieBreakingInfo.setMatchResult('E', B_vs_E_gamesResults, MatchStatus.WIN);
        tieBreakingService.calculateGamesWonToLostRatio(playerTieBreakingInfo);
        assertEquals("wrong games won calculation", 5, playerTieBreakingInfo.getGamesWon());
        assertEquals("wrong games lost calculation", 4, playerTieBreakingInfo.getGamesLost());
        tieBreakingService.calculatePointsWonToLost(playerTieBreakingInfo, pointsPerGame);
        assertEquals("wrong points won calculation", 94, playerTieBreakingInfo.getPointsWon());
        assertEquals("wrong points lost calculation", 84, playerTieBreakingInfo.getPointsLost());
    }

    @Test
    public void testCalculateWonVsLost_D_vs_B_E() {
        int pointsPerGame = 11;
        char playerCode = 'D';
        List<Character> allPlayerCodes = Arrays.asList('B', 'D', 'E');
        PlayerTieBreakingInfo playerTieBreakingInfo = new PlayerTieBreakingInfo(makePlayerProfileId(playerCode), playerCode, allPlayerCodes);
        List<Integer> D_vs_B_gamesResults = Arrays.asList(-7, 10, 9, -9, 11);
        playerTieBreakingInfo.setMatchResult('B', D_vs_B_gamesResults, MatchStatus.WIN);
        List<Integer> D_vs_E_gamesResults = Arrays.asList(7, -8, 9, 6);
        playerTieBreakingInfo.setMatchResult('E', D_vs_E_gamesResults, MatchStatus.WIN);
        tieBreakingService.calculateGamesWonToLostRatio(playerTieBreakingInfo);
        assertEquals("wrong games won calculation", 6, playerTieBreakingInfo.getGamesWon());
        assertEquals("wrong games lost calculation", 3, playerTieBreakingInfo.getGamesLost());
        tieBreakingService.calculatePointsWonToLost(playerTieBreakingInfo, pointsPerGame);
        assertEquals("wrong points won calculation", 93, playerTieBreakingInfo.getPointsWon());
        assertEquals("wrong points lost calculation", 85, playerTieBreakingInfo.getPointsLost());
    }

    @Test
    public void testNoDefaultsScenario() {
        int pointsPerGame = 11;
        int numberOfGames = 5;
        Map<Character, Integer> playerCodeToExpectedRankMap = new HashMap<>();
        playerCodeToExpectedRankMap.put('A', 2);
        playerCodeToExpectedRankMap.put('B', 3);
        playerCodeToExpectedRankMap.put('C', 1);
        playerCodeToExpectedRankMap.put('D', 4);
        playerCodeToExpectedRankMap.put('E', 5);
        playerCodeToExpectedRankMap.put('F', 6);

        Map<Character, Integer> playerCodeToExpectedMatchPointsMap = new HashMap<>();
        playerCodeToExpectedMatchPointsMap.put('A', 8);
        playerCodeToExpectedMatchPointsMap.put('B', 8);
        playerCodeToExpectedMatchPointsMap.put('C', 9);
        playerCodeToExpectedMatchPointsMap.put('D', 8);
        playerCodeToExpectedMatchPointsMap.put('E', 6);
        playerCodeToExpectedMatchPointsMap.put('F', 6);

        List<Match> matches = makeMatchesWithoutDefaults();
        MatchCard matchCard = makeMatchCard(matches, numberOfGames, playerCodeToExpectedRankMap.size());

        GroupTieBreakingInfo groupTieBreakingInfo = tieBreakingService.rankPlayers(matchCard, pointsPerGame, numberOfGames);
        List<PlayerTieBreakingInfo> playerTieBreakingInfoList = groupTieBreakingInfo.getPlayerTieBreakingInfoList();
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            char playerCode = playerTieBreakingInfo.getPlayerCode();
            int expectedRank = playerCodeToExpectedRankMap.get(playerCode);
            assertEquals("wrong overall rank", expectedRank, playerTieBreakingInfo.getRank());
            int expectedMatchPoints = playerCodeToExpectedMatchPointsMap.get(playerCode);
            assertEquals("wrong match points", expectedMatchPoints, playerTieBreakingInfo.getMatchPoints());
        }
    }

    @Test
    public void testScenarioWithDefaultedMatches() {
        int pointsPerGame = 11;
        int numberOfGames = 5;
        Map<Character, Integer> playerCodeToExpectedRankMap = new HashMap<>();
        playerCodeToExpectedRankMap.put('A', 5);
        playerCodeToExpectedRankMap.put('B', 7);
        playerCodeToExpectedRankMap.put('C', 2);
        playerCodeToExpectedRankMap.put('D', 8);
        playerCodeToExpectedRankMap.put('E', 6);
        playerCodeToExpectedRankMap.put('F', 3);
        playerCodeToExpectedRankMap.put('G', 4);
        playerCodeToExpectedRankMap.put('H', 1);

        Map<Character, Integer> playerCodeToExpectedMatchPointsMap = new HashMap<>();
        playerCodeToExpectedMatchPointsMap.put('A', 9);
        playerCodeToExpectedMatchPointsMap.put('B', 9);
        playerCodeToExpectedMatchPointsMap.put('C', 12);
        playerCodeToExpectedMatchPointsMap.put('D', 8);
        playerCodeToExpectedMatchPointsMap.put('E', 9);
        playerCodeToExpectedMatchPointsMap.put('F', 10);
        playerCodeToExpectedMatchPointsMap.put('G', 10);
        playerCodeToExpectedMatchPointsMap.put('H', 14);

        List<Match> matches = makeMatchesWithDefaults();
        MatchCard matchCard = makeMatchCard(matches, numberOfGames, playerCodeToExpectedRankMap.size());

        GroupTieBreakingInfo groupTieBreakingInfo = tieBreakingService.rankPlayers(matchCard, pointsPerGame, numberOfGames);
        List<PlayerTieBreakingInfo> playerTieBreakingInfoList = groupTieBreakingInfo.getPlayerTieBreakingInfoList();
        for (PlayerTieBreakingInfo playerTieBreakingInfo : playerTieBreakingInfoList) {
            char playerCode = playerTieBreakingInfo.getPlayerCode();
            int expectedRank = playerCodeToExpectedRankMap.get(playerCode);
            assertEquals("wrong overall rank", expectedRank, playerTieBreakingInfo.getRank());
            int expectedMatchPoints = playerCodeToExpectedMatchPointsMap.get(playerCode);
            assertEquals("wrong match points", expectedMatchPoints, playerTieBreakingInfo.getMatchPoints());
        }
    }

    private MatchCard makeMatchCard(List<Match> matches, int numberOfGames, int numPlayers) {
        MatchCard matchCard = new MatchCard();
        matchCard.setNumberOfGames(numberOfGames);
        Map<String, String> profileIdToNameMap = new HashMap<>();
        for (int i = 0; i < numPlayers; i++) {
            char playerCode = (char) ('A' + i);
            profileIdToNameMap.put(makePlayerProfileId(playerCode), "Player, " + playerCode);
        }
        matchCard.setProfileIdToNameMap(profileIdToNameMap);
        matchCard.setMatches(matches);
        return matchCard;
    }

    private List<Match> makeMatchesWithoutDefaults() {
        List<Match> matches = new ArrayList<>();
        matches.add(makeMatch('A', 'B', ArrayUtils.toArray(7, -7, 8, -9, -8), false));
        matches.add(makeMatch('A', 'C', ArrayUtils.toArray(-8, -6, -10), false));
        matches.add(makeMatch('A', 'D', ArrayUtils.toArray(-9, 9, 4, -9, 7), true));
        matches.add(makeMatch('A', 'E', ArrayUtils.toArray(8, -10, 8, 7), true));
        matches.add(makeMatch('A', 'F', ArrayUtils.toArray(13, 9, 8), true));

        matches.add(makeMatch('B', 'C', ArrayUtils.toArray(8, -9, 10, -12, 8), true));
        matches.add(makeMatch('B', 'D', ArrayUtils.toArray(7, -10, -9, 9, -11), false));
        matches.add(makeMatch('B', 'E', ArrayUtils.toArray(-9, 5, 9, 7), true));
        matches.add(makeMatch('B', 'F', ArrayUtils.toArray(-6, -9, 10, -7), false));

        matches.add(makeMatch('C', 'D', ArrayUtils.toArray(8, 9, 7), true));
        matches.add(makeMatch('C', 'E', ArrayUtils.toArray(12, 9, 8), true));
        matches.add(makeMatch('C', 'F', ArrayUtils.toArray(-8, 7, 5, -9, 6), true));

        matches.add(makeMatch('D', 'E', ArrayUtils.toArray(7, -8, 9, 6), true));
        matches.add(makeMatch('D', 'F', ArrayUtils.toArray(9, 10, 11), true));

        matches.add(makeMatch('E', 'F', ArrayUtils.toArray(-8, 7, -9, 8, 10), true));
        return matches;
    }

    private List<Match> makeMatchesWithDefaults() {
        List<Match> matches = new ArrayList<>();
        Integer [] unplayedMatchPoints = {};
        // when first player won more positive than negative scores, last score positive or 0
        // when first player lost more negative than positive scores, last score negative or 0
        matches.add(makeMatch('A', 'B', ArrayUtils.toArray(7, -7, 8, -9, 8), true));
        matches.add(makeMatch('A', 'C', ArrayUtils.toArray(5, -8, -6, -10), false));
        matches.add(makeMatch('A', 'D', ArrayUtils.toArray(-9, 9, 4, -9, 7), true));
        matches.add(makeMatch('A', 'E', unplayedMatchPoints, true, false, true));
        matches.add(makeMatch('A', 'F', unplayedMatchPoints, false, true, false));
        matches.add(makeMatch('A', 'G', ArrayUtils.toArray(-9, 9, -9, -7), false));
        matches.add(makeMatch('A', 'H', ArrayUtils.toArray(-9, -4, -8), false));

        matches.add(makeMatch('B', 'C', ArrayUtils.toArray(-9, -12, -14), false));
        matches.add(makeMatch('B', 'D', ArrayUtils.toArray(7, -9, 9, 11), true));
        matches.add(makeMatch('B', 'E', ArrayUtils.toArray(-9, 5, 9, -8, -7), false));
        matches.add(makeMatch('B', 'F', ArrayUtils.toArray(-6, -9, 10, -7), false));
        matches.add(makeMatch('B', 'G', ArrayUtils.toArray(-6, -9, 10, 7, 11), true));
        matches.add(makeMatch('B', 'H', ArrayUtils.toArray(-6, -9, 10, -7), false));

        matches.add(makeMatch('C', 'D', ArrayUtils.toArray(8, 9, 7), true));
        matches.add(makeMatch('C', 'E', ArrayUtils.toArray(12, -9, 8, -7, -13), false));
        matches.add(makeMatch('C', 'F', ArrayUtils.toArray(-8, 7, 5, -9, 6), true));
        matches.add(makeMatch('C', 'G', ArrayUtils.toArray(-9, 7, 4, 8), true));
        matches.add(makeMatch('C', 'H', ArrayUtils.toArray(-8, 11, 6, -9, -6), false));

        matches.add(makeMatch('D', 'E', ArrayUtils.toArray(-7, -5, -4), false));
        matches.add(makeMatch('D', 'F', ArrayUtils.toArray(9, 10, -8, -9, 11), true));
        matches.add(makeMatch('D', 'G', ArrayUtils.toArray(9, 10, -3, -6, -5), false));
        matches.add(makeMatch('D', 'H', ArrayUtils.toArray(7, -10, -8, -9), false));

        matches.add(makeMatch('E', 'F', ArrayUtils.toArray(-8, 7, 8, 6), true));
        matches.add(makeMatch('E', 'G', ArrayUtils.toArray(-5, -4, -3), false));
        matches.add(makeMatch('E', 'H', unplayedMatchPoints, false, true, false));

        matches.add(makeMatch('F', 'G', ArrayUtils.toArray(9, -8, 7, 11), true));
        matches.add(makeMatch('F', 'H', ArrayUtils.toArray(-5, -6, -4), false));

        matches.add(makeMatch('G', 'H', ArrayUtils.toArray(8, -7, 7, -6, -9), false));

        return matches;
    }

    private Match makeMatch(char playerA, char playerB, Integer[] gameScores, boolean firstPlayerWon) {
        return makeMatch(playerA, playerB, gameScores, firstPlayerWon, false, false);
    }

    private Match makeMatch(char playerA, char playerB, Integer[] gameScores, boolean firstPlayerWon, boolean playerADefaulted, boolean playerBDefaulted) {
        Match match = new Match();
        match.setPlayerALetter(playerA);
        match.setPlayerBLetter(playerB);
        match.setPlayerAProfileId(makePlayerProfileId(playerA));
        match.setPlayerBProfileId(makePlayerProfileId(playerB));
        match.setSideADefaulted(playerADefaulted);
        match.setSideBDefaulted(playerBDefaulted);
        int gameNum = 0;
        for (Integer gameScore : gameScores) {
            int playerAGameScore = 0;
            int playerBGameScore = 0;
            if (gameScore < 0) {
                playerAGameScore = -1 * gameScore;
                playerBGameScore = (gameScore > -10) ? 11 : (gameScore - 2) * -1;
            } else if (gameScore > 0) {
                playerAGameScore = (gameScore < 10) ? 11 : (gameScore + 2);
                playerBGameScore = gameScore;
            } else {
                playerAGameScore = firstPlayerWon ? 11 : 0;
                playerBGameScore = firstPlayerWon ? 0 : 11;
            }
            gameNum++;
            switch (gameNum) {
                case 1:
                    match.setGame1ScoreSideA((byte) playerAGameScore);
                    match.setGame1ScoreSideB((byte) playerBGameScore);
                    break;
                case 2:
                    match.setGame2ScoreSideA((byte) playerAGameScore);
                    match.setGame2ScoreSideB((byte) playerBGameScore);
                    break;
                case 3:
                    match.setGame3ScoreSideA((byte) playerAGameScore);
                    match.setGame3ScoreSideB((byte) playerBGameScore);
                    break;
                case 4:
                    match.setGame4ScoreSideA((byte) playerAGameScore);
                    match.setGame4ScoreSideB((byte) playerBGameScore);
                    break;
                case 5:
                    match.setGame5ScoreSideA((byte) playerAGameScore);
                    match.setGame5ScoreSideB((byte) playerBGameScore);
                    break;
                case 6:
                    match.setGame6ScoreSideA((byte) playerAGameScore);
                    match.setGame6ScoreSideB((byte) playerBGameScore);
                    break;
                case 7:
                    match.setGame7ScoreSideA((byte) playerAGameScore);
                    match.setGame7ScoreSideB((byte) playerBGameScore);
                    break;
            }
        }

        return match;
    }

    private String makePlayerProfileId(char playerCode) {
        return "player" + playerCode + "_pid";
    }

}
