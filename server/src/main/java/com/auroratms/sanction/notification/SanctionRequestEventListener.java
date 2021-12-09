package com.auroratms.sanction.notification;

import com.auroratms.profile.UserProfile;
import com.auroratms.sanction.SanctionRequest;
import com.auroratms.sanction.SanctionRequestStatus;
import com.auroratms.sanction.notification.event.SanctionRequestEvent;
import com.auroratms.usatt.UsattPersonnelService;
import com.auroratms.users.UserRoles;
import com.auroratms.utils.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

public class SanctionRequestEventListener {
    private static final Logger logger = LoggerFactory.getLogger(SanctionRequestEventListener.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private UsattPersonnelService usattPersonnelService;

    @Value("${client.host.url}")
    private String clientHostUrl;

    @Async
    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleEvent(SanctionRequestEvent sanctionRequestEvent) {
        SanctionRequestStatus newStatus = sanctionRequestEvent.getSanctionRequest().getStatus();
        SanctionRequestStatus oldStatus = sanctionRequestEvent.getOldStatus();
        if (newStatus != oldStatus) {
            sendEmail(sanctionRequestEvent);
        }
    }

    private void sendEmail(SanctionRequestEvent sanctionRequestEvent) {
        try {
            SanctionRequest sanctionRequest = sanctionRequestEvent.getSanctionRequest();
            SanctionRequestStatus status = sanctionRequest.getStatus();

            UserProfile sanctionCoordinator = this.usattPersonnelService.getPersonInRole(UserRoles.USATTSanctionCoordinators);
            Map<String, Object> templateModel = new HashMap<>();
            String associationAdminEmail = null;
            if (sanctionCoordinator != null) {
                String associationAdminName = sanctionCoordinator.getFirstName() + " " + sanctionCoordinator.getLastName();
                String associationAdminFirstName = sanctionCoordinator.getFirstName();
                associationAdminEmail = sanctionCoordinator.getEmail();
                templateModel.put("associationAdminName", associationAdminName);
                templateModel.put("associationAdminFirstName", associationAdminFirstName);
                templateModel.put("associationAdminEmail", associationAdminEmail);
            } else {
                logger.error("Unable to find USATT sanction coordinator profile");
            }

            // todo - get profile of owner of this request
            String contactName = "Swavek Lorenc"; // sanctionRequest.getContactName();
            String contactEmail = "swaveklorenc@gmail.com"; // sanctionRequest.getContactEmail();
            templateModel.put("contactName", contactName);
            templateModel.put("contactEmail", contactEmail);

            String sanctionRequestUrl = clientHostUrl + "/sanction/edit/" + sanctionRequest.getId();
            templateModel.put("sanctionRequestUrl", sanctionRequestUrl);
            String strStatus = sanctionRequest.getStatus().toString().toLowerCase();
            String subject = "Tournament Sanction Request " + strStatus;

            switch (status) {
                case Submitted:
                    if (associationAdminEmail != null) {
                        emailService.sendMessageUsingThymeleafTemplate(associationAdminEmail, null,
                                subject, "sanction-request/sr-submitted.html", templateModel);
                    }
                    break;
                case Completed:
                    emailService.sendMessageUsingThymeleafTemplate(contactEmail, null,
                            subject, "sanction-request/sr-completed.html", templateModel);
                    break;
            }
        } catch (MessagingException e) {
            logger.error("Unable to send email ", e);
        }
    }

}
