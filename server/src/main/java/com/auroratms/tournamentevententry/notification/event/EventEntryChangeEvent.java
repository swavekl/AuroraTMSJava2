package com.auroratms.tournamentevententry.notification.event;

import com.auroratms.tournamentevententry.EventEntryStatus;

public class EventEntryChangeEvent {

    // type of change
    private ChangeType changeType;

    // in which tournament
    private long tournamentId;

    // in which event
    private long eventId;

    private EventEntryStatus status;

    public EventEntryChangeEvent(ChangeType changeType, long tournamentId, long eventId, EventEntryStatus status) {
        this.changeType = changeType;
        this.tournamentId = tournamentId;
        this.eventId = eventId;
        this.status = status;
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

    public EventEntryStatus getStatus() {
        return status;
    }
}
