package com.auroratms.tournamententry;

import lombok.Data;

import java.util.List;

@Data
public class TournamentEntryInfo {

    // tournament entry id
    private long entryId;

    // player profile id
    private String profileId;

    // last, first name
    private String firstName;
    private String lastName;

    // rating as of eligibility date
    private int eligibilityRating;

    // current rating
    private int seedRating;

    // ids of events player entered
    private List<Long> eventIds;

    // ids of events player is waiting on
    private List<Long> waitingListEventIds;

    // ids of events in pending confirmation state
    private List<Long> pendingEventIds;

    // name of player's home club
    private String clubName;

    // state abbreviation
    private String state;
}
