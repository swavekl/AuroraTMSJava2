package com.auroratms.tournamentprocessing.notification;

import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.paymentrefund.PaymentRefundFor;
import com.auroratms.paymentrefund.notification.event.PaymentEvent;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamentprocessing.TournamentProcessingRequest;
import com.auroratms.tournamentprocessing.TournamentProcessingRequestDetail;
import com.auroratms.tournamentprocessing.TournamentProcessingRequestService;
import com.auroratms.usatt.UsattPersonnelService;
import com.auroratms.users.UserRoles;
import com.auroratms.utils.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.mail.MessagingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class PaymentRefundEventTPRListener {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UsattPersonnelService usattPersonnelService;

    @Autowired
    private TournamentProcessingRequestService tournamentProcessingRequestService;

    @Async
    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handlePaymentEvent(PaymentEvent event) {
        // run this task as system principal so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                PaymentRefundFor paymentRefundFor = event.getPaymentRefundFor();
                switch (paymentRefundFor) {
                    case CLUB_AFFILIATION_FEE:
                        break;
                    case TOURNAMENT_REPORT_FEE:
                        sendTournamentReportPaymentEmail(event);
                        break;
                }
            }

        };
        task.execute();
    }

    /**
     *
     * @param event
     */
    private void sendTournamentReportPaymentEmail(PaymentEvent event) {
        try {
            log.info("Sending email about tournament report payment");
            Map<String, Object> templateModel = new HashMap<>();
            // get the entry so we can get tournament information
            long detailId = event.getItemId();

            TournamentProcessingRequest tournamentProcessingRequest =
                    tournamentProcessingRequestService.findByDetailId(detailId);
            String tournamentName = tournamentProcessingRequest.getTournamentName();
            templateModel.put("tournamentName", tournamentName);

            String processorFullName = "";
            String processorEmail = null;
            UserProfile processorUserProfile = usattPersonnelService.getPersonInRole(UserRoles.USATTTournamentManagers);
            if (processorUserProfile != null) {
                processorFullName = processorUserProfile.getFirstName() + " " + processorUserProfile.getLastName();
                processorEmail = processorUserProfile.getEmail();
            }
            templateModel.put("processorFullName", processorFullName);

            String submitterUserProfileId = null;
            List<TournamentProcessingRequestDetail> details = tournamentProcessingRequest.getDetails();
            for (TournamentProcessingRequestDetail detail : details) {
                if (detail.getId() == detailId) {
                    submitterUserProfileId = detail.getCreatedByProfileId();
                    break;
                }
            }

            if (submitterUserProfileId != null) {
                UserProfile submitterUserProfile = userProfileService.getProfile(submitterUserProfileId);
                templateModel.put("payeeFirstName", submitterUserProfile.getFirstName());

                String toAddress = submitterUserProfile.getEmail();

                // Add payment information
                // convert 12545 to 125.45
                Double amount = (double)event.getPaidAmount() / 100;
                templateModel.put("amount", amount);
                templateModel.put("currency", event.getPaidCurrency());

                emailService.sendMessageUsingThymeleafTemplate(toAddress, processorEmail,
                        "Tournament Report Payment Received",
                        "tournament-processing/tpr-paid.html",
                        templateModel);
                log.info("Email sent to " + toAddress);
            }

        } catch (MessagingException e) {
            log.error("Errors sending tournament report paid email", e);
        }
    }
}
