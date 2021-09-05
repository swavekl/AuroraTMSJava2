package com.auroratms.tiebreaking.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PlayerMatchResults {
    private char opponentCode;
    private MatchStatus matchStatus;
    private List<Integer> gameScores;

    public PlayerMatchResults(char opponentCode) {
        this.opponentCode = opponentCode;
    }
}
