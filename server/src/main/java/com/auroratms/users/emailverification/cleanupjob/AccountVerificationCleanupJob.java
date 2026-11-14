package com.auroratms.users.emailverification.cleanupjob;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.users.emailverification.EmailVerificationService;
import com.auroratms.utils.EmailService;
import com.okta.sdk.resource.client.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Hourly Quartz task that cleans abandoned Okta registrations and expired verification-token records. */
@Component
@Slf4j
public class AccountVerificationCleanupJob implements Job {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private EmailService emailService;

    @Value("${account-verification.account-retention:P30D}")
    private Duration accountRetention;

    @Value("${account-verification.staged-account-retention:PT1H}")
    private Duration stagedAccountRetention;

    @Value("${account-verification.consumed-retention:P30D}")
    private Duration consumedRecordRetention;

    /** Processes eligible accounts before token purging so their Okta state can still be checked. */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("AccountVerificationCleanupJob - BEGIN");
            int deletedAccounts = cleanupAbandonedAccounts();
            int expiredDeleted = emailVerificationService.deleteExpiredUnclaimedTokens();
            int consumedDeleted = emailVerificationService.deleteOldConsumedRecords(consumedRecordRetention);
            log.info("AccountVerificationCleanupJob - END; Okta accounts deleted: {}, expired tokens deleted: {}, old consumed records deleted: {}",
                    deletedAccounts, expiredDeleted, consumedDeleted);
        } catch (Exception e) {
            log.error("AccountVerificationCleanupJob failed", e);
            throw new JobExecutionException(e);
        }
    }

    /** Claims old self-registrations, checks current Okta state, notifies, then deletes when eligible. */
    private int cleanupAbandonedAccounts() {
        int deletedAccounts = 0;
        for (EmailVerificationService.CleanupCandidate candidate : emailVerificationService.findCleanupCandidates(stagedAccountRetention)) {
            EmailVerificationService.CleanupCandidate claimed = emailVerificationService
                    .claimForCleanup(candidate.oktaUserId(), stagedAccountRetention).orElse(null);
            if (claimed == null) {
                continue;
            }

            try {
                UserProfile profile = userProfileService.getCurrentProfile(claimed.oktaUserId());
                if (profile == null) {
                    emailVerificationService.deleteRecord(claimed.oktaUserId());
                    continue;
                }
                if (AccountCleanupPolicy.isProtectedEmail(profile.getEmail())) {
                    log.info("Skipping protected registration {} during account cleanup", claimed.oktaUserId());
                    emailVerificationService.releaseCleanupClaim(claimed.oktaUserId());
                    continue;
                }
                if ("ACTIVE".equalsIgnoreCase(profile.getUserStatus()) && AccountCleanupPolicy.isProfileComplete(profile)) {
                    // Okta activation succeeded but token consumption may not have been recorded.
                    emailVerificationService.deleteRecord(claimed.oktaUserId());
                    continue;
                }
                if (!isReadyForCleanup(profile, claimed)) {
                    emailVerificationService.releaseCleanupClaim(claimed.oktaUserId());
                    continue;
                }
                if (!AccountCleanupPolicy.shouldDelete(profile)) {
                    log.warn("Preserving registration {} with unexpected Okta status {} for cleanup review",
                            claimed.oktaUserId(), profile.getUserStatus());
                    emailVerificationService.markCleanupReviewRequired(claimed.oktaUserId());
                    continue;
                }

                if (claimed.cleanupNoticeSentAt() == null) {
                    Map<String, Object> templateModel = new HashMap<>();
                    templateModel.put("firstName", profile.getFirstName());
                    templateModel.put("email", profile.getEmail());
                    emailService.sendMessageUsingThymeleafTemplate(profile.getEmail(), null,
                            "Your TTAurora account was not completed", "email-verification/account-cleanup-notice", templateModel);
                    emailVerificationService.markCleanupNoticeSent(claimed.oktaUserId());
                }

                // Recheck immediately before deletion because the account or profile may have changed while mail was sent.
                UserProfile currentProfile = userProfileService.getCurrentProfile(claimed.oktaUserId());
                if (currentProfile == null) {
                    emailVerificationService.deleteRecord(claimed.oktaUserId());
                    continue;
                }
                if (AccountCleanupPolicy.isProtectedEmail(currentProfile.getEmail())) {
                    emailVerificationService.releaseCleanupClaim(claimed.oktaUserId());
                    continue;
                }
                if ("ACTIVE".equalsIgnoreCase(currentProfile.getUserStatus())
                        && AccountCleanupPolicy.isProfileComplete(currentProfile)) {
                    emailVerificationService.deleteRecord(claimed.oktaUserId());
                    continue;
                }
                if (!isReadyForCleanup(currentProfile, claimed)) {
                    emailVerificationService.releaseCleanupClaim(claimed.oktaUserId());
                    continue;
                }
                if (!AccountCleanupPolicy.shouldDelete(currentProfile)) {
                    emailVerificationService.markCleanupReviewRequired(claimed.oktaUserId());
                    continue;
                }

                // No database transaction is held while sending mail or calling Okta.
                userProfileService.deleteProfile(claimed.oktaUserId());
                emailVerificationService.deleteRecord(claimed.oktaUserId());
                deletedAccounts++;
            } catch (ApiException e) {
                if (e.getCode() == 404) {
                    // A previous attempt may have deleted the Okta user before failing to remove its token row.
                    emailVerificationService.deleteRecord(claimed.oktaUserId());
                } else {
                    emailVerificationService.releaseCleanupClaim(claimed.oktaUserId());
                    log.error("Could not clean up an abandoned Okta registration", e);
                }
            } catch (Exception e) {
                emailVerificationService.releaseCleanupClaim(claimed.oktaUserId());
                log.error("Could not clean up an abandoned Okta registration", e);
            }
        }
        return deletedAccounts;
    }

    /** Requires staged accounts to have an expired latest link; other incomplete accounts keep their longer retention. */
    private boolean isReadyForCleanup(UserProfile profile, EmailVerificationService.CleanupCandidate candidate) {
        if ("STAGED".equalsIgnoreCase(profile.getUserStatus())) {
            return !candidate.expiresAt().isAfter(java.time.Instant.now());
        }
        return !candidate.createdAt().isAfter(java.time.Instant.now().minus(accountRetention));
    }
}
