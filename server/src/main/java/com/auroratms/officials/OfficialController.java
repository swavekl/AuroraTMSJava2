package com.auroratms.officials;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
@Transactional
public class OfficialController {

    @Autowired
    private OfficialService officialService;

    @GetMapping("/officials")
    public ResponseEntity<Page<Official>> listOfficials(@RequestParam Map<String, String> params, Pageable pageable) {
        try {
            String nameContains = params.get("nameContains");
            nameContains = (!StringUtils.isEmpty(nameContains)) ? nameContains : "";
            Page<Official> page = null;
            if (!StringUtils.isEmpty(nameContains)) {
                page = officialService.findByFirstNameLikeOrLastNameLike(nameContains, nameContains, pageable);
            } else {
                page = officialService.list(pageable);
            }
            return ResponseEntity.ok(page);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/official/{officialId}")
    public ResponseEntity<Official> get( @PathVariable Long officialId) {
        try {
            Official official = officialService.findById(officialId);
            return ResponseEntity.ok(official);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/official")
    @ResponseBody
    @PreAuthorize("hasAuthority('Admins') or hasAuthority('USATTMatchOfficialsManagers')")
    public ResponseEntity<Official> create(@RequestBody Official official) {
        try {
            Official savedOfficial = officialService.save(official);
            return new ResponseEntity<>(savedOfficial, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/official/{officialId}")
    @ResponseBody
    @PreAuthorize("hasAuthority('Admins') or hasAuthority('USATTMatchOfficialsManagers')")
    public ResponseEntity<Void> update(@RequestBody Official official,
                                           @PathVariable Long officialId) {
        try {
            officialService.save(official);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
