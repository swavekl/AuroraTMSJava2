package com.auroratms.match.publish.message;

import com.auroratms.match.Match;
import lombok.ToString;

import java.io.Serializable;

/**
 * A message sent to display monitor to show match progress for spectators
 */
@ToString
public class MonitorMessage implements Serializable {

    // current match status
    Match match;

    String playerAName;
    String playerBName;
    String playerAPartnerName;
    String playerBPartnerName;

    boolean doubles;

    // best of 3, 5, 7 or 9 games per match in the main round (i.e. round robin)
    int numberOfGames;

    int pointsPerGame;

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public String getPlayerAName() {
        return playerAName;
    }

    public void setPlayerAName(String playerAName) {
        this.playerAName = playerAName;
    }

    public String getPlayerBName() {
        return playerBName;
    }

    public void setPlayerBName(String playerBName) {
        this.playerBName = playerBName;
    }

    public String getPlayerAPartnerName() {
        return playerAPartnerName;
    }

    public void setPlayerAPartnerName(String playerAPartnerName) {
        this.playerAPartnerName = playerAPartnerName;
    }

    public String getPlayerBPartnerName() {
        return playerBPartnerName;
    }

    public void setPlayerBPartnerName(String playerBPartnerName) {
        this.playerBPartnerName = playerBPartnerName;
    }

    public boolean isDoubles() {
        return this.doubles;
    }

    public void setDoubles(boolean doubles) {
        this.doubles = doubles;
    }

    public int getNumberOfGames() {
        return numberOfGames;
    }

    public void setNumberOfGames(int numberOfGames) {
        this.numberOfGames = numberOfGames;
    }

    public int getPointsPerGame() {
        return pointsPerGame;
    }

    public void setPointsPerGame(int pointsPerGame) {
        this.pointsPerGame = pointsPerGame;
    }
}
