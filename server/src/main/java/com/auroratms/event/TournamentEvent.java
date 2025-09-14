package com.auroratms.event;

import com.auroratms.tournament.EligibilityRestriction;
import com.auroratms.tournament.TournamentConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class TournamentEvent implements Serializable {
    private Long id;

    private long tournamentFk;

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

    // fees
    private double feeAdult;
    private double feeJunior;

    // if true match scores were entered for this event so redoing draws should be prohibited
    private boolean matchScoresEntered;

    // prize money and other non-queryable information
    private TournamentEventConfiguration configuration;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TournamentEvent that = (TournamentEvent) o;
        return tournamentFk == that.tournamentFk && ordinalNumber == that.ordinalNumber && day == that.day && Double.compare(that.startTime, startTime) == 0 && singleElimination == that.singleElimination && doubles == that.doubles && maxEntries == that.maxEntries && numEntries == that.numEntries && minPlayerRating == that.minPlayerRating && maxPlayerRating == that.maxPlayerRating && minPlayerAge == that.minPlayerAge && maxPlayerAge == that.maxPlayerAge && playersPerGroup == that.playersPerGroup && numTablesPerGroup == that.numTablesPerGroup && pointsPerGame == that.pointsPerGame && numberOfGames == that.numberOfGames && numberOfGamesSEPlayoffs == that.numberOfGamesSEPlayoffs && numberOfGamesSEQuarterFinals == that.numberOfGamesSEQuarterFinals && numberOfGamesSESemiFinals == that.numberOfGamesSESemiFinals && numberOfGamesSEFinals == that.numberOfGamesSEFinals && play3rd4thPlace == that.play3rd4thPlace && playersToAdvance == that.playersToAdvance && advanceUnratedWinner == that.advanceUnratedWinner && playersToSeed == that.playersToSeed && Double.compare(that.feeAdult, feeAdult) == 0 && Double.compare(that.feeJunior, feeJunior) == 0 && id.equals(that.id) && name.equals(that.name) && ageRestrictionType == that.ageRestrictionType && Objects.equals(ageRestrictionDate, that.ageRestrictionDate) && genderRestriction == that.genderRestriction && drawMethod == that.drawMethod && configuration == that.configuration && matchScoresEntered == that.matchScoresEntered;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tournamentFk, name, ordinalNumber, day, startTime, singleElimination, doubles, maxEntries, numEntries, minPlayerRating, maxPlayerRating, minPlayerAge, maxPlayerAge, ageRestrictionType, ageRestrictionDate, genderRestriction, playersPerGroup, drawMethod, numTablesPerGroup, pointsPerGame, numberOfGames, numberOfGamesSEPlayoffs, numberOfGamesSEQuarterFinals, numberOfGamesSESemiFinals, numberOfGamesSEFinals, play3rd4thPlace, playersToAdvance, advanceUnratedWinner, playersToSeed, feeAdult, feeJunior, matchScoresEntered, configuration);
    }

    public TournamentEventEntity toEntity() {
        TournamentEventEntity tournamentEventEntity = new TournamentEventEntity();
        tournamentEventEntity.setId(this.getId());
        tournamentEventEntity.setTournamentFk(this.getTournamentFk());
        tournamentEventEntity.setName(this.getName());
        tournamentEventEntity.setOrdinalNumber(this.getOrdinalNumber());
        tournamentEventEntity.setDay(this.getDay());
        tournamentEventEntity.setStartTime(this.getStartTime());
        tournamentEventEntity.setSingleElimination(this.isSingleElimination());
        tournamentEventEntity.setDoubles(this.isDoubles());
        tournamentEventEntity.setMaxEntries(this.getMaxEntries());
        tournamentEventEntity.setNumEntries(this.getNumEntries());
        tournamentEventEntity.setMinPlayerRating(this.getMinPlayerRating());
        tournamentEventEntity.setMaxPlayerRating(this.getMaxPlayerRating());
        tournamentEventEntity.setMinPlayerAge(this.getMinPlayerAge());
        tournamentEventEntity.setMaxPlayerAge(this.getMaxPlayerAge());
        tournamentEventEntity.setAgeRestrictionType(this.getAgeRestrictionType());
        tournamentEventEntity.setAgeRestrictionDate(this.getAgeRestrictionDate());
        tournamentEventEntity.setGenderRestriction(this.getGenderRestriction());
        tournamentEventEntity.setEligibilityRestriction(this.getEligibilityRestriction());
        tournamentEventEntity.setPlayersPerGroup(this.getPlayersPerGroup());
        tournamentEventEntity.setDrawMethod(this.getDrawMethod());
        tournamentEventEntity.setNumTablesPerGroup(this.getNumTablesPerGroup());
        tournamentEventEntity.setPointsPerGame(this.getPointsPerGame());
        tournamentEventEntity.setNumberOfGames(this.getNumberOfGames());
        tournamentEventEntity.setNumberOfGamesSEPlayoffs(this.getNumberOfGamesSEPlayoffs());
        tournamentEventEntity.setNumberOfGamesSEQuarterFinals(this.getNumberOfGamesSEQuarterFinals());
        tournamentEventEntity.setNumberOfGamesSESemiFinals(this.getNumberOfGamesSESemiFinals());
        tournamentEventEntity.setNumberOfGamesSEFinals(this.getNumberOfGamesSEFinals());
        tournamentEventEntity.setPlay3rd4thPlace(this.isPlay3rd4thPlace());
        tournamentEventEntity.setPlayersToAdvance(this.getPlayersToAdvance());
        tournamentEventEntity.setAdvanceUnratedWinner(this.isAdvanceUnratedWinner());
        tournamentEventEntity.setPlayersToSeed(this.getPlayersToSeed());
        tournamentEventEntity.setFeeAdult(this.getFeeAdult());
        tournamentEventEntity.setFeeJunior(this.getFeeJunior());
        tournamentEventEntity.setMatchScoresEntered(this.isMatchScoresEntered());

        if (this.getConfiguration() != null) {
            try {
                StringWriter stringWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.writeValue(stringWriter, this.getConfiguration());
                String content = stringWriter.toString();
                tournamentEventEntity.setContent(content);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return tournamentEventEntity;
    }

    public static TournamentEvent fromEntity(TournamentEventEntity tournamentEventEntity) {
        TournamentEvent tournamentEvent = new TournamentEvent();
        tournamentEvent.setId(tournamentEventEntity.getId());
        tournamentEvent.setTournamentFk(tournamentEventEntity.getTournamentFk());
        tournamentEvent.setName(tournamentEventEntity.getName());
        tournamentEvent.setOrdinalNumber(tournamentEventEntity.getOrdinalNumber());
        tournamentEvent.setDay(tournamentEventEntity.getDay());
        tournamentEvent.setStartTime(tournamentEventEntity.getStartTime());
        tournamentEvent.setSingleElimination(tournamentEventEntity.isSingleElimination());
        tournamentEvent.setDoubles(tournamentEventEntity.isDoubles());
        tournamentEvent.setMaxEntries(tournamentEventEntity.getMaxEntries());
        tournamentEvent.setNumEntries(tournamentEventEntity.getNumEntries());
        tournamentEvent.setMinPlayerRating(tournamentEventEntity.getMinPlayerRating());
        tournamentEvent.setMaxPlayerRating(tournamentEventEntity.getMaxPlayerRating());
        tournamentEvent.setMinPlayerAge(tournamentEventEntity.getMinPlayerAge());
        tournamentEvent.setMaxPlayerAge(tournamentEventEntity.getMaxPlayerAge());
        tournamentEvent.setAgeRestrictionType(tournamentEventEntity.getAgeRestrictionType());
        tournamentEvent.setAgeRestrictionDate(tournamentEventEntity.getAgeRestrictionDate());
        tournamentEvent.setGenderRestriction(tournamentEventEntity.getGenderRestriction());
        tournamentEvent.setEligibilityRestriction(tournamentEventEntity.getEligibilityRestriction());
        tournamentEvent.setPlayersPerGroup(tournamentEventEntity.getPlayersPerGroup());
        tournamentEvent.setDrawMethod(tournamentEventEntity.getDrawMethod());
        tournamentEvent.setNumTablesPerGroup(tournamentEventEntity.getNumTablesPerGroup());
        tournamentEvent.setPointsPerGame(tournamentEventEntity.getPointsPerGame());
        tournamentEvent.setNumberOfGames(tournamentEventEntity.getNumberOfGames());
        tournamentEvent.setNumberOfGamesSEPlayoffs(tournamentEventEntity.getNumberOfGamesSEPlayoffs());
        tournamentEvent.setNumberOfGamesSEQuarterFinals(tournamentEventEntity.getNumberOfGamesSEQuarterFinals());
        tournamentEvent.setNumberOfGamesSESemiFinals(tournamentEventEntity.getNumberOfGamesSESemiFinals());
        tournamentEvent.setNumberOfGamesSEFinals(tournamentEventEntity.getNumberOfGamesSEFinals());
        tournamentEvent.setPlay3rd4thPlace(tournamentEventEntity.isPlay3rd4thPlace());
        tournamentEvent.setPlayersToAdvance(tournamentEventEntity.getPlayersToAdvance());
        tournamentEvent.setAdvanceUnratedWinner(tournamentEventEntity.isAdvanceUnratedWinner());
        tournamentEvent.setPlayersToSeed(tournamentEventEntity.getPlayersToSeed());
        tournamentEvent.setFeeAdult(tournamentEventEntity.getFeeAdult());
        tournamentEvent.setFeeJunior(tournamentEventEntity.getFeeJunior());
        tournamentEvent.setMatchScoresEntered(tournamentEventEntity.isMatchScoresEntered());

        tournamentEvent.setConfiguration(null);
        // convert from JSON to configuration
        String content = tournamentEventEntity.getContent();
        if (content != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                tournamentEvent.setConfiguration(mapper.readValue(content, TournamentEventConfiguration.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tournamentEvent;
    }

    public TournamentEvent(TournamentEvent cloneFrom) {
        this.setId(cloneFrom.getId());
        this.setTournamentFk(cloneFrom.getTournamentFk());
        this.setName(cloneFrom.getName());
        this.setOrdinalNumber(cloneFrom.getOrdinalNumber());
        this.setDay(cloneFrom.getDay());
        this.setStartTime(cloneFrom.getStartTime());
        this.setSingleElimination(cloneFrom.isSingleElimination());
        this.setDoubles(cloneFrom.isDoubles());
        this.setMaxEntries(cloneFrom.getMaxEntries());
        this.setNumEntries(cloneFrom.getNumEntries());
        this.setMinPlayerRating(cloneFrom.getMinPlayerRating());
        this.setMaxPlayerRating(cloneFrom.getMaxPlayerRating());
        this.setMinPlayerAge(cloneFrom.getMinPlayerAge());
        this.setMaxPlayerAge(cloneFrom.getMaxPlayerAge());
        this.setAgeRestrictionType(cloneFrom.getAgeRestrictionType());
        this.setAgeRestrictionDate(cloneFrom.getAgeRestrictionDate());
        this.setGenderRestriction(cloneFrom.getGenderRestriction());
        this.setEligibilityRestriction(cloneFrom.getEligibilityRestriction());
        this.setPlayersPerGroup(cloneFrom.getPlayersPerGroup());
        this.setDrawMethod(cloneFrom.getDrawMethod());
        this.setNumTablesPerGroup(cloneFrom.getNumTablesPerGroup());
        this.setPointsPerGame(cloneFrom.getPointsPerGame());
        this.setNumberOfGames(cloneFrom.getNumberOfGames());
        this.setNumberOfGamesSEPlayoffs(cloneFrom.getNumberOfGamesSEPlayoffs());
        this.setNumberOfGamesSEQuarterFinals(cloneFrom.getNumberOfGamesSEQuarterFinals());
        this.setNumberOfGamesSESemiFinals(cloneFrom.getNumberOfGamesSESemiFinals());
        this.setNumberOfGamesSEFinals(cloneFrom.getNumberOfGamesSEFinals());
        this.setPlay3rd4thPlace(cloneFrom.isPlay3rd4thPlace());
        this.setPlayersToAdvance(cloneFrom.getPlayersToAdvance());
        this.setAdvanceUnratedWinner(cloneFrom.isAdvanceUnratedWinner());
        this.setPlayersToSeed(cloneFrom.getPlayersToSeed());
        this.setFeeAdult(cloneFrom.getFeeAdult());
        this.setFeeJunior(cloneFrom.getFeeJunior());
        this.setMatchScoresEntered(cloneFrom.isMatchScoresEntered());
        this.setConfiguration(new TournamentEventConfiguration(cloneFrom.getConfiguration()));
    }
}
