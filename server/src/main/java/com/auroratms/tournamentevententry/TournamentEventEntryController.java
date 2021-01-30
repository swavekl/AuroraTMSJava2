package com.auroratms.tournamentevententry;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class TournamentEventEntryController {

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentEventEntityService tournamentEventService;

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping("/tournament/{tournamentId}/tournamententry/{tournamentEntryId}/tournamentevententry")
    public List<TournamentEventEntry> getAllEntries (@PathVariable Long tournamentId,
                                                     @PathVariable Long tournamentEntryId) {
        // get a list of event entries for this player
        List<TournamentEventEntry> existingEntries = tournamentEventEntryService.getEntries(tournamentEntryId);

        TournamentEntry tournamentEntry = tournamentEntryService.get(tournamentEntryId);
        int eligibilityRating = tournamentEntry.getEligibilityRating();
        String userProfileId = tournamentEntry.getProfileId();

        UserProfile userProfile = userProfileService.getProfile(userProfileId);
        String gender = userProfile.getGender();
        Date dateOfBirth = userProfile.getDateOfBirth();

        // get a list of all event entries and calculate their status
        Collection<TournamentEventEntity> allEvents = tournamentEventService.list(tournamentId, Pageable.unpaged());

        // determine eligibility of this player for all events
        List<TournamentEventEntry> finalEntries = new ArrayList<>();
        finalEntries.addAll(existingEntries);
        for (TournamentEventEntity event : allEvents) {
            boolean alreadyEntered = false;
            for (TournamentEventEntry existingEntry : existingEntries) {
                if (event.getId() == existingEntry.getTournamentEventFk()) {
                    alreadyEntered = true;
                    break;
                }
            }
            if (!alreadyEntered) {

            }
        }

        return finalEntries;
    }



}
