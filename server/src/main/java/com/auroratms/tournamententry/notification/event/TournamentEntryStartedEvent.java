package com.auroratms.tournamententry.notification.event;

/**
 * Event which initiates sending email after tournament registration is complete
 */
public class TournamentEntryStartedEvent {

    long tournamentEntryFk;

    public TournamentEntryStartedEvent(long tournamentEntryFk) {
        this.tournamentEntryFk = tournamentEntryFk;
    }

    public long getTournamentEntryFk() {
        return tournamentEntryFk;
    }
}
