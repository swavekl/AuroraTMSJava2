package com.auroratms.insurance.notification;

import com.auroratms.insurance.InsuranceRequest;
import com.auroratms.insurance.InsuranceRequestStatus;
import com.auroratms.insurance.notification.event.InsuranceRequestEvent;
import com.auroratms.profile.UserProfile;
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
public class InsuranceRequestEventListener {

    private static final Logger logger = LoggerFactory.getLogger(InsuranceRequestEventListener.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private UsattPersonnelService usattPersonnelService;

    @Value("${client.host.url}")
    private String clientHostUrl;

    @Async
    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleEvent(InsuranceRequestEvent insuranceRequestEvent) {
        InsuranceRequestStatus newStatus = insuranceRequestEvent.getInsuranceRequest().getStatus();
        InsuranceRequestStatus oldStatus = insuranceRequestEvent.getOldStatus();
        if (newStatus != oldStatus) {
            sendEmail(insuranceRequestEvent);
        }
    }

    private void sendEmail(InsuranceRequestEvent insuranceRequestEvent) {
        try {
            InsuranceRequest insuranceRequest = insuranceRequestEvent.getInsuranceRequest();
            InsuranceRequestStatus status = insuranceRequest.getStatus();

            UserProfile usattInsuranceManager = this.usattPersonnelService.getPersonInRole(UserRoles.USATTInsuranceManagers);
            Map<String, Object> templateModel = new HashMap<>();
            String associationAdminEmail = null;
            if (usattInsuranceManager != null) {
                String associationAdminName = usattInsuranceManager.getFirstName() + " " + usattInsuranceManager.getLastName();
                String associationAdminFirstName = usattInsuranceManager.getFirstName();
                associationAdminEmail = usattInsuranceManager.getEmail();
                templateModel.put("associationAdminName", associationAdminName);
                templateModel.put("associationAdminFirstName", associationAdminFirstName);
                templateModel.put("associationAdminEmail", associationAdminEmail);
            } else {
                logger.error("Unable to find USATT insurance manager profile");
            }

            String contactName = insuranceRequest.getContactName();
            String contactEmail = insuranceRequest.getContactEmail();
            templateModel.put("contactName", contactName);
            templateModel.put("contactEmail", contactEmail);

            String applicationUrl = clientHostUrl + "/insurance/edit/" + insuranceRequest.getId();
            templateModel.put("applicationUrl", applicationUrl);
            String strStatus = insuranceRequest.getStatus().toString().toLowerCase();
            String subject = "Insurance Certificate Request " + strStatus;

            switch (status) {
                case Submitted:
                    if (associationAdminEmail != null) {
                        emailService.sendMessageUsingThymeleafTemplate(associationAdminEmail, null,
                                subject, "insurance-request/ir-submitted.html", templateModel);
                    }
                    break;
                case Completed:
                    emailService.sendMessageUsingThymeleafTemplate(contactEmail, null,
                            subject, "insurance-request/ir-completed.html", templateModel);
                    break;
            }
        } catch (MessagingException e) {
            logger.error("Unable to send email ", e);
        }
    }

}
