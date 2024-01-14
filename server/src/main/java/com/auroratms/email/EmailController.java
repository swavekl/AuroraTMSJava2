package com.auroratms.email;

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
 * Controller for various email related functions
 */
@RestController
@RequestMapping("api/email")
@PreAuthorize("isAuthenticated()")
@Transactional
@Slf4j
public class EmailController {

    @Autowired
    private EmailGenerationService emailGenerationService;

    @GetMapping("/{tournamentId}")
    public @ResponseBody ResponseEntity<List<String>> getEmailsForTournament(@PathVariable Long tournamentId) {
        try {
            List<String> emails = emailGenerationService.listEmailsForTournament(tournamentId);
            return new ResponseEntity<>(emails, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/sendtestemail")
    public @ResponseBody ResponseEntity<Boolean> sendTestEmail(@RequestBody EmailServerConfigurationEntity config) throws MessagingException {
        emailGenerationService.sendTestEmail(config);
        return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
    }

    // handler for the exception from above call to sendtest
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity handleMessagingException(MessagingException e) {
        log.error("Error sending test email", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
    }
}
