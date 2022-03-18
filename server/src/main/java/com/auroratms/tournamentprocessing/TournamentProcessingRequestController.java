package com.auroratms.tournamentprocessing;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
@Slf4j
public class TournamentProcessingRequestController {

    @Autowired
    private TournamentProcessingRequestService service;

    /**
     * Creates or updates tournament processing data
     * @param tournamentProcessingRequest
     * @return
     */
    @PostMapping("/tournamentprocessingrequest")
    public @ResponseBody
    ResponseEntity<TournamentProcessingRequest> save (@RequestBody TournamentProcessingRequest tournamentProcessingRequest) {
        try {
            boolean creating = (tournamentProcessingRequest.getId() == null);
            TournamentProcessingRequest savedTournamentProcessingRequest = service.save(tournamentProcessingRequest);
            URI uri = new URI("/api/tournamentprocessingrequest/" + savedTournamentProcessingRequest.getId());
            return (creating)
                    ? ResponseEntity.created(uri).body(savedTournamentProcessingRequest)
                    : ResponseEntity.ok(savedTournamentProcessingRequest);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gets tournament processing data
     *
     * @param tournamentProcessingRequestId
     * @return
     */
    @GetMapping("/tournamentprocessingrequest/{tournamentProcessingRequestId}")
    public ResponseEntity<TournamentProcessingRequest> get(@PathVariable Long tournamentProcessingRequestId) {
        try {
            TournamentProcessingRequest TournamentProcessingRequest = this.service.findById(tournamentProcessingRequestId);
            return ResponseEntity.ok(TournamentProcessingRequest);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

//    /**
//     * Deletes request
//     *
//     * @param tournamentProcessingDataId
//     * @return
//     */
//    @DeleteMapping("/tournamentprocessingrequest/{tournamentProcessingDataId}")
//    public ResponseEntity<Void> delete(@PathVariable Long tournamentProcessingDataId) {
//        try {
//            this.service.delete(tournamentProcessingDataId);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            return ResponseEntity.notFound().build();
//        }
//    }

    /**
     * Get page worth of tournament processing data
     *
     * @param params
     * @param pageable
     * @return
     */
    @GetMapping("/tournamentprocessingrequests")
    public ResponseEntity<Page<TournamentProcessingRequest>> list(
            @RequestParam Map<String, String> params,
            Pageable pageable) {
        try {
            String tournamentId = params.get("tournamentId");
            String nameContains = params.get("nameContains");
            System.out.println("nameContains = " + nameContains);
            if (tournamentId == null) {
                nameContains = (StringUtils.isNotEmpty(nameContains)) ? nameContains : "";
                Page<TournamentProcessingRequest> page = this.service.findByName(nameContains, pageable);
                return ResponseEntity.ok(page);
            } else {
                TournamentProcessingRequest tournamentProcessingRequest = this.service.findByTournamentId(Long.parseLong(tournamentId));
                List<TournamentProcessingRequest> list = (tournamentProcessingRequest != null) ? Collections.singletonList(tournamentProcessingRequest)
                        : Collections.emptyList();
                Page<TournamentProcessingRequest> page = new PageImpl<>(list, pageable, list.size());
                return ResponseEntity.ok(page);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
