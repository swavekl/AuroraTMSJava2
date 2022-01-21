package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.AvailabilityStatus;

/**
 * Checks if event is full
 */
public class FullEventPolicy implements IEventPolicy {

    private boolean isDenied = false;

    public FullEventPolicy() {
    }

    @Override
    public boolean isEntryDenied(TournamentEvent event) {
        // no limit don't deny
        if (event.getMaxEntries() == 0) {
            isDenied = false;
        } else {
            isDenied = !(event.getNumEntries() < event.getMaxEntries());
        }
        return isDenied;
    }

    @Override
    public AvailabilityStatus getStatus() {
        return (isDenied) ? AvailabilityStatus.EVENT_FULL : AvailabilityStatus.AVAILABLE_FOR_ENTRY;
    }
}
