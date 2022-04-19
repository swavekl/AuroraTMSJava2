package com.auroratms.match;

import lombok.Data;

/**
 * Abbreviated match result in games
 */
@Data
public class MatchResult {

    // number of games won by A and B sides
    private int gamesWonByA;
    private int gamesWonByB;

    // compact result e.g. 6, 7, -5, 9
    private String compactMatchResult;

    private Character playerALetter;
    private Character playerBLetter;

    // true if the side defaulted.  If both are true the match wasn't played
    private boolean sideADefaulted;
    private boolean sideBDefaulted;

    /**
     * Makes reverse of match by switching side A with side B
     * @return
     */
    public MatchResult makeReverse() {
        MatchResult reversedMatchResult = new MatchResult();
        reversedMatchResult.gamesWonByA = this.gamesWonByB;
        reversedMatchResult.gamesWonByB = this.gamesWonByA;
        reversedMatchResult.playerALetter = this.playerBLetter;
        reversedMatchResult.playerBLetter = this.playerALetter;
        reversedMatchResult.sideADefaulted = sideBDefaulted;
        reversedMatchResult.sideBDefaulted = sideADefaulted;
        return reversedMatchResult;
    }
}
