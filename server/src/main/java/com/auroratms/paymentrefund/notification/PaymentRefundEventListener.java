package com.auroratms.paymentrefund.notification;

import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.paymentrefund.PaymentRefund;
import com.auroratms.paymentrefund.PaymentRefundFor;
import com.auroratms.paymentrefund.PaymentRefundService;
import com.auroratms.paymentrefund.notification.event.PaymentEvent;
import com.auroratms.paymentrefund.notification.event.RefundsEvent;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.utils.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listener for payment & refund events to send emails about
 */
@Component
public class PaymentRefundEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRefundEventListener.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private PaymentRefundService paymentRefundService;

    @Async
    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handlePaymentEvent(PaymentEvent event) {
        // run this task as system principal so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                PaymentRefundFor paymentRefundFor = event.getPaymentRefundFor();
                switch (paymentRefundFor) {
                    case TOURNAMENT_ENTRY:
                        sendTournamentPaymentEmail(event);
                        break;
                    case CLINIC:
                        break;
                    case USATT_FEE:
                        break;
                }
            }

        };
        task.execute();
    }

    @Async
    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleRefundsEvent(RefundsEvent event) {
        // run this task as system principal so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                PaymentRefundFor paymentRefundFor = event.getPaymentRefundFor();
                switch (paymentRefundFor) {
                    case TOURNAMENT_ENTRY:
                        sendTournamentRefundEmail(event);
                        break;
                    case CLINIC:
                        break;
                    case USATT_FEE:
                        break;
                }
            }

        };
        task.execute();
    }

    /**
     *
     * @param event
     */
    private void sendTournamentPaymentEmail(PaymentEvent event) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            // get the entry so we can get tournament information
            long tournamentEntryId = event.getItemId();
            TournamentEntry tournamentEntry = tournamentEntryService.get(tournamentEntryId);
            long tournamentId = tournamentEntry.getTournamentFk();

            Tournament tournament = addTournamentInformation(templateModel, tournamentId);
            String tournamentDirectorEmail = tournament.getEmail();

            UserProfile userProfile = addPlayerInformation(templateModel, tournamentEntry.getProfileId());
            String toAddress = userProfile.getEmail();

            // Add payment information
            // convert 12545 to 125.45
            Double amount = (double)event.getAmount() / 100;
            templateModel.put("amount", amount);
            templateModel.put("currency", event.getPaidCurrency());

            emailService.sendMessageUsingThymeleafTemplate(toAddress, tournamentDirectorEmail,
                    "Tournament Payment Received",
                    "tournament-payment-confirmation.html",
                    templateModel);
        } catch (Throwable t) {
            logger.error("Error during sending email about payment", t);
        }
    }

    /**
     *
     * @param templateModel
     * @param tournamentId
     * @return
     */
    private Tournament addTournamentInformation(Map<String, Object> templateModel, long tournamentId) {
        Tournament tournament = tournamentService.getByKey(tournamentId);
        String tournamentName = tournament.getName();
        String tournamentDirectorName = tournament.getContactName();
        String tournamentDirectorEmail = tournament.getEmail();
        String tournamentDirectorPhone = tournament.getPhone();

        templateModel.put("tournamentName", tournamentName);
        templateModel.put("tournamentDirectorName", tournamentDirectorName);
        templateModel.put("tournamentDirectorEmail", tournamentDirectorEmail);
        templateModel.put("tournamentDirectorPhone", tournamentDirectorPhone);
        return tournament;
    }

    private UserProfile addPlayerInformation(Map<String, Object> templateModel, String profileId) {
        UserProfile userProfile = userProfileService.getProfile(profileId);
        templateModel.put("playerFirstName", userProfile.getFirstName());
        templateModel.put("playerLastName", userProfile.getLastName());
        templateModel.put("city", userProfile.getCity());
        templateModel.put("state", userProfile.getState());
        return userProfile;
    }

    /**
     *
     * @param event
     */
    private void sendTournamentRefundEmail (RefundsEvent event) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            // get the entry so we can get tournament information
            long tournamentEntryId = event.getItemId();
            TournamentEntry tournamentEntry = tournamentEntryService.get(tournamentEntryId);
            long tournamentId = tournamentEntry.getTournamentFk();

            Tournament tournament = addTournamentInformation(templateModel, tournamentId);
            String tournamentDirectorEmail = tournament.getEmail();

            UserProfile userProfile = addPlayerInformation(templateModel, tournamentEntry.getProfileId());
            String toAddress = userProfile.getEmail();

            // get refunds only for this refund action (not previous)
            List<Long> refundItemIds = event.getRefundItemIds();
            List<RefundItem> refundItemList = new ArrayList<>(refundItemIds.size());
            List<PaymentRefund> allPaymentRefunds = this.paymentRefundService.getPaymentRefunds(tournamentEntryId, event.getPaymentRefundFor());
            for (PaymentRefund paymentRefund : allPaymentRefunds) {
                long paymentRefundId = paymentRefund.getId();
                if (refundItemIds.contains(paymentRefundId)) {
                    double paidAmount = ((double)paymentRefund.getPaidAmount()) / 100;
                    refundItemList.add(new RefundItem(paidAmount, paymentRefund.getPaidCurrency()));
                }
            }

            Double amount = ((double)event.getAmount()) / 100;
            templateModel.put("amount", amount);
            templateModel.put("currency", event.getCurrency());
            templateModel.put("refundItems", refundItemList);

            emailService.sendMessageUsingThymeleafTemplate(toAddress, tournamentDirectorEmail,
                    "Tournament Refund Issued",
                    "tournament-refund-confirmation.html",
                    templateModel);
        } catch (Throwable t) {
            logger.error("Error during sending email about refund", t);
        }
    }

    /**
     * Individual refund items
     */
    private class RefundItem {
        double paidAmount;
        String paidCurrency;

        public RefundItem(double paidAmount, String paidCurrency) {
            this.paidAmount = paidAmount;
            this.paidCurrency = paidCurrency;
        }

        public double getPaidAmount() {
            return paidAmount;
        }

        public String getPaidCurrency() {
            return paidCurrency;
        }
    }

}

