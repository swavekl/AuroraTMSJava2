package com.auroratms.reports.notification;

import com.auroratms.reports.notification.event.EventType;
import com.auroratms.reports.notification.event.TournamentReportsProcessingEvent;
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

    public void publishGenerateReportsEvent(TournamentProcessingRequest request, long detailId) {
        String currentUserName = UserRolesHelper.getCurrentUsername();
        TournamentReportsProcessingEvent event = new TournamentReportsProcessingEvent(request.getId(), currentUserName, EventType.GenerateReports);
        event.setDetailId(detailId);
        this.applicationEventPublisher.publishEvent(event);
    }

    public void publishSubmitReportsEvent(TournamentProcessingRequest request) {
        String currentUserName = UserRolesHelper.getCurrentUsername();
        TournamentReportsProcessingEvent event = new TournamentReportsProcessingEvent(request.getId(), currentUserName, EventType.Submit);
        this.applicationEventPublisher.publishEvent(event);
    }
}
