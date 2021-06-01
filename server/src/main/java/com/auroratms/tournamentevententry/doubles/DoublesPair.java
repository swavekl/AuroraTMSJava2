package com.auroratms.tournamentevententry.doubles;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Represents information about doubles pair
 */
@Entity
@Table(name = "doublespair")
@NoArgsConstructor
@Getter
@Setter
public class DoublesPair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // event id for which this doubles pair is created
    @Column(nullable = false)
    private long tournamentEventFk;

    // event entry id for player A
    @Column(nullable = false)
    private long playerAEventEntryFk;

    // event entry id for player B (if null the profileB indicates which player is requested)
    @Column(nullable = true)
    private long playerBEventEntryFk;

    // team eligibility rating (rating on a particular cut off date)
    private int eligibilityRating;

    // team seed rating (rating used for making draws)
    private int seedRating;
}
