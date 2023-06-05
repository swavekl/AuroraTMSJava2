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
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

@Component
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
        try {
            SanctionRequest sanctionRequest = sanctionRequestEvent.getSanctionRequest();
            SanctionRequestStatus status = sanctionRequest.getStatus();
            String venueState = sanctionRequest.getVenueState();
            int starLevel = sanctionRequest.getStarLevel();
            String region = usattPersonnelService.getSanctionCoordinatorRegion(starLevel, venueState);
            UserProfile sanctionCoordinator = this.usattPersonnelService.getSanctionCoordinator(region);
            Map<String, Object> templateModel = new HashMap<>();
            String sanctionCoordiantorEmail = null;
            if (sanctionCoordinator != null) {
                String sanctionCoordinatorFullName = sanctionCoordinator.getFirstName() + " " + sanctionCoordinator.getLastName();
                String sanctionCoordinatorFirstName = sanctionCoordinator.getFirstName();
                sanctionCoordiantorEmail = sanctionCoordinator.getEmail();
                templateModel.put("sanctionCoordinatorName", sanctionCoordinatorFullName);
                templateModel.put("sanctionCoordinatorFirstName", sanctionCoordinatorFirstName);
                templateModel.put("sanctionCoordinatorEmail", sanctionCoordiantorEmail);
            } else {
                logger.error("Unable to find USATT sanction coordinator profile");
            }

            String contactName = sanctionRequest.getContactPersonName();
            String contactEmail = sanctionRequest.getContactPersonEmail();
            templateModel.put("contactName", contactName);
            templateModel.put("contactEmail", contactEmail);

            String sanctionRequestUrl = clientHostUrl + "/ui/sanction/edit/" + sanctionRequest.getId();
            templateModel.put("sanctionRequestUrl", sanctionRequestUrl);
            String strStatus = sanctionRequest.getStatus().toString().toLowerCase();
            String subject = "Tournament Sanction Request " + strStatus;
            templateModel.put("tournamentName", sanctionRequest.getTournamentName());
            templateModel.put("tournamentDate", sanctionRequest.getStartDate());
            templateModel.put("approvalRejectionNotes", sanctionRequest.getApprovalRejectionNotes());

            switch (status) {
                case Submitted:
                    if (sanctionCoordiantorEmail != null) {
                        emailService.sendMessageUsingThymeleafTemplate(sanctionCoordiantorEmail, null,
                                subject, "sanction-request/sr-submitted.html", templateModel);
                    }
                    break;
                case Approved:
                    if (contactEmail != null) {
                        emailService.sendMessageUsingThymeleafTemplate(contactEmail, null,
                                subject, "sanction-request/sr-approved.html", templateModel);
                    }
                    break;
                case Rejected:
                    if (contactEmail != null) {
                        emailService.sendMessageUsingThymeleafTemplate(contactEmail, null,
                                subject, "sanction-request/sr-rejected.html", templateModel);
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
