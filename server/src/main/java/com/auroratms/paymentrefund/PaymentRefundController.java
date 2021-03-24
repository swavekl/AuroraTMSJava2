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
    @GetMapping("/publickey")
    @ResponseBody
    public ResponseEntity getStripeKey() {
        if (stripePublicKey != null && stripePublicKey.length() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("stripePublicKey", this.stripePublicKey);
            return new ResponseEntity(map, HttpStatus.OK);
        } else {
            return new ResponseEntity("Stripe public key is empty", HttpStatus.NOT_FOUND);
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
                            paymentRefundRequest.getFullName());
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
     * @return
     */
    private String makeTournamentPaymentIntent(long tournamentId, int amount, String statementDescriptor, String customerFullName) {
        String clientSecret = null;
        // get email address representing login of the tournament director who created this tournament
        try {
            String tournamentOwnerLoginId = tournamentService.getTournamentOwner(tournamentId);
            if (tournamentOwnerLoginId != null) {
                String profileByLoginId = this.userProfileService.getProfileByLoginId(tournamentOwnerLoginId);
                if (profileByLoginId != null) {
                    AccountEntity accountEntity = this.accountService.findById(profileByLoginId);
                    if (accountEntity != null) {
                        String accountId = accountEntity.getAccountId();
                        // get currency for charge and create a payment intent
                        Stripe.apiKey = this.stripeApiKey;
                        Account stripeAccount = Account.retrieve(accountId);
                        PaymentIntent paymentIntent = this.paymentRefundService.createPaymentIntent(amount, 100,
                                stripeAccount.getDefaultCurrency(), accountId, statementDescriptor, customerFullName);
                        clientSecret = paymentIntent.getClientSecret();
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to make a payment to tournament with id %d in the amount %d", tournamentId, amount);
            throw new PaymentException(errorMessage, e);
        }
        return clientSecret;
    }
}
