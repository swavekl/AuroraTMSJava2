# Registration Email Verification: Task List

Tracks the work in the [implementation plan](registration-email-verification-implementation-plan.md) and [spec](registration-email-verification-sdd.md).

## Phase 0 — Confirm Okta lifecycle behavior

- [ ] Create a staged test user in a non-production Okta org using the current password payload and `activate=false`.
- [ ] Activate the staged user with `sendEmail=false` and confirm Okta sends no activation email.
- [ ] Confirm the user cannot authenticate while staged and can authenticate after activation with the supplied password.
- [ ] Confirm lifecycle behavior against the target org's policies and record any required changes.

## Phase 1 — Token persistence and service

- [ ] Review existing Liquibase changelog conventions and database column naming.
- [ ] Add the `account_verification_token` schema fields for one row per Okta user, unique token hash, expiry, claim/use timestamps, resend window start/count, cleanup claim/notice/review state, and creation timestamp.
- [ ] Add the JPA entity and Spring Data repository.
- [ ] Implement repository operation to invalidate/replace a prior unused token for an Okta user.
- [ ] Implement an atomic conditional claim operation for an unexpired, unused token hash.
- [ ] Implement repository operations to mark a token used, release a claim after activation failure, and find records for cleanup.
- [ ] Implement `EmailVerificationService` to generate 32-byte secure URL-safe tokens, persist only SHA-256 hashes, set a configurable one-hour expiry, rotate tokens, atomically claim/consume them, enforce resend limits, and clean up eligible token records. Keep Okta and email delivery outside this service.
- [ ] Make token regeneration replace the previous unused token in a transaction.
- [ ] Implement deletion of expired token records and sufficiently old consumed records.
- [ ] Define and implement stale-claim timeout/recovery behavior.
- [ ] Keep token service transactions limited to database work; do not call Okta or send email inside them.
- [ ] Add Spring/JPA tests for `EmailVerificationService`: token creation, SHA-256 hash-only persistence, one-row-per-user rotation invalidating the prior token, one-hour expiry, resend-window reset and two-resend cap, and cleanup selection/deletion.
- [ ] Extend `com.auroratms.AbstractServiceTest` for tests requiring the Spring Boot/JPA context, following `EmailCampaignServiceTest`.
- [ ] Verify failed/limited resend attempts do not rotate the current token or send work to email; keep mail and Okta external calls out of token-service tests.

## Phase 2 — Registration and verification

- [ ] Update `client/src/app/user/register/register.component.ts` and `register.component.html`, which currently announce the Okta activation email after registration.
- [ ] Add a custom Thymeleaf verification email template under `server/src/main/resources/mail-templates/email-verification/` with the configured public HTTPS URL and one-hour expiry copy.
- [ ] Reuse the existing activation email's user-facing text and styling; do not mention Okta or any identity provider in the new email. Include the welcome heading, personalized greeting, email registration explanation, administrator contact, one-hour expiry instructions, centered purple **Activate Your Account** button, and TTAurora Administrator sign-off.
- [ ] Inject the `client.host.url` application property into the Thymeleaf model and build the button URL from it plus `/ui/home/userwelcome`, with URL-encoded `email`, raw one-time verification `token`, and `firstName` query parameters. Confirm local uses `https://lorencmini:4200` and production uses `https://ttaurora.com:443`.
- [ ] Ensure the user-welcome route submits the token to the verification API; do not put the old Okta `secondEmail` value in the link.
- [ ] Set `spring.mail.properties.mail.smtp.from: ${spring.mail.username}` so the SMTP envelope sender (and bounce address) uses the Vault-provided Gmail account.
- [ ] Send a verification email to a controlled mailbox and inspect its original headers to verify that the delivered message has the expected `Return-Path`.
- [ ] Send a test message to an address that produces a hard bounce; inspect the bounce report received in Gmail and record the failure reason and any limits in the information Gmail provides.
- [ ] Update `registerUser` to leave newly created users `STAGED`; remove its activate-then-suspend sequence.
- [ ] Issue a verification token through `EmailVerificationService` and send the custom email through `EmailService` after the token transaction commits.
- [ ] Preserve the administrator registration notification.
- [ ] Ensure registration responses and logs never contain the raw token or verification URL.
- [ ] Replace `validateEmail`'s `profile.secondEmail`/unsuspend logic with token hash lookup and atomic claim.
- [ ] Activate the token-associated Okta user with `sendEmail=false` only after valid token claim.
- [ ] Mark the token consumed after activation succeeds; release the claim after a definite activation failure.
- [ ] Handle retries where Okta activation succeeded but final token consumption did not.
- [ ] Provide user-facing success, invalid, expired, and already-used link states without account enumeration.

## Phase 3 — Resend verification

- [ ] Replace the registration success message in `register.component.ts` with the custom verification-email announcement.
- [ ] Add a **Resend email** button to `register.component.html`, shown only after successful self-service registration (not tournament-director registration).
- [ ] Implement resend handling in `register.component.ts` using the entered email; disable the button while submitting and show generic confirmation advising users to wait after repeated requests.
- [ ] Add `POST /api/users/resend-verification` accepting the registered email address and look up the Okta user server-side.
- [ ] Return a generic response for unknown or ineligible email addresses without exposing account details.
- [ ] Atomically permit at most two resends in a fixed five-minute window from the initial verification email; after the cap suppress token rotation and sending but return the same generic accepted response as unknown/ineligible accounts.
- [ ] Set the configured default per-account resend limit to two and cover the limit in service tests.
- [ ] For eligible staged users, rotate the token in the same row and send the email after committing the token transaction.
- [ ] Ensure a resend invalidates the prior unused link for that account.
- [ ] Apply source-level rate limiting in shared storage, including for unknown email addresses.
- [ ] Disable the resend control during an in-flight request and show generic confirmation without revealing account-specific rate-limit state.
- [ ] Handle email delivery failure so the user can retry within the rate limits.
- [ ] After two submitted resend attempts, replace the **Resend email** button with “The limit has been reached. Want to start over with a different email?” and show a **Start Over** button below it.
- [ ] Clear the email and password fields when starting over; allow the user to submit a new registration with another email through the normal registration and duplicate-account checks.
- [ ] Leave the original unverified Okta account staged; document and verify that the hourly cleanup job deletes it only when it is at least one hour old and its latest verification link has expired, with normal protected-account exclusions and final-notification handling. Starting over must not immediately activate, delete, or change its email.
- [ ] Add a cleanup-job test proving an abandoned staged account is retained before the configured retention period and deleted after the retention period has elapsed, provided it passes the normal eligibility and protected-account checks.
- [ ] Add cleanup tests for eligible `STAGED` and incomplete `ACTIVE` accounts; preserved complete `ACTIVE`, unexpected lifecycle, and protected accounts; reserved `nonokta<digits>` aliases; and no deletion when final notice delivery fails.
- [ ] Configure the hourly cleanup with Quartz `Job`/`JobConfig` classes in `com.auroratms.users.emailverification.cleanupjob`, scheduled at the start of every UTC hour.
- [ ] Add client tests for showing the start-over action after two resend submissions and navigating to Create Account with email/password cleared.

## Phase 4 — Hourly token and account cleanup

- [ ] Confirm whether Spring scheduling is already enabled; enable it if necessary.
- [ ] Configure the hourly cleanup schedule and token/consumed-record retention values.
- [ ] Delete expired token records and consumed records beyond the retention period without deleting live claims.
- [ ] Configure staged-account retention at one hour, and require the latest token to be expired before deletion. Keep active incomplete-account retention separate (initial proposal: 30 days from registration).
- [ ] Identify only self-registered accounts managed by this verification flow for account cleanup.
- [ ] For each eligible account, fetch its current Okta user and check lifecycle status immediately before action.
- [ ] Never delete `swaveklorenc+td@gmail.com`, `swaveklorenc@gmail.com`, or `swaveklorenc@yahoo.com` in any environment.
- [ ] Preserve other `swaveklorenc+<tag>@gmail.com` test accounts by default, without notification or deletion; reserve tags matching `nonokta` plus digits (for example `nonokta1`, `nonokta2`) as cleanup-test accounts eligible for normal cleanup rules.
- [ ] Do not strip/normalize the `+` tag when checking email addresses.
- [ ] Treat sufficiently old `STAGED` accounts as unverified; profile completion is not expected for staged users.
- [ ] For sufficiently old `ACTIVE` accounts, apply the existing `isProfileComplete` rule to the current Okta profile.
- [ ] Preserve active users with complete profiles, including activation/token-recording partial failures.
- [ ] Add cleanup tests proving all three protected addresses and ordinary plus-alias test accounts are skipped, while `swaveklorenc+nonokta1@gmail.com` / `swaveklorenc+nonokta2@gmail.com` can be selected when they meet normal criteria.
- [ ] Keep staged-account retention configurable at one hour and active incomplete-account retention separately configurable; cleanup-test aliases still follow the same age and status criteria.
- [ ] Leave suspended or unexpected lifecycle states for review.
- [ ] Send the final unverified-registration/incomplete-onboarding notice before deleting an eligible account.
- [ ] Retain and retry cleanup work if notification fails; do not delete without the required notice.
- [ ] Delete the eligible Okta account using the Management API and make retries idempotent.
- [ ] Remove or mark verification records after successful Okta deletion.
- [ ] Log aggregate cleanup and delivery results without logging raw tokens; expose failure signals for operations.

## Phase 5 — Rollout

- [ ] Confirm production verification links use the correct public HTTPS base URL.
- [ ] Apply the database changeset before enabling the new registration path.
- [ ] Verify registration, successful activation, expiry, replay, concurrent verification, resend rotation, mail failure, Okta failure, stale claim recovery, and cleanup in a non-production environment.
- [ ] Enable the new flow and monitor custom email delivery, Okta activation/deletion outcomes, resend volume, and old staged/incomplete accounts.
- [ ] Confirm the production retention period and final cleanup email wording before enabling account deletion.

## Decisions to resolve during implementation

- [ ] Confirm `sendEmail=false` behavior and password policy in the target Okta org.
- [ ] Select the final client verification route and expired-link recovery UX.
- [ ] Set exact stale-claim timeout and retry semantics.
- [ ] Select a multi-instance-safe rate-limit implementation.
- [ ] Confirm consumed-token retention, account-retention period, and unexpected Okta status handling.
