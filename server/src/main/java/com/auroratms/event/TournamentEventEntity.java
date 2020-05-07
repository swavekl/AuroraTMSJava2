package com.auroratms.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tournamentevent")
@NoArgsConstructor
@Getter
@Setter
public class TournamentEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "tournament_fk", nullable = false)
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    @JsonIgnore
//    private TournamentEntity tournamentEntity;
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

    // maximum entries, 0 if no limit
    private int maxEntries;

    // current number of entries
    private int numEntries;

    private int minPlayerRating;
    private int maxPlayerRating;

    private int minPlayerAge;
    private int maxPlayerAge;

    // flag indicating if event has any gender restrictions (men's or women's only event)
    private GenderRestriction genderRestriction = GenderRestriction.NONE;

    // round robin options
    private int playersPerGroup;
    private int drawMethod;

    // number of tables per group
    private int numTablesPerGroup = 1;

    // best of 3, 5, 7 or 9 games per match
    private int numberOfGames;

    // number of players to advance, 0, 1 or 2
    private int playersToAdvance;

    // number of players to seed directly into next round
    private int playersToSeed;

    // fees
    private double feeAdult;
    private double feeJunior;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TournamentEventEntity that = (TournamentEventEntity) o;
        return tournamentFk == that.tournamentFk &&
                ordinalNumber == that.ordinalNumber &&
                day == that.day &&
                Double.compare(that.startTime, startTime) == 0 &&
                singleElimination == that.singleElimination &&
                doubles == that.doubles &&
                maxEntries == that.maxEntries &&
                minPlayerRating == that.minPlayerRating &&
                maxPlayerRating == that.maxPlayerRating &&
                minPlayerAge == that.minPlayerAge &&
                maxPlayerAge == that.maxPlayerAge &&
                playersPerGroup == that.playersPerGroup &&
                drawMethod == that.drawMethod &&
                numberOfGames == that.numberOfGames &&
                playersToAdvance == that.playersToAdvance &&
                playersToSeed == that.playersToSeed &&
                Double.compare(that.feeAdult, feeAdult) == 0 &&
                Double.compare(that.feeJunior, feeJunior) == 0 &&
                id.equals(that.id) &&
                name.equals(that.name) &&
                genderRestriction == that.genderRestriction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tournamentFk, name, ordinalNumber, day, startTime, singleElimination, doubles, maxEntries, minPlayerRating, maxPlayerRating, minPlayerAge, maxPlayerAge, genderRestriction, playersPerGroup, drawMethod, numberOfGames, playersToAdvance, playersToSeed, feeAdult, feeJunior);
    }
}
