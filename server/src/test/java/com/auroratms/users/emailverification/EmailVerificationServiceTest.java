package com.auroratms.users.emailverification;

import com.auroratms.AbstractServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class EmailVerificationServiceTest extends AbstractServiceTest {

    @Autowired
    private EmailVerificationService service;

    @Autowired
    private AccountVerificationTokenRepository repository;

    @Test
    void createPersistsOnlyHashAndExpiresInOneHour() {
        String userId = testUserId();
        Instant before = Instant.now();

        EmailVerificationService.TokenIssue issue = service.createForNewUser(userId);

        AccountVerificationToken stored = repository.findByOktaUserId(userId).orElseThrow();
        assertNotEquals(issue.token(), stored.getTokenHash());
        assertEquals(service.hashToken(issue.token()), stored.getTokenHash());
        assertTrue(stored.getExpiresAt().isAfter(before.plus(Duration.ofMinutes(59))));
        assertTrue(stored.getExpiresAt().isBefore(before.plus(Duration.ofMinutes(61))));
        assertEquals(0, stored.getResendCount());
    }

    @Test
    void resendReplacesPreviousTokenInSameRecord() {
        String userId = testUserId();
        EmailVerificationService.TokenIssue original = service.createForNewUser(userId);

        EmailVerificationService.TokenIssue resent = service.regenerateForResend(userId);

        AccountVerificationToken stored = repository.findByOktaUserId(userId).orElseThrow();
        assertNotEquals(original.token(), resent.token());
        assertNotEquals(service.hashToken(original.token()), stored.getTokenHash());
        assertTrue(repository.findByTokenHash(service.hashToken(original.token())).isEmpty());
        assertEquals(service.hashToken(resent.token()), stored.getTokenHash());
        assertEquals(1, stored.getResendCount());
    }

    @Test
    void resendLimitAllowsTwoRequestsThenRejectsNext() {
        String userId = testUserId();
        service.createForNewUser(userId);

        for (int i = 0; i < 2; i++) {
            assertDoesNotThrow(() -> service.regenerateForResend(userId));
        }
        assertThrows(EmailVerificationService.TooManyResendsException.class,
                () -> service.regenerateForResend(userId));
        assertEquals(2, repository.findByOktaUserId(userId).orElseThrow().getResendCount());
    }

    @Test
    void cleanupCandidateIsClaimedOnlyAfterAccountRetentionAndCanBeRemoved() {
        String recentUserId = testUserId();
        service.createForNewUser(recentUserId);
        assertTrue(service.findCleanupCandidates(Duration.ofDays(30)).stream()
                .noneMatch(candidate -> candidate.oktaUserId().equals(recentUserId)));

        String abandonedUserId = testUserId();
        Instant createdAt = Instant.now().minus(Duration.ofDays(31));
        AccountVerificationToken abandonedToken = new AccountVerificationToken(
                abandonedUserId, service.hashToken(UUID.randomUUID().toString()), createdAt, createdAt);
        repository.saveAndFlush(abandonedToken);

        assertTrue(service.findCleanupCandidates(Duration.ofDays(30)).stream()
                .anyMatch(candidate -> candidate.oktaUserId().equals(abandonedUserId)));
        assertTrue(service.claimForCleanup(abandonedUserId, Duration.ofDays(30)).isPresent());

        service.deleteRecord(abandonedUserId);
        assertTrue(repository.findByOktaUserId(abandonedUserId).isEmpty());
    }

    @Test
    void accountMarkedForCleanupReviewIsNotDeletedAsAnExpiredToken() {
        String userId = testUserId();
        Instant createdAt = Instant.now().minus(Duration.ofDays(31));
        AccountVerificationToken token = new AccountVerificationToken(
                userId, service.hashToken(UUID.randomUUID().toString()), createdAt, createdAt);
        repository.saveAndFlush(token);

        service.markCleanupReviewRequired(userId);

        assertTrue(service.findCleanupCandidates(Duration.ofDays(30)).stream()
                .noneMatch(candidate -> candidate.oktaUserId().equals(userId)));
        assertEquals(0, service.deleteExpiredUnclaimedTokens());
        assertTrue(repository.findByOktaUserId(userId).isPresent());
    }

    private String testUserId() {
        return "test-verification-" + UUID.randomUUID();
    }
}
