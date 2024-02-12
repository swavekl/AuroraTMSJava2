package com.auroratms.reports.notification.event;

/**
 * Event for initiating report generation, submition or payment
 */
public class TournamentReportsProcessingEvent {

    // request id in database
    private long tournamentProcessingRequestId;

    // detail id which we are processing
    private long detailId = -1;

    // name of the user who requested report generation
    private String currentUserName;

    private EventType eventType;

    public TournamentReportsProcessingEvent(long tournamentProcessingRequestId, String currentUserName, EventType eventType) {
        this.tournamentProcessingRequestId = tournamentProcessingRequestId;
        this.currentUserName = currentUserName;
        this.eventType = eventType;
    }

    public long getTournamentProcessingRequestId() {
        return tournamentProcessingRequestId;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setDetailId(long detailId) {
        this.detailId = detailId;
    }

    public long getDetailId() {
        return detailId;
    }
}
