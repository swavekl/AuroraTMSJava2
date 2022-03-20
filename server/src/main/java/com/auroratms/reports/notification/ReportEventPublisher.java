package com.auroratms.reports.notification;

import com.auroratms.reports.notification.event.TournamentReportsGenerateEvent;
import com.auroratms.tournamentprocessing.TournamentProcessingRequest;
import com.auroratms.users.UserRolesHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publisher of event initiating report generation so that it happens in a asynchronous
 * manner since it takes a few moments to complete.
 */
@Component
public class ReportEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(TournamentProcessingRequest request) {
        String currentUserName = UserRolesHelper.getCurrentUsername();
        TournamentReportsGenerateEvent event = new TournamentReportsGenerateEvent(request.getId(), currentUserName);
        this.applicationEventPublisher.publishEvent(event);
    }
}
