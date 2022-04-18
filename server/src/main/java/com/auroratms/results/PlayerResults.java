package com.auroratms.results;

import com.auroratms.match.MatchResult;
import lombok.Data;

import java.util.List;

@Data
public class PlayerResults {
    private Character letterCode;
    private String profileId;
    private int rank;
    private int rating;
    private String fullName;

    private int matchesWon;
    private int matchesLost;

    private List<MatchResult> matchResults;

    public void addNumMatchesWon() {
        matchesWon++;
    }

    public void addNumMatchesLost() {
        matchesLost++;
    }
}
