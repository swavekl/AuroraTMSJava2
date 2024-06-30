package com.auroratms.tournamentevententry.notification;

import com.auroratms.account.AccountEntity;
import com.auroratms.account.AccountService;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.paymentrefund.CartSession;
import com.auroratms.paymentrefund.CartSessionService;
import com.auroratms.paymentrefund.PaymentRefundFor;
import com.auroratms.paymentrefund.PaymentRefundService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamententry.notification.EventEntryInfo;
import com.auroratms.tournamententry.notification.TournamentEventListener;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.tournamentevententry.notification.event.EventEntryChangeEvent;
import com.auroratms.utils.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.mail.MessagingException;
import java.util.*;

/**
 * Listener for handling tournament event withdrawal (manual or as a result of cleanup).
 * Moves the next person on the waiting list to the spot which opened up and informs them
 * to pay by email.
 */
@Component
@Transactional
@Slf4j
@RequiredArgsConstructor  // instead of autowired
public class EventEntryChangeListener {

    private final TournamentEventEntityService tournamentEventEntityService;

    private final TournamentEventEntryService tournamentEventEntryService;

    private final TournamentEntryService tournamentEntryService;

    private final TournamentService tournamentService;

    private final CartSessionService cartSessionService;

    private final EmailService emailService;

    private final UserProfileService userProfileService;

    private final AccountService accountService;

    private final PaymentRefundService paymentRefundService;

    @Value("${client.host.url}")
    private String clientHostUrl;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEventEntryChangedEvent(EventEntryChangeEvent event) {
        // run this task as system principal, so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            protected void taskBody() {
                handleEventEntryChangedEventInternal(event);
            }
        };
        task.execute();
    }

    /**
     *
     * @param event
     */
    private void handleEventEntryChangedEventInternal(EventEntryChangeEvent event) {
        TournamentEvent tournamentEvent = tournamentEventEntityService.get(event.getEventId());
        log.info("Got tournament entry event of type " + event.getChangeType() + " for event " + tournamentEvent.getName() + " entry status " + event.getStatus());

        // check if anyone is on the waiting list in this event
        List<TournamentEventEntry> waitingListEntries = tournamentEventEntryService.listWaitingListEntriesForEvent(event.getEventId());
        log.info("There are " + waitingListEntries.size() + " players on the waiting list for event id (" + event.getEventId() + ") " + tournamentEvent.getName());
        if (!waitingListEntries.isEmpty()) {
            // check if there is room in this event
            long confirmedEntriesCount = tournamentEventEntryService.getCountValidEntriesInEvent(event.getEventId());
            log.info(confirmedEntriesCount + " spots are entered, pending confirmation or deletion");
            int maxEntries = tournamentEvent.getMaxEntries();
            long availableEventEntries = maxEntries - confirmedEntriesCount;
            log.info(availableEventEntries + " spots are available for people on the waiting list for event " + tournamentEvent.getName());
            // the first entry is the one we need
            for (int i = 0; i < availableEventEntries && i < waitingListEntries.size(); i++) {
                TournamentEventEntry waitingListEntry = waitingListEntries.get(i);
                // start a 'cart session' which will last 4 hours
                // this will give the player time to confirm entry by payment
                // if they don't confirm in 2 hours a cleanup job will
                // throw them out of the event
                Date futureCartSessionStartDate = DateUtils.addHours(new Date(), 4);
                CartSession cartSession = cartSessionService.startSession(PaymentRefundFor.TOURNAMENT_ENTRY, waitingListEntry.getTournamentEntryFk(), futureCartSessionStartDate);
                waitingListEntry.setCartSessionId(cartSession.getSessionUUID());
                waitingListEntry.setStatus(EventEntryStatus.PENDING_CONFIRMATION);

                tournamentEventEntryService.update(waitingListEntry);

                String playerFullName = sendEmail (waitingListEntry, futureCartSessionStartDate);
                log.info(String.format("Moved player %s from waiting list to event entry pending payment", playerFullName));
            }
        }
    }

    private String sendEmail(TournamentEventEntry tournamentEventEntry, Date futureCartSessionStartDate) {
        String playerFullName = null;
        try {
            // get personal information of a person who was added to the event
            TournamentEntry tournamentEntry = tournamentEntryService.get(tournamentEventEntry.getTournamentEntryFk());
            String profileId = tournamentEntry.getProfileId();
            UserProfile userProfile = userProfileService.getProfile(profileId);
            String playerFirstName = userProfile.getFirstName();
            String playerLastName = userProfile.getLastName();
            String playerEmail = userProfile.getEmail();
            playerFullName = playerLastName + ", " + playerFirstName;

            // get tournament director for this tournament
            long tournamentFk = tournamentEntry.getTournamentFk();
            Tournament tournament = tournamentService.getByKey(tournamentFk);
            String tournamentName = tournament.getName();
            String contactEmail = tournament.getEmail();
            String contactName = tournament.getContactName();
            String contactPhone = tournament.getPhone();

            // get event name
            TournamentEvent tournamentEvent = tournamentEventEntityService.get(tournamentEventEntry.getTournamentEventFk());
            String eventName = tournamentEvent.getName();
            double feeAdult = tournamentEvent.getFeeAdult();
            double feeJunior = tournamentEvent.getFeeJunior();
            String eventDateAndTime = new EventEntryInfo(tournament.getStartDate(),tournamentEvent).getEventDayAndTime();

            // send email to player and cc TD
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("playerFirstName", playerFirstName);
            templateModel.put("playerLastName", playerLastName);
            templateModel.put("tournamentName", tournamentName);
            templateModel.put("tournamentDirectorName", contactName);
            templateModel.put("tournamentDirectorPhone", contactPhone);
            templateModel.put("tournamentDirectorEmail", contactEmail);

            templateModel.put("eventName", eventName);
            templateModel.put("eventPrice", feeAdult);
            templateModel.put("eventPriceJunior", feeJunior);
            templateModel.put("eventDateAndTime", eventDateAndTime);

            String currency = this.getTournamentCurrency(tournamentFk);
            templateModel.put("currency", currency);

            templateModel.put("cartSessionExpiration", futureCartSessionStartDate);

            // get other events
            addEventsInformation(templateModel, tournament, tournamentEventEntry.getTournamentEntryFk());

            String eventEntryUrl = String.format("%s/ui/entries/entrywizard/%d/edit/%d",
                    this.clientHostUrl, tournamentFk, tournamentEntry.getId());
            templateModel.put("eventEntryUrl", eventEntryUrl);

            String emailSubject = String.format("%s - you were added to %s event", tournamentName, eventName);

            emailService.sendMessageUsingThymeleafTemplate(playerEmail, contactEmail, emailSubject,
                    "tournament-entry/tournament-entry-waiting-to-pending.html", templateModel);
        } catch (MessagingException e) {
            log.error("Unable to send email message about adding player from waiting list to " + playerFullName);
        }
        return playerFullName;
    }

    private String getTournamentCurrency (long tournamentId) {
        String currency = "";
        String tournamentOwnerLoginId = tournamentService.getTournamentOwner(tournamentId);
        if (tournamentOwnerLoginId != null) {
            String userProfileId = this.userProfileService.getProfileByLoginId(tournamentOwnerLoginId);
            if (userProfileId != null) {
                AccountEntity accountEntity = this.accountService.findById(userProfileId);
                currency = this.paymentRefundService.getAccountCurrency(accountEntity.getAccountId());
                currency = (currency != null) ? currency.toUpperCase() : currency;
            }
        }
        return currency;
    }

    /**
     * Adds events (confirmed and waited on) to the context
     * @param templateModel
     * @param tournament
     * @param tournamentEntryId
     */
    private int addEventsInformation(Map<String, Object> templateModel, Tournament tournament, long tournamentEntryId) {
        PageRequest pageRequest = PageRequest.of(0, 200);
        Collection<TournamentEvent> eventEntityCollection = tournamentEventEntityService.list(tournament.getId(), pageRequest);
        Date tournamentStartDate = tournament.getStartDate();

        // get this player's entries in this tournament
        List<TournamentEventEntry> eventEntries = tournamentEventEntryService.getEntries(tournamentEntryId);
        List<EventEntryInfo> enteredEvents = new ArrayList<>();

        for (TournamentEventEntry eventEntry : eventEntries) {
            if (Objects.requireNonNull(eventEntry.getStatus()) == EventEntryStatus.ENTERED) {
                TournamentEvent tournamentEvent = getEvent(eventEntry.getTournamentEventFk(), eventEntityCollection);
                if (tournamentEvent != null) {
                    enteredEvents.add(new EventEntryInfo(tournamentStartDate, tournamentEvent));
                }
            }
        }

        templateModel.put("enteredEvents", enteredEvents);
        return enteredEvents.size();
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
