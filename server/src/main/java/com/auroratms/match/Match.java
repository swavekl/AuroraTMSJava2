package com.auroratms.match;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Represents a single match between two players or two doubles teams
 */
@Entity
@Table(name = "matches")  // match is a reserved keyword in SQL so we use plural
@Data
@NoArgsConstructor
public class Match implements Serializable, Cloneable {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // match card grouping matches together
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_card_fk", nullable = false)
    @JsonBackReference
    private MatchCard matchCard;

    // match number within a round so that matches are ordered properly on the match card
    private int matchNum;

    // profile id of two players for singles matches
    // for doubles matches profile ids of team members are separated by ; like this
    // playerAProfileId;playerAPartnerProfileId and playerBProfileId;playerBPartnerProfileId
    @NonNull
    @Column(length = 100)
    private String playerAProfileId;

    @NonNull
    @Column(length = 100)
    private String playerBProfileId;

    // true if the side defaulted.  If both are true the match wasn't played
    private boolean sideADefaulted;
    private boolean sideBDefaulted;

    // indicates if side took a timeout - help for umpire
    private boolean sideATimeoutTaken;
    private boolean sideBTimeoutTaken;

    // indicates if side A is to serve first, if false side B servers first - help for umpire
    private boolean sideAServesFirst;

    // game (set) scores of played match e.g. 11:7, 11:8.  First number is for player A, second is for player B
    @Column(nullable = true)
    private byte game1ScoreSideA;
    @Column(nullable = true)
    private byte game1ScoreSideB;
    @Column(nullable = true)
    private byte game2ScoreSideA;
    @Column(nullable = true)
    private byte game2ScoreSideB;
    @Column(nullable = true)
    private byte game3ScoreSideA;
    @Column(nullable = true)
    private byte game3ScoreSideB;
    @Column(nullable = true)
    private byte game4ScoreSideA;
    @Column(nullable = true)
    private byte game4ScoreSideB;
    @Column(nullable = true)
    private byte game5ScoreSideA;
    @Column(nullable = true)
    private byte game5ScoreSideB;
    @Column(nullable = true)
    private byte game6ScoreSideA;
    @Column(nullable = true)
    private byte game6ScoreSideB;
    @Column(nullable = true)
    private byte game7ScoreSideA;
    @Column(nullable = true)
    private byte game7ScoreSideB;

    @Column()
    private String scoreEnteredByProfileId;  // profile id of player who entered score or null

    // letters A vs D, B vs C etc. codes for each player
    private Character playerALetter;
    private Character playerBLetter;

    // player or combined team seed rating
    private int playerARating;
    private int playerBRating;

    @Override
    public String toString() {
        return "Match{" +
                "id=" + id +
                ", matchNum=" + matchNum +
                ", playerAProfileId='" + playerAProfileId + '\'' +
                ", playerBProfileId='" + playerBProfileId + '\'' +
                ", sideADefaulted=" + sideADefaulted +
                ", sideBDefaulted=" + sideBDefaulted +
                ", sideATimeoutTaken=" + sideATimeoutTaken +
                ", sideBTimeoutTaken=" + sideBTimeoutTaken +
                ", sideAServesFirst=" + sideAServesFirst +
                ", game1ScoreSideA=" + game1ScoreSideA +
                ", game1ScoreSideB=" + game1ScoreSideB +
                ", game2ScoreSideA=" + game2ScoreSideA +
                ", game2ScoreSideB=" + game2ScoreSideB +
                ", game3ScoreSideA=" + game3ScoreSideA +
                ", game3ScoreSideB=" + game3ScoreSideB +
                ", game4ScoreSideA=" + game4ScoreSideA +
                ", game4ScoreSideB=" + game4ScoreSideB +
                ", game5ScoreSideA=" + game5ScoreSideA +
                ", game5ScoreSideB=" + game5ScoreSideB +
                ", game6ScoreSideA=" + game6ScoreSideA +
                ", game6ScoreSideB=" + game6ScoreSideB +
                ", game7ScoreSideA=" + game7ScoreSideA +
                ", game7ScoreSideB=" + game7ScoreSideB +
                ", playerALetter=" + playerALetter +
                ", playerBLetter=" + playerBLetter +
                ", playerARating=" + playerARating +
                ", playerBRating=" + playerBRating +
                '}';
    }

    /**
     * @param profileId
     * @param numberOfGames
     * @param pointsPerGame
     */
    public boolean isMatchWinner(String profileId,
                                 int numberOfGames,
                                 int pointsPerGame) {
        int numGamesWonByA = 0;
        int numGamesWonByB = 0;
        for (int i = 0; i < numberOfGames; i++) {
            int playerAGameScore = 0;
            int playerBGameScore = 0;
            switch (i) {
                case 0:
                    playerAGameScore = this.game1ScoreSideA;
                    playerBGameScore = this.game1ScoreSideB;
                    break;
                case 1:
                    playerAGameScore = this.game2ScoreSideA;
                    playerBGameScore = this.game2ScoreSideB;
                    break;
                case 2:
                    playerAGameScore = this.game3ScoreSideA;
                    playerBGameScore = this.game3ScoreSideB;
                    break;
                case 3:
                    playerAGameScore = this.game4ScoreSideA;
                    playerBGameScore = this.game4ScoreSideB;
                    break;
                case 4:
                    playerAGameScore = this.game5ScoreSideA;
                    playerBGameScore = this.game5ScoreSideB;
                    break;
                case 5:
                    playerAGameScore = this.game6ScoreSideA;
                    playerBGameScore = this.game6ScoreSideB;
                    break;
                case 6:
                    playerAGameScore = this.game7ScoreSideA;
                    playerBGameScore = this.game7ScoreSideB;
                    break;
            }

            if (playerAGameScore >= pointsPerGame && playerBGameScore < playerAGameScore) {
                numGamesWonByA++;
            } else if (playerBGameScore >= pointsPerGame && playerAGameScore < playerBGameScore) {
                numGamesWonByB++;
            }
        }
        // console.log('A defaulted', this.sideADefaulted);
        // console.log('B defaulted', this.sideBDefaulted);
        // in best of 3 need to win 2 games, best of 5 need to win 3, best of 7 need to win 4
        int minimumNumberOfGamesToWin = (numberOfGames == 3) ? 2 : ((numberOfGames == 5) ? 3 : 4);
        if (profileId.equals(this.playerAProfileId)) {
            return (numGamesWonByA == minimumNumberOfGamesToWin) || (this.sideBDefaulted && !this.sideADefaulted);
        } else {
            return (numGamesWonByB == minimumNumberOfGamesToWin) || (this.sideADefaulted && !this.sideBDefaulted);
        }
    }

    /**
     * Tests if the complete match score was entered
     *
     * @param numberOfGames
     * @param pointsPerGame
     */
    public boolean isMatchFinished(int numberOfGames, int pointsPerGame) {
        return this.isMatchWinner(this.playerAProfileId, numberOfGames, pointsPerGame) ||
                this.isMatchWinner(this.playerBProfileId, numberOfGames, pointsPerGame);
    }

    /**
     *
     * @return
     */
    public String getCompactResult(int numberOfGames, int pointsPerGame) {
        String compactResult = "";
        int numGamesWonByA = 0;
        int numGamesWonByB = 0;
        int minimumNumberOfGamesToWin = (numberOfGames == 3) ? 2 : ((numberOfGames == 5) ? 3 : 4);
        boolean playerAWonMatch = this.isMatchWinner(this.playerAProfileId, numberOfGames, pointsPerGame);
        for (int i = 0; i < numberOfGames; i++) {
            int playerAGameScore = 0;
            int playerBGameScore = 0;
            switch (i) {
                case 0:
                    playerAGameScore = this.game1ScoreSideA;
                    playerBGameScore = this.game1ScoreSideB;
                    break;
                case 1:
                    playerAGameScore = this.game2ScoreSideA;
                    playerBGameScore = this.game2ScoreSideB;
                    break;
                case 2:
                    playerAGameScore = this.game3ScoreSideA;
                    playerBGameScore = this.game3ScoreSideB;
                    break;
                case 3:
                    playerAGameScore = this.game4ScoreSideA;
                    playerBGameScore = this.game4ScoreSideB;
                    break;
                case 4:
                    playerAGameScore = this.game5ScoreSideA;
                    playerBGameScore = this.game5ScoreSideB;
                    break;
                case 5:
                    playerAGameScore = this.game6ScoreSideA;
                    playerBGameScore = this.game6ScoreSideB;
                    break;
                case 6:
                    playerAGameScore = this.game7ScoreSideA;
                    playerBGameScore = this.game7ScoreSideB;
                    break;
            }

            if (playerAGameScore >= pointsPerGame && playerBGameScore < playerAGameScore) {
                numGamesWonByA++;
            } else if (playerBGameScore >= pointsPerGame && playerAGameScore < playerBGameScore) {
                numGamesWonByB++;
            }

            boolean playerAWonGame = (playerAGameScore >= pointsPerGame && playerBGameScore < playerAGameScore);
            compactResult += (compactResult.isEmpty()) ? "" : ",";
            if (playerAWonMatch) {
                compactResult += (playerAWonGame) ?  playerBGameScore : (-1 * playerAGameScore);
            } else {
                // player B won match
                compactResult += (playerAWonGame) ?  (-1 * playerBGameScore) : playerAGameScore;
            }

            boolean enoughGamesCollected = (numGamesWonByA == minimumNumberOfGamesToWin) || (numGamesWonByB == minimumNumberOfGamesToWin);
            if (enoughGamesCollected) {
                break;
            }
        }

        return compactResult;
    }

    /**
     *
     * @param numberOfGames
     * @param pointsPerGame
     * @return
     */
    public MatchResult getGamesOnlyResult(int numberOfGames, int pointsPerGame) {
        int numGamesWonByA = 0;
        int numGamesWonByB = 0;
        int minimumNumberOfGamesToWin = (numberOfGames == 3) ? 2 : ((numberOfGames == 5) ? 3 : 4);
        for (int i = 0; i < numberOfGames; i++) {
            int playerAGameScore = 0;
            int playerBGameScore = 0;
            switch (i) {
                case 0:
                    playerAGameScore = this.game1ScoreSideA;
                    playerBGameScore = this.game1ScoreSideB;
                    break;
                case 1:
                    playerAGameScore = this.game2ScoreSideA;
                    playerBGameScore = this.game2ScoreSideB;
                    break;
                case 2:
                    playerAGameScore = this.game3ScoreSideA;
                    playerBGameScore = this.game3ScoreSideB;
                    break;
                case 3:
                    playerAGameScore = this.game4ScoreSideA;
                    playerBGameScore = this.game4ScoreSideB;
                    break;
                case 4:
                    playerAGameScore = this.game5ScoreSideA;
                    playerBGameScore = this.game5ScoreSideB;
                    break;
                case 5:
                    playerAGameScore = this.game6ScoreSideA;
                    playerBGameScore = this.game6ScoreSideB;
                    break;
                case 6:
                    playerAGameScore = this.game7ScoreSideA;
                    playerBGameScore = this.game7ScoreSideB;
                    break;
            }

            if (playerAGameScore >= pointsPerGame && playerBGameScore < playerAGameScore) {
                numGamesWonByA++;
            } else if (playerBGameScore >= pointsPerGame && playerAGameScore < playerBGameScore) {
                numGamesWonByB++;
            }
        }

        MatchResult matchResult = new MatchResult();
        matchResult.setGamesWonByA(numGamesWonByA);
        matchResult.setGamesWonByB(numGamesWonByB);
        matchResult.setPlayerALetter(this.getPlayerALetter());
        matchResult.setPlayerBLetter(this.getPlayerBLetter());
        matchResult.setSideADefaulted(this.isSideADefaulted());
        matchResult.setSideBDefaulted(this.isSideBDefaulted());
        return matchResult;
    }

    @Override
    public Match clone() {
        try {
            Match clone = (Match) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
