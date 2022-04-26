package com.auroratms.results;

import com.auroratms.match.MatchResult;
import lombok.Data;

import java.util.List;

@Data
public class PlayerResults {
    // player letter code e.g. A, B, C etc. in a group or match
    private Character letterCode;

    // profile id of a player or doubles team
    private String profileId;

    // rank achieved in this group e.g. 1st, 2nd, etc.
    private int rank;

    // player rating or combined players rating for doubles team
    private int rating;

    // full name of names of doubles team members separated by slash
    private String fullName;

    // single elimination round seed number
    private int seSeedNumber;

    // single elimination round bye number
    private int byeNumber;

    // number of matches won in this group
    private int matchesWon;

    // number of matches lost in this group
    private int matchesLost;

    // match results against other players reduced to game scores, or match results of single elimnation roungs
    private List<MatchResult> matchResults;

    public void addNumMatchesWon() {
        matchesWon++;
    }

    public void addNumMatchesLost() {
        matchesLost++;
    }
}
