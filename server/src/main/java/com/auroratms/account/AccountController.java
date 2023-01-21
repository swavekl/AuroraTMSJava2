package com.auroratms.account;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/account")
@PreAuthorize("isAuthenticated()")
@Transactional
public class AccountController {

    @Value("${stripe.secret.key}")
    private String stripeApiKey;

    @Value("${client.host.url}")
    private String clientHostUrl;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private AccountService accountService;

    /**
     * Finds out if the account exists and if it was successfully activated
     * @param userProfileId
     * @return
     */
    @GetMapping("/status/{userProfileId}")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public ResponseEntity<String> checkAccountStatus(@PathVariable String userProfileId) {

        boolean accountExists = accountService.existsById(userProfileId);
        boolean isActivated = false;
        if (accountExists) {
            AccountEntity account = accountService.findById(userProfileId);
            isActivated = account.isActivated();
        }
        // find out if user created an account already
        String json = String.format("{\"accountExists\": %b, \"isActivated\": %b}", accountExists, isActivated);
        return new ResponseEntity<String>(json, HttpStatus.OK);
    }

    /**
     * Creates account and a link for onboarding
     *
     * @param userProfileId
     * @return
     */
    @PostMapping("/create/{userProfileId}")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public ResponseEntity<String> configureAccount(@PathVariable String userProfileId) {
        Stripe.apiKey = this.stripeApiKey;
        try {
            UserProfile profile = this.userProfileService.getProfile(userProfileId);

            Account account = createAccount(profile);

            // record creation of this account and map it to userProfileId
            // so we can retrieve it later
            AccountEntity accountEntity = new AccountEntity();
            accountEntity.setAccountId(account.getId());
            accountEntity.setActivated(false);
            accountEntity.setProfileId(userProfileId);
            accountEntity.setEmail(profile.getEmail());
            this.accountService.save(accountEntity);

            AccountLink accountLink = createAccountLink(account.getId(), profile.getUserId());
            String accountLinkUrl = accountLink.getUrl();
            String json = String.format("{\"accountLinkUrl\": \"%s\"}", accountLinkUrl);
            return new ResponseEntity<String>(json, HttpStatus.CREATED);
        } catch (StripeException e) {
            String errorMessage = e.getMessage();
            String json = "{\"error\": \"" + errorMessage + "\"}";
            return new ResponseEntity<String>(json, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param stripeAccountId
     * @param userProfileId
     * @return
     */
    private AccountLink createAccountLink(String stripeAccountId, String userProfileId) throws StripeException {
        // The URL that the user will be redirected to upon leaving or completing the linked flow
        String returnUrl = this.clientHostUrl + "/account/onboardreturn/" + userProfileId;

        // The URL the user will be redirected to if the account link is expired, has been previously-visited,
        // or is otherwise invalid. The URL you specify should attempt to generate a new account link with the same parameters
        // used to create the original account link, then redirect the user to the new account link’s URL so they can
        // continue with Connect Onboarding. If a new account link cannot be generated or the redirect fails you
        // should display a useful error to the user.
        String refreshUrl = this.clientHostUrl + "/account/onboardrefresh/" + userProfileId;

        AccountLinkCreateParams accountLinkCreateParams =
                AccountLinkCreateParams.builder()
                        .setAccount(stripeAccountId)
                        .setReturnUrl(returnUrl)
                        .setRefreshUrl(refreshUrl)
                        .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                        .build();

        return AccountLink.create(accountLinkCreateParams);
    }

    /**
     * @param userProfile
     * @return
     * @throws StripeException
     */
    private Account createAccount(UserProfile userProfile) throws StripeException {
        AccountCreateParams params =
                AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.STANDARD)
                        .build();
        return Account.create(params);
    }

    /**
     * @param userProfileId
     * @return
     */
    @PostMapping("/complete/{userProfileId}")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public ResponseEntity<String> completeAccountConfiguration(@PathVariable String userProfileId) {
        boolean exists = accountService.existsById(userProfileId);
        if (exists) {
            try {
                // retrieve the account id which was already started
                AccountEntity accountEntity = accountService.findById(userProfileId);
                String accountId = accountEntity.getAccountId();
                // create a new link
                Stripe.apiKey = this.stripeApiKey;

                // check charges_enabled
                Account retrievedAccount = Account.retrieve(accountId);
                Boolean chargesEnabled = retrievedAccount.getChargesEnabled();
                Boolean detailsSubmitted = retrievedAccount.getDetailsSubmitted();
                if (chargesEnabled && detailsSubmitted) {
                    // mark the process as completed
                    accountEntity.setActivated(true);
                    accountService.save(accountEntity);
                }
                String json = String.format("{\"accountActivated\": %b, \"detailsSubmitted\": %b}", chargesEnabled, detailsSubmitted);
                return new ResponseEntity<String>(json, HttpStatus.OK);
            } catch (StripeException e) {
                String json = String.format("{\"error\": \"%s\"}", e.getMessage());
                return new ResponseEntity<String>(json, HttpStatus.BAD_REQUEST);
            }
        } else {
            String errorMessage = "Account associated with profile " + userProfileId + " not found";
            String json = String.format("{\"error\": \"%s\"}", errorMessage);
            return new ResponseEntity<String>(json, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param userProfileId
     * @return
     */
    @PostMapping("/resume/{userProfileId}")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public ResponseEntity<String> resumeAccountConfiguration(@PathVariable String userProfileId) {
        boolean exists = accountService.existsById(userProfileId);
        if (exists) {
            try {
                // retrieve the account id which was already started
                AccountEntity accountEntity = accountService.findById(userProfileId);

                // create a new link
                Stripe.apiKey = this.stripeApiKey;
                AccountLink accountLink = createAccountLink(accountEntity.getAccountId(), userProfileId);
                String accountLinkUrl = accountLink.getUrl();
                String json = "{\"accountLinkUrl\": \"" + accountLinkUrl + "\"}";
                return new ResponseEntity<String>(json, HttpStatus.CREATED);
            } catch (StripeException e) {
                String errorMessage = e.getMessage();
                String json = "{\"error\": \"" + errorMessage + "\"}";
                return new ResponseEntity<String>(json, HttpStatus.BAD_REQUEST);
            }
        } else {
            // start from scratch
            return this.configureAccount(userProfileId);
        }
    }

//    @DeleteMapping("/{userProfileId}")
//    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
//    public void deleteAccount(@PathVariable String userProfileId) {
//        boolean exists = accountService.existsById(userProfileId);
//        if (exists) {
//            try {
//                // retrieve the account id which was already started
//                AccountEntity accountEntity = accountService.findById(userProfileId);
//
//                // create a new link
//                Stripe.apiKey = this.stripeApiKey;
//                Account account = Account.retrieve(accountEntity.getAccountId());
//                Account deletedAccount = account.delete();
//
//                accountService.delete(userProfileId);
//
//            } catch (StripeException e) {
//                String errorMessage = e.getMessage();
//                String json = "{\"error\": \"" + errorMessage + "\"}";
//            }
//        }
//    }
}
