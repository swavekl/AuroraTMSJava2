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
}
