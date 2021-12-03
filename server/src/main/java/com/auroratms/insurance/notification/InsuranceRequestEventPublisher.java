package com.auroratms.insurance.notification;

import com.auroratms.insurance.InsuranceRequest;
import com.auroratms.insurance.InsuranceRequestStatus;
import com.auroratms.insurance.notification.event.InsuranceRequestEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class InsuranceRequestEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(InsuranceRequest insuranceRequest, InsuranceRequestStatus oldStatus) {
        InsuranceRequestEvent event = new InsuranceRequestEvent(insuranceRequest, oldStatus);
        applicationEventPublisher.publishEvent(event);
    }

}
