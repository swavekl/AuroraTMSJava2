package com.auroratms.draw;

import com.auroratms.draw.conflicts.ConflictType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

/**
 * Persistent entity for representing event draw items (round robin or single elimination)
 */
@Entity
@Table(name = "drawitem")
@Data
@NoArgsConstructor
public class DrawItem {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // id of the event for which this draw is made
    private long eventFk;

    // draw group number
    private int groupNum;

    // for round robin phase 0,
    // for single elimination - 64, 32, 16, 8 (quarter finals), 4 (semifinals), 2 (finals and 3rd/4th place)
    private int round;

    // Players place in the draw for the group
    private int placeInGroup;

    // seed number of player/doubles team in single elimination round,
    // 0 in round robin round
    private int seSeedNumber;

    // bye number if this draw item represents a bye (e.g. 1, 2, 3 etc), 0 otherwise
    private int byeNum;

    // single elimination line number for preserving order of the bracket -
    private int singleElimLineNum;

    private DrawType drawType;

    // player profile id (Okta) for fetching state, club etc.
    @NonNull
    private String playerId;

    // constant indicating that player id is to be determined
    public static final String TBD_PROFILE_ID = "TBD";

    // type of conflict
    @Column(columnDefinition = "integer default 0")
    private ConflictType conflictType = ConflictType.NO_CONFLICT;

    // seed rating at a time of making the draws
    private int rating;

    // these values are added to enable easy showing of
    @Transient
    private String playerName;

    // state of US where player lives
    @Transient
    private String state;

    // name of the table tennis club where player plays
    @Transient
    private String clubName;

    // tournament entry id for lookup of above geographical player information
    @Transient
    private long entryId;
}
