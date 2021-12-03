package com.auroratms.insurance;

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
import java.util.Map;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
public class InsuranceRequestController {

    private static final Logger logger = LoggerFactory.getLogger(InsuranceRequestController.class);

    @Autowired
    private InsuranceRequestService service;

    /**
     * Creates or updates insurance request
     * @param insuranceRequest
     * @return
     */
    @PostMapping("/insurancerequest")
    public @ResponseBody
    ResponseEntity<InsuranceRequest> save (@RequestBody InsuranceRequest insuranceRequest) {
        try {
            boolean creating = (insuranceRequest.getId() == null);
            InsuranceRequest savedInsuranceRequest = service.save(insuranceRequest);
            URI uri = new URI("/api/insurancerequest/" + savedInsuranceRequest.getId());
            return (creating)
                    ? ResponseEntity.created(uri).body(savedInsuranceRequest)
                    : ResponseEntity.ok(savedInsuranceRequest);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gets insurance request
     *
     * @param insuranceRequestId
     * @return
     */
    @GetMapping("/insurancerequest/{insuranceRequestId}")
    public ResponseEntity<InsuranceRequest> get(@PathVariable Long insuranceRequestId) {
        try {
            InsuranceRequest InsuranceRequest = this.service.findById(insuranceRequestId);
            return ResponseEntity.ok(InsuranceRequest);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes request
     *
     * @param insuranceRequestId
     * @return
     */
    @DeleteMapping("/insurancerequest/{insuranceRequestId}")
    public ResponseEntity<Void> delete(@PathVariable Long insuranceRequestId) {
        try {
            this.service.delete(insuranceRequestId);
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
    @GetMapping("/insurancerequests")
    public ResponseEntity<Page<InsuranceRequest>> list(
            @RequestParam Map<String, String> params,
            Pageable pageable) {
        try {
            String nameContains = params.get("nameContains");
            nameContains = (StringUtils.isNotEmpty(nameContains)) ? nameContains : "";
            Page<InsuranceRequest> page = this.service.findByName(nameContains, pageable);
            return ResponseEntity.ok(page);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/insurancerequest/{insuranceRequestId}")
    public ResponseEntity<Void> update(@RequestBody InsuranceRequest insuranceRequest,
                                       @PathVariable Long insuranceRequestId) {
        try {
            this.service.updateStatus(insuranceRequestId, insuranceRequest.getStatus());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
