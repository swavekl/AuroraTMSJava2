package com.auroratms.match;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class MatchGeneratorTest {

    @Test
    public void test2PerGroup () {

        String [] expectedMatchOrder = {
            // round 1
            "A - B"
        };


        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(2, 1);
        checkResults(expectedMatchOrder, matchOrder);
    }

    @Test
    public void test3PerGroup () {

        String [] expectedMatchOrder = {
            // round 1
            "A - bye",
            "B - C",
            // round 2
            "A - C",
            "bye - B",
            // round 3
            "C - bye",
            "A - B"
        };


        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(3, 1);
        checkResults(expectedMatchOrder, matchOrder);
    }

    @Test
    public void test3PerGroup2PlayersToAdvance () {

        String [] expectedMatchOrder = {
            // round 1
            "A - C",
            "bye - B",
            // round 2
            "A - B",
            "C - bye",
            // round 3
            "A - bye",
            "B - C"
        };

        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(3, 2);
        checkResults(expectedMatchOrder, matchOrder);
    }

    @Test
    public void test4PerGroup () {
        String [] expectedMatchOrder = {
                // round 1
                "A - D",
                "B - C",
                // round 2
                "A - C",
                "B - D",
                // round 3
                "C - D",
                "A - B"
        };

        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(4, 1);
        checkResults(expectedMatchOrder, matchOrder);
    }

    @Test
    public void test4PerGroup2PlayersToAdvance () {
        String [] expectedMatchOrder = {
                // round 1
                "A - C",
                "B - D",
                // round 2
                "A - B",
                "C - D",
                // round 3
                "A - D",
                "B - C"
        };

        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(4, 2);
        checkResults(expectedMatchOrder, matchOrder);
    }

    @Test
    public void test5PerGroup () {
        String [] expectedMatchOrder = {
                // round 1
                "A - bye",
                "B - E",
                "C - D",
                // round 2
                "A - E",
                "bye - D",
                "B - C",
                // round 3
                "A - D",
                "C - E",
                "bye - B",
                // round 4
                "A - C",
                "B - D",
                "E - bye",
                // round 5
                "C - bye",
                "D - E",
                "A - B"
        };

        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(5, 1);
        checkResults(expectedMatchOrder, matchOrder);
    }

    @Test
    public void test5PerGroup2PlayersToAdvance () {
        String [] expectedMatchOrder = {
                // round 1
                "A - bye",
                "B - E",
                "C - D",
                // round 2
                "A - D",
                "C - E",
                "bye - B",
                // round 3
                "A - C",
                "B - D",
                "E - bye",
                // round 4
                "A - B",
                "C - bye",
                "D - E",
                // round 5
                "A - E",
                "bye - D",
                "B - C"
        };

        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(5, 2);
        checkResults(expectedMatchOrder, matchOrder);
    }

    @Test
    public void test6PerGroup () {
        String [] expectedMatchOrder = {
                // round 1
                "A - F",
                "B - E",
                "C - D",
                // round 2
                "A - E",
                "D - F",
                "B - C",
                // round 3
                "A - D",
                "C - E",
                "B - F",
                // round 4
                "A - C",
                "B - D",
                "E - F",
                // round 5
                "C - F",
                "D - E",
                "A - B"
        };

        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(6, 1);
        checkResults(expectedMatchOrder, matchOrder);
    }

    @Test
    public void test6PerGroup2PlayersToAdvance () {
        String [] expectedMatchOrder = {
                // round 1
                "A - F",
                "B - E",
                "C - D",
                // round 2
                "A - D",
                "C - E",
                "B - F",
                // round 3
                "A - C",
                "B - D",
                "E - F",
                // round 4
                "A - B",
                "C - F",
                "D - E",
                // round 5
                "A - E",
                "D - F",
                "B - C"
        };

        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(6, 2);
        checkResults(expectedMatchOrder, matchOrder);
    }

    @Test
    public void test7PerGroup () {
        String [] expectedMatchOrder = {
                // round 1
                "A - bye",
                "B - G",
                "C - F",
                "D - E",
                // round 2
                "A - G",
                "bye - F",
                "B - E",
                "C - D",
                // round 3
                "A - F",
                "E - G",
                "bye - D",
                "B - C",
                // round 4
                "A - E",
                "D - F",
                "C - G",
                "bye - B",
                // round 5
                "A - D",
                "C - E",
                "B - F",
                "G - bye",
                // round 6
                "A - C",
                "B - D",
                "E - bye",
                "F - G",
                // round 7
                "C - bye",
                "D - G",
                "E - F",
                "A - B"
        };

        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(7, 1);
        checkResults(expectedMatchOrder, matchOrder);
    }
    @Test
    public void test7PerGroup2PlayersToAdvance () {
        String [] expectedMatchOrder = {
                // round 1
                "A - bye",
                "B - G",
                "C - F",
                "D - E",
                // round 2
                "A - G",
                "bye - F",
                "B - E",
                "C - D",
                // round 3
                "A - E",
                "D - F",
                "C - G",
                "bye - B",
                // round 4
                "A - D",
                "C - E",
                "B - F",
                "G - bye",
                // round 5
                "A - C",
                "B - D",
                "E - bye",
                "F - G",
                // round 6
                "A - B",
                "C - bye",
                "D - G",
                "E - F",
                // round 7
                "A - F",
                "E - G",
                "bye - D",
                "B - C"
        };

        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(7, 2);
        checkResults(expectedMatchOrder, matchOrder);
    }

    @Test
    public void test8PerGroup () {
        String [] expectedMatchOrder = {
                // round 1
                "A - H",
                "B - G",
                "C - F",
                "D - E",
                // round 2
                "A - G",
                "F - H",
                "B - E",
                "C - D",
                // round 3
                "A - F",
                "E - G",
                "D - H",
                "B - C",
                // round 4
                "A - E",
                "D - F",
                "C - G",
                "B - H",
                // round 5
                "A - D",
                "C - E",
                "B - F",
                "G - H",
                // round 6
                "A - C",
                "B - D",
                "E - H",
                "F - G",
                // round 7
                "C - H",
                "D - G",
                "E - F",
                "A - B"
        };

        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(8, 1);
        checkResults(expectedMatchOrder, matchOrder);
    }

    @Test
    public void test8PerGroup2PlayersToAdvance () {
        String [] expectedMatchOrder = {
                // round 1
                "A - H",
                "B - G",
                "C - F",
                "D - E",
                // round 2
                "A - G",
                "F - H",
                "B - E",
                "C - D",
                // round 3
                "A - E",
                "D - F",
                "C - G",
                "B - H",
                // round 4
                "A - D",
                "C - E",
                "B - F",
                "G - H",
                // round 5
                "A - C",
                "B - D",
                "E - H",
                "F - G",
                // round 6
                "A - B",
                "C - H",
                "D - G",
                "E - F",
                // round 7
                "A - F",
                "E - G",
                "D - H",
                "B - C"
        };

        List<MatchOpponents> matchOrder = MatchOrderGenerator.generateOrderOfMatches(8, 2);
        checkResults(expectedMatchOrder, matchOrder);
    }

    private void checkResults(String[] expectedMatchOrder, List<MatchOpponents> matchOrder) {
        int index = 0;
        for (MatchOpponents matchOpponents : matchOrder) {
            String leftSide = (matchOpponents.playerALetter != MatchOrderGenerator.BYE) ? ("" + matchOpponents.playerALetter) : "bye";
            String rightSide = (matchOpponents.playerBLetter != MatchOrderGenerator.BYE) ? ("" + matchOpponents.playerBLetter) : "bye";
            String actualMatch = leftSide + " - " + rightSide;
            String expectedMatch = expectedMatchOrder[index];
            index++;
            assertEquals("wrong match order at index " + index, expectedMatch, actualMatch);
        }
    }
}
