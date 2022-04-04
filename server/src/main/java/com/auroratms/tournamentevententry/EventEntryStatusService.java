package com.auroratms.tournamentevententry;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.paymentrefund.CartSessionService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamententry.notification.TournamentEventPublisher;
import com.auroratms.tournamentevententry.doubles.notification.DoublesEventPublisher;
import com.auroratms.tournamentevententry.doubles.notification.event.MakeBreakDoublesPairsEvent;
import com.auroratms.tournamentevententry.policy.PolicyApplicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class EventEntryStatusService {

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentEventEntityService tournamentEventService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private TournamentEventPublisher eventPublisher;

    @Autowired
    private DoublesEventPublisher doublesEventPublisher;

    @Autowired
    private CartSessionService cartSessionService;

    /**
     * Gets information about event entries with current status, availability and next command
     * @param tournamentEntryId entry id
     * @return list of event entry infos with that information
     */
    @Transactional(readOnly = true)
    List<TournamentEventEntryInfo> getEntriesWithStatus(long tournamentEntryId) {
        // get a list of event entries for this player
        List<TournamentEventEntryInfo> eventEntryInfos = Collections.EMPTY_LIST;
        TournamentEntry tournamentEntry = tournamentEntryService.get(tournamentEntryId);
        if (tournamentEntry != null) {
            long tournamentId = tournamentEntry.getTournamentFk();
            // get all events
            PageRequest pageRequest = PageRequest.of(0, 200);
            Collection<TournamentEvent> eventEntityCollection = tournamentEventService.list(tournamentId, pageRequest);

            List<TournamentEventEntry> eventEntries = tournamentEventEntryService.getEntries(tournamentEntryId);
            eventEntryInfos = new ArrayList<>(eventEntityCollection.size());
            // put entered events in the map for quick lookup
            // and in the list of infos to return
            Map<Long, TournamentEventEntry> eventsToEventEntriesMap = new HashMap<>();
            for (TournamentEventEntry eventEntry : eventEntries) {
                eventsToEventEntriesMap.put(eventEntry.getTournamentEventFk(), eventEntry);

                // put in the list
                TournamentEventEntryInfo eventEntryInfo = new TournamentEventEntryInfo();
                eventEntryInfo.setEventEntryFk(eventEntry.getId());
                eventEntryInfo.setEventFk(eventEntry.getTournamentEventFk());
                // get current entry status & price
                eventEntryInfo.setStatus(eventEntry.getStatus());
                switch (eventEntry.getStatus()) {
                    case ENTERED:
                    case PENDING_CONFIRMATION:
                    case PENDING_WAITING_LIST:
                    case ENTERED_WAITING_LIST:
                        eventEntryInfo.setAvailabilityStatus(AvailabilityStatus.ALREADY_ENTERED);
                        break;
                    case PENDING_DELETION:
                    case RESERVED_WAITING_LIST:
                        eventEntryInfo.setAvailabilityStatus(AvailabilityStatus.AVAILABLE_FOR_ENTRY);
                        break;
                }
                eventEntryInfo.setPrice(eventEntry.getPrice());
                eventEntryInfo.setDoublesPartnerProfileId(eventEntry.getDoublesPartnerProfileId());
                eventEntryInfos.add(eventEntryInfo);
            }

            // now create event entry infos for events that were not entered
            // find entry into this even if it exists
            for (TournamentEvent event : eventEntityCollection) {
                // if there is no entry for this event, make info
                if (!eventsToEventEntriesMap.containsKey(event.getId())) {
                    TournamentEventEntryInfo notEnteredEventEntryInfo = new TournamentEventEntryInfo();
                    notEnteredEventEntryInfo.setEventFk(event.getId());
                    eventEntryInfos.add(notEnteredEventEntryInfo);
                }
            }

            // gather data needed to evaluate restrictions, age, rating etc.
            String profileId = tournamentEntry.getProfileId();
            int eligibilityRating = tournamentEntry.getEligibilityRating();
            UserProfile userProfile = userProfileService.getProfile(profileId);
            Tournament tournament = tournamentService.getByKey(tournamentId);
            Date tournamentStartDate = tournament.getStartDate();

            if (userProfile != null) {
                // now determine availability status of the events that are not entered yet
                PolicyApplicator policyApplicator = new PolicyApplicator();
                List<TournamentEvent> eventEntityList = new ArrayList<>(eventEntityCollection);
                policyApplicator.configurePolicies(eventEntries, eventEntityList, userProfile, eligibilityRating, tournamentStartDate);
                eventEntryInfos = policyApplicator.evaluateRestrictions(eventEntityList, eventEntryInfos);
            }

            // set doubles partner information
            for (TournamentEventEntryInfo eventEntryInfo : eventEntryInfos) {
                if (eventEntryInfo.getDoublesPartnerProfileId() != null) {
                    UserProfile partnerUserProfile = userProfileService.getProfile(eventEntryInfo.getDoublesPartnerProfileId());
                    String fullName = partnerUserProfile.getLastName() + ", " + partnerUserProfile.getFirstName();
                    eventEntryInfo.setDoublesPartnerName(fullName);
                }
            }
        }

        // given entry status and availability status, determine what user can do (enter, drop, etc.)
        for (TournamentEventEntryInfo eventEntryInfo : eventEntryInfos) {
            determineNextCommand(eventEntryInfo);
        }

        return eventEntryInfos;
    }

    /**
     * Determines the next command for the event entry info based on current satus
     * @param eventEntryInfo event entry info to evaluate
     */
    private void determineNextCommand(TournamentEventEntryInfo eventEntryInfo) {
        EventEntryStatus eventEntryStatus = eventEntryInfo.getStatus();
        AvailabilityStatus availabilityStatus = eventEntryInfo.getAvailabilityStatus();
        EventEntryCommand command = EventEntryCommand.NO_COMMAND;
        switch (eventEntryStatus) {
            case NOT_ENTERED:
                switch (availabilityStatus) {
                    case AVAILABLE_FOR_ENTRY:
                        command = EventEntryCommand.ENTER;
                        break;
                    case EVENT_FULL:
                        command = EventEntryCommand.ENTER_WAITING_LIST;
                        break;
                    default:
                        break;
                }
                break;

            case ENTERED:    // drop out what was just chosen - go back to available
            case PENDING_CONFIRMATION:
                command = EventEntryCommand.DROP;
                break;

            case PENDING_DELETION:
                command = EventEntryCommand.REVERT_DROP;  // reeneter
                break;

            case PENDING_WAITING_LIST:
            case ENTERED_WAITING_LIST:
                // this is when they no longer want to be wait or
                // are not interested in this event after being offered entry into it
                command = EventEntryCommand.DROP_WAITING_LIST;
                break;

            default:
                break;
        }
        eventEntryInfo.setEventEntryCommand(command);
//        System.out.println("--------------------------------------");
//        System.out.print("eventFk = " + eventEntryInfo.getEventFk());
//        System.out.print(" eventEntryStatus = " + eventEntryStatus);
//        System.out.print(" availabilityStatus = " + availabilityStatus);
//        System.out.println(" new command = " + command);
    }

    /**
     * Computes the next status and command of the event entry
     *
     * @param currentStatus current status of event entry
     * @param command command to transition status
     * @return next status
     */
    private EventEntryStatus determineNextStatus(EventEntryStatus currentStatus, EventEntryCommand command) {
        EventEntryStatus nextStatus = currentStatus;
        switch (currentStatus) {
            case NOT_ENTERED:
                switch (command) {
                    case ENTER:
                        nextStatus = EventEntryStatus.PENDING_CONFIRMATION;
                        break;
                    case ENTER_WAITING_LIST:
                        nextStatus = EventEntryStatus.PENDING_WAITING_LIST;
                        break;
                    default:
                        break;
                }
                break;

            case PENDING_CONFIRMATION:
                switch (command) {
                    case CONFIRM:
                        nextStatus = EventEntryStatus.ENTERED;
                        break;
                    case DROP:
                        // drop out what was just chosen - go back to not entered
                        nextStatus = EventEntryStatus.NOT_ENTERED;
                        break;
                    default:
                        break;
                }
                break;

            case ENTERED:
                switch (command) {
                    case DROP:
                        nextStatus = EventEntryStatus.PENDING_DELETION;
                        break;
                    case CONFIRM:
                        nextStatus = EventEntryStatus.ENTERED;
                        break;
                    default:
                        break;
                }
                break;

            case PENDING_WAITING_LIST:
                switch (command) {
                    case CONFIRM:
                        nextStatus = EventEntryStatus.ENTERED_WAITING_LIST;
                        break;
                    case DROP_WAITING_LIST:
                        nextStatus = EventEntryStatus.NOT_ENTERED;
                        break;
                    default:
                        break;
                }
                break;

            case PENDING_DELETION:
                switch (command) {
                    case CONFIRM:
                        nextStatus = EventEntryStatus.NOT_ENTERED;
                        break;
                    case REVERT_DROP:
                        nextStatus = EventEntryStatus.ENTERED;
                        break;
                    default:
                        break;
                }
                break;

            case ENTERED_WAITING_LIST:
                switch (command) {
                    case DROP_WAITING_LIST:
                        nextStatus = EventEntryStatus.NOT_ENTERED;
                        break;
                    case ENTER:
                        nextStatus = EventEntryStatus.PENDING_CONFIRMATION;
                        break;
                    default:
                        break;
                }
                break;

            default:
                break;
        }
//        System.out.println("eventFk              = " + eventEntryInfo.getEventFk() + " nextStatus = " + nextStatus);

        return nextStatus;
    }

    /**
     * Updates count of entries in this event
     *
     * @param eventId
     */
    private long updateNumEntriesInEvent(long eventId) {
        TournamentEvent tournamentEvent = tournamentEventService.get(eventId);
        long count = tournamentEventEntryService.getCountValidEntriesInEvent(eventId);
        tournamentEvent.setNumEntries((int) count);
        tournamentEventService.update(tournamentEvent);
//        System.out.println("eventId = " + eventId + ", count = " + count);
        return count;
    }

    /**
     * Creates, deletes or changes status of the event entry
     *
     * @param tournamentEntryId
     * @param eventEntryInfo
     */
    @Transactional
    public void changeStatus(Long tournamentEntryId, TournamentEventEntryInfo eventEntryInfo) {
        EventEntryCommand eventEntryCommand = eventEntryInfo.getEventEntryCommand();
        EventEntryStatus currentStatus = (eventEntryInfo.getEventEntryFk() != null) ? eventEntryInfo.getStatus() : EventEntryStatus.NOT_ENTERED;
        EventEntryStatus nextEventEntryStatus = determineNextStatus(currentStatus, eventEntryCommand);
        TournamentEventEntry eventEntry = null;
        switch (eventEntryCommand) {
            case ENTER:
            case ENTER_WAITING_LIST:
                TournamentEvent event = tournamentEventService.get(eventEntryInfo.getEventFk());
                // if event filled up while user was waiting on the screen then put him on the waiting list
                nextEventEntryStatus = (event.getNumEntries() < event.getMaxEntries()) ? nextEventEntryStatus : (EventEntryStatus.PENDING_WAITING_LIST);
                eventEntry = new TournamentEventEntry();
                eventEntry.setDateEntered(new Date());
                eventEntry.setTournamentFk(event.getTournamentFk());
                eventEntry.setTournamentEventFk(eventEntryInfo.getEventFk());
                eventEntry.setTournamentEntryFk(tournamentEntryId);
                eventEntry.setStatus(nextEventEntryStatus);
                eventEntry.setCartSessionId(eventEntryInfo.getCartSessionId());
                tournamentEventEntryService.create(eventEntry);
                break;

            case DROP:
            case DROP_WAITING_LIST:
                if (nextEventEntryStatus == EventEntryStatus.NOT_ENTERED) {
                    tournamentEventEntryService.delete(eventEntryInfo.getEventEntryFk());
                } else {
                    // to pending deletion status
                    eventEntry = tournamentEventEntryService.get(eventEntryInfo.getEventEntryFk());
                    if (eventEntry != null) {
                        eventEntry.setStatus(nextEventEntryStatus);
                        eventEntry.setCartSessionId(eventEntryInfo.getCartSessionId());
                        tournamentEventEntryService.update(eventEntry);
                    }
                }
                break;

            case REVERT_DROP:
            case CONFIRM:
                eventEntry = tournamentEventEntryService.get(eventEntryInfo.getEventEntryFk());
                if (eventEntry != null) {
                    eventEntry.setStatus(nextEventEntryStatus);
                    eventEntry.setCartSessionId(eventEntryInfo.getCartSessionId());
                    tournamentEventEntryService.update(eventEntry);
                }
                break;

            case UPDATE_DOUBLES:
                eventEntry = tournamentEventEntryService.get(eventEntryInfo.getEventEntryFk());
                if (eventEntry != null) {
                    eventEntry.setStatus(nextEventEntryStatus);
                    eventEntry.setCartSessionId(eventEntryInfo.getCartSessionId());
                    eventEntry.setDoublesPartnerProfileId(eventEntryInfo.getDoublesPartnerProfileId());
                    tournamentEventEntryService.update(eventEntry);
                }
                break;

            default:
                break;
        }

        // update count of entries in this event now that status has changed
        updateNumEntriesInEvent(eventEntryInfo.getEventFk());

        // each time event is entered or dropped update last time cart session was active
        // so we can determine which sessions were abandoned
        cartSessionService.updateSession(eventEntryInfo.getCartSessionId());
    }

    /**
     *
     * @param tournamentEntryId
     * @param cartSessionId
     */
    @Transactional
    public void confirmAll(Long tournamentEntryId, String cartSessionId) {
        // confirm entries
        MakeBreakDoublesPairsEvent makeBreakDoublesPairsEvent = confirmAllInternal(tournamentEntryId);

        // if there were any entries or withdrawals from doubles events process them
        if (makeBreakDoublesPairsEvent.getEntryWithdrawalList().size() > 0) {
            doublesEventPublisher.publishMakeBreakPairsEvent(makeBreakDoublesPairsEvent);
        }

        // send email to TD and player confirming entry
        eventPublisher.publishRegistrationCompleteEvent(tournamentEntryId);

        // each time event is entered or dropped update last time cart session was active
        // so we can determine which sessions were abandoned
        cartSessionService.finishSession(cartSessionId);
    }

    /**
     *
     * @param tournamentEntryId
     * @return
     */
    private MakeBreakDoublesPairsEvent confirmAllInternal(Long tournamentEntryId) {
        List<TournamentEventEntry> allEntries = tournamentEventEntryService.getEntries(tournamentEntryId);

        // get a list of doubles event ids in this tournament
        List<Long> doublesEventIds = new ArrayList<>();
        if (allEntries.size() > 0) {
            TournamentEventEntry eventEntry = allEntries.get(0);
            List<TournamentEvent> doublesEvents = tournamentEventService.listDoublesEvents(eventEntry.getTournamentFk());
            for (TournamentEvent doublesEvent : doublesEvents) {
                doublesEventIds.add(doublesEvent.getId());
            }
        }

        // record which doubles events were entered or withdrawn so we can make or break existing doubles pairs
        MakeBreakDoublesPairsEvent makeBreakDoublesPairsEvent = new MakeBreakDoublesPairsEvent(tournamentEntryId);
        for (TournamentEventEntry eventEntry : allEntries) {
            EventEntryStatus nextStatus = determineNextStatus(eventEntry.getStatus(), EventEntryCommand.CONFIRM);
            if (nextStatus == EventEntryStatus.NOT_ENTERED) {
                // if this is a doubles event record that we are deleting it
                if (doublesEventIds.contains(eventEntry.getTournamentEventFk())) {
                    if (eventEntry.getDoublesPartnerProfileId() != null) {
                        makeBreakDoublesPairsEvent.addDeletedDoublesEvent(
                                eventEntry.getTournamentEventFk(), eventEntry.getId(), eventEntry.getDoublesPartnerProfileId());
                    }
                }
                tournamentEventEntryService.delete(eventEntry.getId());
            } else {
                eventEntry.setStatus(nextStatus);
                // session is finished
                eventEntry.setCartSessionId(null);
                tournamentEventEntryService.update(eventEntry);
                // if this is doubles event record that we are in this event
                if (doublesEventIds.contains(eventEntry.getTournamentEventFk())) {
                    if (eventEntry.getDoublesPartnerProfileId() != null) {
                        makeBreakDoublesPairsEvent.addEnteredDoublesEvent(
                                eventEntry.getTournamentEventFk(), eventEntry.getId(), eventEntry.getDoublesPartnerProfileId());
                    }
                }
            }
            // update count of entries in this event now that status has changed
            updateNumEntriesInEvent(eventEntry.getTournamentEventFk());
        }
        return makeBreakDoublesPairsEvent;
    }
}
