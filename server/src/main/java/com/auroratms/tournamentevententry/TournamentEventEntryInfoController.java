package com.auroratms.tournamentevententry;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.policy.PolicyApplicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class TournamentEventEntryInfoController {

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentEventEntityService tournamentEventService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping ("/tournamententry/{tournamentEntryId}/tournamentevententryinfos")
    List<TournamentEventEntryInfo> list (@PathVariable Long tournamentEntryId) {
        // get a list of event entries for this player
        List<TournamentEventEntryInfo> eventEntryInfos = Collections.EMPTY_LIST;
        TournamentEntry tournamentEntry = tournamentEntryService.get(tournamentEntryId);
        if (tournamentEntry != null) {
            long tournamentId = tournamentEntry.getTournamentFk();
            // get all events
            PageRequest pageRequest = PageRequest.of(0, 200);
            Collection<TournamentEventEntity> eventEntityCollection = tournamentEventService.list(tournamentId, pageRequest);

            List<TournamentEventEntry> eventEntries = tournamentEventEntryService.getEntries(tournamentEntryId);
            // make intros for every one of them
            long eventNum = 0;
            eventEntryInfos = new ArrayList<>(eventEntityCollection.size());
            for (TournamentEventEntity tournamentEventEntity : eventEntityCollection) {
                TournamentEventEntryInfo eventEntryInfo = new TournamentEventEntryInfo();
                eventEntryInfo.setEvent(tournamentEventEntity);
                // make unique id for this artificial object
                long id = (tournamentEntryId * 1000) + eventNum;
                eventEntryInfo.setId(id);
                eventNum++;
                eventEntryInfos.add(eventEntryInfo);

                // find entry into this even if it exists
                boolean entered = false;
                for (TournamentEventEntry eventEntry : eventEntries) {
                    if (eventEntry.getTournamentEventFk() == tournamentEventEntity.getId()) {
                        eventEntryInfo.setEventEntry(eventEntry);
                        entered = true;
                        break;
                    }
                }
                if (!entered) {
                    TournamentEventEntry fakeEventEntry = new TournamentEventEntry();
                    //fakeEventEntry.setId();  // null id indicates this event entry is not persisted
                    fakeEventEntry.setStatus(EventEntryStatus.NOT_ENTERED);
                    fakeEventEntry.setTournamentEventFk(tournamentEventEntity.getId());
                    fakeEventEntry.setTournamentEntryFk(tournamentEntryId);
                    fakeEventEntry.setTournamentFk(tournamentId);
                    eventEntryInfo.setEventEntry(fakeEventEntry);
                }
            }
            String profileId = tournamentEntry.getProfileId();
            UserProfile userProfile = userProfileService.getProfile(profileId);
            int eligibilityRating = 1600;

            if (userProfile != null) {
                // now determine status of the rest of them
                PolicyApplicator policyApplicator = new PolicyApplicator();
                List<TournamentEventEntity> eventEntityList = new ArrayList<>(eventEntityCollection);
                policyApplicator.configurePolicies(eventEntries, eventEntityList, userProfile, eligibilityRating);
                policyApplicator.applyPolicies(eventEntryInfos);
            }
        }
        return eventEntryInfos;
    }
}
