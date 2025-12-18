package com.auroratms.team;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<Team> create(@RequestBody Team team) {
        try {
            Team savedTeam = teamService.save(team);
            return new ResponseEntity<>(savedTeam, HttpStatus.CREATED);
        } catch (Exception e) {
            String message = e.getMessage();
            message = message.substring(message.indexOf("{"));
            return new ResponseEntity(message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     *
     * @param teamId
     * @return
     */
    @GetMapping("/team/{teamId}")
    @ResponseBody
    public ResponseEntity<Team> create(@PathVariable Long teamId) {
        try {
            Team team = teamService.get(teamId);
            return new ResponseEntity<>(team, HttpStatus.OK);
        } catch (Exception e) {
            String message = e.getMessage();
            message = message.substring(message.indexOf("{"));
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
    public ResponseEntity<Void> update(@RequestBody Team team,
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
    public ResponseEntity<List<Team>> list (@RequestParam Map<String, String> params) {
        try {
            List<Team> teams = Collections.emptyList();
            // list teams for one player
            String strTeamEvenIds = params.get("teamEventIds");
            if (StringUtils.isNotEmpty(strTeamEvenIds)) {
                String[] eventIdsArray = strTeamEvenIds.split(",");
                List<Long> eventIdsList = Arrays.stream(eventIdsArray)
                        .map(Long::parseLong)
                        .toList();
                teams = teamService.listForEvents(eventIdsList);
            }
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            String message = e.getMessage();
            return new ResponseEntity(message, HttpStatus.BAD_REQUEST);
        }
    }
}
