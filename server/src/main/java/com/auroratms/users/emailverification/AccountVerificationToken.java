package com.auroratms.users.emailverification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/** One current verification token and its resend/cleanup state for a self-registered Okta user. */
@Entity
@Table(name = "account_verification_token",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_account_verification_token_user", columnNames = "okta_user_id"),
                @UniqueConstraint(name = "uq_account_verification_token_hash", columnNames = "token_hash")
        },
        indexes = @Index(name = "ix_account_verification_token_expires", columnList = "expires_at"))
public class AccountVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "okta_user_id", nullable = false, length = 64)
    private String oktaUserId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "resend_window_started_at", nullable = false)
    private Instant resendWindowStartedAt;

    @Column(name = "resend_count", nullable = false)
    private int resendCount;

    @Column(name = "cleanup_notice_sent_at")
    private Instant cleanupNoticeSentAt;

    @Column(name = "cleanup_claimed_at")
    private Instant cleanupClaimedAt;

    @Column(name = "cleanup_review_required", nullable = false, columnDefinition = "boolean not null default false")
    private boolean cleanupReviewRequired;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** JPA-only constructor; normal token rows use the explicit constructor below. */
    protected AccountVerificationToken() {
    }

    /** Creates a verification record; callers persist only the hash, never the raw token. */
    public AccountVerificationToken(String oktaUserId, String tokenHash, Instant expiresAt, Instant now) {
        this.oktaUserId = oktaUserId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.resendWindowStartedAt = now;
        this.resendCount = 0;
        this.createdAt = now;
    }

    /** Supplies a creation time when a persistence caller did not set one explicitly. */
    @PrePersist
    void setCreatedAtIfMissing() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /** Database-generated row identifier. */
    public Long getId() { return id; }
    /** Okta account associated with this token record. */
    public String getOktaUserId() { return oktaUserId; }
    /** Persisted SHA-256 digest; the original token is never stored here. */
    public String getTokenHash() { return tokenHash; }
    /** Expiration instant after which the token cannot be claimed. */
    public Instant getExpiresAt() { return expiresAt; }
    /** Timestamp of an in-flight verification attempt, if present. */
    public Instant getClaimedAt() { return claimedAt; }
    /** Timestamp when verification completed successfully, if present. */
    public Instant getUsedAt() { return usedAt; }
    /** Start of the current fixed-window resend counter. */
    public Instant getResendWindowStartedAt() { return resendWindowStartedAt; }
    /** Number of resend requests accepted in the current window. */
    public int getResendCount() { return resendCount; }
    /** Timestamp when the final account-cleanup notice was sent. */
    public Instant getCleanupNoticeSentAt() { return cleanupNoticeSentAt; }
    /** Timestamp reserving this record for one account-cleanup worker. */
    public Instant getCleanupClaimedAt() { return cleanupClaimedAt; }
    /** Whether cleanup found a lifecycle state that requires manual review. */
    public boolean isCleanupReviewRequired() { return cleanupReviewRequired; }
    /** Original record-creation instant used to enforce account retention. */
    public Instant getCreatedAt() { return createdAt; }

    /** Rotates the token in this row and updates the fixed-window resend count. */
    public void rotate(String tokenHash, Instant expiresAt, Instant now, boolean resetResendWindow) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.claimedAt = null;
        this.usedAt = null;
        this.cleanupNoticeSentAt = null;
        if (resetResendWindow) {
            this.resendWindowStartedAt = now;
            this.resendCount = 1;
        } else {
            this.resendCount++;
        }
    }

    /** Marks a valid token as being processed so concurrent submissions cannot use it. */
    public void claim(Instant now) { this.claimedAt = now; }
    /** Releases a verification claim after an activation attempt fails. */
    public void releaseClaim() { this.claimedAt = null; }
    /** Marks verification complete and clears its in-flight claim. */
    public void markUsed(Instant now) { this.usedAt = now; this.claimedAt = null; }
    /** Records successful sending of the final cleanup notice for this account. */
    public void markCleanupNoticeSent(Instant now) { this.cleanupNoticeSentAt = now; }
    /** Releases a cleanup claim so a failed cleanup attempt can be retried. */
    public void releaseCleanupClaim() { this.cleanupClaimedAt = null; }
    /** Holds an unexpected lifecycle state for manual review instead of deleting it. */
    public void markCleanupReviewRequired() { this.cleanupReviewRequired = true; this.cleanupClaimedAt = null; }

}
