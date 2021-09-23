package com.auroratms.draw.notification;

import com.auroratms.draw.DrawItem;
import com.auroratms.draw.DrawType;
import com.auroratms.draw.notification.event.DrawAction;
import com.auroratms.draw.notification.event.DrawsEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DrawsEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(long eventId, DrawAction action, DrawType drawType, List<DrawItem> drawItems) {
        DrawsEvent event = new DrawsEvent(eventId, drawType, action, drawItems);
        applicationEventPublisher.publishEvent(event);
    }
}


