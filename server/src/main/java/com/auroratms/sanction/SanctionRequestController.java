package com.auroratms.sanction;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
public class SanctionRequestController {

    private static final Logger logger = LoggerFactory.getLogger(SanctionRequestController.class);

    @Autowired
    private SanctionRequestService service;
    
    /**
     * Creates or updates insurance request
     * @param sanctionRequest
     * @return
     */
    @PostMapping("/sanctionrequest")
    public @ResponseBody
    ResponseEntity<SanctionRequest> save (@RequestBody SanctionRequest sanctionRequest) {
        try {
            boolean creating = (sanctionRequest.getId() == null);
            SanctionRequest savedSanctionRequest = service.save(sanctionRequest);
            URI uri = new URI("/api/sanctionrequest/" + savedSanctionRequest.getId());
            return (creating)
                    ? ResponseEntity.created(uri).body(savedSanctionRequest)
                    : ResponseEntity.ok(savedSanctionRequest);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gets insurance request
     *
     * @param sanctionRequestId
     * @return
     */
    @GetMapping("/sanctionrequest/{sanctionRequestId}")
    public ResponseEntity<SanctionRequest> get(@PathVariable Long sanctionRequestId) {
        try {
            SanctionRequest SanctionRequest = this.service.findById(sanctionRequestId);
            return ResponseEntity.ok(SanctionRequest);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes request
     *
     * @param sanctionRequestId
     * @return
     */
    @DeleteMapping("/sanctionrequest/{sanctionRequestId}")
    public ResponseEntity<Void> delete(@PathVariable Long sanctionRequestId) {
        try {
            this.service.delete(sanctionRequestId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get page worth of insurance requests
     *
     * @param params
     * @param pageable
     * @return
     */
    @GetMapping("/sanctionrequests")
    public ResponseEntity<Page<SanctionRequest>> list(
            @RequestParam Map<String, String> params,
            Pageable pageable) {
        try {
            String nameContains = params.get("nameContains");
            nameContains = (StringUtils.isNotEmpty(nameContains)) ? nameContains : "";
            Page<SanctionRequest> page = this.service.findByName(nameContains, pageable);
            return ResponseEntity.ok(page);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/sanctionrequest/{sanctionRequestId}")
    public ResponseEntity<Void> update(@RequestBody SanctionRequest sanctionRequest,
                                       @PathVariable Long sanctionRequestId) {
        try {
            this.service.updateStatus(sanctionRequestId, sanctionRequest.getStatus());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}
