package com.auroratms.results;

import com.auroratms.draw.DrawType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Results of individual match of a player.  Used for listing all matches played by a player
 * played in a tournament
 */
@Data
@NoArgsConstructor
public class PlayerMatchSummary {

    // profile id of the opponent
    private String opponentProfileId;

    // membership id of the opponent
    private int opponentMembershipId;

    // full name of opponent
    private String opponentFullName;

    // opponent's rating
    private int opponentRating;

    // event name
    private String eventName;

    // event round
    private int round;

    // group number
    private int group;

    // match number on the match card
    private int matchNum;

    // true if doubles match
    private boolean doubles;

    // format of the whole event RR or SE
    private DrawType eventFormat;

    // compact result e.g. 6, 7, -5, 9
    private String compactMatchResult;

    // if true this is this player's win, if false opponent won
    private boolean matchWon;

    // number of exchanged points for a win or loss
    private int pointsExchanged;

    // day of tournament on which the match was played
    private int matchDay;
}
