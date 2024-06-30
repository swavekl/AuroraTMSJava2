package com.auroratms.tournamententry.notification;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamententry.notification.event.TournamentEntryConfirmedEvent;
import com.auroratms.tournamententry.notification.event.TournamentEntryStartedEvent;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.utils.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.mail.MessagingException;
import java.util.*;

/**
 * Listener for events about registration, updates and withdrawals
 */
@Component
public class TournamentEventListener {

    private static final Logger logger = LoggerFactory.getLogger(TournamentEventListener.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentEventEntityService tournamentEventService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    /**
     * Send entry started email to TD
     * @param event
     */
    @Async
    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleEvent(TournamentEntryStartedEvent event) {
        // run this task as system principal so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                sendEntryStartedEmail(event);
            }
        };
        task.execute();
    }

    /**
     * Sends email in response to completed payment
     * @param event
     */
    @Async
    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleEntryConfirmedEvent(TournamentEntryConfirmedEvent event) {
        // run this task as system principal so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                sendEntryCompletedEmail(event);
            }
        };
        task.execute();
    }

    /**
     * send email
     *
     * @param event
     */
    private void sendEntryStartedEmail(TournamentEntryStartedEvent event) {
        try {
            // collect information in
            Map<String, Object> templateModel = new HashMap<>();
            TournamentEntry tournamentEntry = addTournamentEntryInformation(templateModel, event.getTournamentEntryFk());

            Tournament tournament = addTournamentInformation(templateModel, tournamentEntry.getTournamentFk());
            String tournamentDirectorEmail = tournament.getEmail();

            UserProfile userProfile = addPlayerInformation(templateModel, tournamentEntry.getProfileId());

            // send email
            emailService.sendMessageUsingThymeleafTemplate(tournamentDirectorEmail, null,
                    "Player Entered Tournament",
                    "tournament-entry-started.html",
                    templateModel);
            logger.info("Player " + userProfile.getLastName() + ", " + userProfile.getFirstName() + " entered tournament " + tournament.getName() + ". Tournament entry id " + event.getTournamentEntryFk());
        } catch (MessagingException e) {
            logger.error("Unable to send email ", e);
        }
    }

    /**
     *
     * @param event
     */
    private void sendEntryCompletedEmail(TournamentEntryConfirmedEvent event) {
        try {
            logger.info("Sending entry completed email for tournament entry " + event.getTournamentEntryId());
            Map<String, Object> templateModel = new HashMap<>();
            TournamentEntry tournamentEntry = addTournamentEntryInformation(templateModel, event.getTournamentEntryId());

            Tournament tournament = addTournamentInformation(templateModel, tournamentEntry.getTournamentFk());

            int countOfEvents = addEventsInformation(templateModel, tournament, event.getTournamentEntryId());

            UserProfile userProfile = addPlayerInformation(templateModel, tournamentEntry.getProfileId());
            // is player in any events
            if (!event.isWithdrawing()) {
                emailService.sendMessageUsingThymeleafTemplate(userProfile.getEmail(), tournament.getEmail(),
                        "Tournament Registration Confirmation",
                        "tournament-entry-completed.html",
                        templateModel);
                logger.info("Player " + userProfile.getLastName() + ", " + userProfile.getFirstName() + " confirmed entry into tournament " + tournament.getName() );
            } else {
                // no, so it is a withdrawal
                emailService.sendMessageUsingThymeleafTemplate(userProfile.getEmail(), tournament.getEmail(),
                        "Tournament Withdrawal Confirmation",
                        "tournament-withdrawal-completed.html",
                        templateModel);
                logger.info("Player " + userProfile.getLastName() + ", " + userProfile.getFirstName() + " withdrew from tournament " + tournament.getName() );
            }
        } catch (MessagingException e) {
            logger.error("Unable to send email ", e);
        }
    }

    /**
     * Adds tournament information to context
     * @param templateModel
     * @param tournamentFk
     * @return
     */
    private Tournament addTournamentInformation(Map<String, Object> templateModel, long tournamentFk) {
        Tournament tournament = tournamentService.getByKey(tournamentFk);
        templateModel.put("tournamentName", tournament.getName());
        templateModel.put("tournamentDirectorName", tournament.getContactName());
        templateModel.put("tournamentDirectorEmail", tournament.getEmail());
        templateModel.put("tournamentDirectorPhone", tournament.getPhone());
        templateModel.put("tournamentStartDate", tournament.getStartDate());
        if (!tournament.getEndDate().equals(tournament.getStartDate())) {
            templateModel.put("tournamentEndDate", tournament.getEndDate());
        } else {
            templateModel.put("tournamentEndDate", null);
        }
        return tournament;
    }

    /**
     * Adds entry information to context
     * @param templateModel
     * @param tournamentEntryId
     * @return
     */
    private TournamentEntry addTournamentEntryInformation(Map<String, Object> templateModel, long tournamentEntryId) {
        TournamentEntry tournamentEntry = tournamentEntryService.get(tournamentEntryId);
        templateModel.put("eligibilityRating", tournamentEntry.getEligibilityRating());
        return tournamentEntry;
    }

    /**
     * Adds player information to context
     * @param templateModel
     * @param profileId
     * @return
     */
    private UserProfile addPlayerInformation(Map<String, Object> templateModel, String profileId) {
        UserProfile userProfile = userProfileService.getProfile(profileId);
        templateModel.put("playerFirstName", userProfile.getFirstName());
        templateModel.put("playerLastName", userProfile.getLastName());
        templateModel.put("city", userProfile.getCity());
        templateModel.put("state", userProfile.getState());
        templateModel.put("email", userProfile.getEmail());
        templateModel.put("phone", userProfile.getMobilePhone());
        return userProfile;
    }

    /**
     * Adds events (confirmed and waited on) to the context
     * @param templateModel
     * @param tournament
     * @param tournamentEntryId
     */
    private int addEventsInformation(Map<String, Object> templateModel, Tournament tournament, long tournamentEntryId) {
        PageRequest pageRequest = PageRequest.of(0, 200);
        Collection<TournamentEvent> eventEntityCollection = tournamentEventService.list(tournament.getId(), pageRequest);
        Date tournamentStartDate = tournament.getStartDate();

        // get this player's entries in this tournament
        List<TournamentEventEntry> eventEntries = tournamentEventEntryService.getEntries(tournamentEntryId);
        List<EventEntryInfo> enteredEvents = new ArrayList<>();
        List<EventEntryInfo> waitListEvents = new ArrayList<>();

        for (TournamentEventEntry eventEntry : eventEntries) {
            switch (eventEntry.getStatus()) {
                case ENTERED:
                case PENDING_CONFIRMATION:
                    TournamentEvent tournamentEvent = getEvent(eventEntry.getTournamentEventFk(), eventEntityCollection);
                    if (tournamentEvent != null) {
                        enteredEvents.add(new EventEntryInfo(tournamentStartDate, tournamentEvent));
                    }
                    break;
                case PENDING_WAITING_LIST:
                case ENTERED_WAITING_LIST:
                    TournamentEvent tournamentEvent2 = getEvent(eventEntry.getTournamentEventFk(), eventEntityCollection);
                    if (tournamentEvent2 != null) {
                        waitListEvents.add(new EventEntryInfo(tournamentStartDate, tournamentEvent2));
                    }
                    break;
                default:
                    break;
            }
        }

        templateModel.put("enteredEvents", enteredEvents);
        templateModel.put("waitListEvents", waitListEvents);
        return enteredEvents.size() + waitListEvents.size();
    }

    private TournamentEvent getEvent(long tournamentEventFk, Collection<TournamentEvent> eventEntityCollection) {
        for (TournamentEvent tournamentEvent : eventEntityCollection) {
            if (tournamentEvent.getId().equals(tournamentEventFk)) {
                return tournamentEvent;
            }
        }
        return null;
    }

}
