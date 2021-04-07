package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.profile.UserProfile;
import com.auroratms.tournamentevententry.AvailabilityStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PolicyApplicator {

    // policies that can check entry status without cross checking
    private List<IEventPolicy> individualPolicies = new ArrayList<>();

    public void configurePolicies(List<TournamentEventEntry> eventEntries,
                                  List<TournamentEventEntity> events,
                                  UserProfile userProfile, int eligibilityRating,
                                  Date tournamentStartDate) {
        addPolicy(new GenderRestrictedEventPolicy(userProfile.getGender()));
        addPolicy(new AgeRestrictionEventPolicy(tournamentStartDate, userProfile.getDateOfBirth()));
        addPolicy(new RatingRestrictionEventPolicy(eligibilityRating));
        addPolicy(new SchedulingConflictEventPolicy(eventEntries, events));
        addPolicy(new FullEventPolicy());
    }

    public void addPolicy(IEventPolicy eventPolicy) {
        individualPolicies.add(eventPolicy);
    }

    /**
     *
     * @param eventList
     * @param eventEntryInfos
     * @return
     */
    public List<TournamentEventEntryInfo> evaluateRestrictions(List<TournamentEventEntity> eventList,
                                                               List<TournamentEventEntryInfo> eventEntryInfos) {
        for (TournamentEventEntryInfo info : eventEntryInfos) {
            // find event
            for (TournamentEventEntity event : eventList) {
                if (event.getId().equals(info.getEventFk())) {
                    // todo: set the price based on age
                    info.setPrice(event.getFeeAdult());
                    if (info.getEventEntryFk() == null) {
                        AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE_FOR_ENTRY;
                        for (IEventPolicy policy : individualPolicies) {
                            if (policy.isEntryDenied(event)) {
                                availabilityStatus = policy.getStatus();
                                break;
                            }
                        }
                        info.setAvailabilityStatus(availabilityStatus);
                    }
                }
            }
        }
        return eventEntryInfos;
    }
}
