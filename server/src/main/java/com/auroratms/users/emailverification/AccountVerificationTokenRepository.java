package com.auroratms.users.emailverification;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Persistence operations for token issuance, atomic verification claims, and account cleanup. */
public interface AccountVerificationTokenRepository extends JpaRepository<AccountVerificationToken, Long> {

    /** Locks a user's verification row while changing its token or cleanup state. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from AccountVerificationToken token where token.oktaUserId = :userId")
    Optional<AccountVerificationToken> findByOktaUserIdForUpdate(@Param("userId") String userId);

    /** Finds the current verification record for an Okta user. */
    Optional<AccountVerificationToken> findByOktaUserId(String userId);

    /** Finds the record associated with a presented token hash. */
    Optional<AccountVerificationToken> findByTokenHash(String tokenHash);

    /** Finds records created before the supplied cutoff. */
    List<AccountVerificationToken> findByCreatedAtBefore(Instant cutoff);

    /** Returns old, unused records whose verification and cleanup claims are absent or stale. */
    @Query("select token from AccountVerificationToken token where token.createdAt < :cutoff " +
            "and token.usedAt is null and token.cleanupReviewRequired = false " +
            "and (token.claimedAt is null or token.claimedAt < :claimCutoff) " +
            "and (token.cleanupClaimedAt is null or token.cleanupClaimedAt < :claimCutoff)")
    List<AccountVerificationToken> findCleanupCandidates(@Param("cutoff") Instant cutoff,
                                                         @Param("claimCutoff") Instant claimCutoff);

    /** Atomically reserves an old unused record for one cleanup worker. */
    @Modifying
    @Query("update AccountVerificationToken token set token.cleanupClaimedAt = :now " +
            "where token.oktaUserId = :userId and token.createdAt < :cutoff and token.usedAt is null " +
            "and token.cleanupReviewRequired = false " +
            "and (token.claimedAt is null or token.claimedAt < :claimCutoff) " +
            "and (token.cleanupClaimedAt is null or token.cleanupClaimedAt < :claimCutoff)")
    int claimForCleanup(@Param("userId") String userId,
                        @Param("cutoff") Instant cutoff,
                        @Param("claimCutoff") Instant claimCutoff,
                        @Param("now") Instant now);

    /** Removes a user's verification record after successful account cleanup or reconciliation. */
    @Modifying
    @Query("delete from AccountVerificationToken token where token.oktaUserId = :userId")
    int deleteForOktaUser(@Param("userId") String userId);

    /** Purges consumed records past retention unless they are held for cleanup or review. */
    @Modifying
    @Query("delete from AccountVerificationToken token where token.usedAt is not null and token.usedAt < :cutoff " +
            "and token.cleanupNoticeSentAt is null and token.cleanupReviewRequired = false")
    int deleteOldConsumedTokens(@Param("cutoff") Instant cutoff);

    /** Deletes expired unused records while preserving current verification and cleanup claims. */
    @Modifying
    @Query("delete from AccountVerificationToken token where token.usedAt is null and token.expiresAt < :now " +
            "and token.cleanupClaimedAt is null and token.cleanupReviewRequired = false " +
            "and (token.claimedAt is null or token.claimedAt < :claimCutoff)")
    int deleteExpiredUnclaimedTokens(@Param("now") Instant now,
                                     @Param("claimCutoff") Instant claimCutoff);

    /** Atomically permits one request to claim an unexpired, unused token. */
    @Modifying
    @Query("update AccountVerificationToken token set token.claimedAt = :now " +
            "where token.tokenHash = :hash and token.usedAt is null and token.cleanupClaimedAt is null " +
            "and token.cleanupReviewRequired = false and token.expiresAt > :now " +
            "and (token.claimedAt is null or token.claimedAt < :claimCutoff)")
    int claimValidToken(@Param("hash") String tokenHash,
                        @Param("now") Instant now,
                        @Param("claimCutoff") Instant claimCutoff);
}
