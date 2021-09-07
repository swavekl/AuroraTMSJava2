package com.auroratms.tiebreaking;

import com.auroratms.tiebreaking.model.GroupTieBreakingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for tie breaking procedures i.e. who took which place in a group in round robin round
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
public class TieBreakingController {

    @Autowired
    private TieBreakingService tieBreakingService;

    @GetMapping("/tiebreaking/{matchCardId}")
    public ResponseEntity<GroupTieBreakingInfo> performTieBreaking (@PathVariable Long matchCardId) {
        try {
            GroupTieBreakingInfo groupTieBreakingInfo = this.tieBreakingService.rankAndAdvancePlayers(matchCardId);
            return new ResponseEntity(groupTieBreakingInfo, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tiebreaking/{matchCardId}/explain")
    public ResponseEntity<GroupTieBreakingInfo> performTieBreakingAndExplain (@PathVariable Long matchCardId) {
        try {
            GroupTieBreakingInfo groupTieBreakingInfo = this.tieBreakingService.rankAndExplain(matchCardId);
            return new ResponseEntity(groupTieBreakingInfo, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
