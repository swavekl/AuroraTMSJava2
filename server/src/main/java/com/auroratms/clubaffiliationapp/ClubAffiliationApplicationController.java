package com.auroratms.clubaffiliationapp;

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
public class ClubAffiliationApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ClubAffiliationApplicationController.class);

    @Autowired
    private ClubAffiliationApplicationService service;

    /**
     * Save application
     * @param application
     * @return
     */
    @PostMapping("/clubaffiliationapplication")
    public @ResponseBody
    ResponseEntity<ClubAffiliationApplication> save(
            @RequestBody ClubAffiliationApplication application) {
        try {
            boolean creating = (application.getId() == null);
            ClubAffiliationApplication savedClub = service.save(application);
            URI uri = new URI("/api/clubaffiliationapplication/" + savedClub.getId());
            return (creating)
                    ? ResponseEntity.created(uri).body(savedClub)
                    : ResponseEntity.ok(savedClub);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get single applicaition
     *
     * @return
     */
    @GetMapping("/clubaffiliationapplication/{applicationId}")
    public ResponseEntity<ClubAffiliationApplication> get(@PathVariable Long applicationId) {
        try {
            ClubAffiliationApplication clubAffiliationApplication = this.service.findById(applicationId);
            return ResponseEntity.ok(clubAffiliationApplication);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes application
     *
     * @param applicationId
     * @return
     */
    @DeleteMapping("/clubaffiliationapplication/{applicationId}")
    public ResponseEntity<Void> delete(@PathVariable Long applicationId) {
        try {
            this.service.delete(applicationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Get page worth of applications
     *
     * @param params
     * @param pageable
     * @return
     */
    @GetMapping("/clubaffiliationapplications")
    public ResponseEntity<Page<ClubAffiliationApplication>> list(
            @RequestParam Map<String, String> params,
            Pageable pageable) {
        try {
            String nameContains = params.get("nameContains");
            nameContains = (StringUtils.isNotEmpty(nameContains)) ? nameContains : "";
            String strLatest = params.get("latest");
            boolean latest = strLatest != null && Boolean.parseBoolean(strLatest);
            Page<ClubAffiliationApplication> page = null;
            if (!latest) {
                page = this.service.findByName(nameContains, pageable);
            } else {
                page = this.service.findByNameAndLatest(nameContains, pageable);
            }
            return ResponseEntity.ok(page);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/clubaffiliationapplication/{applicationId}")
    public ResponseEntity<Void> update(@RequestBody ClubAffiliationApplication clubAffiliationApplication,
                                       @PathVariable Long applicationId) {
        try {
            this.service.updateStatus(clubAffiliationApplication.getId(),
                    clubAffiliationApplication.getStatus());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}
