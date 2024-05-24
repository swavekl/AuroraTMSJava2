package com.auroratms.paymentrefund;

import com.auroratms.account.AccountEntity;
import com.auroratms.account.AccountService;
import com.auroratms.paymentrefund.exception.PaymentException;
import com.auroratms.paymentrefund.notification.PaymentsRefundsEventPublisher;
import com.auroratms.paymentrefund.notification.event.PaymentEvent;
import com.auroratms.paymentrefund.notification.event.RefundsEvent;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/paymentrefund")
@PreAuthorize("isAuthenticated()")
@Transactional
public class PaymentRefundController {

    private static final Logger log = LoggerFactory.getLogger(PaymentRefundController.class);
    @Autowired
    private PaymentRefundService paymentRefundService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private UsattDataService usattDataService;

    @Autowired
    private PaymentsRefundsEventPublisher eventPublisher;

    @Value("${stripe.secret.key}")
    private String stripeApiKey;

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    @Value("${association.profileid}")
    private String associationProfileId;

    /**
     * Gets the stripe public key for the Connect account
     *
     * @return
     */
    @GetMapping("/keyaccountinfo/{paymentRefundFor}/{accountItemId}")
    @ResponseBody
    public ResponseEntity getStripeKey(@PathVariable PaymentRefundFor paymentRefundFor,
                                       @PathVariable long accountItemId) {
        Map<String, String> map = new HashMap<>();
        try {
            AccountEntity accountEntity = getAccountForItem(paymentRefundFor, accountItemId);
            if (accountEntity != null) {
                if (stripePublicKey != null && stripePublicKey.length() > 0) {
                    map.put("stripePublicKey", this.stripePublicKey);

                    Stripe.apiKey = this.stripeApiKey;
                    map.put("stripeAccountId", accountEntity.getAccountId());
                    String defaultAccountCurrency = paymentRefundService.getAccountCurrency(accountEntity.getAccountId());
                    map.put("defaultAccountCurrency", defaultAccountCurrency);
                    return new ResponseEntity(map, HttpStatus.OK);
                } else {
                    return new ResponseEntity("Stripe public key is empty", HttpStatus.NOT_FOUND);
                }
            } else {
                String errorMessage = String.format("Stripe account for account item id %d not found", accountItemId);
                return new ResponseEntity(errorMessage, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
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
            String clientSecret = getClientSecret(paymentRequest);
            Map<String, String> map = new HashMap<>();
            map.put("clientSecret", clientSecret);
            return new ResponseEntity(map, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private String getClientSecret(PaymentRequest paymentRequest) {
        String clientSecret = null;
        // get email address representing login of the tournament director who created this tournament
        PaymentRefundFor paymentRefundFor = paymentRequest.getPaymentRefundFor();
        long accountItemId = paymentRequest.getAccountItemId();
        int amount = paymentRequest.getAmount();
        int applicationFee = 0;
        String paymentType = "";
        switch (paymentRefundFor) {
            case TOURNAMENT_ENTRY:
                paymentType = "tournament";
                applicationFee = (int) (amount * 0.01);
                break;
            case CLINIC:
                paymentType = "clinic";
                applicationFee = (int) (amount * 0.01);
                break;
            case CLUB_AFFILIATION_FEE:
                paymentType = "club affiliation";
                break;
            case TOURNAMENT_SANCTION_FEE:
                paymentType = "tournament sanction";
                break;
            case MEMBERSHIP_FEE:
                paymentType = "membership";
                break;
            case TOURNAMENT_REPORT_FEE:
                paymentType = "tournament report";
                break;
        }
        try {
            AccountEntity accountEntity = this.getAccountForItem(paymentRefundFor, accountItemId);
            if (accountEntity != null) {
                String accountId = accountEntity.getAccountId();
                String currencyCode = paymentRequest.getCurrencyCode();
                String statementDescriptor = paymentRequest.getStatementDescriptor();
                String customerFullName = paymentRequest.getFullName();
                String receiptEmail = paymentRequest.getReceiptEmail();
                // get currency for charge and create a payment intent
                PaymentIntent paymentIntent = this.paymentRefundService.createPaymentIntent(amount, applicationFee,
                        currencyCode.toLowerCase(), accountId, statementDescriptor, customerFullName, receiptEmail);
                clientSecret = paymentIntent.getClientSecret();
            } else {
                String errorMessage = String.format("Unable to find Stripe account id to %s with id %d",
                        paymentType, accountItemId);
                throw new PaymentException(errorMessage, null);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to make a payment to %s with id %d in the amount %d",
                    paymentType, accountItemId, amount);
            throw new PaymentException(errorMessage, e);
        }
        return clientSecret;
    }

    /**
     * Gets account for item and payment type
     *
     * @param paymentRefundFor
     * @param accountItemId
     * @return
     */
    private AccountEntity getAccountForItem(PaymentRefundFor paymentRefundFor, long accountItemId) {
        AccountEntity accountEntity = null;
        switch (paymentRefundFor) {
            case CLUB_AFFILIATION_FEE:
            case TOURNAMENT_SANCTION_FEE:
            case MEMBERSHIP_FEE:
            case TOURNAMENT_REPORT_FEE:
                accountEntity = this.accountService.findById(associationProfileId);
                break;
            case TOURNAMENT_ENTRY:
                String tournamentOwnerLoginId = tournamentService.getTournamentOwner(accountItemId);
                if (tournamentOwnerLoginId != null) {
                    String userProfileId = this.userProfileService.getProfileByLoginId(tournamentOwnerLoginId);
                    if (userProfileId != null) {
                        accountEntity = this.accountService.findById(userProfileId);
                    }
                }
                break;
            case CLINIC:
            default:
                throw new RuntimeException("Not implemented");
        }
        return accountEntity;
    }

    /**
     * @param paymentRefund
     */
    @PostMapping("/recordpayment")
    @ResponseBody
    public ResponseEntity recordPayment(@RequestBody PaymentRefund paymentRefund) {
        log.info("Recording payment " + paymentRefund);
        try {
            PaymentRefund paymentRefundSaved = this.paymentRefundService.recordPaymentRefund(paymentRefund);

            if (PaymentRefundStatus.PAYMENT_COMPLETED == paymentRefund.getStatus()) {
                PaymentEvent event = makePaymentEvent(paymentRefundSaved);
                eventPublisher.publishPaymentEvent(event);
            } else {
                // check & cash payments need to send out proper email
                if (paymentRefund.getPaymentForm() != PaymentForm.CREDIT_CARD) {
                    RefundRequest refundRequest = new RefundRequest();
                    refundRequest.setPaymentRefundFor(paymentRefund.getPaymentRefundFor());
                    refundRequest.setAmount(paymentRefund.getAmount());
                    refundRequest.setTransactionItemId(paymentRefund.getItemId());
                    refundRequest.setAccountItemId(0L); // not needed for check/cash payments
                    refundRequest.setCurrencyCode(paymentRefund.getPaidCurrency());
                    refundRequest.setAmountInAccountCurrency(paymentRefund.getPaidAmount());
                    refundRequest.setExchangeRate(1.0);

                    List<Long> processedRefundIds = new ArrayList<>();
                    processedRefundIds.add(paymentRefundSaved.getId());

                    RefundsEvent event = new RefundsEvent(refundRequest, processedRefundIds);

                    eventPublisher.publishRefundEvents(event);
                }
            }
            log.info("Payment recorded successfully: " + paymentRefund);
            return new ResponseEntity(paymentRefundSaved, HttpStatus.CREATED);
        } catch (Exception e) {
            String errorMessage = String.format("Unable to record payment in the amount of %d for %d, due to %s",
                    paymentRefund.getAmount(), paymentRefund.getItemId(), e.getMessage());
            log.error(errorMessage, e);
            return new ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Gets a list of payments and refunds
     *
     * @param paymentRefundFor what the payment is for - i.e. tournament entry, application fee
     * @param accountItemId id of that item for which to get payments
     * @return
     */
    @GetMapping("/list/{paymentRefundFor}/{accountItemId}")
    @ResponseBody
    public ResponseEntity<List<PaymentRefund>> listPaymentsAndRefunds(@PathVariable PaymentRefundFor paymentRefundFor,
                                                                      @PathVariable long accountItemId) {
        List<PaymentRefund> paymentRefunds = this.paymentRefundService.getPaymentRefunds(accountItemId, paymentRefundFor);
        return new ResponseEntity(paymentRefunds, HttpStatus.OK);
    }

    /**
     * Gets a list of payments and refunds
     *
     * @param paymentRefundFor what the payment is for - i.e. tournament entry, application fee
     * @param eventItemId id of the event (tournament, clinic etc.) for which to get
     * @return
     */
    @GetMapping("/listforevent/{paymentRefundFor}/{eventItemId}")
    @ResponseBody
    public ResponseEntity<List<PaymentRefundInfo>> listPaymentsAndRefundsForEvent(
            @PathVariable PaymentRefundFor paymentRefundFor,
            @PathVariable long eventItemId) {
        try {
            List<Long> itemIds = Collections.emptyList();
            Set<String> profileIdsSet = Collections.emptySet();
            List<PaymentRefundInfo> paymentRefundInfos = Collections.emptyList();

            Map<String, Long> mapProfileIdToEntryId = new HashMap<>();
            if (paymentRefundFor == PaymentRefundFor.TOURNAMENT_ENTRY) {
                List<TournamentEntry> allTournamentEntries = this.tournamentEntryService.listForTournament(eventItemId);
                itemIds = new ArrayList<>(allTournamentEntries.size());
                profileIdsSet = new HashSet<>(allTournamentEntries.size());
                for (TournamentEntry tournamentEntry : allTournamentEntries) {
                    itemIds.add(tournamentEntry.getId());
                    profileIdsSet.add(tournamentEntry.getProfileId());
                    mapProfileIdToEntryId.put(tournamentEntry.getProfileId(), tournamentEntry.getId());
                }
            }

            // get all payments
            List<PaymentRefund> paymentRefunds = this.paymentRefundService.findAllPaymentRefunds(itemIds, paymentRefundFor);
            Map<String, String> profileToName = makeProfileToFullNameMap(profileIdsSet);
            paymentRefundInfos = new ArrayList<>(profileIdsSet.size());
            for (Map.Entry<String, String> entry : profileToName.entrySet()) {
                String profileId = entry.getKey();
                String fullName = entry.getValue();

                // find all payments (and refunds) for this entry
                Long entryId = mapProfileIdToEntryId.get(profileId);
                List<PaymentRefund> paymentsRefundsForThisEntry = paymentRefunds
                        .stream()
                        .filter(c -> c.getItemId() == entryId)
                        .sorted(Comparator.comparing(PaymentRefund::getTransactionDate))
                        .collect(Collectors.toList());
                // skip players who didn't pay
                if (paymentsRefundsForThisEntry.size() > 0) {
                    PaymentRefundInfo paymentRefundInfo = new PaymentRefundInfo();
                    paymentRefundInfo.setProfileId(profileId);
                    paymentRefundInfo.setFullName(fullName);

                    paymentRefundInfo.setPaymentRefundList(paymentsRefundsForThisEntry);

                    paymentRefundInfos.add(paymentRefundInfo);
                }
            }

            Collections.sort(paymentRefundInfos, Comparator.comparing(PaymentRefundInfo::getFullName));

            return new ResponseEntity(paymentRefundInfos, HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = String.format("Unable to list payments and refunds for event %d.  Error: %s",
                    eventItemId, e.getMessage());
            return new ResponseEntity(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get profile id to player full name map
     *
     * @param profileIdsSet set of unique profile ids
     * @return
     */
    private Map<String, String> makeProfileToFullNameMap (Set<String> profileIdsSet) {
        Map<String, String> profileIdToFullnameMap = new HashMap<>(profileIdsSet.size());
        // first find all member ids so we can pull up player records with first and last name
        List<String> profileIds = new ArrayList<>(profileIdsSet);

        // collect membership ids
        Map<String, UserProfileExt> userProfileExtMap = userProfileExtService.findByProfileIds(profileIds);
        List<Long> membershipIds = new ArrayList<>(profileIdsSet.size());
        Map<Long, String> reverseMapMembershipIdToProfileId = new HashMap<>();
        for (Map.Entry<String, UserProfileExt> entry : userProfileExtMap.entrySet()) {
            UserProfileExt userProfileExt = entry.getValue();
            membershipIds.add(userProfileExt.getMembershipId());
            reverseMapMembershipIdToProfileId.put(userProfileExt.getMembershipId(), userProfileExt.getProfileId());
        }

        // pull player records for first and last name
        List<UsattPlayerRecord> playerRecords = usattDataService.findAllByMembershipIdIn(membershipIds);
        for (UsattPlayerRecord playerRecord : playerRecords) {
            Long membershipId = playerRecord.getMembershipId();
            String profileId = reverseMapMembershipIdToProfileId.get(membershipId);
            if (profileId != null) {
                String fullName = playerRecord.getLastName() + ", " + playerRecord.getFirstName();
                profileIdToFullnameMap.put(profileId, fullName);
            }
        }

        return profileIdToFullnameMap;
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
        log.info("Issuing refund " + refundRequest);
        try {
            // accountItemId could be tournament id for tournament entry fees
            // tournament sanction application id for tournament sanction
            // club affiliation application id for those fees,
            // membership id for memberships
            long accountItemId = refundRequest.getAccountItemId();
            PaymentRefundFor paymentRefundFor = refundRequest.getPaymentRefundFor();
            // get account which should issue the refund
            AccountEntity accountEntity = getAccountForItem(paymentRefundFor, accountItemId);

            // get all previous payments and refunds
            List<PaymentRefund> paymentRefunds = this.paymentRefundService.getPaymentRefunds(
                    refundRequest.getTransactionItemId(), paymentRefundFor);
            // issue refunds
            List<Long> processedRefundIds = issueRefunds(paymentRefunds, refundRequest, accountEntity.getAccountId());

            // notify user of refund
            RefundsEvent refundsEvent = new RefundsEvent(refundRequest, processedRefundIds);
            eventPublisher.publishRefundEvents(refundsEvent);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("refunds", processedRefundIds);
            log.info("Refund issued successfully " + refundRequest );
            return new ResponseEntity(resultMap, HttpStatus.CREATED);
        } catch (Exception e) {
            String errorMessage = String.format("Unable to issue full refund %s", e.getMessage());
            log.error(errorMessage, e);
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
            refund.setPaymentForm(PaymentForm.CREDIT_CARD);
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
}
