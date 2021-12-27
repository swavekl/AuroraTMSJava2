package com.auroratms.tableusage;

import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
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
public class TableUsageController {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TableUsageService tableUsageService;

    /**
     *
     * @param tournamentId
     * @return
     */
    @GetMapping("/tableusages")
    @ResponseBody ResponseEntity<List<TableUsage>> list (@RequestParam long tournamentId) {
        try {
            List<TableUsage> tableUsageList = tableUsageService.list(tournamentId);
            if (tableUsageList.size() == 0) {
                Tournament tournament = this.tournamentService.getByKey(tournamentId);
                int numTables = tournament.getConfiguration().getNumberOfTables();
                tableUsageList = tableUsageService.create(tournamentId, numTables);
            }
            return ResponseEntity.ok(tableUsageList);
        } catch (Exception e) {
            log.error("Unable to find table usage", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update status of all passed in table usages
     * @return
     */
    @PutMapping("/tableusage")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    @ResponseBody ResponseEntity<List<TableUsage>> update (@RequestBody List<TableUsage> tableUsageList) {
        try {
            List<TableUsage> updatedTableUsageList = tableUsageService.updateAll(tableUsageList);
            return ResponseEntity.ok(updatedTableUsageList);
        } catch (Exception e) {
            log.error("Error while saving table usage", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
