package com.auroratms.tournamententry;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Date;

@Data
public class TournamentEntryInfo {

    // tournament entry id
    private long entryId;

    // player profile id
    private String profileId;

    // last, first name
    private String firstName;
    private String lastName;

    // player gender
    private String gender;

    // rating as of eligibility date
    private int eligibilityRating;

    // current rating
    private int seedRating;

    // ids of events player entered
    private List<Long> eventIds;

    // ids of events player is waiting on
    private List<Long> waitingListEventIds;

    // date when entered this event waiting list
    private List<Date> waitingListEnteredDates;

    // ids of events in pending confirmation state
    private List<Long> pendingEventIds;

    // event-specific partner information for doubles events; populated only when requested
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DoublesPartnerInfo> doublesPartnerStatuses;

    // name of player's home club
    private String clubName;

    // state abbreviation
    private String state;

    @Data
    public static class DoublesPartnerInfo {
        private long eventId;
        private String partnerProfileId;
        private String partnerName;
        private Status status;

        public enum Status {
            CONFIRMED,
            REQUESTED
        }
    }
}
