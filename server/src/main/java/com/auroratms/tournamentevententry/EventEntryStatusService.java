package com.auroratms.tournamentevententry;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
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
            Collection<TournamentEventEntity> eventEntityCollection = tournamentEventService.list(tournamentId, pageRequest);

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
                eventEntryInfos.add(eventEntryInfo);
            }

            // now create event entry infos for events that were not entered
            // find entry into this even if it exists
            for (TournamentEventEntity event : eventEntityCollection) {
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
                List<TournamentEventEntity> eventEntityList = new ArrayList<>(eventEntityCollection);
                policyApplicator.configurePolicies(eventEntries, eventEntityList, userProfile, eligibilityRating, tournamentStartDate);
                eventEntryInfos = policyApplicator.evaluateRestrictions(eventEntityList, eventEntryInfos);
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
        System.out.println("--------------------------------------");
        System.out.print("eventFk = " + eventEntryInfo.getEventFk());
        System.out.print(" eventEntryStatus = " + eventEntryStatus);
        System.out.print(" availabilityStatus = " + availabilityStatus);
        System.out.println(" new command = " + command);
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
        TournamentEventEntity tournamentEventEntity = tournamentEventService.get(eventId);
        long count = tournamentEventEntryService.getCountValidEntriesInEvent(eventId);
        tournamentEventEntity.setNumEntries((int) count);
        tournamentEventService.update(tournamentEventEntity);
        System.out.println("eventId = " + eventId + ", count = " + count);
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
                TournamentEventEntity event = tournamentEventService.get(eventEntryInfo.getEventFk());
                eventEntry = new TournamentEventEntry();
                eventEntry.setDateEntered(new Date());
                eventEntry.setTournamentFk(event.getTournamentFk());
                eventEntry.setTournamentEventFk(eventEntryInfo.getEventFk());
                eventEntry.setTournamentEntryFk(tournamentEntryId);
                eventEntry.setStatus(nextEventEntryStatus);
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
                        tournamentEventEntryService.update(eventEntry);
                    }
                }
                break;

            case REVERT_DROP:
            case CONFIRM:
                eventEntry = tournamentEventEntryService.get(eventEntryInfo.getEventEntryFk());
                if (eventEntry != null) {
                    eventEntry.setStatus(nextEventEntryStatus);
                    tournamentEventEntryService.update(eventEntry);
                }
                break;

            default:
                break;
        }

        // update count of entries in this event now that status has changed
        updateNumEntriesInEvent(eventEntryInfo.getEventFk());
    }

    /**
     *
     * @param tournamentEntryId
     */
    @Transactional
    public void confirmAll(Long tournamentEntryId) {
        List<TournamentEventEntry> allEntries = tournamentEventEntryService.getEntries(tournamentEntryId);
        for (TournamentEventEntry eventEntry : allEntries) {
            EventEntryStatus nextStatus = determineNextStatus(eventEntry.getStatus(), EventEntryCommand.CONFIRM);
            if (nextStatus == EventEntryStatus.NOT_ENTERED) {
                tournamentEventEntryService.delete(eventEntry.getId());
            } else {
                eventEntry.setStatus(nextStatus);
                tournamentEventEntryService.update(eventEntry);
            }
        }
    }
}
