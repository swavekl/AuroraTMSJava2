package com.auroratms.tournamententry;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.notification.TournamentEventPublisher;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
public class TournamentEntryController {

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private UsattDataService usattDataService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private TournamentEventPublisher eventPublisher;

    /**
     * Returns player entry if it exists or empty list if it doesn't
     * @param tournamentId
     * @param profileId
     * @return
     */
    @GetMapping("/tournamententries")
    public List<TournamentEntry> query(@RequestParam Long tournamentId,
                                       @RequestParam String profileId,
                                       @RequestParam(required = false) Date date) {
        if (tournamentId != 0) {
            return tournamentEntryService.listForTournamentAndUser(tournamentId, profileId);
        } else {
            return tournamentEntryService.listTodaysTournamentForUser(profileId, date);
        }
    }

    @PostMapping("/tournamententry")
    public TournamentEntry create(@RequestBody TournamentEntry tournamentEntry) {
        updateRatings(tournamentEntry);
        TournamentEntry savedEntry = tournamentEntryService.create(tournamentEntry);
        this.updateTournamentStatistics(tournamentEntry.getTournamentFk());

        this.eventPublisher.publishTournamentEnteredEvent(savedEntry.getId());

        return savedEntry;
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
    public TournamentEntry update(@PathVariable Long entryId,
                                  @RequestBody TournamentEntry tournamentEntry) {
        TournamentEntry updatedEntry = tournamentEntryService.update(tournamentEntry);

        updateTournamentStatistics(tournamentEntry.getTournamentFk());

        return updatedEntry;
    }

    /**
     * Deletes tournament entry
     * @param entryId
     */
    @DeleteMapping("/tournamententry/{entryId}")
    public void delete(@PathVariable Long entryId) {
        TournamentEntry tournamentEntry = tournamentEntryService.get(entryId);
        long tournamentId = tournamentEntry.getTournamentFk();

        tournamentEntryService.delete(entryId);

        updateTournamentStatistics(tournamentId);
    }

    /**
     * Updates counts
     * @param tournamentFk
     */
    private void updateTournamentStatistics(long tournamentFk) {
        Tournament tournament = tournamentService.getByKey(tournamentFk);
        int countOfEntries = tournamentEntryService.getCountOfEntries(tournamentFk);
//        System.out.println("countOfEntries = " + countOfEntries);

        // get all events for tournament
        int maxNumEventEntries = 0;
        Collection<TournamentEventEntity> eventList = tournamentEventEntityService.list(tournamentFk, Pageable.unpaged());
        for (TournamentEventEntity tournamentEventEntity : eventList) {
            maxNumEventEntries += tournamentEventEntity.getMaxEntries();
        }
        int numEventEntries = tournamentEventEntryService.getCountOfValidEntriesInAllEvents(tournamentFk);
        tournament.setNumEntries(countOfEntries);
        tournament.setNumEventEntries(numEventEntries);
        tournament.setMaxNumEventEntries(maxNumEventEntries);
        tournamentService.updateTournament(tournament);
    }
}
