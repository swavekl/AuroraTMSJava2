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

    // id of the club where player plays
    private Long clubId;

    // state abbreviation for geographical isolation
    private String state;

}
