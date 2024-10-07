package com.auroratms.umpire;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/umpire")
@PreAuthorize("isAuthenticated()")
@Transactional
@Slf4j
public class UmpiringController {

    @Autowired
    private UmpiringService umpiringService;

    /**
     * Assigns umpires to the specified match
     *
     * @param umpireWork
     * @return
     */
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Referees')")
    @ResponseBody
    public ResponseEntity<Void> assignOfficials(@RequestBody UmpireWork umpireWork) {
        try {
            umpiringService.assignOfficials(umpireWork);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Unable to assign umpires for the match with id " + umpireWork.getMatchFk(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gets summary of matches umpired by each umpire assigned to this tournament
     *
     * @param tournamentId
     * @return
     */
    @GetMapping("/summary/{tournamentId}")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Referees')")
    @ResponseBody
    public ResponseEntity<List<UmpireWorkSummary>> getSummaries(@PathVariable Long tournamentId) {
        try {
            List<UmpireWorkSummary> summaries = umpiringService.getSummaries(tournamentId);
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            log.error("Unable to get umpire summaries for tournament " + tournamentId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gets a list of all matches at all tournament umpired by this umpire
     *
     * @param profileId
     * @return
     */
    @GetMapping("/matches/{profileId}")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Referees')")
    @ResponseBody
    public ResponseEntity<List<UmpiredMatchInfo>> getUmpiredMatches(@PathVariable String profileId) {
        try {
            List<UmpiredMatchInfo> umpiredMatchInfos = umpiringService.getUmpiredMatches(profileId);
            return ResponseEntity.ok(umpiredMatchInfos);
        } catch (Exception e) {
            log.error("Unable to get umpired match list for umpire with profile id " + profileId, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
