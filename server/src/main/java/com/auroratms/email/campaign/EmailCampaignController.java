package com.auroratms.email.campaign;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

/**
 * Controller for manipulating email compaigns
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
@Slf4j
public class EmailCampaignController {
    
    @Autowired
    private EmailCampaignService service;

    @PostMapping("/emailcampaign")
    public @ResponseBody
    ResponseEntity<EmailCampaign> save (@RequestBody EmailCampaign EmailCampaign) {
        try {
            boolean creating = (EmailCampaign.getId() == null);
            EmailCampaign savedEmailCampaign = service.save(EmailCampaign);
            URI uri = new URI("/api/emailcampaign/" + savedEmailCampaign.getId());
            return (creating)
                    ? ResponseEntity.created(uri).body(savedEmailCampaign)
                    : ResponseEntity.ok(savedEmailCampaign);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gets email campaign
     *
     * @param emailCampaignId
     * @return
     */
    @GetMapping("/emailcampaign/{emailCampaignId}")
    public ResponseEntity<EmailCampaign> get(@PathVariable Long emailCampaignId) {
        try {
            EmailCampaign emailCampaign = this.service.findById(emailCampaignId);
            return ResponseEntity.ok(emailCampaign);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes email campaign
     *
     * @param emailCampaignId
     * @return
     */
    @DeleteMapping("/emailcampaign/{emailCampaignId}")
    public ResponseEntity<Void> delete(@PathVariable Long emailCampaignId) {
        try {
            this.service.delete(emailCampaignId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lists email campaigns by page
     * @param params
     * @param pageable
     * @return
     */
    @GetMapping("/emailcampaigns")
    public ResponseEntity<Page<EmailCampaign>> list(
            @RequestParam Map<String, String> params,
            Pageable pageable) {
        try {
            String nameContains = params.get("nameContains");
            nameContains = (StringUtils.isNotEmpty(nameContains)) ? nameContains : "";
            Page<EmailCampaign> page = this.service.findByName(nameContains, pageable);
            return ResponseEntity.ok(page);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
