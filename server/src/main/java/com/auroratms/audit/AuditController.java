package com.auroratms.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
@Slf4j
public class AuditController {

    @Autowired
    private AuditService auditService;

    @PostMapping("/audit")
    @ResponseBody
    public ResponseEntity<Void> save (AuditEntity auditEntity) {
        try {
            auditService.save(auditEntity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Unable to create audit " + auditEntity.toString(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/audits")
    public ResponseEntity<List<AuditEntity>> list (@RequestParam String eventIdentifier,
                                                   @RequestParam String type) {
        try {
            List<AuditEntity> auditList = auditService.findByIdentifierAndType(eventIdentifier, type);
            return ResponseEntity.ok(auditList);
        } catch (Exception e) {
            log.error("Error getting list of audit entities for " + eventIdentifier, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
