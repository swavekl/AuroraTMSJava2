# Registration Email Verification: Spec Driven Development

## Status

Proposed. This spec replaces Okta's registration activation email with an application managed email verification flow. It does not replace Okta as the identity provider.

## Problem

Public registration creates an Okta user through the Management API. The current flow creates the user with `activate=false`, activates it through Okta's lifecycle endpoint, and then suspends it until the submitted email address is verified. Okta sends its activation email during activation. The custom verification logic stores a token-like value in the Okta profile and unsuspends the account after validation.

The desired flow sends a verification email through the application's email service, keeps the Okta account staged until verification, and activates it without asking Okta to send its own activation email.

## Goals

- Keep Okta as the user directory and authentication provider.
- Send registration verification mail using the application's existing email service and templates.
- Keep a newly registered user unable to authenticate until their email is verified.
- Use a random, expiring, single-use verification token.
- Store only a one-way hash of the token in the application database.
- Ensure concurrent verification requests cannot consume the same token twice.
- Provide a daily cleanup process for expired and consumed verification records.
- Keep tournament-director-created users' existing registration behavior unless explicitly changed as part of implementation.

## Out of scope

- Replacing Okta authentication, password storage, or user profile management.
- Changing marketing email delivery or configuration.
- Reworking forgotten-password or account recovery flows.
- Re-sending or migrating activation messages for existing users.

## Current behavior and relevant code

`UsersController.registerUser` creates the user with `POST /api/v1/users?activate=false`, calls `POST /api/v1/users/{id}/lifecycle/activate` without `sendEmail=false`, then suspends the account. This activation call triggers Okta's activation email. The current `validateEmail` handler looks up the account by email, compares a value stored in `profile.secondEmail`, and unsuspends it. The controller currently sends an administrator notification but does not show a custom verification email to the registrant.

The implementation uses the Okta Management API REST path `/api/v1`; the Java SDK dependency versions do not change that endpoint. The proposed activation behavior must be confirmed against the current Okta Users API documentation and the target org before rollout.

## Proposed registration flow

1. Validate registration input and create the Okta user with `activate=false`.
2. Generate 32 cryptographically secure random bytes and encode them with URL-safe Base64 without padding. Never derive the token from the email address, user ID, or a predictable identifier.
3. Compute SHA-256 over the raw token and store the hash, Okta user ID, expiry timestamp, and unused/unclaimed state in a new application table. Do not store the raw token in the database, Okta profile, or logs.
4. Email the registrant a link to the application's verification page with the raw token as a URL parameter. Use the existing `EmailService`/Thymeleaf mail path. The link must use the configured public application URL and HTTPS in production.
5. Return a registration response that does not include the token. Keep the existing administrator notification.
6. If sending mail fails after Okta user creation, preserve enough state to retry sending, or cleanly delete/disable the staged user and report a retryable failure. Do not activate the user as a fallback.

The token's validity period is configurable; the initial default is one hour. Re-requesting verification invalidates any prior outstanding token and issues a new one, subject to reasonable rate limiting.

## Verification email template and link

Add an application-owned Thymeleaf template under `server/src/main/resources/mail-templates/email-verification/`. Reuse the existing activation email's user-facing text and visual style as the source; do not mention Okta or any identity provider in the email, and do not modify the existing template. The message content is:

> **Welcome to TTAurora!**
>
> Hello `{firstName}`,
>
> You or someone else registered on the TTAurora website using this email `{email}`. If it was not you, please contact the TTAurora administrator.
>
> If it was you, please click the button below to verify your email and activate your account. Then, follow the on-screen instructions to complete the onboarding process. This link expires in 1 hour.
>
> Found this email in your spam folder? Move it to your inbox before clicking “Activate Your Account.” Some email services block links in spam.
>
> **Activate Your Account**
>
> Thank you,
>
> TTAurora Administrator

Render **Activate Your Account** as a centered purple button matching the existing Okta email style (white text, approximately `#673ab7`). Inject the `client.host.url` application property into the Thymeleaf template model as the client base URL. Its destination is that base URL plus `/ui/home/userwelcome`, with `email`, `token`, and `firstName` query parameters. The local value is `client.host.url: https://lorencmini:4200`; production uses `client.host.url: https://ttaurora.com:443`. Thus the URL shape is:

```text
https://lorencmini:4200/ui/home/userwelcome?email=...&token=...&firstName=...
```

Use the raw one-time verification token as `token`; do not use the old Okta `secondEmail` value. URL-encode every query value (for example, `+` in an email address becomes `%2B`). The `userwelcome` route must submit the token to the application's verification endpoint, which validates it and performs the Okta activation. The URL must be built from configuration so production uses its public HTTPS host, not the development hostname.

## Mail sender and bounce verification

Configure Spring Mail's SMTP envelope sender with `spring.mail.properties.mail.smtp.from: ${spring.mail.username}` in the application configurations used to send verification messages. The username is supplied from Vault. This directs SMTP bounce messages to the configured sending account; do not try to set `Return-Path` as a message header, because the receiving mail server adds that header based on the SMTP envelope sender.

Before enabling the flow in production, send a verification message to a controlled mailbox and inspect the original message headers to confirm the delivered message has the expected `Return-Path`. Then send a test message to an address that produces a hard bounce, inspect the bounce report in Gmail, and record the rejection reason and any limits in the diagnostic detail Gmail provides. A successful SMTP submission alone does not prove delivery.

## Resend verification flow

The current registration-completion message is implemented in `client/src/app/user/register/register.component.ts`, and its screen is rendered by `client/src/app/user/register/register.component.html`. Update these files for the new verification announcement and resend action. After successful public self-registration, the screen must say a verification email was sent and show a **Resend email** button. The button uses the email address already entered, so the user does not need to re-enter it. It is shown only for self-service registration, not the tournament-director registration path. Disable it while a resend is in progress and display the result, including the rate-limit response, in the completion message area. After the user has submitted two resend requests for this registration, replace the **Resend email** button with the message “The limit has been reached. Want to start over with a different email?” and show a **Start Over** button below the message.

The resend action submits the registered email address; the server looks up the corresponding Okta account and obtains its Okta user ID from Okta.

Add a public endpoint in `UsersController`, for example `POST /api/users/resend-verification`, accepting the registered email address. The endpoint flow is:

1. Normalize and validate the email address, look up the account through the server-side Okta API, then find the single verification record by the Okta user ID returned by Okta.
2. Atomically check and update the per-record resend window and count. The policy permits at most two resend requests in a fixed five-minute window starting with the initial verification email; the initial email is not counted as a resend. Once two resends have been accepted, suppress further sends until the window expires. Then reset the window and count.
3. For a rate-limited account, do not rotate the token or send email. Return the same generic accepted response used for unknown and ineligible addresses; do not reveal whether the account-specific limit was reached.
4. For an allowed resend, generate a fresh cryptographically random token, replace the token hash in the same row, set a new one-hour expiry, increment the resend count, and send the message through `EmailService` after the transaction commits.
5. Apply a separate per-source limit as well. Keep unknown email addresses generic, and do not expose user data or Okta identifiers. The email address is a lookup value, not proof of ownership; only a valid verification token activates the account.

The resend page action should disable repeated submissions while a request is in progress and explain that the newest link invalidates previous links. Its generic confirmation should say a message will be sent if the account needs verification and advise waiting before retrying after several requests. Do not provide an account-specific HTTP status or response when the per-account limit is reached. Apply per-source limits even when the email address is unknown, to reduce abuse. Configure the per-registration limit as two resends per five-minute fixed window.

After the user has submitted two resend requests from the registration completion screen, remove the **Resend email** button, show “The limit has been reached. Want to start over with a different email?”, and place a **Start Over** button below it. This action returns to the Create Account screen so the user can enter a different email address and password and submit a new registration. Clear the email and password fields on the new registration form; other fields may follow the existing form's normal reset behavior. Do not activate, delete, or change the email on the original unverified Okta account as part of this client action. Leave it staged; the daily account cleanup job can delete it once it reaches the configured retention age and meets the normal cleanup rules, including the protected-account exclusions and final-notification handling. The new registration must go through the normal registration and duplicate-account checks. Because the resend endpoint intentionally returns a generic response, the UI can reveal the start-over state after two submitted resend attempts without depending on an account-specific server response.

## Verification flow

The verification endpoint accepts only the raw token. It hashes the supplied token and atomically claims a matching record only if it is unexpired, unused, and not currently claimed. The server obtains the Okta user ID from that record; it must not select the account using an email or user ID supplied by the client.

After the claim has committed, call Okta:

```text
POST /api/v1/users/{oktaUserId}/lifecycle/activate?sendEmail=false
```

On success, mark the record consumed (`used_at`) and clear the claim. On a definite activation failure, release the claim so the user can retry. Claims must have a timeout/recovery rule so a process crash cannot permanently strand a valid token. Okta activation may be retried safely only after checking the user's lifecycle state and handling an already-active account as appropriate.

The response should communicate success or invalid/expired/already-used status without exposing whether an arbitrary email address has an account. Verification links and raw tokens must be excluded from application logs and analytics.

## Atomic token consumption

Use a conditional database update (or equivalent row lock) as the claim operation. For example:

```sql
UPDATE account_verification_token
SET claimed_at = CURRENT_TIMESTAMP(6)
WHERE token_hash = :tokenHash
  AND used_at IS NULL
  AND claimed_at IS NULL
  AND expires_at > CURRENT_TIMESTAMP(6);
```

Exactly one request may proceed when the affected row count is one. A zero-row update means the token is invalid, expired, consumed, or already claimed. Commit this claim before the network call to Okta; do not hold a database transaction open across the HTTP request. Finalize as consumed after successful activation. A stale-claim timeout must be documented and implemented so transient errors or process termination permit recovery without allowing two concurrent activations.

## Persistence

Add a Liquibase changeset for a table equivalent to:

```sql
CREATE TABLE account_verification_token (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    okta_user_id    VARCHAR(64) NOT NULL,
    token_hash      CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    expires_at      DATETIME(6) NOT NULL,
    claimed_at      DATETIME(6) NULL,
    used_at         DATETIME(6) NULL,
    resend_window_started_at DATETIME(6) NOT NULL,
    resend_count    INT NOT NULL DEFAULT 0,
    cleanup_notice_sent_at DATETIME(6) NULL,
    cleanup_claimed_at DATETIME(6) NULL,
    cleanup_review_required BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_account_verification_token_okta_user_id (okta_user_id),
    UNIQUE KEY uq_account_verification_token_token_hash (token_hash),
    KEY ix_account_verification_token_expires_at (expires_at)
);
```

Adapt types and syntax to the project's actual database and Liquibase conventions. The token hash is lowercase SHA-256 hex (64 characters). Keep one current verification row per Okta user; resending replaces the token hash and expiry in that row instead of inserting another token row. Do not add a foreign key to Okta, since Okta users are not in the application database. Retain consumed rows long enough for operational troubleshooting, then remove them during cleanup.

## Expiration and cleanup

Expiration is enforced on every verification attempt using database time or a consistent UTC clock; cleanup timing is not part of token validity. Add a daily Quartz cleanup job using the project's `Job` and `JobConfig` pattern to delete expired records and old consumed records. Cleanup must not delete an active claim or a record held for cleanup review. Configure the retention period for consumed records (initial proposal: 30 days) and account retention (initial proposal: 30 days from registration), and log aggregate cleanup counts without logging token values.

The daily job must also clean up abandoned registrations and incomplete onboarding. In the new flow, a user remains in Okta's `STAGED` status until the verification link is validated and the application activates that user. Do not use an expired token by itself as the deletion criterion: use a configurable account-retention period (initial proposal: 30 days from registration) so a user can request a new one-hour token during that period. For each self-registered account old enough for cleanup:

1. Confirm it belongs to this application's email-verification flow and has not been verified.
2. Fetch the current Okta user and inspect its lifecycle status immediately before deletion.
3. **Never delete these protected accounts:** `swaveklorenc+td@gmail.com`, `swaveklorenc@gmail.com`, and `swaveklorenc@yahoo.com`. Also preserve `swaveklorenc+<tag>@gmail.com` test accounts by default, without notice or deletion, except for explicitly reserved cleanup-test tags `nonokta1`, `nonokta2`, etc. (pattern `nonokta` followed by digits). Those named cleanup-test accounts remain eligible for normal cleanup rules so the cleanup process can be exercised. Do not normalize away or strip the Gmail plus tag. Apply these exclusions in every environment, including production.
4. If the user is still `STAGED`, classify it as an email-unverified registration. Profile completeness is not required for this branch because staged users cannot sign in to complete onboarding.
5. If the user is `ACTIVE`, use the existing profile-completion rule (currently `UsersController.isProfileComplete`) against the current Okta profile. Preserve complete profiles. An active account created through this registration flow whose profile remains incomplete beyond the retention period is an abandoned onboarding candidate.
6. For a qualifying staged/unverified account or active/incomplete-onboarding account, send a final notice that the registration/onboarding was not completed and the account will be deleted, then delete the Okta user using the Management API. Track notification and deletion failures so a failed email does not cause an untracked deletion attempt.
7. If an `ACTIVE` user's profile is complete, do not delete it. This includes the case where Okta activation succeeded but the application failed before recording token consumption; reconcile/close the verification record safely.
8. If the user is suspended, deprovisioned, or in another unexpected state, do not delete automatically; log/record it for review or handle it only under an explicitly defined policy.
9. After successful deletion, remove or mark the verification records for that Okta user as cleaned up.

Because this sends email and calls Okta, the scheduled job must not hold a database transaction open during those operations. Atomically claim each cleanup candidate before external work, persist notice-sent state for retry, and make deletion handling idempotent. If notice sending fails, retain the candidate and retry later; do not delete the account. Mark unexpected lifecycle states for review so token cleanup does not erase their review record.

## API and user experience

- Registration success confirms that a verification email was sent, without returning the verification token.
- The verification link opens a user-facing confirmation page and submits the token to the server.
- Invalid, expired, or previously used links show a clear next step, such as requesting another verification email.
- A resend endpoint limits abuse, invalidates earlier outstanding tokens, and does not reveal account existence to unauthenticated callers.
- The post-registration confirmation page offers a resend action; resending issues a fresh one-hour token for an eligible staged account and leaves prior links invalid.
- `register.component.html` shows the **Resend email** button after successful self-service registration, and `register.component.ts` submits the existing email to the resend endpoint and displays its response.
- Registration verification does not ask the user to set their password through Okta's activation page; the password is already supplied during account creation. Confirm this behavior with the org's Okta policy and the `sendEmail=false` lifecycle call.

## Security and operational requirements

- Use `SecureRandom` for token generation and a cryptographic hash for persistence.
- Never put token material into logs, metrics, exception messages, or the Okta profile.
- Require HTTPS for verification links in production.
- Rate-limit registration and resend requests, and avoid account-enumeration responses.
- Enforce single-use atomically and handle stale claims.
- Expire staged accounts whose verification is never completed according to a separately configured retention policy; token cleanup alone does not remove the Okta account.
- Keep Okta service credentials server-side and use least-privilege permissions required for user creation and lifecycle activation.

## Acceptance criteria

1. Public registration creates a staged Okta user and does not trigger an Okta activation email.
2. A custom verification email is sent with a high-entropy, URL-safe token and an expiry.
3. Only a valid, unexpired token activates the Okta user, and Okta is called with `sendEmail=false`.
4. The application stores only the token hash; raw tokens are not returned by the registration API or written to logs.
5. Expired, consumed, unknown, and concurrently claimed tokens cannot activate an account.
6. A temporary activation failure permits safe retry; an abandoned claim can be recovered after its timeout.
7. A daily cleanup task removes expired records and consumed records beyond retention without removing live records.
8. Resending verification invalidates older tokens and does not disclose whether an email is registered.
9. The existing administrator notification continues to be sent for public registration.
10. The tournament-director registration path remains unchanged unless its behavior is separately approved and specified.
11. The daily account cleanup notifies and deletes only sufficiently old self-registered `STAGED` users or `ACTIVE` users whose profile is incomplete; it preserves active users with complete profiles and does not automatically delete unexpected lifecycle states.
12. Cleanup never deletes `swaveklorenc+td@gmail.com`, `swaveklorenc@gmail.com`, or `swaveklorenc@yahoo.com`. It also preserves ordinary `swaveklorenc+<tag>@gmail.com` test accounts, while allowing explicitly named cleanup-test accounts such as `swaveklorenc+nonokta1@gmail.com` and `swaveklorenc+nonokta2@gmail.com` to follow the normal cleanup rules.

## Rollout considerations

Before enabling the new flow in production, verify on a non-production Okta org that a staged user created with the current password payload can be activated with `sendEmail=false`, and that the user can then authenticate with the supplied password. Verify that the current Okta policy does not require an Okta-hosted password setup step. Deploy the persistence changes before enabling the code path. Monitor mail-send failures, token expiry, activation errors, and staged accounts left unverified.
