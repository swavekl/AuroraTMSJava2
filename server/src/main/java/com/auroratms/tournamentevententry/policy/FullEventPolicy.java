package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.EventEntryStatus;

/**
 * Checks if event is full
 */
public class FullEventPolicy implements IEventPolicy {

    private boolean isDenied = false;

    public FullEventPolicy() {
    }

    @Override
    public boolean isEntryDenied(TournamentEventEntity event) {
        // no limit don't deny
        if (event.getMaxEntries() == 0) {
            isDenied = false;
        } else {
            isDenied = !(event.getNumEntries() < event.getMaxEntries());
        }
        return isDenied;
    }

    @Override
    public EventEntryStatus getStatus() {
        return (isDenied) ? EventEntryStatus.WAITING_LIST : EventEntryStatus.NOT_ENTERED;
    }
}
