package com.auroratms.paymentrefund;

import com.auroratms.account.AccountEntity;
import com.auroratms.account.AccountService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.TournamentService;
import com.stripe.Stripe;
import com.stripe.model.Account;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Value("${stripe.secret.key}")
    private String stripeApiKey;

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    /**
     * Gets the stripe public key for the Connect account
     * @return
     */
    @GetMapping("/keyaccountinfo/{tournamentId}")
    @ResponseBody
    public ResponseEntity getStripeKey(@PathVariable long tournamentId) {
        Map<String, String> map = new HashMap<>();
        AccountEntity accountForTournament = this.getAccountForTournament(tournamentId);
        if (accountForTournament != null) {
            map.put("tournamentAccountId", accountForTournament.getAccountId());
            if (stripePublicKey != null && stripePublicKey.length() > 0) {
                map.put("stripePublicKey", this.stripePublicKey);
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
     * @param paymentRefundRequest
     * @return
     */
    @PostMapping("/secret")
    @ResponseBody
    public ResponseEntity getSecret(@RequestBody PaymentRefundRequest paymentRefundRequest) {
        try {
            // retrieve the account for this entity
            long itemId = paymentRefundRequest.getItemId();
            PaymentRefundFor paymentRefundFor = paymentRefundRequest.getPaymentRefundFor();
            String clientSecret = null;
            switch (paymentRefundFor) {
                case TOURNAMENT_ENTRY:
                    clientSecret = makeTournamentPaymentIntent(itemId,
                            paymentRefundRequest.getAmount(),
                            paymentRefundRequest.getStatementDescriptor(),
                            paymentRefundRequest.getFullName(),
                            paymentRefundRequest.getReceiptEmail());
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
                Stripe.apiKey = this.stripeApiKey;
                Account stripeAccount = Account.retrieve(accountId);
                PaymentIntent paymentIntent = this.paymentRefundService.createPaymentIntent(amount, 100,
                        stripeAccount.getDefaultCurrency(), accountId, statementDescriptor, customerFullName, receiptEmail);
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
     *
     * @param paymentRefund
     */
    @PostMapping("/recordpayment")
    @ResponseBody
    public ResponseEntity recordPayment(@RequestBody PaymentRefund paymentRefund) {
        return new ResponseEntity(this.paymentRefundService.recordPayment(paymentRefund), HttpStatus.CREATED);
    }

    /**
     * Gets a list of payments and refunds
     * @param tournamentEntryId
     * @return
     */
    @GetMapping("/listforentry/{tournamentEntryId}")
    @ResponseBody
    public ResponseEntity<List<PaymentRefund>> listTournamentEntryPaymentsAndRefunds (@PathVariable long tournamentEntryId) {
        List<PaymentRefund> paymentRefunds = this.paymentRefundService.getPaymentRefunds(tournamentEntryId, PaymentRefundFor.TOURNAMENT_ENTRY);
        return new ResponseEntity(paymentRefunds, HttpStatus.OK);
    }
}
