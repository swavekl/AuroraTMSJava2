package com.auroratms.match;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

/**
 * Represents a single match between two players or two doubles teams
 */
@Entity
@Table(name = "matches")  // match is a reserved keyword in SQL so we use plural
@Data
@NoArgsConstructor
public class Match {

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
}
