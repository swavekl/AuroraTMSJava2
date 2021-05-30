package com.auroratms.paymentrefund;

import com.auroratms.account.AccountEntity;
import com.auroratms.account.AccountService;
import com.auroratms.paymentrefund.exception.PaymentException;
import com.auroratms.paymentrefund.notification.PaymentsRefundsEventPublisher;
import com.auroratms.paymentrefund.notification.event.PaymentEvent;
import com.auroratms.paymentrefund.notification.event.RefundsEvent;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.TournamentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/paymentrefund")
@PreAuthorize("isAuthenticated()")
@Transactional
public class PaymentRefundController {

    @Autowired
    private PaymentRefundService paymentRefundService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PaymentsRefundsEventPublisher eventPublisher;

    @Value("${stripe.secret.key}")
    private String stripeApiKey;

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    /**
     * Gets the stripe public key for the Connect account
     *
     * @return
     */
    @GetMapping("/keyaccountinfo/{tournamentId}")
    @ResponseBody
    public ResponseEntity getStripeKey(@PathVariable long tournamentId) {
        Map<String, String> map = new HashMap<>();
        AccountEntity accountForTournament = this.getAccountForTournament(tournamentId);
        if (accountForTournament != null) {
            if (stripePublicKey != null && stripePublicKey.length() > 0) {
                map.put("stripePublicKey", this.stripePublicKey);

                Stripe.apiKey = this.stripeApiKey;
                map.put("tournamentAccountId", accountForTournament.getAccountId());
                String defaultAccountCurrency = "usd";
                try {
                    Account stripeAccount = Account.retrieve(accountForTournament.getAccountId());
                    defaultAccountCurrency = stripeAccount.getDefaultCurrency();
                } catch (StripeException e) {

                }
                map.put("defaultAccountCurrency", defaultAccountCurrency);
                return new ResponseEntity(map, HttpStatus.OK);
            } else {
                return new ResponseEntity("Stripe public key is empty", HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = String.format("Stripe account for tournament%d not found", tournamentId);
            return new ResponseEntity(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Starts the payment session
     *
     * @param paymentRequest
     * @return
     */
    @PostMapping("/secret")
    @ResponseBody
    public ResponseEntity getSecret(@RequestBody PaymentRequest paymentRequest) {
        try {
            // retrieve the account for this entity
            long accountItemId = paymentRequest.getAccountItemId();
            PaymentRefundFor paymentRefundFor = paymentRequest.getPaymentRefundFor();
            String clientSecret = null;
            switch (paymentRefundFor) {
                case TOURNAMENT_ENTRY:
                    clientSecret = makeTournamentPaymentIntent(accountItemId,
                            paymentRequest.getAmount(),
                            paymentRequest.getCurrencyCode(),
                            paymentRequest.getStatementDescriptor(),
                            paymentRequest.getFullName(),
                            paymentRequest.getReceiptEmail());
                    break;
                case CLINIC:
                    break;
                case USATT_FEE:
                    break;
            }

            Map<String, String> map = new HashMap<>();
            map.put("clientSecret", clientSecret);
            return new ResponseEntity(map, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param tournamentId
     * @param customerFullName
     * @param receiptEmail
     * @return
     */
    private String makeTournamentPaymentIntent(long tournamentId,
                                               int amount,
                                               String currencyCode,
                                               String statementDescriptor,
                                               String customerFullName,
                                               String receiptEmail) {
        String clientSecret = null;
        // get email address representing login of the tournament director who created this tournament
        try {
            AccountEntity accountForTournament = this.getAccountForTournament(tournamentId);
            if (accountForTournament != null) {
                String accountId = accountForTournament.getAccountId();
                // get currency for charge and create a payment intent
                int applicationFee = (int) (amount * 0.01);
                PaymentIntent paymentIntent = this.paymentRefundService.createPaymentIntent(amount, applicationFee,
                        currencyCode.toLowerCase(), accountId, statementDescriptor, customerFullName, receiptEmail);
                clientSecret = paymentIntent.getClientSecret();
            } else {
                String errorMessage = String.format("Unable to find Stripe account id for tournament with id %d", tournamentId);
                throw new PaymentException(errorMessage, null);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to make a payment to tournament with id %d in the amount %d", tournamentId, amount);
            throw new PaymentException(errorMessage, e);
        }
        return clientSecret;
    }

    /**
     * Gets account for tournament by finding its owner (Tournament Director)
     *
     * @param tournamentId id of the tournament for which to get the account id
     * @return
     */
    private AccountEntity getAccountForTournament(long tournamentId) {
        AccountEntity accountEntity = null;
        String tournamentOwnerLoginId = tournamentService.getTournamentOwner(tournamentId);
        if (tournamentOwnerLoginId != null) {
            String userProfileId = this.userProfileService.getProfileByLoginId(tournamentOwnerLoginId);
            if (userProfileId != null) {
                accountEntity = this.accountService.findById(userProfileId);
            }
        }
        return accountEntity;
    }

    /**
     * @param paymentRefund
     */
    @PostMapping("/recordpayment")
    @ResponseBody
    public ResponseEntity recordPayment(@RequestBody PaymentRefund paymentRefund) {
        try {
            PaymentRefund paymentRefundSaved = this.paymentRefundService.recordPaymentRefund(paymentRefund);

            PaymentEvent event = makePaymentEvent(paymentRefundSaved);
            eventPublisher.publishPaymentEvent(event);

            return new ResponseEntity(paymentRefundSaved, HttpStatus.CREATED);
        } catch (StripeException e) {
            String errorMessage = String.format("Unable to record payment %s", e.getMessage());
            return new ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Gets a list of payments and refunds
     *
     * @param tournamentEntryId
     * @return
     */
    @GetMapping("/listforentry/{tournamentEntryId}")
    @ResponseBody
    public ResponseEntity<List<PaymentRefund>> listTournamentEntryPaymentsAndRefunds(@PathVariable long tournamentEntryId) {
        List<PaymentRefund> paymentRefunds = this.paymentRefundService.getPaymentRefunds(tournamentEntryId, PaymentRefundFor.TOURNAMENT_ENTRY);
        return new ResponseEntity(paymentRefunds, HttpStatus.OK);
    }

    /**
     * Starts the payment session
     *
     * @param refundRequest
     * @return
     */
    @PostMapping("/issuerefund")
    @ResponseBody
    public ResponseEntity issueRefund(@RequestBody RefundRequest refundRequest) {
        try {
            long accountItemId = refundRequest.getAccountItemId();
            PaymentRefundFor paymentRefundFor = refundRequest.getPaymentRefundFor();
            List<Long> processedRefundIds = new ArrayList<>();
            List<PaymentRefund> paymentRefunds = new ArrayList<>();
            switch (paymentRefundFor) {
                case TOURNAMENT_ENTRY:
                    // get account id for this tournament
                    long tournamentEntryId = refundRequest.getTransactionItemId();
                    AccountEntity accountEntity = getAccountForTournament(accountItemId);
                    // get all previous payments and refunds
                    paymentRefunds = this.paymentRefundService.getPaymentRefunds(tournamentEntryId,
                            PaymentRefundFor.TOURNAMENT_ENTRY);
                    processedRefundIds = issueRefunds(paymentRefunds, refundRequest, accountEntity.getAccountId());
                    break;
                case CLINIC:
                    break;
                case USATT_FEE:
                    break;
            }

            // notify user of refund
            RefundsEvent refundsEvent = new RefundsEvent(refundRequest, processedRefundIds);
            eventPublisher.publishRefundEvents(refundsEvent);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("refunds", processedRefundIds);
            return new ResponseEntity(resultMap, HttpStatus.CREATED);
        } catch (Exception e) {
            String errorMessage = String.format("Unable to issue full refund %s", e.getMessage());
            return new ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param paymentRefunds
     * @param refundRequest
     * @param accountId
     * @return
     */
    private List<Long> issueRefunds(List<PaymentRefund> paymentRefunds, RefundRequest refundRequest, String accountId) throws StripeException {
        // figure out which payments need to be refunded
        RefundCalculator refundCalculator = new RefundCalculator(paymentRefunds,
                refundRequest.getAmountInAccountCurrency(),
                refundRequest.getExchangeRate());
        List<PaymentRefund> refundsToIssue = refundCalculator.determineRefunds();
        List<Long> processedRefundIds = new ArrayList<>(refundsToIssue.size());
        for (PaymentRefund refund : refundsToIssue) {
            // issue refund via Stripe
            Refund stripeRefund = this.paymentRefundService.refundCharge(refund, accountId);

            // record successful refund
            refund.setRefundId(stripeRefund.getId());
            PaymentRefund savedRefund = this.paymentRefundService.recordPaymentRefund(refund);
            processedRefundIds.add(savedRefund.getId());
        }
        return processedRefundIds;
    }

    /**
     * @param paymentRefund
     * @return
     */
    private PaymentEvent makePaymentEvent(PaymentRefund paymentRefund) {
        PaymentEvent event = new PaymentEvent();
        event.setPaymentRefundFor(paymentRefund.getPaymentRefundFor());
        event.setItemId(paymentRefund.getItemId());
        event.setTransactionDate(paymentRefund.getTransactionDate());
        event.setAmount(paymentRefund.getAmount());
        event.setPaidAmount(paymentRefund.getPaidAmount());
        event.setPaidCurrency(paymentRefund.getPaidCurrency());
        return event;
    }

    /**
     * Prepares the event which
     * @param refundIds
     * @param refundRequest
     * @return
     */
    private RefundsEvent makeRefundsEvent(List<Long> refundIds, RefundRequest refundRequest) {
        RefundsEvent refundsEvent = new RefundsEvent();
        return refundsEvent;
    }

}
