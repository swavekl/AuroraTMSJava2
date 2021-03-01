package com.auroratms.tournamententry;

import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class TournamentEntryController {

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private UsattDataService usattDataService;

    @Autowired
    private UserProfileExtService userProfileExtService;



    @GetMapping("/tournamententries")
    public List<TournamentEntry> query(@RequestParam Long tournamentId,
                                 @RequestParam String profileId) {
        return tournamentEntryService.listForTournamentAndUser(tournamentId, profileId);
    }

    @PostMapping("/tournamententry")
    public TournamentEntry create(@RequestBody TournamentEntry tournamentEntry) {
        updateRatings(tournamentEntry);
        return tournamentEntryService.create(tournamentEntry);
    }

    @GetMapping("/tournamententry/{entryId}")
    public TournamentEntry get(@PathVariable Long entryId) {
        TournamentEntry entry = tournamentEntryService.get(entryId);
        // refresh ratings and if they changed update them
        int seedRating = entry.getSeedRating();
        int eligibilityRating = entry.getEligibilityRating();
        updateRatings(entry);
        if (entry.getSeedRating() != seedRating || entry.getEligibilityRating() != eligibilityRating) {
            tournamentEntryService.update(entry);
            entry = tournamentEntryService.get(entryId);
        }
        return entry;
    }

    /**
     * updates player's seed rating and tournament elgibility rating.
     * @param tournamentEntry
     */
    private void updateRatings(TournamentEntry tournamentEntry) {
        UserProfileExt userProfileExt = userProfileExtService.getByProfileId(tournamentEntry.getProfileId());
        if (userProfileExt != null) {
            Long membershipId = userProfileExt.getMembershipId();
            // get current player rating
            UsattPlayerRecord playerRecord = usattDataService.getPlayerByMembershipId(membershipId);
            if (playerRecord != null) {
                int tournamentRating = playerRecord.getTournamentRating();
                tournamentEntry.setSeedRating(tournamentRating);
            }

            // get eligibility rating date specified by TD - usually a week or more before the tournament
            Date today = new Date();
            Date tournamentEligibilityDate = null;
            Tournament tournament = this.tournamentService.getByKey(tournamentEntry.getTournamentFk());
            if (tournament != null && tournament.getConfiguration() != null) {
                tournamentEligibilityDate = tournament.getConfiguration().getEligibilityDate();
            }

            // determine eligibility rating on that date
            int eligibilityRating = tournamentEntry.getSeedRating();
            if (today.after(tournamentEligibilityDate)) {
                eligibilityRating = this.usattDataService.getPlayerRatingAsOfDate(membershipId, tournamentEligibilityDate);
            }
            tournamentEntry.setEligibilityRating(eligibilityRating);
        }
    }


    @PutMapping("/tournamententry/{entryId}")
//    @PreAuthorize("hasAuthority('TournamentDirector') or hasAuthority('Admins')")
    public TournamentEntry update(@PathVariable Long entryId,
                                  @RequestBody TournamentEntry tournamentEntry) {
        return tournamentEntryService.update(tournamentEntry);
    }

    @DeleteMapping("/tournamententry/{entryId}")
    public void delete(@PathVariable Long entryId) {
        tournamentEntryService.delete(entryId);
    }
}
