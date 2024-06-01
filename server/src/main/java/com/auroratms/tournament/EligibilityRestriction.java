package com.auroratms.tournament;

public enum EligibilityRestriction {
    // open to all players
    OPEN,
    // closed to players from outside of the state where the venue is located
    CLOSED_STATE,
    // closed to players from outside of the region e.g. Midwest
    CLOSED_REGIONAL,
    // closed to players from outside of the Nation
    CLOSED_NATIONAL
}

