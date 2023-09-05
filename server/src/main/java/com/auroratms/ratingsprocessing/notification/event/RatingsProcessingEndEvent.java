package com.auroratms.ratingsprocessing.notification.event;

import org.springframework.context.ApplicationEvent;

public class RatingsProcessingEndEvent extends ApplicationEvent {
    public RatingsProcessingEndEvent(Object source) {
        super(source);
    }
}
