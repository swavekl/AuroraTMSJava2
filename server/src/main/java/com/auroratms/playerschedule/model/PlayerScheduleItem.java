package com.auroratms.playerschedule.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Information about the round-robin round group opponents
 * or a single match from a single elimination round
 * This is data mostly from the match cards
 */
@Data
@NoArgsConstructor
public class PlayerScheduleItem {
    // day 1 of tournament, day 2 etc.
    private int day;

    // event start time or individual match start time, 10.5 is 10:30
    private double startTime;

    // name of the event
    private String eventName;

    // event id
    private long eventId;

    // 0 for RR, 16, 8, 4, 2 etc for single elimination phase
    private int round;

    // group number for RR phase
    private int group;

    // comma separated, if more than one
    private String assignedTables;

    // id of a match card corresponding to this
    private long matchCardId;

    // details of each player in this item (group or match)
    private List<PlayerDetail> playerDetails;
}
