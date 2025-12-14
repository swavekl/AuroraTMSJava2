package com.auroratms.event;

import com.auroratms.tournament.EligibilityRestriction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "tournamentevent", indexes = {
        @Index(name = "idx_tournamentfk", columnList = "tournamentFk")
})
@NoArgsConstructor
@Getter
@Setter
public class TournamentEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long tournamentFk;

    @NonNull
    @Column(length = 60)
    // name e.g. U-2200, Open Singles, Under 17, Over 40 etc.
    private String name;

    // event number listed on blank entry form (used for sorting)
    private int ordinalNumber;

    // day of the tournament on which this event is played 1, 2, 3 etc
    private int day;

    // fractional start time e.g. 9.5 = 9:30 am, 17.0 = 5:00 pm, -1.0 = To be Determined
    private double startTime;

    // single elimination (true), round robin (false)
    private boolean singleElimination;

    // doubles (true) or singles (false)
    private boolean doubles;

    @Column(columnDefinition = "integer default 0")
    private EventEntryType eventEntryType = EventEntryType.INDIVIDUAL;

    // maximum entries, 0 if no limit
    private int maxEntries;

    // current number of entries
    private int numEntries;

    private int minPlayerRating;
    private int maxPlayerRating;

    private int minPlayerAge;
    private int maxPlayerAge;

    private AgeRestrictionType ageRestrictionType = AgeRestrictionType.NONE;
    private Date ageRestrictionDate;

    // flag indicating if event has any gender restrictions (men's or women's only event)
    private GenderRestriction genderRestriction = GenderRestriction.NONE;

    @Column(columnDefinition = "integer default 0")
    private EligibilityRestriction eligibilityRestriction = EligibilityRestriction.OPEN;

    // round robin options
    private int playersPerGroup;
    private DrawMethod drawMethod;

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

    private FeeStructure feeStructure = FeeStructure.FIXED;

    // fees
    private double feeAdult;
    private double feeJunior;

    // fixed team fees
    private double perTeamFee;
    private double perPlayerFee;

    // applies to all tournaments i.e. withdrawal penalty
    private double cancellationFee;

    // team size is a range 2 to 3, or 3 to  5.
    private int minTeamPlayers;
    private int maxTeamPlayers;

    // method used to calculate team rating
    private TeamRatingCalculationMethod teamRatingCalculationMethod = TeamRatingCalculationMethod.SUM_TOP_TWO;

    // if true match scores were entered for this event so redoing draws should be prohibited
    private boolean matchScoresEntered;

    // prize money and other non-queryable information
    // to avoid having to change database schema each time we add new field to configuration
    // we will persist configuration as JSON in this field.
    @Column(length = 6000)
    private String content;

    // rounds draws and divisions configuration
    // to avoid having to change database schema each time we add new field to configuration
    // we will persist configuration as JSON in this field.
    @Column(length = 10000)
    private String roundsContent;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TournamentEventEntity that = (TournamentEventEntity) o;
        return tournamentFk == that.tournamentFk && ordinalNumber == that.ordinalNumber && day == that.day && Double.compare(startTime, that.startTime) == 0 && singleElimination == that.singleElimination && doubles == that.doubles && maxEntries == that.maxEntries && numEntries == that.numEntries && minPlayerRating == that.minPlayerRating && maxPlayerRating == that.maxPlayerRating && minPlayerAge == that.minPlayerAge && maxPlayerAge == that.maxPlayerAge && playersPerGroup == that.playersPerGroup && numTablesPerGroup == that.numTablesPerGroup && pointsPerGame == that.pointsPerGame && numberOfGames == that.numberOfGames && numberOfGamesSEPlayoffs == that.numberOfGamesSEPlayoffs && numberOfGamesSEQuarterFinals == that.numberOfGamesSEQuarterFinals && numberOfGamesSESemiFinals == that.numberOfGamesSESemiFinals && numberOfGamesSEFinals == that.numberOfGamesSEFinals && play3rd4thPlace == that.play3rd4thPlace && playersToAdvance == that.playersToAdvance && advanceUnratedWinner == that.advanceUnratedWinner && playersToSeed == that.playersToSeed && Double.compare(feeAdult, that.feeAdult) == 0 && Double.compare(feeJunior, that.feeJunior) == 0 && Double.compare(perTeamFee, that.perTeamFee) == 0 && Double.compare(perPlayerFee, that.perPlayerFee) == 0 && Double.compare(cancellationFee, that.cancellationFee) == 0 && minTeamPlayers == that.minTeamPlayers && maxTeamPlayers == that.maxTeamPlayers && matchScoresEntered == that.matchScoresEntered && Objects.equals(id, that.id) && Objects.equals(name, that.name) && eventEntryType == that.eventEntryType && ageRestrictionType == that.ageRestrictionType && Objects.equals(ageRestrictionDate, that.ageRestrictionDate) && genderRestriction == that.genderRestriction && eligibilityRestriction == that.eligibilityRestriction && drawMethod == that.drawMethod && teamRatingCalculationMethod == that.teamRatingCalculationMethod && Objects.equals(content, that.content) && Objects.equals(roundsContent, that.roundsContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tournamentFk, name, ordinalNumber, day, startTime, singleElimination, doubles, eventEntryType, maxEntries, numEntries, minPlayerRating, maxPlayerRating, minPlayerAge, maxPlayerAge, ageRestrictionType, ageRestrictionDate, genderRestriction, eligibilityRestriction, playersPerGroup, drawMethod, numTablesPerGroup, pointsPerGame, numberOfGames, numberOfGamesSEPlayoffs, numberOfGamesSEQuarterFinals, numberOfGamesSESemiFinals, numberOfGamesSEFinals, play3rd4thPlace, playersToAdvance, advanceUnratedWinner, playersToSeed, feeAdult, feeJunior, perTeamFee, perPlayerFee, cancellationFee, minTeamPlayers, maxTeamPlayers, teamRatingCalculationMethod, matchScoresEntered, content, roundsContent);
    }
}
