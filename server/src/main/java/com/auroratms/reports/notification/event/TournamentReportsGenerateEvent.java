package com.auroratms.reports.notification.event;

/**
 * Event for initiating report generation
 */
public class TournamentReportsGenerateEvent {

    // request id in database
    private long tournamentProcessingRequestId;

    // name of the user who requested report generation
    private String currentUserName;

    public TournamentReportsGenerateEvent(long tournamentProcessingRequestId, String currentUserName) {
        this.tournamentProcessingRequestId = tournamentProcessingRequestId;
        this.currentUserName = currentUserName;
    }

    public long getTournamentProcessingRequestId() {
        return tournamentProcessingRequestId;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }
}
