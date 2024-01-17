package com.auroratms.email.sender;

import com.auroratms.email.campaign.EmailCampaign;
import com.auroratms.email.campaign.FilterConfiguration;
import com.auroratms.email.config.EmailServerConfigurationEntity;
import com.auroratms.users.UserRolesHelper;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for various email sending related functions
 */
@RestController
@RequestMapping("api/emailsender")
@PreAuthorize("isAuthenticated()")
@Transactional
@Slf4j
public class EmailSenderController {

    @Autowired
    private EmailSenderService emailSenderService;

    private static Map<String, CampaignSendingStatus> statusPerThread = new ConcurrentHashMap<>();

    /**
     * @param tournamentId
     * @param filterConfiguration
     * @return
     */
    @PostMapping("/{tournamentId}")
    public @ResponseBody ResponseEntity<List<FilterConfiguration.Recipient>> getRecipients(@PathVariable Long tournamentId,
                                                                                           @RequestBody FilterConfiguration filterConfiguration) {
        try {
            List<FilterConfiguration.Recipient> recipients = emailSenderService.getFilteredRecipients(tournamentId, filterConfiguration);
            return new ResponseEntity<>(recipients, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param tournamentId
     * @param emailCampaign
     * @return
     */
    @PostMapping("/sendcampaign/{tournamentId}")
    public @ResponseBody ResponseEntity<String> getRecipients(@PathVariable Long tournamentId,
                                                              @RequestBody EmailCampaign emailCampaign) {
        final String currentUserName = UserRolesHelper.getCurrentUsername();
        CampaignSendingStatus campaignSendingStatus = new CampaignSendingStatus();
        campaignSendingStatus.phase = String.format("Staring email campaign generation for user %s", currentUserName);
        campaignSendingStatus.startTime = System.currentTimeMillis();
        campaignSendingStatus.endTime = campaignSendingStatus.startTime;
        ObjectIdGenerators.UUIDGenerator uuidGenerator = new ObjectIdGenerators.UUIDGenerator();
        final UUID uuid = uuidGenerator.generateId(campaignSendingStatus);
        campaignSendingStatus.id = uuid.toString();
        log.info(campaignSendingStatus.phase);

        statusPerThread.put(campaignSendingStatus.id, campaignSendingStatus);

        try {
            Runnable emailSendingTask = new Runnable() {
                @Override
                @Transactional
                public void run() {
                    emailSenderService.sendCampaign(tournamentId, emailCampaign, campaignSendingStatus, currentUserName);
                    log.info("Finished campaign for username " + currentUserName);
                    statusPerThread.remove(campaignSendingStatus.id);
                }
            };
            Thread thread = new Thread(emailSendingTask);
            thread.start();

            return new ResponseEntity<>(campaignSendingStatus.id, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/sendcampaign/status/{statusid}")
    public @ResponseBody ResponseEntity<CampaignSendingStatus> getStatus (@PathVariable String statusId) {
        CampaignSendingStatus campaignSendingStatus = statusPerThread.get(statusId);
        if (campaignSendingStatus != null) {
            return ResponseEntity.ok(campaignSendingStatus);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

        /**
         * Sends a test email from email server owner to the same using configuration
         *
         * @param config
         * @return
         * @throws MessagingException
         */
    @PostMapping("/testemail")
    public @ResponseBody ResponseEntity<Boolean> sendTestEmail(@RequestBody EmailServerConfigurationEntity config) throws MessagingException {
        emailSenderService.sendTestEmail(config);
        return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
    }

    // handler for the exception from above call to sendtest
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity handleMessagingException(MessagingException e) {
        log.error("Error sending test email", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
    }
}
