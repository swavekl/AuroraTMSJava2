package com.auroratms.draw.generation;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Helper class with information needed for draws
 */
@Data
@NoArgsConstructor
public class PlayerDrawInfo {

    // player profile id
    private String profileId;

    // full name last, first name
    private String playerName;

    // player rating
    private int rating;

    // club where player plays
    private long clubId;

    // club where player plays
    private String clubName;

    // city for geographical separation
    private String city;

    // state abbreviation for geographical isolation
    private String state;

    // country of player
    private String country = "US";
}
