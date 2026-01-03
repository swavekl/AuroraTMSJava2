package com.auroratms.team.notification;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.team.Team;
import com.auroratms.team.TeamMember;
import com.auroratms.team.notification.event.TeamChangedEvent;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.EntryType;
import com.auroratms.tournamententry.MembershipType;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor  // instead of autowired
public class TeamChangedEventListener {

    private final TournamentEventEntryService tournamentEventEntryService;

    private final TournamentEventEntityService tournamentEventEntityService;

    private final TournamentEntryService tournamentEntryService;

    private final TournamentService tournamentService;

    private final UsattDataService usattDataService;

    private final UserProfileService profileService;

    private final UserProfileExtService userProfileExtService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTeamChangedEvent(TeamChangedEvent event) {
        // Execute as system principal to ensure access to Tournament/Event services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                processEvent(event);
            }
        };
        task.execute();
    }

    private void processEvent(TeamChangedEvent teamChangedEvent) {
        log.info("Begin processing" + teamChangedEvent);
        switch (teamChangedEvent.getTeamAction()) {
            case CREATED:
            case UPDATED:
                addRemoveTournamentAndEventEntries(teamChangedEvent);
                break;

            case DELETED:
                break;
        }
        log.info("Finished processing " + teamChangedEvent);
    }

    /**
     *
     * @param teamChangedEvent
     */
    private void addRemoveTournamentAndEventEntries(TeamChangedEvent teamChangedEvent) {

        Team team = teamChangedEvent.getTeam();
        List<String> previousIds = teamChangedEvent.getPreviousProfileIds();
        String cartSessionId = teamChangedEvent.getCartSessionId();

        // Current members in the database (after save)
        List<String> currentMembersProfileIds = team.getTeamMembers().stream()
                .map(TeamMember::getProfileId)
                .toList();

        // 1. ADDED: They are in currentMembersProfileIds but were NOT in previousIds
        List<String> addedMembersProfileIds = currentMembersProfileIds.stream()
                .filter(id -> !previousIds.contains(id))
                .toList();

        // 2. REMOVED: They were in previousIds but are NOT in currentMembersProfileIds
        List<String> removedMembersProfileIds = previousIds.stream()
                .filter(id -> !currentMembersProfileIds.contains(id))
                .toList();

        // get the tournament and event information
        TournamentEvent tournamentEvent = tournamentEventEntityService.get(team.getTournamentEventFk());

        // Now process the creation/deletion of TournamentEntries
        processAdditions(addedMembersProfileIds, team, cartSessionId, tournamentEvent);
        processRemovals(removedMembersProfileIds, team, cartSessionId, tournamentEvent);
    }

    /**
     *
     * @param addedMembersProfileIds
     * @param team
     * @param cartSessionId
     * @param tournamentEvent
     * @return
     */
    private void processAdditions(List<String> addedMembersProfileIds, Team team, String cartSessionId, TournamentEvent tournamentEvent) {
        long eventId = tournamentEvent.getId();
        long tournamentId = tournamentEvent.getTournamentFk();

        // get tournament data
        Tournament tournament = tournamentService.getByKey(tournamentId);
        Date elegibilityRatingDate = tournament.getConfiguration().getEligibilityDate();
        Date tournamentStartDate = tournament.getStartDate();

        // get the price they are paying for it so the entry shows it correctly
        double price = team.getEntryPricePaid();

        // create tournament entries if they don't exist
        for (String profileId : addedMembersProfileIds) {
            log.info("Adding/fetching tournament entry for player " + profileId);
            List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournamentAndUser(tournamentId, profileId);
            TournamentEntry tournamentEntry = null;
            log.info(" Found " + tournamentEntries.size() + " tournament entries for profile " + profileId);
            if (tournamentEntries.isEmpty()) {
                tournamentEntry = makeTournamentEntry(profileId, elegibilityRatingDate, tournamentStartDate, tournamentId);
            } else {
                // entry already exists
                log.info("Tournament entry already exists");
                tournamentEntry = tournamentEntries.get(0);
            }
            makeEventEntry(tournamentId, tournamentEntry.getId(), eventId, price, cartSessionId, team.getId());
        }
    }

    private void processRemovals(List<String> removedMembersProfileIds, Team team, String cartSessionId, TournamentEvent tournamentEvent) {
        // put removed player entries in PENDING_DELETION state.  They will be cleaned up later by confirm
        long tournamentId = tournamentEvent.getTournamentFk();
        Long eventId = tournamentEvent.getId();
        for (String removedMemberProfileId : removedMembersProfileIds) {
            log.info("removedMemberProfileId = " + removedMemberProfileId);
            List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournamentAndUser(tournamentId, removedMemberProfileId);
            List<Long> tournamentEntryIds = tournamentEntries.stream().map(TournamentEntry::getId).toList();
            if (!tournamentEntryIds.isEmpty()) {
                Long tournamentEntryId = tournamentEntryIds.get(0);
                TournamentEventEntry playerTeamEventEntry =
                        tournamentEventEntryService.getByTournamentEventIdAndTournamentEntryId(eventId, tournamentEntryId);
                if (playerTeamEventEntry != null) {
                    log.info("Updating status of removed player event entry " + playerTeamEventEntry.getId());
                    playerTeamEventEntry.setStatus(EventEntryStatus.PENDING_DELETION);
                    playerTeamEventEntry.setCartSessionId(cartSessionId);
                    tournamentEventEntryService.update(playerTeamEventEntry);
                }
            }
        }
    }

    /**
     *
     * @param tournamentId
     * @param tournamentEntryId
     * @param eventId
     * @param price
     * @param cartSessionId
     * @param teamId
     * @return
     */
    private void makeEventEntry(long tournamentId, long tournamentEntryId, long eventId, double price, String cartSessionId, long teamId) {
        TournamentEventEntry tournamentEventEntry = tournamentEventEntryService.getByTournamentEventIdAndTournamentEntryId(eventId, tournamentEntryId);
        if (tournamentEventEntry == null) {
            tournamentEventEntry = new TournamentEventEntry();
            tournamentEventEntry.setTournamentFk (tournamentId);
            tournamentEventEntry.setTournamentEventFk (eventId);
            tournamentEventEntry.setTournamentEntryFk (tournamentEntryId);
            tournamentEventEntry.setDateEntered (new Date());
            tournamentEventEntry.setPrice (price);
            tournamentEventEntry.setStatus (EventEntryStatus.PENDING_CONFIRMATION);
            tournamentEventEntry.setCartSessionId (cartSessionId);
            tournamentEventEntry.setTeamFk (teamId);
            TournamentEventEntry createdEntry = tournamentEventEntryService.create(tournamentEventEntry);
            log.info("created TournamentEventEntry id " + createdEntry.getId() + " for tournamentEntryId " + tournamentEntryId);
        }
    }

    /**
     *
     * @param profileId
     * @param elegibilityRatingDate
     * @param tournamentStartDate
     * @param tournamentId
     * @return
     */
    private TournamentEntry makeTournamentEntry(String profileId, Date elegibilityRatingDate, Date tournamentStartDate, long tournamentId) {
        int eligibilityRating = 0;
        int seedRating = 0;
        boolean membershipExpired = true;
        log.info("Checking if profileId exists " + profileId);
        if (userProfileExtService.existsByProfileId(profileId)) {
            log.info("getting userProfileExt " + profileId);
            UserProfileExt userProfileExt = userProfileExtService.getByProfileId(profileId);
            if (userProfileExt != null) {
                log.info("userProfileExt = " + userProfileExt);
                long membershipId = userProfileExt.getMembershipId();
                eligibilityRating = usattDataService.getPlayerRatingAsOfDate(membershipId, elegibilityRatingDate);
                UsattPlayerRecord usattPlayerRecord = usattDataService.getPlayerByMembershipId(membershipId);
                // todo - contact async USATT service for current membership status.
                seedRating = usattPlayerRecord.getTournamentRating();
                Date membershipExpirationDate = usattPlayerRecord.getMembershipExpirationDate();
                membershipExpired = membershipExpirationDate.before(tournamentStartDate);
            }
        } else {
            log.warn ("User profile id " + profileId + " is not mapped to USATT record");
        }
        MembershipType membershipType = (membershipExpired) ? MembershipType.BASIC_PLAN : MembershipType.NO_MEMBERSHIP_REQUIRED;
        log.info("membershipType = " + membershipType);
        log.info("creating TournamentEntry for profile " + profileId);
        TournamentEntry tournamentEntry = new TournamentEntry();
        tournamentEntry.setTournamentFk(tournamentId);
        tournamentEntry.setEntryType(EntryType.INDIVIDUAL);
        tournamentEntry.setDateEntered(new Date());
        tournamentEntry.setEligibilityRating(eligibilityRating);
        tournamentEntry.setSeedRating(seedRating);
        tournamentEntry.setOwningTournamentEntryFk(null);
        tournamentEntry.setProfileId(profileId);
        tournamentEntry.setMembershipOption(membershipType);
        tournamentEntry.setUsattDonation(0);

        return tournamentEntryService.create(tournamentEntry);
    }
}
