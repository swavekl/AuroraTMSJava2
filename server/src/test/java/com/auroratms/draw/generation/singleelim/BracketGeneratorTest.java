package com.auroratms.draw.generation.singleelim;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class BracketGeneratorTest {

    @Test
    public void test2 () {
        BracketGenerator bracketGenerator = new BracketGenerator(2);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 2, bracketLines.length);

        int [] expectedLineNumbers = {1, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);
    }

    @Test
    public void test4 () {
        BracketGenerator bracketGenerator = new BracketGenerator(4);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 4, bracketLines.length);

        int [] expectedLineNumbers = {1, 3, 3, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);
    }

    @Test
    public void test4_Bye1 () {
        BracketGenerator bracketGenerator = new BracketGenerator(3);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 4, bracketLines.length);

        int [] expectedLineNumbers = {1, 3, 3, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);
        int [] expectedByes = { 0, 1, 0, 0};
        checkByes(bracketLines, expectedByes);
    }

    @Test
    public void test8 () {
        BracketGenerator bracketGenerator = new BracketGenerator(8);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 8, bracketLines.length);

        int [] expectedLineNumbers = {1, 5, 5, 3, 3, 5, 5, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);
    }

    @Test
    public void test8_Bye2 () {
        BracketGenerator bracketGenerator = new BracketGenerator(6);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 8, bracketLines.length);

        int [] expectedLineNumbers = {1, 5, 5, 3, 3, 5, 5, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);
        int [] expectedByes = { 0, 1, 0, 0, 0, 0, 2, 0};
        checkByes(bracketLines, expectedByes);
    }

    @Test
    public void test8_Bye3 () {
        BracketGenerator bracketGenerator = new BracketGenerator(5);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 8, bracketLines.length);

        int [] expectedLineNumbers = {1, 5, 5, 3, 3, 5, 5, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);
        int [] expectedByes = { 0, 1, 0, 0, 0, 3, 2, 0};
        checkByes(bracketLines, expectedByes);
    }

    @Test
    public void test16 () {
        BracketGenerator bracketGenerator = new BracketGenerator(16);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 16, bracketLines.length);

        int [] expectedLineNumbers = {1, 9, 9, 5, 5, 9, 9, 3, 3, 9, 9, 5, 5, 9, 9, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);
    }

    @Test
    public void test16_Bye4 () {
        BracketGenerator bracketGenerator = new BracketGenerator(12);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 16, bracketLines.length);

        int [] expectedLineNumbers = {1, 9, 9, 5, 5, 9, 9, 3, 3, 9, 9, 5, 5, 9, 9, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);

        int [] expectedByes = { 0, 1, 0, 0, 0, 0, 4, 0,
                                0, 3, 0, 0, 0, 0, 2, 0};
        checkByes(bracketLines, expectedByes);
    }

    @Test
    public void test16_Bye5 () {
        BracketGenerator bracketGenerator = new BracketGenerator(11);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 16, bracketLines.length);

        int [] expectedLineNumbers = {1, 9, 9, 5, 5, 9, 9, 3, 3, 9, 9, 5, 5, 9, 9, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);

        int [] expectedByes = { 0, 1, 0, 0, 0, 5, 4, 0,
                                0, 3, 0, 0, 0, 0, 2, 0};
        checkByes(bracketLines, expectedByes);
    }

    @Test
    public void test16_Bye6 () {
        BracketGenerator bracketGenerator = new BracketGenerator(10);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 16, bracketLines.length);

        int [] expectedLineNumbers = {1, 9, 9, 5, 5, 9, 9, 3, 3, 9, 9, 5, 5, 9, 9, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);

        int [] expectedByes = { 0, 1, 0, 0, 0, 5, 4, 0,
                                0, 3, 6, 0, 0, 0, 2, 0};
        checkByes(bracketLines, expectedByes);
    }

    @Test
    public void test16_Bye7 () {
        BracketGenerator bracketGenerator = new BracketGenerator(9);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 16, bracketLines.length);

        int [] expectedLineNumbers = {1, 9, 9, 5, 5, 9, 9, 3, 3, 9, 9, 5, 5, 9, 9, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);

        int [] expectedByes = { 0, 1, 0, 0, 0, 5, 4, 0,
                                0, 3, 6, 0, 0, 7, 2, 0};
        checkByes(bracketLines, expectedByes);
    }

    @Test
    public void test32 () {
        BracketGenerator bracketGenerator = new BracketGenerator(32);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 32, bracketLines.length);

        int [] expectedLineNumbers = {1, 17, 17, 9, 9, 17, 17, 5, 5, 17, 17, 9, 9, 17, 17, 3,
                                      3, 17, 17, 9, 9, 17, 17, 5, 5, 17, 17, 9, 9, 17, 17, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);
    }

    @Test
    public void test32_Bye8 () {
        BracketGenerator bracketGenerator = new BracketGenerator(24);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 32, bracketLines.length);

        int [] expectedLineNumbers = {1, 17, 17, 9, 9, 17, 17, 5, 5, 17, 17, 9, 9, 17, 17, 3,
                                      3, 17, 17, 9, 9, 17, 17, 5, 5, 17, 17, 9, 9, 17, 17, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);

        int [] expectedByes = { 0, 1, 0, 0, 0, 0, 8, 0,
                                0, 5, 0, 0, 0, 0, 4, 0,
                                0, 3, 0, 0, 0, 0, 6, 0,
                                0, 7, 0, 0, 0, 0, 2, 0};
        checkByes(bracketLines, expectedByes);
    }

    @Test
    public void test64 () {
        BracketGenerator bracketGenerator = new BracketGenerator(64);
        BracketLine[] bracketLines = bracketGenerator.generateBracket();
        assertEquals ("wrong size", 64, bracketLines.length);

        int [] expectedLineNumbers = {1, 33, 33, 17, 17, 33, 33, 9, 9, 33, 33, 17, 17, 33, 33, 5, 5, 33, 33, 17, 17, 33, 33, 9, 9, 33, 33, 17, 17, 33, 33, 3, 3, 33, 33, 17, 17, 33, 33, 9, 9, 33, 33, 17, 17, 33, 33, 5, 5, 33, 33, 17, 17, 33, 33, 9, 9, 33, 33, 17, 17, 33, 33, 2};
        checkDrawLines(bracketLines, expectedLineNumbers);
    }

    private void checkByes(BracketLine[] bracketLines, int[] expectedByes) {
        for (int i = 0; i < bracketLines.length; i++) {
            BracketLine bracketLine = bracketLines[i];
            int expectedBye = expectedByes[i];
            if (expectedBye != 0) {
                assertTrue("wrong bye", bracketLine.isBye);
                assertEquals("wrong bye num on line " + (i + 1), expectedBye, bracketLine.byeSeedNumber);
            } else {
                assertFalse("wrong bye", bracketLine.isBye);
            }
        }
    }

    private void checkDrawLines(BracketLine[] bracketLines, int[] expectedLineNumbers) {
        for (int i = 0; i < bracketLines.length; i++) {
            BracketLine bracketLine = bracketLines[i];
            int expectedLineNum = expectedLineNumbers[i];
            assertEquals("wrong line num", expectedLineNum, bracketLine.normalizedSeedNumber);
        }
    }
}
