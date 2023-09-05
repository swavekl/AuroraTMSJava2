package com.auroratms.ratingsprocessing.notification;

import com.auroratms.ratingsprocessing.notification.event.RatingsProcessingEndEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class RatingsProcessingEventPublisher {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishRatingsProcessingEndEvent() {
        RatingsProcessingEndEvent event = new RatingsProcessingEndEvent(this);
        applicationEventPublisher.publishEvent(event);
    }
}
