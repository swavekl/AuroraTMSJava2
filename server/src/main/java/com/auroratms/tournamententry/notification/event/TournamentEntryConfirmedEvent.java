package com.auroratms.tournamententry.notification.event;

public class TournamentEntryConfirmedEvent {

    private long tournamentEntryId;

    public TournamentEntryConfirmedEvent(long tournamentEntryId) {
        this.tournamentEntryId = tournamentEntryId;
    }

    public long getTournamentEntryId() {
        return tournamentEntryId;
    }
}
