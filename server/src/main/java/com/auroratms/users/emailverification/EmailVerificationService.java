package com.auroratms.users.emailverification;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** Owns verification-token persistence, hashing, atomic claims, resend limits, and cleanup state. */
@Service
public class EmailVerificationService {

    private static final Duration CLAIM_TIMEOUT = Duration.ofMinutes(5);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AccountVerificationTokenRepository repository;
    private final Duration tokenLifetime;
    private final Duration resendWindow;
    private final int maxResendsPerWindow;
    private final Duration sourceLimitWindow;
    private final int maxRequestsPerSourceWindow;
    private final IMap<String, Integer> sourceRequestCounts;

    /** Wires the token repository, configurable limits, and shared source-rate-limit map. */
    public EmailVerificationService(AccountVerificationTokenRepository repository,
                                    HazelcastInstance hazelcastInstance,
                                    @Value("${account-verification.token-lifetime:PT1H}") Duration tokenLifetime,
                                    @Value("${account-verification.resend-window:PT5M}") Duration resendWindow,
                                    @Value("${account-verification.max-resends:2}") int maxResendsPerWindow,
                                    @Value("${account-verification.source-limit-window:PT5M}") Duration sourceLimitWindow,
                                    @Value("${account-verification.source-limit-max-requests:20}") int maxRequestsPerSourceWindow) {
        this.repository = repository;
        this.tokenLifetime = tokenLifetime;
        this.resendWindow = resendWindow;
        this.maxResendsPerWindow = maxResendsPerWindow;
        this.sourceLimitWindow = sourceLimitWindow;
        this.maxRequestsPerSourceWindow = maxRequestsPerSourceWindow;
        this.sourceRequestCounts = hazelcastInstance.getMap("account-verification-resend-source-rate-limit");
    }

    /** Creates the first one-hour verification token for a newly staged Okta user. */
    @Transactional
    public TokenIssue createForNewUser(String oktaUserId) {
        if (repository.findByOktaUserIdForUpdate(oktaUserId).isPresent()) {
            throw new IllegalStateException("An account verification record already exists for this user");
        }
        Instant now = Instant.now();
        String rawToken = generateToken();
        AccountVerificationToken entity = new AccountVerificationToken(
                oktaUserId, hashToken(rawToken), now.plus(tokenLifetime), now);
        repository.saveAndFlush(entity);
        return new TokenIssue(rawToken, entity.getExpiresAt());
    }

    /** Replaces the outstanding token and atomically enforces the per-account resend window. */
    @Transactional
    public TokenIssue regenerateForResend(String oktaUserId) {
        AccountVerificationToken entity = repository.findByOktaUserIdForUpdate(oktaUserId)
                .orElseThrow(() -> new IllegalArgumentException("No pending verification for account"));
        if (entity.getUsedAt() != null || entity.getClaimedAt() != null || entity.getCleanupClaimedAt() != null
                || entity.isCleanupReviewRequired()) {
            throw new IllegalStateException("Account verification is already being completed");
        }

        Instant now = Instant.now();
        boolean resetWindow = !now.isBefore(entity.getResendWindowStartedAt().plus(resendWindow));
        if (!resetWindow && entity.getResendCount() >= maxResendsPerWindow) {
            throw new TooManyResendsException();
        }

        String rawToken = generateToken();
        entity.rotate(hashToken(rawToken), now.plus(tokenLifetime), now, resetWindow);
        repository.saveAndFlush(entity);
        return new TokenIssue(rawToken, entity.getExpiresAt());
    }

    /** Claims an unexpired token hash so only one request can activate its associated account. */
    @Transactional
    public Optional<VerificationClaim> claim(String tokenHash) {
        Instant now = Instant.now();
        int updated = repository.claimValidToken(tokenHash, now, now.minus(CLAIM_TIMEOUT));
        if (updated != 1) {
            return Optional.empty();
        }
        AccountVerificationToken token = repository.findByTokenHash(tokenHash).orElse(null);
        if (token == null) {
            return Optional.empty();
        }
        return Optional.of(new VerificationClaim(token.getOktaUserId(), token.getTokenHash()));
    }

    /** Marks a claimed token as consumed after Okta activation succeeds. */
    @Transactional
    public void markUsed(String tokenHash) {
        AccountVerificationToken token = repository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalStateException("Verification record not found"));
        if (token.getClaimedAt() == null) {
            throw new IllegalStateException("Verification token is not claimed");
        }
        token.markUsed(Instant.now());
    }

    /** Makes a token available again after a definite activation failure. */
    @Transactional
    public void releaseClaim(String tokenHash) {
        repository.findByTokenHash(tokenHash).ifPresent(AccountVerificationToken::releaseClaim);
    }

    /** Persists that the final account-cleanup notice was accepted by the email sender. */
    @Transactional
    public void markCleanupNoticeSent(String userId) {
        repository.findByOktaUserIdForUpdate(userId).ifPresent(token -> token.markCleanupNoticeSent(Instant.now()));
    }

    /** Lists old unused verification records that may belong to abandoned registrations. */
    @Transactional(readOnly = true)
    public List<CleanupCandidate> findCleanupCandidates(Duration accountRetention) {
        Instant now = Instant.now();
        return repository.findCleanupCandidates(now.minus(accountRetention), now.minus(CLAIM_TIMEOUT))
                .stream()
                .map(token -> new CleanupCandidate(token.getOktaUserId(), token.getCleanupNoticeSentAt(),
                        token.getCreatedAt(), token.getExpiresAt()))
                .toList();
    }

    /** Atomically claims an old record and returns its current cleanup-notice state. */
    @Transactional
    public Optional<CleanupCandidate> claimForCleanup(String userId, Duration accountRetention) {
        Instant now = Instant.now();
        int claimed = repository.claimForCleanup(userId, now.minus(accountRetention), now.minus(CLAIM_TIMEOUT), now);
        if (claimed != 1) {
            return Optional.empty();
        }
        return repository.findByOktaUserId(userId)
                .map(token -> new CleanupCandidate(token.getOktaUserId(), token.getCleanupNoticeSentAt(),
                        token.getCreatedAt(), token.getExpiresAt()));
    }

    /** Releases a cleanup claim after a retryable email or Okta operation fails. */
    @Transactional
    public void releaseCleanupClaim(String userId) {
        repository.findByOktaUserIdForUpdate(userId).ifPresent(AccountVerificationToken::releaseCleanupClaim);
    }

    /** Marks an unexpected Okta lifecycle state so token cleanup preserves its record for review. */
    @Transactional
    public void markCleanupReviewRequired(String userId) {
        repository.findByOktaUserIdForUpdate(userId).ifPresent(AccountVerificationToken::markCleanupReviewRequired);
    }

    /** Deletes the verification record after the associated account is deleted or reconciled. */
    @Transactional
    public void deleteRecord(String userId) {
        repository.deleteForOktaUser(userId);
    }

    /** Removes consumed verification records older than the configured diagnostic retention. */
    @Transactional
    public int deleteOldConsumedRecords(Duration retention) {
        return repository.deleteOldConsumedTokens(Instant.now().minus(retention));
    }

    /** Removes expired unused tokens after any active verification claim has timed out. */
    @Transactional
    public int deleteExpiredUnclaimedTokens() {
        Instant now = Instant.now();
        return repository.deleteExpiredUnclaimedTokens(now, now.minus(CLAIM_TIMEOUT));
    }

    /** Applies a shared source-address resend limit without storing the address as a Hazelcast key. */
    public boolean allowSourceRequest(String sourceAddress) {
        String key = hashToken(sourceAddress == null ? "unknown" : sourceAddress);
        sourceRequestCounts.lock(key);
        try {
            Integer count = sourceRequestCounts.get(key);
            if (count != null && count >= maxRequestsPerSourceWindow) {
                return false;
            }
            int nextCount = count == null ? 1 : count + 1;
            long ttlMillis = Math.max(sourceLimitWindow.toMillis(), 1L);
            sourceRequestCounts.put(key, nextCount, ttlMillis, TimeUnit.MILLISECONDS);
            return true;
        } finally {
            sourceRequestCounts.unlock(key);
        }
    }

    /** Returns the lowercase SHA-256 hex digest used for token persistence and rate-limit keys. */
    public String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /** Generates the high-entropy token returned once for constructing the verification email link. */
    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Raw token and expiry returned only to the caller that sends the verification email. */
    public record TokenIssue(String token, Instant expiresAt) { }

    /** Okta user associated with a token that was atomically claimed for verification. */
    public record VerificationClaim(String oktaUserId, String tokenHash) { }

    /** Old verification record reserved for cleanup, including whether its final notice was sent. */
    public record CleanupCandidate(String oktaUserId, Instant cleanupNoticeSentAt,
                                   Instant createdAt, Instant expiresAt) { }

    /** Indicates that the account has exhausted its allowed resends for the current window. */
    public static class TooManyResendsException extends RuntimeException {
        public TooManyResendsException() {
            super("Too many resend requests");
        }
    }
}
