package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.AvailabilityStatus;

public interface IEventPolicy {

    public boolean isEntryDenied(TournamentEventEntity event);

    AvailabilityStatus getStatus();
}
