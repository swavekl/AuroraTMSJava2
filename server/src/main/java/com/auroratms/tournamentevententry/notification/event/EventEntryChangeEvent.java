package com.auroratms.tournamentevententry.notification.event;

public class EventEntryChangeEvent {

    // type of change
    private ChangeType changeType;

    // in which tournament
    private long tournamentId;

    // in which event
    private long eventId;

    public EventEntryChangeEvent(ChangeType changeType, long tournamentId, long eventId) {
        this.changeType = changeType;
        this.tournamentId = tournamentId;
        this.eventId = eventId;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public long getEventId() {
        return eventId;
    }
}
