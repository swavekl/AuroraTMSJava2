package com.auroratms.draw.generation.singleelim;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for generating bracket for single elimination which contains
 * draw line numbers and bye information
 */
public class BracketGenerator {

    // number of player entries.  Used to calculate how many byes if any
    int numEntries;

    // number of slots which will have byes
    int requiredByes;

    public BracketGenerator(int numEntries) {
        this.numEntries = numEntries;
    }

    public BracketLine[] generateBracket() {
        int participantsCount = this.numEntries;
        int rounds = (int) Math.ceil(Math.log(participantsCount) / Math.log(2));
        int bracketSize = (int) Math.pow(2, rounds);
        this.requiredByes = bracketSize - participantsCount;

        List<List<BracketLine>> roundDrawLines = new ArrayList<>();
        List<BracketLine> simpleMatch = new ArrayList<>();
        simpleMatch.add(new BracketLine(1, 1, false));
        simpleMatch.add(new BracketLine(2, 2, false));
        roundDrawLines.add(simpleMatch);

        List<BracketLine> bracket = new ArrayList<>(bracketSize);
        if (participantsCount > 2) {
            // go through each round, each time expanding draw lines to fill 4, 8, 16 etc lines
            for (int round = 1; round < rounds; round++) {
                List<List<BracketLine>> roundMatches = new ArrayList<>();
                // e.g. 3, 5, 9, 17 etc.
                int normalizedSeedNum = (int) (Math.pow(2, round + 1) + 1);
//                System.out.println("round = " + round + " => sum = " + normalizedSeedNum);
                for (List<BracketLine> match : roundDrawLines) {
                    // first match
                    int homeSeedNum = match.get(0).seedNumber;
                    int awaySeedNum = normalizedSeedNum - match.get(0).seedNumber;
                    List<BracketLine> roundMatches1 = new ArrayList<>();
                    roundMatches1.add(new BracketLine(homeSeedNum, homeSeedNum, !(homeSeedNum <= participantsCount)));
                    roundMatches1.add(new BracketLine(awaySeedNum, awaySeedNum, !(awaySeedNum <= participantsCount)));
                    roundMatches.add(roundMatches1);
                    // second match
                    homeSeedNum = normalizedSeedNum - match.get(1).seedNumber;
                    awaySeedNum = match.get(1).seedNumber;
                    List<BracketLine> roundMatches2 = new ArrayList<>();
                    roundMatches2.add(new BracketLine(homeSeedNum, homeSeedNum, !(homeSeedNum <= participantsCount)));
                    roundMatches2.add(new BracketLine(awaySeedNum, awaySeedNum, !(awaySeedNum <= participantsCount)));
                    roundMatches.add(roundMatches2);
                }
                roundDrawLines = roundMatches;
            }

            // flatten out list of lists and normalize seed numbers
            for (List<BracketLine> roundBracketLine : roundDrawLines) {
                for (BracketLine bracketLine : roundBracketLine) {
                    bracketLine.normalizedSeedNumber = normalizeSeedNumber(bracketLine.seedNumber, rounds);
                    bracket.add(bracketLine);
                }
            }

            if (requiredByes > 0) {
                int seedToFind = bracketSize;
                for (int byeNumber = 1; byeNumber <= requiredByes; byeNumber++) {
                    for (BracketLine bracketLine : bracket) {
                        if (bracketLine.isBye && bracketLine.seedNumber == seedToFind) {
                            bracketLine.byeSeedNumber = byeNumber;
                            break;
                        }
                    }
                    seedToFind--;
                }
            }
        }

        // convert list to array
//System.out.println("bracket start -----");
        BracketLine[] finalResult = new BracketLine[bracket.size()];
        for (int i = 0; i < finalResult.length; i++) {
            finalResult[i] = bracket.get(i);
//if (finalResult[i].isBye) {
//    System.out.println(String.format("%2d) nln %2d, ln %2d, bye %d",
//            (i + 1), finalResult[i].getNormalizedSeedNumber(), finalResult[i].getSeedNumber(),
//            finalResult[i].byeSeedNumber));
//} else {
//    System.out.println(String.format("%2d) nln %2d, ln %2d",
//            (i + 1), finalResult[i].getNormalizedSeedNumber(), finalResult[i].getSeedNumber()));
//}
        }
//System.out.println("bracket end   -----");

        return finalResult;
    }

    /**
     * @param lineNumber
     * @param rounds
     * @return
     */
    private int normalizeSeedNumber(int lineNumber, int rounds) {
        int result = lineNumber;
        if (lineNumber > 2) {
            for (int round = 1; round < rounds; round++) {
                int upperSeedNumberLimit = (int) (Math.pow(2, round + 1));
                if (lineNumber <= upperSeedNumberLimit) {
                    result = (int) (Math.pow(2, round)) + 1;
                    break;
                }
            }
        }
        return result;
    }

    public int getRequiredByes() {
        return this.requiredByes;
    }
}
