# Registration Email Verification: Implementation Plan

## Purpose

Turn the [registration email verification spec](registration-email-verification-sdd.md) into ordered implementation increments. This plan covers the public self-registration flow and preserves Okta as the identity provider.

## Agreed implementation boundaries

- `EmailVerificationService` owns verification-token persistence, hashing, issue/rotation, atomic claim/consumption, resend limits, and token-record cleanup. It does not send email or call Okta.
- `OktaUserLifecycleService` owns Okta Management API lookups and lifecycle operations. The controller coordinates the email service, `EmailVerificationService`, and `OktaUserLifecycleService`.
- `UsersController` remains the HTTP boundary. Registration orchestration can be in the controller initially or a separate workflow service; it coordinates Okta, the token service, and `EmailService`.
- Token persistence commits before email delivery. If delivery fails, the user can request a resend, which rotates the token. No transactional outbox is required for the first implementation.
- Token lifetime defaults to one hour and is configurable. An hourly scheduled cleanup removes expired and sufficiently old consumed rows.
- Okta user activation is performed only after token validation, with `sendEmail=false`; the current activate-then-suspend flow is removed for public registration.

## Phase 0 — Confirm Okta lifecycle behavior

1. In a non-production Okta org, create a user with the current password payload and `activate=false`.
2. Confirm activation of that staged user using `POST /api/v1/users/{id}/lifecycle/activate?sendEmail=false` does not send Okta mail and results in an account that can authenticate with the supplied password.
3. Confirm the account remains unable to authenticate while staged and that lifecycle behavior matches the production org's policies.
4. Record any policy-specific changes required before rollout.

**Completion gate:** the staged-user lifecycle and no-email activation behavior are verified for the target Okta configuration.

## Phase 1 — Persistence and token service

1. Add a Liquibase changeset for `account_verification_token`, including a unique token hash, Okta user ID, expiry, claim timestamp, used timestamp, and created timestamp. Follow the repository's existing database naming and migration conventions.
2. Add a JPA entity and Spring Data repository with one current verification row per Okta user. Include a unique current token hash, resend window start/count fields, and repository operations for replacing the token, atomically checking/updating the resend window, claiming a valid token, marking it consumed, releasing a claim after definite activation failure, atomically claiming an old registration for cleanup, recording notice/review state, and purging eligible rows.
3. Add `EmailVerificationService` with transactional operations to issue/rotate a one-hour token, hash tokens, atomically claim/consume a valid token, enforce resend limits, and delete eligible verification rows. Generate 32 bytes with `SecureRandom`, encode URL-safe Base64 without padding, persist only the SHA-256 hash, and return the raw token only to the caller that needs to construct the email.
4. Keep database transactions short. Commit token claims before calling Okta and commit token creation/rotation before sending email; never hold a transaction open across network operations.
5. Ensure one request wins the conditional claim update. Add a stale-claim recovery rule so process crashes do not strand tokens indefinitely.
6. Add Spring/JPA service tests for token creation, hash-only persistence, one-row-per-user rotation, one-hour expiry, resend-window/cap updates, and cleanup eligibility. Following the existing integration-test convention, tests that require the Spring Boot context should extend `com.auroratms.AbstractServiceTest` (see `EmailCampaignServiceTest`).

**Completion gate:** one `EmailVerificationService` owns the verification record lifecycle and atomically claims/consumes tokens, with no email or Okta responsibilities inside it.

## Phase 2 — Registration email and verification endpoint

1. Update `registerUser` to create the Okta user with `activate=false`, issue a token through `EmailVerificationService`, and send the custom verification email through `EmailService`.
2. Remove the activate-then-suspend sequence from public self-registration. Continue sending the administrator notification.
3. Add an application-owned Thymeleaf verification template under `server/src/main/resources/mail-templates/email-verification/`, reusing the existing activation email's user-facing text and purple button styling. Do not mention Okta or any identity provider in the email. Include a note telling users who found the message in Spam to move it to their inbox before clicking **Activate Your Account**, because some email services block links in Spam. Render the full activation URL as visible, clickable text immediately below the button, and tell users to copy and paste it into their browser if the button does not work. Set the button and visible URL to the same `client.host.url + /ui/home/userwelcome` destination with URL-encoded `email`, raw one-time `token`, and `firstName` parameters; local development uses `https://lorencmini:4200` and production uses `https://ttaurora.com:443`. State the one-hour expiry and ensure the user-welcome route submits the token to the verification API. Do not log the link or token.
4. Replace `validateEmail`'s profile `secondEmail` comparison and unsuspend call with token-based validation. Resolve the Okta user ID from the claimed token record, activate that user with `sendEmail=false`, and consume the token on success.
5. On definite Okta activation failure, release the claim for retry. Handle a retry where Okta has already activated the account without permitting an unrelated or invalid token to activate it.
6. Return user-facing success or invalid/expired/used states without exposing account existence or token internals.
7. Configure `spring.mail.properties.mail.smtp.from: ${spring.mail.username}` in the mail-sending application configurations so the SMTP envelope sender uses the Vault-provided account. Do not set a `Return-Path` message header directly; the receiving mail server derives it from the envelope sender.
8. Send a verification message to a controlled mailbox and inspect its original headers to verify the expected `Return-Path`.
9. Send a test message to an address that produces a hard bounce. Inspect the bounce report in Gmail and record the rejection reason and any limits in the diagnostic detail provided.

**Completion gate:** a registrant receives the custom email, remains staged until following its valid link, and can authenticate after successful activation without an Okta activation email.

## Phase 3 — Resend flow and confirmation page

1. Update `client/src/app/user/register/register.component.ts` and `register.component.html`, which currently implement the registration completion message and screen. Announce the custom verification email and add a **Resend email** button for successful self-service registrations only. Reuse the entered email address, disable the button while the request is in progress, and display generic confirmation advising users to wait after repeated requests.
2. After the user submits two resend requests for the current registration, replace the **Resend email** button with the message “The limit has been reached. Want to start over with a different email?” and place a **Start Over** button below it. The button navigates to the Create Account screen and clears email and password fields so the user can register with a different email. Leave the original unverified Okta account staged; the hourly cleanup job can delete it once it is at least one hour old and its latest verification link has expired and meets the normal cleanup rules, including protected-account exclusions and final-notification handling. The start-over action does not activate, delete, or change the old account's email. The new registration goes through the normal registration and duplicate-account checks. Keep this UI decision client-side so the generic resend endpoint does not reveal account-specific state.
3. Add `POST /api/users/resend-verification`, accepting the registered email address. Look up the Okta user server-side and resolve its verification row by Okta user ID. Atomically permit at most two resends in a fixed five-minute window starting at the initial email. After the cap, suppress sending and token rotation but return the same generic accepted response as unknown or ineligible addresses. Rotate the token through `EmailVerificationService`, and send mail after the token transaction commits.
4. Keep unknown email addresses generic and do not expose user or Okta account details. Do not send email or alter active users.
5. If mail delivery fails after rotation, allow a later resend subject to limits. Do not wrap email sending in the token database transaction.
6. Apply a configurable source-level limit in shared storage, including for unknown email addresses. The client may disable the resend action during an in-flight request, but server-side atomic limits are authoritative.

**Completion gate:** users can request at most two replacement messages per five-minute window, older links stop working, concurrent requests cannot exceed the per-registration resend cap, and the public endpoint does not expose account details. After two submitted resend attempts, users can return to Create Account and register with a different email while the original staged account remains subject to age-based cleanup.

## Phase 4 — Hourly cleanup and operations

1. Add the cleanup Quartz `Job` and `JobConfig` under `com.auroratms.users.emailverification.cleanupjob`, following `DailyUpdateRatingsJobConfig`. Schedule it at the start of every hour in UTC.
2. Have the job clean eligible abandoned accounts before purging expired verification records. Delete expired records and consumed records older than the agreed retention (initial proposal: 30 days); do not remove active token claims or records marked for manual cleanup review.
3. Keep token expiry enforcement in the validation/claim query; cleanup schedule is not relied on for security.
4. In the hourly process, identify staged self-registered accounts at least one hour old whose latest verification token has expired. A resent token extends eligibility until its new expiry, so cleanup never deletes an account while its latest link is valid. Keep active incomplete accounts on a separate configurable retention period (initial proposal: 30 days from registration).
   Keep both retention periods configurable so tests can exercise cleanup promptly. Cleanup-test alias tags do not bypass normal age or lifecycle-status requirements.
5. Before deletion, fetch the current Okta user and inspect its full email address and lifecycle status. Never delete `swaveklorenc+td@gmail.com`, `swaveklorenc@gmail.com`, or `swaveklorenc@yahoo.com`. Preserve other `swaveklorenc+<tag>@gmail.com` test accounts by default, except reserved cleanup-test tags matching `nonokta` followed by digits (for example `nonokta1`, `nonokta2`), which remain eligible for normal cleanup. Do not strip or normalize away the plus tag. Apply exclusions in every environment.
6. For other eligible users, an old `STAGED` status means unverified registration; profile completion is not expected because staged users cannot sign in. For an `ACTIVE` user, apply the existing profile-completion check (`UsersController.isProfileComplete`, or a shared equivalent) against the current Okta profile. Preserve complete profiles; an old active account with an incomplete profile is an abandoned-onboarding candidate.
7. Atomically claim each qualifying registration before external calls. Send a final notification, persist that it was sent, then delete the Okta account. If email delivery fails, release the cleanup claim and retry later without deleting the account. Make notification and deletion attempts idempotent and do not hold a database transaction open across either operation.
8. Preserve active users with complete profiles, including users for whom Okta activation succeeded but recording token consumption failed; reconcile their verification record as verified. Do not automatically delete suspended or otherwise unexpected lifecycle states; log them for review unless a separate policy is approved.
9. After successful Okta deletion, remove or mark that user's verification records as cleaned up. Log aggregate mail/activation/cleanup outcomes without token material. Define monitoring for mail failures and old staged/incomplete accounts.

**Completion gate:** cleanup runs hourly, removes eligible token records, notifies and deletes staged accounts at least one hour old only after the latest link expires, and deletes active incomplete-onboarding accounts only after their longer retention period, never deletes the three protected addresses, preserves active users with complete profiles and ordinary `swaveklorenc+<tag>@gmail.com` test accounts, while allowing explicitly named `nonokta<digits>` cleanup-test accounts to exercise normal cleanup, respects claim/retention rules, and reports failures for retry or review.

## Phase 5 — Rollout

1. Deploy the Liquibase changeset before enabling the new endpoint flow.
2. Verify the registration and resend links use the configured public base URL and HTTPS in production.
3. Exercise successful verification, expiry, replay, concurrent token submission, resend rotation, mail failure/retry, Okta failure/retry, and cleanup behavior in a non-production environment.
4. Enable the new registration path and monitor Okta activation calls, custom mail delivery, resend volume, and unverified staged accounts.

## Key files and areas

- Registration and verification endpoints: `server/src/main/java/com/auroratms/users/UsersController.java`.
- Existing mail abstraction and Thymeleaf templates: `server/src/main/java/com/auroratms/utils/EmailService.java` and the corresponding email template resources.
- Verification persistence, repository, `EmailVerificationService`, and Okta lifecycle client: `server/src/main/java/com/auroratms/users/emailverfication/`.
- Liquibase changelogs/configuration: `server/src/main/resources/`.
- Registration completion/resend UI: `client/src/app/user/register/register.component.ts` and `register.component.html`.
- Verification link handling: `client/src/app/home/user-welcome/` (route `/ui/home/userwelcome`).

## Risks and decisions to settle during implementation

- Confirm the exact `sendEmail=false` semantics and password/lifecycle policy against the target Okta org before coding the activation transition.
- Choose a stable public route for the email link and ensure client routing can show expired/used-link guidance.
- Define precise stale-claim timeout and recovery handling for the Okta call boundary.
- Define account-level and source-level resend rate-limit storage/mechanism; do not rely only on in-memory counters in a multi-instance deployment.
- Decide how long consumed token rows are retained and how long unverified staged Okta users remain before separate account cleanup.
- Confirm the final-notice wording and account-retention period before enabling staged/incomplete-account deletion in production.
