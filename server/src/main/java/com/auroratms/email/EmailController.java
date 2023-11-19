package com.auroratms.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for various email related functions
 */
@RestController
@RequestMapping("api/email")
@PreAuthorize("isAuthenticated()")
@Transactional
public class EmailController {

    @Autowired
    private EmailGenerationService emailGenerationService;

    @GetMapping("/{tournamentId}")
    public ResponseEntity<List<String>> getEmailsForTournament(@PathVariable Long tournamentId) {
        try {
            List<String> emails = emailGenerationService.listEmailsForTournament(tournamentId);
            return new ResponseEntity<>(emails, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
