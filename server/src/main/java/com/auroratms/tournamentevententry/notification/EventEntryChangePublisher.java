package com.auroratms.tournamentevententry.notification;

import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.notification.event.ChangeType;
import com.auroratms.tournamentevententry.notification.event.EventEntryChangeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publisher to send events about event entry changes.  Initially deletion from event
 */
@Component
@RequiredArgsConstructor
public class EventEntryChangePublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishTournamentEventDeletedEvent (long tournamentId, long eventId, EventEntryStatus status) {
        EventEntryChangeEvent eventEntryChangedEvent = new EventEntryChangeEvent(ChangeType.DELETED, tournamentId, eventId, status);
        this.applicationEventPublisher.publishEvent(eventEntryChangedEvent);
    }
}
