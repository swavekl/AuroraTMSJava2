package com.auroratms.email;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class EmailGenerationService {

    @Autowired
    private TournamentEntryService entryService;

    @Autowired
    private UserProfileService userProfileService;

    public List<String> listEmailsForTournament(long tournamentId) {
        List<TournamentEntry> tournamentEntries = entryService.listForTournament(tournamentId);
        List<String> playerProfileIds = new ArrayList<>(tournamentEntries.size());
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            String profileId = tournamentEntry.getProfileId();
            playerProfileIds.add(profileId);
        }

        List<String> emailAddresses = new ArrayList<>(playerProfileIds.size());
        Collection<UserProfile> userProfiles = this.userProfileService.listByProfileIds(playerProfileIds);
        for (UserProfile userProfile : userProfiles) {
            emailAddresses.add(userProfile.getEmail());
        }

        return emailAddresses;
    }

}
