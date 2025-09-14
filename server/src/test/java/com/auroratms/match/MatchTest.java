package com.auroratms.match;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MatchTest {

    @Test
    public void testSimpleWin () {
        Match match = new Match();
        match.setPlayerAProfileId("123");
        match.setPlayerBProfileId("456");
        match.setPlayerALetter('A');
        match.setPlayerALetter('B');
        match.setScoreEnteredByProfileId("123");

        match.setGame1ScoreSideA((byte)11);
        match.setGame1ScoreSideB((byte)7);

        match.setGame2ScoreSideA((byte)11);
        match.setGame2ScoreSideB((byte)3);

        match.setGame3ScoreSideA((byte)11);
        match.setGame3ScoreSideB((byte)8);

        String compactResult = match.getCompactResult(5, 11);
        assertEquals("7,3,8", compactResult, "wrong match result");
    }
    @Test
    public void testComplexWin () {
        Match match = new Match();
        match.setPlayerAProfileId("123");
        match.setPlayerBProfileId("456");
        match.setPlayerALetter('A');
        match.setPlayerALetter('B');
        match.setScoreEnteredByProfileId("123");

        match.setGame1ScoreSideA((byte)11);
        match.setGame1ScoreSideB((byte)7);

        match.setGame2ScoreSideA((byte)11);
        match.setGame2ScoreSideB((byte)3);

        match.setGame3ScoreSideA((byte)8);
        match.setGame3ScoreSideB((byte)11);

        match.setGame4ScoreSideA((byte)10);
        match.setGame4ScoreSideB((byte)12);

        match.setGame5ScoreSideA((byte)11);
        match.setGame5ScoreSideB((byte)6);

        String compactResult = match.getCompactResult(5, 11);
        assertEquals("7,3,-8,-10,6", compactResult, "wrong match result");
    }
    @Test
    public void testDefault () {
        Match match = new Match();
        match.setPlayerAProfileId("123");
        match.setPlayerBProfileId("456");
        match.setPlayerALetter('A');
        match.setPlayerALetter('B');
        match.setScoreEnteredByProfileId("123");
        match.setSideADefaulted(true);

        String compactResult = match.getCompactResult(3, 11);
        assertEquals("0,0", compactResult, "wrong match result");

        compactResult = match.getCompactResult(5, 11);
        assertEquals("0,0,0", compactResult, "wrong match result");

        compactResult = match.getCompactResult(7, 11);
        assertEquals("0,0,0,0", compactResult, "wrong match result");
    }
}
