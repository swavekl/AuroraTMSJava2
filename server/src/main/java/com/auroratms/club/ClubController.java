package com.auroratms.club;

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
@Transactional
public class ClubController {

    @Autowired
    private ClubService clubService;

    @GetMapping("/clubs")
    public ResponseEntity<Page<ClubEntity>> listClubs(@RequestParam Map<String, String> params, Pageable pageable) {
        String nameContains = params.get("nameContains");
        nameContains = (!StringUtils.isEmpty(nameContains)) ? nameContains : "";
        Page<ClubEntity> page = clubService.findByNameLike(nameContains, pageable);
        return new ResponseEntity<Page<ClubEntity>>(page, HttpStatus.OK);
    }

    @PostMapping("/club")
    @ResponseBody
    public ResponseEntity<ClubEntity> save(@RequestBody ClubEntity club) {
        try {
            ClubEntity savedClub = clubService.save(club);
            return new ResponseEntity<>(savedClub, HttpStatus.CREATED);
        } catch (Exception e) {
            String message = e.getMessage();
            message = message.substring(message.indexOf("{"));
            return new ResponseEntity(message, HttpStatus.BAD_REQUEST);
        }
    }
}
