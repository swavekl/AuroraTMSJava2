package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEvent;
import com.auroratms.tournamentevententry.AvailabilityStatus;

public interface IEventPolicy {

    public boolean isEntryDenied(TournamentEvent event);

    AvailabilityStatus getStatus();
}
