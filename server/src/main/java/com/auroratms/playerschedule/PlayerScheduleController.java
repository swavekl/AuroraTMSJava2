package com.auroratms.playerschedule;

import com.auroratms.playerschedule.model.PlayerScheduleItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class PlayerScheduleController {

    @Autowired
    private PlayerScheduleService playerScheduleService;

    /**
     * Gets player schedule for all days of the tournament he/she entered
     *
     * @param tournamentEntryId tournament entry id
     * @param playerProfileId   player profile id
     * @return
     */
    @GetMapping("/playerschedule/{tournamentEntryId}/{playerProfileId}")
    @ResponseBody
    public ResponseEntity<List<PlayerScheduleItem>> getPlayerSchedule(@PathVariable Long tournamentEntryId,
                                                                      @PathVariable String playerProfileId) {
        try {
            List<PlayerScheduleItem> playerScheduleItems = playerScheduleService.getPlayerSchedule(tournamentEntryId, playerProfileId);
            return new ResponseEntity(playerScheduleItems, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets all scheduling and status details for one event or match specified by matchcardid
     *
     * @param matchCardId match card id
     * @return
     */
    @GetMapping("/playerschedule/detail/{matchCardId}")
    @ResponseBody
    public ResponseEntity<PlayerScheduleItem> getPlayerSchedule(@PathVariable Long matchCardId) {
        try {
            PlayerScheduleItem playerScheduleItem = playerScheduleService.getPlayerSchedule(matchCardId);
            return new ResponseEntity<>(playerScheduleItem, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.NOT_FOUND);
        }
    }
}
