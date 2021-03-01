package com.auroratms.tournamentevententry.policy;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.profile.UserProfile;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PolicyApplicator {

    // policies that rely on cross checking - e.g. time conflicts, max events per day etc.
    private List<IEventPolicy> crossCheckingPolicies = new ArrayList<>();

    // policies that can check entry status without cross checking
    private List<IEventPolicy> individualPolicies = new ArrayList<>();

    public void configurePolicies(List<TournamentEventEntry> eventEntries,
                                  List<TournamentEventEntity> events,
                                  UserProfile userProfile, int eligibilityRating,
                                  Date tournamentStartDate) {
        addPolicy(new GenderRestrictedEventPolicy(userProfile.getGender()));
        addPolicy(new AgeRestrictionEventPolicy(tournamentStartDate, userProfile.getDateOfBirth()));
        addPolicy(new RatingRestrictionEventPolicy(eligibilityRating));
        addPolicy(new FullEventPolicy());
        addPolicy(new SchedulingConflictEventPolicy(eventEntries, events));

//        crossCheckingPolicies.add(new SchedulingConflictEventPolicy(eventEntries, events));
    }

    public void applyPolicies(List<TournamentEventEntryInfo> infos) {

        for (TournamentEventEntryInfo info : infos) {
            EventEntryStatus currentStatus = info.getEventEntry().getStatus();
            if (currentStatus.equals(EventEntryStatus.NOT_ENTERED)) {
                for (IEventPolicy policy : individualPolicies) {
                    if (policy.isEntryDenied(info.getEvent())) {
                        info.getEventEntry().setStatus(policy.getStatus());
                        break;
                    }
                }
            }
        }

//        for (TournamentEventEntryInfo info : infos) {
//            for (IEventPolicy policy : crossCheckingPolicies) {
//                if (policy.isEntryDenied(info.getEvent())) {
//                    EventEntryStatus status = policy.getStatus();
//                    info.getEventEntry().setStatus(status);
//                    break;
//                }
//            }
//        }
    }

    public void addPolicy(IEventPolicy eventPolicy) {
        individualPolicies.add(eventPolicy);
    }
}
