package com.auroratms.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Holds information about each division e.g. name, points per game etc.
 */
@NoArgsConstructor
@Getter
@Setter
public class TournamentEventRoundDivision implements Serializable {

    // name of division e.g. Championship, Class AA, Class A etc.
    private String divisionName;

    // round robin options
    private int playersPerGroup;

    // how draws should be made in this division
    private DrawMethod drawMethod;

    // which players to pull into this division from previous round
    private int previousRoundPlayerRanking;
    private int previousRoundPlayerRankingEnd;

    // previous division index from which players will be drawn into this division
    private int previousDivisionIdx;

    // number of tables per group
    private int numTablesPerGroup = 1;

    // points per game - 11 but sometimes 21
    private int pointsPerGame = 11;

    // best of 3, 5, 7 or 9 games per match in the main round (i.e. round robin)
    private int numberOfGames;

    // in single elimination round or if event is a single elimination only
    // number of games in rounds prior to quarter finals e.g. 5
    private int numberOfGamesSEPlayoffs = 5;

    // number of games in quarter, semi finals and 3rd/4th place matches
    private int numberOfGamesSEQuarterFinals = 5;
    private int numberOfGamesSESemiFinals = 5;
    private int numberOfGamesSEFinals = 5;

    // indicates if a match for 3rd adn 4th place is to be played
    private boolean play3rd4thPlace;

    // number of players to advance, 0, 1 or 2
    private int playersToAdvance;

    // if this event advances player to another event or round - indicate if unrated players are to be advanced
    // typically not but in Open Singles they usually are
    private boolean advanceUnratedWinner = false;

    // number of players to seed directly into next round
    private int playersToSeed;

    public TournamentEventRoundDivision(TournamentEventRoundDivision division) {
        this.divisionName = division.divisionName;
        this.playersPerGroup = division.playersPerGroup;
        this.drawMethod = division.drawMethod;
        this.previousRoundPlayerRanking = division.previousRoundPlayerRanking;
        this.previousRoundPlayerRankingEnd = division.previousRoundPlayerRankingEnd;
        this.previousDivisionIdx = division.previousDivisionIdx;
        this.numTablesPerGroup = division.numTablesPerGroup;
        this.pointsPerGame = division.pointsPerGame;
        this.numberOfGames = division.numberOfGames;
        this.numberOfGamesSEPlayoffs = division.numberOfGamesSEPlayoffs;
        this.numberOfGamesSEQuarterFinals = division.numberOfGamesSEQuarterFinals;
        this.numberOfGamesSESemiFinals = division.numberOfGamesSESemiFinals;
        this.numberOfGamesSEFinals = division.numberOfGamesSEFinals;
        this.play3rd4thPlace = division.play3rd4thPlace;
        this.playersToAdvance = division.playersToAdvance;
        this.advanceUnratedWinner = division.advanceUnratedWinner;
        this.playersToSeed = division.playersToSeed;
    }
}
