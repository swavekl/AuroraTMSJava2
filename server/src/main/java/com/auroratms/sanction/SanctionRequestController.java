package com.auroratms.sanction;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
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
            SanctionRequestEntity sanctionRequestEntity = sanctionRequest.convertToEntity();
            SanctionRequestEntity savedSanctionRequestEntity = service.save(sanctionRequestEntity);
            SanctionRequest savedSanctionRequest = new SanctionRequest().convertFromEntity(savedSanctionRequestEntity);
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
            SanctionRequestEntity sanctionRequestEntity = this.service.findById(sanctionRequestId);
            SanctionRequest sanctionRequest = new SanctionRequest().convertFromEntity(sanctionRequestEntity);
            return ResponseEntity.ok(sanctionRequest);
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
            Page<SanctionRequestEntity> pageOfEntities = this.service.findByName(nameContains, pageable);
            Iterator<SanctionRequestEntity> iterator = pageOfEntities.iterator();
            List<SanctionRequest> sanctionRequestList = new ArrayList<>();
            while (iterator.hasNext()) {
                SanctionRequestEntity entity = iterator.next();
                SanctionRequest sanctionRequest = new SanctionRequest().convertFromEntity(entity);
                sanctionRequestList.add(sanctionRequest);
            }
            Page<SanctionRequest> page = new PageImpl<SanctionRequest>(sanctionRequestList);
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
