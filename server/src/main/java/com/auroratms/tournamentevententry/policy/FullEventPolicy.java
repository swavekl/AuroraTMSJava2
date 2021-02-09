package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.EventEntryStatus;

/**
 * Checks if event is full
 */
public class FullEventPolicy implements IEventPolicy {
    public FullEventPolicy() {
    }

    @Override
    public boolean isEntryDenied(TournamentEventEntity event) {
        // no limit don't deny
        if (event.getMaxEntries() == 0) {
            return false;
        }
        return !(event.getNumEntries() < event.getMaxEntries());
    }

    @Override
    public EventEntryStatus getStatus() {
        return EventEntryStatus.WAITING_LIST;
    }
}
