package com.auroratms.users.emailverification.cleanupjob;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.users.emailverification.EmailVerificationService;
import com.auroratms.utils.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AccountVerificationCleanupJobTest {

    private EmailVerificationService emailVerificationService;
    private UserProfileService userProfileService;
    private EmailService emailService;
    private AccountVerificationCleanupJob job;

    @BeforeEach
    void setUp() {
        emailVerificationService = mock(EmailVerificationService.class);
        userProfileService = mock(UserProfileService.class);
        emailService = mock(EmailService.class);
        job = new AccountVerificationCleanupJob();
        ReflectionTestUtils.setField(job, "emailVerificationService", emailVerificationService);
        ReflectionTestUtils.setField(job, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(job, "emailService", emailService);
        ReflectionTestUtils.setField(job, "accountRetention", Duration.ofDays(30));
        ReflectionTestUtils.setField(job, "stagedAccountRetention", Duration.ofHours(1));
        ReflectionTestUtils.setField(job, "consumedRecordRetention", Duration.ofDays(30));
    }

    @Test
    void deletesOldStagedAccountOnlyAfterSendingNotice() throws Exception {
        EmailVerificationService.CleanupCandidate candidate = candidate("okta-user", null);
        UserProfile profile = profile("STAGED", "new-user@example.com");
        when(emailVerificationService.findCleanupCandidates(Duration.ofHours(1))).thenReturn(List.of(candidate));
        when(emailVerificationService.claimForCleanup("okta-user", Duration.ofHours(1))).thenReturn(Optional.of(candidate));
        when(userProfileService.getCurrentProfile("okta-user")).thenReturn(profile);

        job.execute(mock(JobExecutionContext.class));

        var order = inOrder(emailService, emailVerificationService, userProfileService);
        order.verify(emailService).sendMessageUsingThymeleafTemplate(eq("new-user@example.com"), isNull(),
                eq("Your TTAurora account was not completed"), eq("email-verification/account-cleanup-notice"), anyMap());
        order.verify(emailVerificationService).markCleanupNoticeSent("okta-user");
        order.verify(userProfileService).deleteProfile("okta-user");
        order.verify(emailVerificationService).deleteRecord("okta-user");
    }

    @Test
    void doesNotContactOktaForRegistrationsStillWithinRetentionPeriod() throws Exception {
        when(emailVerificationService.findCleanupCandidates(Duration.ofHours(1))).thenReturn(List.of());

        job.execute(mock(JobExecutionContext.class));

        verifyNoInteractions(userProfileService, emailService);
        verify(emailVerificationService).deleteExpiredUnclaimedTokens();
        verify(emailVerificationService).deleteOldConsumedRecords(Duration.ofDays(30));
    }

    @Test
    void protectedAliasesAreNeverNotifiedOrDeleted() throws Exception {
        EmailVerificationService.CleanupCandidate candidate = candidate("okta-user", null);
        when(emailVerificationService.findCleanupCandidates(Duration.ofHours(1))).thenReturn(List.of(candidate));
        when(emailVerificationService.claimForCleanup("okta-user", Duration.ofHours(1))).thenReturn(Optional.of(candidate));
        when(userProfileService.getCurrentProfile("okta-user"))
                .thenReturn(profile("STAGED", "swaveklorenc+td@gmail.com"));

        job.execute(mock(JobExecutionContext.class));

        verify(emailService, never()).sendMessageUsingThymeleafTemplate(anyString(), any(), anyString(), anyString(), anyMap());
        verify(userProfileService, never()).deleteProfile(anyString());
        verify(emailVerificationService).releaseCleanupClaim("okta-user");
    }

    @Test
    void activeCompleteUserIsPreservedAndVerificationRecordIsReconciled() throws Exception {
        EmailVerificationService.CleanupCandidate candidate = candidate("okta-user", null);
        when(emailVerificationService.findCleanupCandidates(Duration.ofHours(1))).thenReturn(List.of(candidate));
        when(emailVerificationService.claimForCleanup("okta-user", Duration.ofHours(1))).thenReturn(Optional.of(candidate));
        when(userProfileService.getCurrentProfile("okta-user")).thenReturn(completeProfile());

        job.execute(mock(JobExecutionContext.class));

        verify(userProfileService, never()).deleteProfile(anyString());
        verify(emailService, never()).sendMessageUsingThymeleafTemplate(anyString(), any(), anyString(), anyString(), anyMap());
        verify(emailVerificationService).deleteRecord("okta-user");
    }

    @Test
    void activeIncompleteUserIsNotifiedAndDeleted() throws Exception {
        EmailVerificationService.CleanupCandidate candidate = candidate("okta-user", null);
        when(emailVerificationService.findCleanupCandidates(Duration.ofHours(1))).thenReturn(List.of(candidate));
        when(emailVerificationService.claimForCleanup("okta-user", Duration.ofHours(1))).thenReturn(Optional.of(candidate));
        when(userProfileService.getCurrentProfile("okta-user"))
                .thenReturn(profile("ACTIVE", "incomplete@example.com"));

        job.execute(mock(JobExecutionContext.class));

        verify(emailService).sendMessageUsingThymeleafTemplate(eq("incomplete@example.com"), isNull(),
                eq("Your TTAurora account was not completed"), eq("email-verification/account-cleanup-notice"), anyMap());
        verify(userProfileService).deleteProfile("okta-user");
        verify(emailVerificationService).deleteRecord("okta-user");
    }

    @Test
    void previouslyNotifiedCandidateIsDeletedWithoutSendingDuplicateNotice() throws Exception {
        EmailVerificationService.CleanupCandidate candidate = candidate("okta-user", Instant.now().minus(Duration.ofDays(1)));
        when(emailVerificationService.findCleanupCandidates(Duration.ofHours(1))).thenReturn(List.of(candidate));
        when(emailVerificationService.claimForCleanup("okta-user", Duration.ofHours(1))).thenReturn(Optional.of(candidate));
        when(userProfileService.getCurrentProfile("okta-user")).thenReturn(profile("STAGED", "new-user@example.com"));

        job.execute(mock(JobExecutionContext.class));

        verify(emailService, never()).sendMessageUsingThymeleafTemplate(anyString(), any(), anyString(), anyString(), anyMap());
        verify(userProfileService).deleteProfile("okta-user");
        verify(emailVerificationService).deleteRecord("okta-user");
    }

    @Test
    void rechecksStatusAfterNoticeAndPreservesProfileCompletedDuringCleanup() throws Exception {
        EmailVerificationService.CleanupCandidate candidate = candidate("okta-user", null);
        when(emailVerificationService.findCleanupCandidates(Duration.ofHours(1))).thenReturn(List.of(candidate));
        when(emailVerificationService.claimForCleanup("okta-user", Duration.ofHours(1))).thenReturn(Optional.of(candidate));
        when(userProfileService.getCurrentProfile("okta-user"))
                .thenReturn(profile("ACTIVE", "incomplete@example.com"), completeProfile());

        job.execute(mock(JobExecutionContext.class));

        verify(emailService).sendMessageUsingThymeleafTemplate(anyString(), any(), anyString(), anyString(), anyMap());
        verify(userProfileService, never()).deleteProfile(anyString());
        verify(emailVerificationService).deleteRecord("okta-user");
    }

    @Test
    void unexpectedOktaStatusIsRetainedForReview() throws Exception {
        EmailVerificationService.CleanupCandidate candidate = candidate("okta-user", null);
        when(emailVerificationService.findCleanupCandidates(Duration.ofHours(1))).thenReturn(List.of(candidate));
        when(emailVerificationService.claimForCleanup("okta-user", Duration.ofHours(1))).thenReturn(Optional.of(candidate));
        when(userProfileService.getCurrentProfile("okta-user"))
                .thenReturn(profile("SUSPENDED", "new-user@example.com"));

        job.execute(mock(JobExecutionContext.class));

        verify(emailService, never()).sendMessageUsingThymeleafTemplate(anyString(), any(), anyString(), anyString(), anyMap());
        verify(userProfileService, never()).deleteProfile(anyString());
        verify(emailVerificationService, never()).deleteRecord(anyString());
        verify(emailVerificationService).markCleanupReviewRequired("okta-user");
    }

    @Test
    void failedNoticeDoesNotDeleteAccountAndReleasesCleanupClaim() throws Exception {
        EmailVerificationService.CleanupCandidate candidate = candidate("okta-user", null);
        when(emailVerificationService.findCleanupCandidates(Duration.ofHours(1))).thenReturn(List.of(candidate));
        when(emailVerificationService.claimForCleanup("okta-user", Duration.ofHours(1))).thenReturn(Optional.of(candidate));
        when(userProfileService.getCurrentProfile("okta-user")).thenReturn(profile("STAGED", "new-user@example.com"));
        doThrow(new jakarta.mail.MessagingException("smtp rejected message"))
                .when(emailService).sendMessageUsingThymeleafTemplate(anyString(), any(), anyString(), anyString(), anyMap());

        job.execute(mock(JobExecutionContext.class));

        verify(userProfileService, never()).deleteProfile(anyString());
        verify(emailVerificationService).releaseCleanupClaim("okta-user");
    }

    private EmailVerificationService.CleanupCandidate candidate(String userId, Instant noticeSentAt) {
        Instant createdAt = Instant.now().minus(Duration.ofDays(31));
        return new EmailVerificationService.CleanupCandidate(userId, noticeSentAt, createdAt,
                Instant.now().minus(Duration.ofHours(1)));
    }

    private UserProfile profile(String status, String email) {
        UserProfile profile = new UserProfile();
        profile.setUserId("okta-user");
        profile.setUserStatus(status);
        profile.setEmail(email);
        profile.setFirstName("New");
        return profile;
    }

    private UserProfile completeProfile() {
        UserProfile profile = profile("ACTIVE", "complete@example.com");
        profile.setLastName("User");
        profile.setMobilePhone("555-1111");
        profile.setStreetAddress("1 Test St");
        profile.setCity("Chicago");
        profile.setState("IL");
        profile.setZipCode("60601");
        profile.setCountryCode("US");
        profile.setGender("Male");
        profile.setDateOfBirth(new java.util.Date());
        return profile;
    }
}
