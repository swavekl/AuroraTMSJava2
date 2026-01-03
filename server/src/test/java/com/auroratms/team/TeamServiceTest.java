package com.auroratms.team;

import com.auroratms.AbstractServiceTest;
import com.auroratms.event.*;
import com.auroratms.paymentrefund.CartSession;
import com.auroratms.paymentrefund.PaymentRefundFor;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.PricingMethod;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentConfiguration;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.users.UserRoles;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TeamServiceTest extends AbstractServiceTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private UserProfileService profileService;
    @Autowired
    private UserProfileService userProfileService;

    @PersistenceContext
    private EntityManager entityManager;

    // keep for cleanup
    private Long teamId;
    private Long tournamentId;
    private Long tournamentEventId;
    @Autowired
    private UserProfileExtService userProfileExtService;

    @AfterEach
    public void tearDown() {
        System.out.println("TeamServiceTest.tearDown - BEGIN");
        // We must ensure a transaction is active for deletions to work
        if (!TestTransaction.isActive()) {
            TestTransaction.start();
        }

        try {
            // 1. Delete Team-related data
            if (teamId != null) {
                // This should ideally delete TeamMembers and then the Team
                teamService.delete(teamId);
            }

            // 2. Delete entries created by the listener for non-captain members
            // You can query for entries associated with this tournament and event
            if (tournamentId != null) {
                List<Long> tournamentEntryIds = tournamentEntryService.findAllEntryIds(tournamentId);
                for (Long tournamentEntryId : tournamentEntryIds) {
                    // Delete event entries first (child records)
                    TournamentEventEntry tee = tournamentEventEntryService.getByTournamentEventIdAndTournamentEntryId(tournamentEventId, tournamentEntryId);
                    tournamentEventEntryService.delete(tee.getId());

                    // Delete the entry shell (parent record)
                    tournamentEntryService.delete(tournamentEntryId);
                }
            }

            // 3. Delete the Event and Tournament
            if (tournamentEventId != null) {
                tournamentEventEntityService.delete(tournamentEventId);
            }
            if (tournamentId != null) {
                tournamentService.deleteTournament(tournamentId);
            }

            TestTransaction.flagForCommit();
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        } finally {
            TestTransaction.end();
            System.out.println("TeamServiceTest.tearDown - END");
        }
    }

    @Test
    @Transactional
    @Rollback(false) // REQUIRED for TransactionPhase.AFTER_COMMIT listeners
    @WithMockUser(username = "swaveklorenc@yahoo.com", authorities = {UserRoles.Admins})
    public void testAddingAndRemoving() {
        Tournament tournament = makeTournament();
        tournamentId = tournament.getId();
        System.out.println("tournamentId = " + tournamentId);
        TournamentEvent tournamentEvent = makeTeamEvent(tournament.getId());
        tournamentEventId = tournamentEvent.getId();
        System.out.println("tournamentEventId = " + tournamentEventId);

        CartSession cartSession = new CartSession();
        cartSession.setPaymentRefundFor(PaymentRefundFor.TOURNAMENT_ENTRY);
        ObjectIdGenerators.UUIDGenerator uuidGenerator = new ObjectIdGenerators.UUIDGenerator();
        UUID uuid = uuidGenerator.generateId(cartSession);
        String cartSessionId = uuid.toString();
        System.out.println("cartSessionId = " + cartSessionId);

        Team team = new Team();
        team.setName("Eola Masters");
        team.setEntryPricePaid(949);
        team.setTournamentEventFk(tournamentEventId);
        team.setTeamRating(3555);
        team.setCreatedDate(new Date());

        printPlayersUserIds();

        TeamMember tm = makeTeamMember("swaveklorenc@yahoo.com", 1881, true, cartSessionId, tournamentEventId);
        team.addTeamMember(tm);

        Team savedTeam = teamService.save(team);
        teamId = savedTeam.getId();
        List<TeamMember> savedTeamMembers = savedTeam.getTeamMembers();
        assertEquals(1, savedTeamMembers.size(), "team size is wrong");

        System.out.println("Committing team save");
        // 2. FORCE THE COMMIT NOW
        // This triggers the TransactionalEventListener phase=AFTER_COMMIT
        TestTransaction.flagForCommit();

        entityManager.flush();
        entityManager.clear();

        TestTransaction.end();

        System.out.println("starting new transaction before additioanl checks");
        // 3. Start a new transaction if you need to perform more saves or DB checks
        TestTransaction.start();

        Team team1 = teamService.getTeamById(teamId);

        List<TeamMember> teamMembers1 = team1.getTeamMembers();
        assertEquals(1, teamMembers1.size(), "wrong team size");

        TestTransaction.flagForCommit();
        TestTransaction.end();

        System.out.println("starting new transaction before adding new team member");
        TestTransaction.start();

        Team team2 = teamService.getTeamById(teamId);
        // Pawel Zych
        TeamMember tm2 = makeTeamMember("swaveklorenc+319@gmail.com", 1500, false, cartSessionId, tournamentEventId);
        team2.addTeamMember(tm2);

        Team savedTeam2 = teamService.save(team2);
        List<TeamMember> savedTeamMembers2 = savedTeam2.getTeamMembers();
        assertEquals(2, savedTeamMembers2.size(), "team size is wrong");

        TestTransaction.flagForCommit();

        entityManager.flush();
        entityManager.clear();

        TestTransaction.end();

        waitAndCheck (tournamentId, tournamentEventId, tm2.getProfileId(), true);

        TestTransaction.start();

        Team team3 = teamService.getTeamById(teamId);
        List<TeamMember> teamMembers3 = team3.getTeamMembers();
        assertEquals(2, teamMembers3.size(), "wrong team size");

        TestTransaction.flagForCommit();
        entityManager.flush();
        entityManager.clear();

        TestTransaction.end();

        TestTransaction.start();

        Team team4 = teamService.getTeamById(teamId);
        List<TeamMember> teamMembers4 = team4.getTeamMembers();
        assertEquals(2, teamMembers4.size(), "wrong team size");

        TeamMember teamMemberToRemove = teamMembers4.stream()
                .filter(teamMember -> !teamMember.isCaptain())
                .findFirst()
                .get();
        String teamMemberToRemoveProfileId = teamMemberToRemove.getProfileId();
        System.out.println("teamMemberToRemoveProfileId = " + teamMemberToRemoveProfileId);

        team4.removeTeamMember(teamMemberToRemove);
        assertEquals(1, team4.getTeamMembers().size(), "wrong team size after removal");

        teamService.save(team4);

        TestTransaction.flagForCommit();
        entityManager.flush();
        entityManager.clear();

        TestTransaction.end();

        waitAndCheck (tournamentId, tournamentEventId, teamMemberToRemoveProfileId, false);

        TestTransaction.start();

        Team team5 = teamService.getTeamById(teamId);
        List<TeamMember> teamMembers5 = team5.getTeamMembers();
        assertEquals(1, teamMembers5.size(), "wrong team size");

        TestTransaction.flagForCommit();
        entityManager.flush();
        entityManager.clear();

        TestTransaction.end();

        //        login = swaveklorenc+320@gmail.com
//        login = swaveklorenc+arek@gmail.com
    }

    public void waitAndCheck(long tournamentId, Long tournamentEventId, String profileId, boolean creationCheck) {
        boolean checkSucceeded = false;
        int maxAttempts = 10;
        System.out.println("waitAndCheck");
        for (int i = 0; i < maxAttempts; i++) {
            // We must check in a fresh transaction or a direct query
            // to see what the background thread committed
            if (creationCheck) {
                if (checkIfEntriesExist(tournamentId, tournamentEventId, profileId)) {
                    checkSucceeded = true;
                    break;
                }
            } else {
                if (checkIfEntrMarkedForDeletion(tournamentId, tournamentEventId, profileId)) {
                    checkSucceeded = true;
                    break;
                }
            }
            try {
                Thread.sleep(1000); // Wait 1 second between checks
            } catch (InterruptedException e) {
                // ignore
            }
            System.out.println("Polling for background entry creation... attempt " + (i + 1));
        }

        assertTrue(checkSucceeded, creationCheck ? "entries not created successfully" : "entires not marked for deletion");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean checkIfEntrMarkedForDeletion(long tournamentId, Long tournamentEventId, String profileId) {
        boolean checkSucceeded = false;
        List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournamentAndUser(tournamentId, profileId);
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            List<TournamentEventEntry> tournamentEventEntries = tournamentEventEntryService.listAllForTournamentEntry(tournamentEntry.getId());
            for (TournamentEventEntry tournamentEventEntry : tournamentEventEntries) {
                if (tournamentEventEntry.getTournamentEventFk() == tournamentEventId) {
                    System.out.println("Found tournamentEventEntry with id: " + tournamentEventEntry.getId() + " status " + tournamentEventEntry.getStatus());
                    if (tournamentEventEntry.getStatus() == EventEntryStatus.PENDING_DELETION) {
                        checkSucceeded = true;
                    }
                }
            }
        }
        return checkSucceeded;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean checkIfEntriesExist(long tournamentId, Long tournamentEventId, String profileId) {
        try {
            List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournamentAndUser(tournamentId, profileId);
            System.out.println(profileId + " tournamentEntries.size() = " + tournamentEntries.size());
            Long tournamentEntryFk = null;
            for (TournamentEntry tournamentEntry : tournamentEntries) {
                if (tournamentEntry.getProfileId().equals(profileId)) {
                    tournamentEntryFk = tournamentEntry.getId();
                    System.out.println("Found tournament entry for " + profileId + " with id " + tournamentEntryFk);
                }
            }

            boolean teeFound = false;
            if (tournamentEntryFk != null) {
                List<TournamentEventEntry> tournamentEventEntries = tournamentEventEntryService.listAllForTournamentEntry(tournamentEntryFk);
                for (TournamentEventEntry tournamentEventEntry : tournamentEventEntries) {
                    if (tournamentEventEntry.getTournamentEventFk() == tournamentEventId) {
                        teeFound = true;
                        System.out.println("Found tournamentEventEntry with id: " + tournamentEventEntry.getId() + " status " + tournamentEventEntry.getStatus().name());
                    }
                }
            } else {
                System.out.println("not found TournamentEntry for profile " + profileId);
            }

            return tournamentEntryFk != null && teeFound;
        } catch (Exception e) {
            return false;
        }
    }

    private TeamMember makeTeamMember(String loginId, int rating, boolean isCaptain, String cartSessionId, Long tournamentEventId) {
        String playerProfileId = profileService.getProfileByLoginId(loginId);
        System.out.println("Got playerProfileId = " + playerProfileId + " for loginId " + loginId);
        TeamMember teamMember = new TeamMember();
        teamMember.setPlayerRating(rating);
        teamMember.setCaptain(isCaptain);
        teamMember.setStatus(isCaptain ? TeamEntryStatus.CONFIRMED : TeamEntryStatus.INVITED);
        teamMember.setCartSessionId(cartSessionId);
        teamMember.setProfileId(playerProfileId);
        teamMember.setTournamentEventFk(tournamentEventId);
        return teamMember;
    }

    private TournamentEvent makeTeamEvent(Long tournamentId) {
        TournamentEvent tournamentEvent = new TournamentEvent();
        tournamentEvent.setName("TEAMS");
        tournamentEvent.setTournamentFk(tournamentId);
        tournamentEvent.setFeeStructure(FeeStructure.PER_SCHEDULE);
        tournamentEvent.setTeamRatingCalculationMethod(TeamRatingCalculationMethod.SUM_TOP_THREE);
        tournamentEvent.setMaxTeamPlayers(5);
        tournamentEvent.setMinTeamPlayers(3);
        TournamentEventConfiguration configuration = new TournamentEventConfiguration();

        List<FeeScheduleItem> feeScheduleItems = makeFeeSchedule();

        configuration.setFeeScheduleItems(feeScheduleItems);
        tournamentEvent.setConfiguration(configuration);

        return this.tournamentEventEntityService.create(tournamentEvent);
    }

    private List<FeeScheduleItem> makeFeeSchedule() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2026);
        calendar.set(Calendar.MONTH, Calendar.MARCH);
        calendar.set(Calendar.DATE, 31);

        FeeScheduleItem feeScheduleItem1 = new FeeScheduleItem();
        feeScheduleItem1.setOfferName("Early Bird");
        feeScheduleItem1.setDeadline(calendar.getTime());
        feeScheduleItem1.setEntryFee(949);
        feeScheduleItem1.setCancellationFee(350);

        calendar.set(Calendar.MONTH, Calendar.JULY);
        calendar.set(Calendar.DATE, 15);

        FeeScheduleItem feeScheduleItem2 = new FeeScheduleItem();
        feeScheduleItem2.setOfferName("Regular Entry");
        feeScheduleItem2.setDeadline(calendar.getTime());
        feeScheduleItem2.setEntryFee(1049);
        feeScheduleItem2.setCancellationFee(350);

        List<FeeScheduleItem> feeScheduleItems = new ArrayList<>();
        feeScheduleItems.add(feeScheduleItem1);
        feeScheduleItems.add(feeScheduleItem2);
        return feeScheduleItems;
    }

    private Tournament makeTournament() {
        Tournament tournament = new Tournament();
        tournament.setName("Team Tournament");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2026);
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
        calendar.set(Calendar.DATE, 5);
        Date startDate = calendar.getTime();
        calendar.set(Calendar.DATE, 7);
        Date endDate = calendar.getTime();
        tournament.setStartDate(startDate);
        tournament.setEndDate(endDate);
        tournament.setStarLevel(5);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        Date eligibilityDate = calendar.getTime();

        TournamentConfiguration configuration = new TournamentConfiguration();
        configuration.setEligibilityDate(eligibilityDate);
        configuration.setPricingMethod(PricingMethod.STANDARD);
        tournament.setConfiguration(configuration);
        return tournamentService.saveTournament(tournament);
    }

    private void printPlayersUserIds () {
//        95553	Pawel	Zych	00uy335elouLGwllC0h7
//        96307	Patryk	Zyworonek	00uy335em99oCDSCb0h7
//        74192	Arkadiusz	Zyworonek	00ux8cj8udvnXcaIv0h7    }
//        73407	Leszek	Zabiegly	00uy335efmIih2EBs0h7
        String [] profileIds = {"00uhroqm92dXK1b8x0h7", "00uy335elouLGwllC0h7", "00uy335em99oCDSCb0h7", "00ux8cj8udvnXcaIv0h7"};
        for (String profileId : profileIds) {
            if (userProfileExtService.existsByProfileId(profileId)) {
                UserProfile profile1 = userProfileService.getProfile(profileId);
                System.out.println("profile " + profileId + " exists for " + profile1.getLastName() + ", " + profile1.getFirstName());
            } else {
                System.out.println("No linked profile for " + profileId);
            }
        }
    }
}


