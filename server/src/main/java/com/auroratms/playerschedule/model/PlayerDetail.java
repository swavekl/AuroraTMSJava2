package com.auroratms.playerschedule.model;

import com.auroratms.status.EventStatusCode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Player detail for this event
 */
@Data
@NoArgsConstructor
public class PlayerDetail {
    // 0 for unrated, otherwise rating
    private int rating;

    // if true this is estimated rating
    private boolean estimated;

    // A, B, C etc.
    private Character playerCode;

    // last name, first name
    private String playerFullName;

    // will play or not
    private EventStatusCode statusCode;

    // ETA if late arrival
    private String estimatedArrivalTime;
}
