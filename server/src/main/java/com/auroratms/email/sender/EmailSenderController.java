package com.auroratms.email.sender;

import com.auroratms.email.campaign.FilterConfiguration;
import com.auroratms.email.config.EmailServerConfigurationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.List;

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
