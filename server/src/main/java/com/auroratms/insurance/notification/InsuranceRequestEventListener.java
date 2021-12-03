package com.auroratms.insurance.notification;

import com.auroratms.insurance.InsuranceRequest;
import com.auroratms.insurance.InsuranceRequestStatus;
import com.auroratms.insurance.notification.event.InsuranceRequestEvent;
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

public class InsuranceRequestEventListener {

    private static final Logger logger = LoggerFactory.getLogger(InsuranceRequestEventListener.class);

    @Autowired
    private EmailService emailService;

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
            String associationAdminName = "Tina Ren";
            String associationAdminFirstName = "Tina";
            String associationAdminEmail = "swaveklorenc+tina@gmail.com";
            String applicationUrl = clientHostUrl + "/insurance/edit/" + insuranceRequest.getId();

            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("applicationUrl", applicationUrl);
            templateModel.put("associationAdminName", associationAdminName);
            templateModel.put("associationAdminFirstName", associationAdminFirstName);
            templateModel.put("associationAdminEmail", associationAdminEmail);
            String strStatus = insuranceRequest.getStatus().toString().toLowerCase();
            String subject = "Club Affiliation Application " + strStatus;

            String template = null;
            switch (status) {
                case Submitted:
                    template = "insurance-request/ir-submitted.html";
                    break;
                case Approved:
                    template = "insurance-request/ir-approved.html";
                    break;
                case Rejected:
                    template = "insurance-request/ir-rejected.html";
                    break;
                case Completed:
                    template = "insurance-request/ir-completed.html";
                    break;
            }

            if (status == InsuranceRequestStatus.Approved ||
                    status == InsuranceRequestStatus.Rejected) {
                // send email to TD
                String clubAdminEmail = insuranceRequest.getContactEmail();
                String clubAdminName = insuranceRequest.getContactName();
                // todo - who to send it to
                String ccAddresses = "";
//                ccAddresses += (StringUtils.isEmpty(ccAddresses)) ? insuranceRequest.getVicePresidentEmail() : "";
                // todo
                String reason = ""; // insuranceRequest.getApprovalRejectionNotes();

                templateModel.put("clubAdminName", clubAdminName);
                templateModel.put("reason", reason);

                // send email
                emailService.sendMessageUsingThymeleafTemplate(clubAdminEmail, ccAddresses,
                        subject, template, templateModel);

            } else if (status == InsuranceRequestStatus.Submitted ||
                    status == InsuranceRequestStatus.Completed) {
                // send email to USATT
                emailService.sendMessageUsingThymeleafTemplate(associationAdminEmail, null,
                        subject, template, templateModel);
            }
        } catch (MessagingException e) {
            logger.error("Unable to send email ", e);
        }
    }

}
