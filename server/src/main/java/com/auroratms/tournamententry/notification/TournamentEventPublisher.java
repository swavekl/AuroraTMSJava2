package com.auroratms.tournamententry.notification;

import com.auroratms.tournamententry.notification.event.TeamMembersTournamentEntriesChangedEvent;
import com.auroratms.tournamententry.notification.event.TournamentEntryConfirmedEvent;
import com.auroratms.tournamententry.notification.event.TournamentEntryStartedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publisher of events about tournament registration, payments etc.
 */
@Component
public class TournamentEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publishes event when user enters tournament
     * @param tournamentEntryId id of the tournament entry
     */
    public void publishTournamentEnteredEvent(long tournamentEntryId) {
        TournamentEntryStartedEvent event = new TournamentEntryStartedEvent(tournamentEntryId);
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publishes event after registration is complete
     *
     * @param tournamentEntryId id of the tournament entry
     * @param withdrawing
     */
    public void publishRegistrationCompleteEvent(long tournamentEntryId, boolean withdrawing) {
        TournamentEntryConfirmedEvent event = new TournamentEntryConfirmedEvent(tournamentEntryId, withdrawing);
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Signals to team members that they were added/dropped from a team
     * @param event
     */
    public void publishTeamMemberChangedEvent(TeamMembersTournamentEntriesChangedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
