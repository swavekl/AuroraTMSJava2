package com.auroratms.tournamentevententry.doubles.notification;

import com.auroratms.tournamentevententry.doubles.notification.event.MakeBreakDoublesPairsEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publisher for events making or breaking doubles pairs
 */
@Component
public class DoublesEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishMakeBreakPairsEvent(MakeBreakDoublesPairsEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
