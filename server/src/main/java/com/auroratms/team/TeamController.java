package com.auroratms.team;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.*;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
@Slf4j
public class TeamController {

    @Autowired
    private TeamService teamService;

    /**
     *
     * @param team
     * @return
     */
    @PostMapping("/team")
    @ResponseBody
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        try {
            Team savedTeam = teamService.save(team);
            return new ResponseEntity<>(savedTeam, HttpStatus.CREATED);
        } catch (Exception e) {
            String message = e.getMessage();
            return new ResponseEntity(message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     *team
     * @param teamId
     * @return
     */
    @GetMapping("/team/{teamId}")
    @ResponseBody
    public ResponseEntity<Team> getTeam(@PathVariable Long teamId) {
        try {
            Team team = teamService.get(teamId);
            return new ResponseEntity<>(team, HttpStatus.OK);
        } catch (Exception e) {
            String message = e.getMessage();
            return new ResponseEntity(message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     *
     * @param team
     * @param teamId
     * @return
     */
    @PutMapping("/team/{teamId}")
    public ResponseEntity<Void> updateTeam(@RequestBody Team team,
                                       @PathVariable Long teamId) {
        try {
            Team savedTeam = teamService.save(team);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     *
     * @param params
     * @return
     */
    @GetMapping("/teams")
    public ResponseEntity<List<Team>> listTeams (@RequestParam Map<String, String> params) {
        try {
            List<Team> teams = Collections.emptyList();
            // list teams for one player
            String strTeamEvenIds = params.get("teamEventIds");
            String playerProfileId = params.get("playerProfileId");
            String tournamentId = params.get("tournamentId");
            if (StringUtils.isNotEmpty(strTeamEvenIds)) {
                String[] eventIdsArray = strTeamEvenIds.split(",");
                List<Long> eventIdsList = Arrays.stream(eventIdsArray)
                        .map(Long::parseLong)
                        .toList();
                if (StringUtils.isEmpty(playerProfileId)) {
                    teams = teamService.listForEvents(eventIdsList);
                } else {
                    teams = teamService.listByEventIdsAndProfile(eventIdsList, playerProfileId, Long.parseLong(tournamentId));
                }
            }
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            String message = e.getMessage();
            return new ResponseEntity(message, HttpStatus.BAD_REQUEST);
        }
    }
}
