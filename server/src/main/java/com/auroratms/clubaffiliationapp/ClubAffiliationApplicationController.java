package com.auroratms.clubaffiliationapp;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
public class ClubAffiliationApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ClubAffiliationApplicationController.class);
    
    @Autowired
    private ClubAffiliationApplicationService service;
    
    @PostMapping("/clubaffiliationapplication")
    public @ResponseBody ResponseEntity<ClubAffiliationApplication> save(
            @RequestBody ClubAffiliationApplication application) {
        try {
            ClubAffiliationApplication savedClub = service.save(application);
            return new ResponseEntity<>(savedClub, HttpStatus.CREATED);
        } catch (Exception e) {
            String message = e.getMessage();
            message = message.substring(message.indexOf("{"));
            return new ResponseEntity(message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get single applicaiton
     * @return
     */
    @GetMapping("/clubaffiliationapplication/{applicationId}")
    public ResponseEntity<ClubAffiliationApplication> get (@PathVariable Long applicationId) {
        try {
            ClubAffiliationApplication clubAffiliationApplication = this.service.findById(applicationId);
            return new ResponseEntity<>(clubAffiliationApplication, HttpStatus.OK);
        } catch (Exception e) {
            String message = e.getMessage();
            message = message.substring(message.indexOf("{"));
            return new ResponseEntity(message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes request
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
     * @param params
     * @param pageable
     * @return
     */
    @GetMapping("/clubaffiliationapplications")
    public ResponseEntity<Page<ClubAffiliationApplication>> list (
            @RequestParam Map<String, String> params,
            Pageable pageable) {
        String nameContains = params.get("nameContains");
        nameContains = (StringUtils.isNotEmpty(nameContains)) ? nameContains : "";
        Page<ClubAffiliationApplication> page = this.service.findByName(nameContains, pageable);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }
}
