package com.auroratms.email.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Controller for configuring email server connection
 */
@RestController
@RequestMapping("api/emailserverconfiguration")
@PreAuthorize("isAuthenticated()")
@Slf4j
@Transactional
public class EmailServerConfigurationController {

    @Autowired
    private EmailServerConfigurationService emailServerConfigurationService;

    @PostMapping("")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    @ResponseBody
    public ResponseEntity<EmailServerConfigurationEntity> create(@RequestBody EmailServerConfigurationEntity emailServerConfigurationEntity) {
        try {
            boolean creating = !emailServerConfigurationService.existsById(emailServerConfigurationEntity.getProfileId());
            EmailServerConfigurationEntity savedEntity = emailServerConfigurationService.save(emailServerConfigurationEntity);
            URI uri = new URI("/api/emailserverconfig/%s".formatted(savedEntity.getProfileId()));
            return (creating)
                    ? ResponseEntity.created(uri).body(savedEntity)
                    : ResponseEntity.ok(savedEntity);
        } catch (Exception e) {
            log.error("Error creating email configuration for profile " + emailServerConfigurationEntity.getProfileId(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{profileId}")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    @ResponseBody
    public ResponseEntity<EmailServerConfigurationEntity> get(@PathVariable String profileId) {
        try {
            EmailServerConfigurationEntity emailServerConfigurationEntity = emailServerConfigurationService.findById(profileId);
            return ResponseEntity.ok(emailServerConfigurationEntity);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{profileId}")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public ResponseEntity<Void> update(@RequestBody EmailServerConfigurationEntity emailServerConfigurationEntity,
                                       @PathVariable String profileId) {
        return ResponseEntity.ok().build();
    }
}
