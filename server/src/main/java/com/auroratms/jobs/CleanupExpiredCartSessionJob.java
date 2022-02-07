package com.auroratms.jobs;


import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.paymentrefund.CartSession;
import com.auroratms.paymentrefund.CartSessionService;
import com.auroratms.paymentrefund.PaymentRefundFor;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.utils.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Job responsible for cleaning up expired sessions i.e. sessions where user failed to complete payment, refund
 * or confirm changes that had no balance.  Sends email to such user to let them know they were deleted.
 */
@Component
@Slf4j
public class CleanupExpiredCartSessionJob implements Job {

    @Autowired
    private CartSessionService cartSessionService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("CleanupExpiredCartSessionJob - BEGIN");
        List<CartSession> expiredSessions = cartSessionService.findExpiredSessions(PaymentRefundFor.TOURNAMENT_ENTRY);
        for (CartSession expiredSession : expiredSessions) {
            cleanupExpiredCartSession(expiredSession);
        }
        log.info("CleanupExpiredCartSessionJob - END");
    }

    private void cleanupExpiredCartSession(CartSession expiredSession) {
        try {
            log.info("Cleaning up unfinished entries tied to session " + expiredSession.getSessionUUID() + " started at " + expiredSession.getSessionLastUpdate());
            cartSessionService.finishSession(expiredSession.getSessionUUID());
            List<TournamentEventEntry> unfinishedTournamentEventEntries = tournamentEventEntryService.listAllForCartSession(expiredSession.getSessionUUID());
            log.info("Found " + unfinishedTournamentEventEntries.size() + " entries for expired session");
            if (unfinishedTournamentEventEntries.size() > 0) {
                long tournamentEntryId = 0L;
                List<Long> playerEventsToDelete = new ArrayList<>();
                for (TournamentEventEntry tournamentEventEntry : unfinishedTournamentEventEntries) {
                    tournamentEntryId = tournamentEventEntry.getTournamentEntryFk();
                    log.info("Deleting unfinished tournamentEventEntry = " + tournamentEventEntry.getId());
                    playerEventsToDelete.add(tournamentEventEntry.getTournamentEventFk());
                    tournamentEventEntryService.delete(tournamentEventEntry.getId());
                }
                // find out who was removed
                TournamentEntry tournamentEntry = tournamentEntryService.get(tournamentEntryId);
                String profileId = tournamentEntry.getProfileId();
                UserProfile userProfile = userProfileService.getProfile(profileId);
                String playerFirstName = userProfile.getFirstName();
                String playerLastName = userProfile.getLastName();
                String playerEmail = userProfile.getEmail();

                // get tournament director for this tournament
                long tournamentFk = tournamentEntry.getTournamentFk();
                Tournament tournament = tournamentService.getByKey(tournamentFk);
                String tournamentName = tournament.getName();
                String contactEmail = tournament.getEmail();
                String contactName = tournament.getContactName();
                String contactPhone = tournament.getPhone();

                // get events information from which player was dropped
                List<TournamentEvent> deletedTournamentEvents = tournamentEventEntityService.findAllById(playerEventsToDelete);

                // send email to player and cc TD
                Map<String, Object> templateModel = new HashMap<>();
                templateModel.put("playerFirstName", playerFirstName);
                templateModel.put("playerLastName", playerLastName);
                templateModel.put("tournamentName", tournamentName);
                templateModel.put("tournamentDirectorName", contactName);
                templateModel.put("tournamentDirectorPhone", contactPhone);
                templateModel.put("tournamentDirectorEmail", contactEmail);
                templateModel.put("removedEvents", deletedTournamentEvents);
                String emailSubject = String.format("Your unconfirmed '%s' entries", tournamentName );

                log.info("Sending email to " + playerEmail + " about entries for " + tournamentName);
                emailService.sendMessageUsingThymeleafTemplate(playerEmail, contactEmail, emailSubject,
                        "cleanup-job/player-entries-removed.html", templateModel);
            }
        } catch (Exception e) {
            log.error("Error cleaning up cart session " + expiredSession.getSessionUUID(), e);
        }
    }
}
