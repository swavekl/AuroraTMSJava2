package com.auroratms.email.awssessqs;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SesFeedbackService listens to the SQS queue linked to AWS SES via SNS.
 * It processes 'Bounce' and 'Complaint' notifications to maintain a clean
 * mailing list by updating UserProfile status and subscription flags.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SesFeedbackService {

    private final UserProfileService userProfileService;
    private final ObjectMapper objectMapper;

    /**
     * Entry point for SQS messages. Spring Cloud AWS automatically
     * deserializes the SQS body into the SesNotification DTO.
     *
     * @param snsEnvelope The top-level SNS message envelope from SQS.
     */
    @SqsListener("ses-bounces-queue")
    @Transactional
    public void onMessage(SesNotification snsEnvelope) {
        log.info("Received SQS message: {}", snsEnvelope.getType());

        // SNS messages contain the actual SES data as a stringified JSON in the 'Message' field.
        SesNotification.SesMessage sesMessage = snsEnvelope.getUnwrappedMessage(objectMapper);

        if (sesMessage != null) {
            String type = sesMessage.getNotificationType();
            if ("Bounce".equalsIgnoreCase(type)) {
                handleBounces(sesMessage.getBounce());
            } else if ("Complaint".equalsIgnoreCase(type)) {
                handleComplaints(sesMessage.getComplaint());
            }
        }
    }

    /**
     * Processes bounce notifications. Only 'Permanent' bounces (hard bounces)
     * result in an account being marked as BOUNCED and unsubscribed.
     *
     * @param bounce The bounce data containing type and recipient list.
     */
    private void handleBounces(SesNotification.Bounce bounce) {
        if (bounce != null && "Permanent".equalsIgnoreCase(bounce.getBounceType())) {
            log.warn("Permanent bounce detected. Processing recipients...");
            for (SesNotification.Recipient recipient : bounce.getBouncedRecipients()) {
                updateProfileStatus(recipient.getEmailAddress(), "BOUNCED");
            }
        } else {
            log.info("Transient or non-permanent bounce ignored.");
        }
    }

    /**
     * Processes spam complaints. If a user clicks 'Mark as Spam', they are
     * immediately unsubscribed to prevent further reputation damage.
     *
     * @param complaint The complaint data containing the recipient list.
     */
    private void handleComplaints(SesNotification.Complaint complaint) {
        if (complaint != null && complaint.getComplainedRecipients() != null) {
            for (SesNotification.Recipient recipient : complaint.getComplainedRecipients()) {
                log.warn("Spam complaint received for: {}. Unsubscribing user.", recipient.getEmailAddress());
                updateProfileStatus(recipient.getEmailAddress(), "COMPLAINT");
            }
        }
    }

    /**
     * Updates the UserProfile in the database. Sets the status and forces
     * emailSubscribed to false to stop future automated mailings.
     *
     * @param email  The email address of the player.
     * @param status The new status (BOUNCED or COMPLAINT).
     */
    private void updateProfileStatus(String email, String status) {
        UserProfile profile = userProfileService.getUserProfileForEmail(email);
        if (profile != null) {
            if (profile.isEmailSubscribed()) {
                log.info("Updating UserProfile for {}: Status={}, Subscribed=false", email, status);
                profile.setEmailStatus(status);
                profile.setEmailSubscribed(false);
                userProfileService.updateProfile(profile);
            } else {
                log.info("UserProfile for {} is already unsubscribed with status {}. Skipping update.", email, status);
            }
        } else {
            log.debug("No UserProfile found for email: {}", email);
        }
    }
}
