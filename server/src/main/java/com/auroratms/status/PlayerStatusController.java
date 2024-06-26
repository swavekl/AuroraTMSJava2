package com.auroratms.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
public class PlayerStatusController {

    @Autowired
    private PlayerStatusService playerStatusService;

    /**
     * lists status of all or one player for this tournament and day
     *
     * @param tournamentId  id of the tournament
     * @param tournamentDay
     */
    @GetMapping("/playerstatuses")
    @ResponseBody
    public ResponseEntity<List<PlayerStatus>> list(@RequestParam Long tournamentId,
                                                   @RequestParam Integer tournamentDay,
                                                   @RequestParam(required = false) String playerProfileId,
                                                   @RequestParam(required = false) Long eventId,
                                                   @RequestParam(required = false) Boolean isDailyCheckin) {
        try {
            List<PlayerStatus> playerStatuses = null;
            if (playerProfileId != null) {
                playerStatuses = playerStatusService.listOnePlayer(playerProfileId, tournamentId, tournamentDay, eventId);
            } else {
                if (eventId == null) {
                    if (tournamentDay == 0) {
                        playerStatuses = playerStatusService.listAllPlayers(tournamentId);
                    } else {
                        playerStatuses = playerStatusService.listAllPlayersByDay(tournamentId, tournamentDay);

                    }
                } else {
                    // for one event
                    playerStatuses = playerStatusService.listPlayersForEvent(tournamentId, tournamentDay, eventId, isDailyCheckin);
                }
            }
            return new ResponseEntity<>(playerStatuses, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/playerstatus")
    @ResponseBody
    public ResponseEntity<PlayerStatus> create (@RequestBody PlayerStatus playerStatus) {
        try {
            PlayerStatus savedPlayerStatus = playerStatusService.save(playerStatus);
            return new ResponseEntity<>(savedPlayerStatus, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/playerstatus")
    @ResponseBody
    public ResponseEntity<PlayerStatus> update (@RequestBody PlayerStatus playerStatus) {
        try {
            PlayerStatus savedPlayerStatus = playerStatusService.save(playerStatus);
            return new ResponseEntity(savedPlayerStatus, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
