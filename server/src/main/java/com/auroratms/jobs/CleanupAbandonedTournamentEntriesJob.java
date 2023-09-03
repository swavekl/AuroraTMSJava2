package com.auroratms.jobs;

import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.paymentrefund.PaymentRefund;
import com.auroratms.paymentrefund.PaymentRefundFor;
import com.auroratms.paymentrefund.PaymentRefundService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Job which removes abandoned tournament entries without any event entries
 * and payments or refunds.
 */
@Component
@Slf4j
public class CleanupAbandonedTournamentEntriesJob implements Job {
    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private PaymentRefundService paymentRefundService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                log.info("CleanupAbandonedTournamentEntriesJob - BEGIN");
                cleanup();
                log.info("CleanupAbandonedTournamentEntriesJob - END");
            }
        };
        task.execute();

    }

    private void cleanup() {
        // list future tournaments
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        calendar.add(Calendar.HOUR, -1);
        Date dateTimeLimit = calendar.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Collection<Tournament> tournaments = tournamentService.listTournamentsAfterDate(today);
        log.info("Found " + tournaments.size() + " future tournaments which start after today " + dateFormatter.format(today));
        for (Tournament tournament : tournaments) {
            if (tournament.isReady()) {
                log.info("Looking for abandoned entries in tournament '" + tournament.getName() + "' which are older than 1 hour.");
                List<Long> entriesWithEventsIds = tournamentEventEntryService.findAllTournamentEntryIds(tournament.getId());
                List<Long> allEntryIds = tournamentEntryService.findAllEntryIds(tournament.getId());
                log.info ("Found " + entriesWithEventsIds.size() + " entries with events and " + allEntryIds.size() + " total entries.");
                allEntryIds.removeAll(entriesWithEventsIds);
                if (!allEntryIds.isEmpty()) {
                    log.info("Found " + allEntryIds.size() + " which didn't have any event entries.");
                    List<TournamentEntry> tournamentEntriesWithoutEvents = tournamentEntryService.listEntries(allEntryIds);
                    // check if this entry is new and perhaps was just started.
                    log.info("Checking if entries are older than 1 day i.e. dateEntered < " + dateFormatter.format(dateTimeLimit));
                    int removedEntries = 0;
                    for (TournamentEntry tournamentEntry : tournamentEntriesWithoutEvents) {
                        Date dateEntered = tournamentEntry.getDateEntered();
                        log.info("Entry " + tournamentEntry.getId() + " entered on "+ dateFormatter.format(dateEntered));
                        if (dateEntered.before(dateTimeLimit)) {
                            // check if there are any payments associated with it and if not then remove it
                            List<PaymentRefund> paymentRefunds = paymentRefundService.getPaymentRefunds(tournamentEntry.getId(), PaymentRefundFor.TOURNAMENT_ENTRY);
                            if (paymentRefunds.isEmpty()) {
                                log.info("No payments or refunds were found.  Removing tournament entry " + tournamentEntry.getId());
                                tournamentEntryService.delete(tournamentEntry.getId());
                                removedEntries++;
                            } else {
                                log.info("Payments or refunds were found. Keeping tournament entry "  + tournamentEntry.getId());
                            }
                        }
                    }
                    if (removedEntries > 0) {
                        int numEntries = tournament.getNumEntries();
                        numEntries = Math.max(0, numEntries - removedEntries);
                        log.info("Removed " + removedEntries + " abandoned entries for tournament '" + tournament.getName() + "'. Updating count of entries to " + numEntries);
                        tournament.setNumEntries(numEntries);
                        tournamentService.updateTournament(tournament);
                    }
                } else {
                    log.info ("No entries to remove for tournament " + tournament.getName());
                }
            } else {
                log.info("Tournament '" + tournament.getName()  + "' is not ready yet.  Skipping entry cleanup.");
            }
        }
    }
}
