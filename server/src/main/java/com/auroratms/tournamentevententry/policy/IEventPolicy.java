package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.EventEntryStatus;

public interface IEventPolicy {

    public boolean isEntryDenied(TournamentEventEntity event);

    EventEntryStatus getStatus();
}
