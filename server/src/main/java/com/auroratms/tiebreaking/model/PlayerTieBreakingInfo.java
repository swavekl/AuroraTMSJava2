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

    private int gamesWon;
    private int gamesLost;

    private int pointsWon;
    private int pointsLost;

    List<PlayerMatchResults> allPlayerMatchResults;

    /**
     * Used for subset of
     * @param playerProfileId
     * @param playerLetterCode
     * @param allPlayerCodes this and o
     */
    public PlayerTieBreakingInfo(String playerProfileId, char playerLetterCode, List<Character> allPlayerCodes) {
        this.playerProfileId = playerProfileId;
        this.playerCode = playerLetterCode;
        this.allPlayerMatchResults = new ArrayList<>(allPlayerCodes.size());
        for (Character opponentLetterCode : allPlayerCodes) {
            PlayerMatchResults playerMatchResults = new PlayerMatchResults(opponentLetterCode);
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
