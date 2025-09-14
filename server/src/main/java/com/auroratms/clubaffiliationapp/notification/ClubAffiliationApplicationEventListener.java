package com.auroratms.clubaffiliationapp.notification;

import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubService;
import com.auroratms.clubaffiliationapp.ClubAffiliationApplication;
import com.auroratms.clubaffiliationapp.ClubAffiliationApplicationStatus;
import com.auroratms.clubaffiliationapp.notification.event.ClubAffiliationApplicationEvent;
import com.auroratms.profile.UserProfile;
import com.auroratms.usatt.UsattPersonnelService;
import com.auroratms.users.UserRoles;
import com.auroratms.utils.EmailService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.mail.MessagingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ClubAffiliationApplicationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ClubAffiliationApplicationEventListener.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private ClubService clubService;

    @Autowired
    private UsattPersonnelService usattPersonnelService;

    @Value("${client.host.url}")
    private String clientHostUrl;

    @Async
    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleEvent(ClubAffiliationApplicationEvent event) {
        ClubAffiliationApplicationStatus newStatus = event.getClubAffiliationApplication().getStatus();
        ClubAffiliationApplicationStatus oldStatus = event.getOldStatus();
        // avoid sending unnecessary emails - only when status changes
        if (newStatus != oldStatus) {
            sendEmail(event);
        }
        // only update once it is completed
        if (event.getClubAffiliationApplication().getStatus() == ClubAffiliationApplicationStatus.Completed) {
            updateClubTable(event);
        }
    }

    /**
     * Transfer information from teh application to the club table so users can see it
     * @param event
     */
    private void updateClubTable(ClubAffiliationApplicationEvent event) {
        ClubAffiliationApplication clubAffiliationApplication = event.getClubAffiliationApplication();
        String clubName = clubAffiliationApplication.getName();
        String state = clubAffiliationApplication.getState();
        List<ClubEntity> clubEntityList = clubService.findByNameAndState(clubName, state);
        // if not found in the state - try by name only
        if (clubEntityList.isEmpty()) {
            clubEntityList = clubService.findByNameAndState(clubName, null);
        }
        if (clubEntityList.isEmpty()) {
            // this is a new club not in a table so create one
            ClubEntity newClub = new ClubEntity();
            transferData(newClub, clubAffiliationApplication);
            clubService.save(newClub);
            logger.info("Created new club for club " + newClub.getClubName());
        } else {
            for (ClubEntity clubEntity : clubEntityList) {
                // find the right club so we can update its data
                transferData(clubEntity, clubAffiliationApplication);
                clubService.save(clubEntity);
                logger.info("Updated club information for club " + clubEntity.getClubName());
                break;
            }
        }
    }

    /**
     * Populates the club entity with information from application
     *
     * @param clubEntity
     * @param clubAffiliationApplication
     */
    private void transferData(ClubEntity clubEntity, ClubAffiliationApplication clubAffiliationApplication) {
        clubEntity.setAffiliated(true);
        clubEntity.setClubName(clubAffiliationApplication.getName());
        clubEntity.setBuildingName(clubAffiliationApplication.getBuildingName());
        clubEntity.setStreetAddress(clubAffiliationApplication.getStreetAddress());
        clubEntity.setCity(clubAffiliationApplication.getCity());
        clubEntity.setState(clubAffiliationApplication.getState());
        clubEntity.setZipCode(clubAffiliationApplication.getZipCode());
        clubEntity.setCountryCode("US");
        clubEntity.setHoursAndDates(clubAffiliationApplication.getHoursAndDates());
        clubEntity.setClubAdminName(clubAffiliationApplication.getClubAdminName());
        clubEntity.setClubAdminEmail(clubAffiliationApplication.getClubAdminEmail());
        clubEntity.setClubPhoneNumber(clubAffiliationApplication.getClubPhoneNumber());
        clubEntity.setClubPhoneNumber2(clubAffiliationApplication.getClubPhoneNumber2());
        clubEntity.setClubWebsite(clubAffiliationApplication.getClubWebsite());
    }

    /**
     * Sends email to the proper recipient about the status change
     * @param event
     */
    private void sendEmail(ClubAffiliationApplicationEvent event) {
        try {
            ClubAffiliationApplication clubAffiliationApplication = event.getClubAffiliationApplication();
            ClubAffiliationApplicationStatus status = clubAffiliationApplication.getStatus();
            UserProfile usattClubManager = this.usattPersonnelService.getPersonInRole(UserRoles.USATTClubManagers);
            Map<String, Object> templateModel = new HashMap<>();
            String associationAdminEmail = null;
            if (usattClubManager != null) {
                String associationAdminName = usattClubManager.getFirstName() + " " + usattClubManager.getLastName();
                String associationAdminFirstName = usattClubManager.getFirstName();
                associationAdminEmail = usattClubManager.getEmail();
                templateModel.put("associationAdminName", associationAdminName);
                templateModel.put("associationAdminFirstName", associationAdminFirstName);
                templateModel.put("associationAdminEmail", associationAdminEmail);
            } else {
                logger.error("Unable to find USATT club manager profile");
            }
            // https://gateway-pc:4200/ui/clubaffiliation/edit/15
            String applicationUrl = clientHostUrl + "/ui/clubaffiliation/edit/" + clubAffiliationApplication.getId();
            templateModel.put("applicationUrl", applicationUrl);

            String strStatus = clubAffiliationApplication.getStatus().toString().toLowerCase();
            String subject = "Club Affiliation Application " + strStatus;

            String template = null;
            switch (status) {
                case Submitted:
                    template = "club-affiliation-application/caa-submitted.html";
                    break;
                case Approved:
                    template = "club-affiliation-application/caa-approved.html";
                    break;
                case Rejected:
                    template = "club-affiliation-application/caa-rejected.html";
                    break;
                case Completed:
                    template = "club-affiliation-application/caa-completed.html";
                    break;
            }

            if (status == ClubAffiliationApplicationStatus.Approved ||
                    status == ClubAffiliationApplicationStatus.Rejected) {
                // send email to TD
                String clubAdminEmail = clubAffiliationApplication.getClubAdminEmail();
                String clubAdminName = clubAffiliationApplication.getClubAdminName();
                String ccAddresses = clubAffiliationApplication.getPresidentEmail();
                ccAddresses += (StringUtils.isEmpty(ccAddresses)) ? clubAffiliationApplication.getVicePresidentEmail() : "";
                String reason = clubAffiliationApplication.getApprovalRejectionNotes();

                templateModel.put("clubAdminName", clubAdminName);
                templateModel.put("reason", reason);

                // send email
                emailService.sendMessageUsingThymeleafTemplate(clubAdminEmail, ccAddresses,
                        subject, template, templateModel);

            } else if (status == ClubAffiliationApplicationStatus.Submitted ||
                    status == ClubAffiliationApplicationStatus.Completed) {
                // send email to USATT
                if (associationAdminEmail != null) {
                    emailService.sendMessageUsingThymeleafTemplate(associationAdminEmail, null,
                            subject, template, templateModel);
                }
            }
        } catch (MessagingException e) {
            logger.error("Unable to send email ", e);
        }
    }
}
