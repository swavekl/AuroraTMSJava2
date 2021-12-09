package com.auroratms.sanction.notification;

import com.auroratms.sanction.SanctionRequest;
import com.auroratms.sanction.SanctionRequestStatus;
import com.auroratms.sanction.notification.event.SanctionRequestEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SanctionRequestEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(SanctionRequest sanctionRequest, SanctionRequestStatus oldStatus) {
        SanctionRequestEvent sanctionRequestEvent = new SanctionRequestEvent(sanctionRequest, oldStatus);
        applicationEventPublisher.publishEvent(sanctionRequestEvent);
    }
}
