package com.auroratms.clubaffiliationapp.notification;

import com.auroratms.clubaffiliationapp.ClubAffiliationApplication;
import com.auroratms.clubaffiliationapp.notification.event.ClubAffiliationApplicationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ClubAffiliationApplicationEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent (ClubAffiliationApplication clubAffiliationApplication) {
        ClubAffiliationApplicationEvent event = new ClubAffiliationApplicationEvent(clubAffiliationApplication);
        applicationEventPublisher.publishEvent(event);
    }
}
