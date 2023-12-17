package com.auroratms.results;

import lombok.Data;

@Data
public class EventResultStatus {

    private long eventId;
    private String eventName;
    private boolean resultsAvailable;

    // names of players (or doubles teams partners) who took 1st, 2nd 3rd and 4th place
    private String firstPlacePlayer;
    private String secondPlacePlayer;
    private String thirdPlacePlayer;
    private String fourthPlacePlayer;

    // indicates if a match for 3rd and 4th place is to be played
    private boolean play3rd4thPlace;

    // is it giant round robin
    private boolean giantRREvent;
}
