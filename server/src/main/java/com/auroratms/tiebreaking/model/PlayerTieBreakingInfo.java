package com.auroratms.tiebreaking.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PlayerTieBreakingInfo {

    private String playerProfileId;
    private char playerCode;
    private int rank;

    private int matchPoints;

    List<PlayerMatchResults> allPlayerMatchResults;

    public PlayerTieBreakingInfo(String playerProfileId, int numPlayers, char playerCode) {
        this.playerProfileId = playerProfileId;
        this.playerCode = playerCode;
        this.allPlayerMatchResults = new ArrayList<>(numPlayers);
        for (int i = 0; i < numPlayers; i++) {
            char opponentCode = (char) ('A' + i);
            PlayerMatchResults playerMatchResults = new PlayerMatchResults(opponentCode);
            this.allPlayerMatchResults.add(playerMatchResults);
        }
    }

    public void setMatchResult(char opponentCode, List<Integer> gamesResults, MatchStatus matchStatus) {
        for (PlayerMatchResults playerMatchResult : allPlayerMatchResults) {
            if (opponentCode == playerMatchResult.getOpponentCode()) {
                playerMatchResult.setGameScores(gamesResults);
                playerMatchResult.setMatchStatus(matchStatus);
                break;
            }
        }
    }
}
