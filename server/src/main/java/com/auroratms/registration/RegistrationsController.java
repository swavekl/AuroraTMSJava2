package com.auroratms.registration;

import com.auroratms.paymentrefund.PaymentRefund;
import com.auroratms.paymentrefund.PaymentRefundFor;
import com.auroratms.paymentrefund.PaymentRefundService;
import com.auroratms.paymentrefund.PaymentRefundStatus;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for retrieving list of registrations in various activities i.e. tournaments, clinics, seminars etc.
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
public class RegistrationsController {

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private PaymentRefundService paymentRefundService;

    @GetMapping("/registrations/{profileId}")
    @ResponseBody
    public ResponseEntity<List<Registration>> get(@PathVariable String profileId) {

        try {
            List<Registration> registrations = new ArrayList<>();
            // list all tournament entries
            List<TournamentEntry> tournamentEntries = tournamentEntryService.listForUser(profileId);
            if (!tournamentEntries.isEmpty()) {
                // get tournaments for these entries
                List<Long> tournamentIds = tournamentEntries.stream()
                        .map((TournamentEntry::getTournamentFk))
                        .collect(Collectors.toList());
                Collection<Tournament> tournaments = tournamentService.listTournamentsByIds(tournamentIds);

                // get entry ids so we can fetch payments & refunds in one query
                List<Long> entryIds = tournamentEntries.stream()
                        .map((TournamentEntry::getId))
                        .collect(Collectors.toList());
                List<PaymentRefund> allPaymentRefunds = this.paymentRefundService.findAllPaymentRefunds(entryIds, PaymentRefundFor.TOURNAMENT_ENTRY);

                for (TournamentEntry tournamentEntry : tournamentEntries) {
                    Registration registration = getRegistration(tournamentEntry, tournaments, allPaymentRefunds);
                    registrations.add(registration);
                }
            }

            // sort by start date descending
            Comparator<Registration> comparator = Comparator
                    .comparing(Registration::getStartDate).reversed();
            Collections.sort(registrations, comparator);

            return ResponseEntity.ok(registrations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private Registration getRegistration(TournamentEntry tournamentEntry, Collection<Tournament> tournaments, List<PaymentRefund> allPaymentRefunds) {
        Registration registration = new Registration();
        registration.setRegistrationEventType(RegistrationEventType.TOURNAMENT);
        registration.setId(tournamentEntry.getId());
        for (Tournament tournament : tournaments) {
            if (tournament.getId().equals(tournamentEntry.getTournamentFk())) {
                registration.setName(tournament.getName());
                registration.setActivityId(tournament.getId());
                registration.setStartDate(tournament.getStartDate());
                registration.setEndDate(tournament.getEndDate());
                break;
            }
        }

        int cost = getCost(allPaymentRefunds, tournamentEntry.getId());
        registration.setCost(cost);
//        registration.setInfo("3 events");
        return registration;
    }

    private int getCost(List<PaymentRefund> allPaymentRefunds, Long tournamentEntryId) {
        int cost = 0;
        for (PaymentRefund paymentRefund : allPaymentRefunds) {
            if (paymentRefund.getItemId() == tournamentEntryId) {
                if (paymentRefund.getStatus().equals(PaymentRefundStatus.PAYMENT_COMPLETED)) {
                    cost += paymentRefund.getAmount();
                } else if (paymentRefund.getStatus().equals(PaymentRefundStatus.REFUND_COMPLETED)) {
                    cost -= paymentRefund.getAmount();
                }
            }
        }
        return cost;
    }
}
