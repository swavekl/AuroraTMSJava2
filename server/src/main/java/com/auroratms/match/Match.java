package com.auroratms.match;

import lombok.Data;
import lombok.NoArgsConstructor;

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
    private MatchCard matchCard;

    // match number within a round so that matches are ordered properly on the match card
    private int matchNum;

    // for round robin phase 0,
    // for single elimination - 64, 32, 16, 8 (quarter finals), 4 (semifinals), 2 (finals and 3rd/4th place)
    private int round;

    // profile id of two players for singles matches
    // for doubles matches profile ids of team members are separated by ; like this
    // playerAProfileId;playerAPartnerProfileId and playerBProfileId;playerBPartnerProfileId
    private String playerAProfileId;
    private String playerBProfileId;

    // true if the side defaulted.  If both are true the match wasn't played
    private boolean sideADefaulted;
    private boolean sideBDefaulted;

    // indicates if side took a timeout - help for umpire
    private boolean sideATimeoutTaken;
    private boolean sideBTimeoutTaken;

    // indicates if side A is to serve first, if false side B servers first - help for umpire
    private boolean sideAServesFirst;

    // game (set) scores of played match e.g. 11:7, 11:8,
    // positive if match winner won the game,
    // negative if match winner lost the game
    @Column(nullable = true)
    private int game1Score;
    @Column(nullable = true)
    private int game2Score;
    @Column(nullable = true)
    private int game3Score;
    @Column(nullable = true)
    private int game4Score;
    @Column(nullable = true)
    private int game5Score;
    @Column(nullable = true)
    private int game6Score;
    @Column(nullable = true)
    private int game7Score;
}
