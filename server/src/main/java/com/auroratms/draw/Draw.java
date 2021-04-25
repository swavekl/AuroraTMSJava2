package com.auroratms.draw;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

/**
 * Persistent entity for representing event draws (round robin or single elimination)
 */
@Entity
@Table(name = "draw")
@Data
@NoArgsConstructor
public class Draw {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // id of the event for which this draw is made
    private long eventFk;

    // draw group number
    private int groupNum;

    // Players place in the draw for the group
    private int placeInGroup;

    private DrawType drawType;

    // id of the player (Okta) for fetching state, club etc.
    @NonNull
    private String playerId;

    // list of conflicts - possibly null or list like 1, 2, 5 representing conflict types
    private String conflicts;
}
