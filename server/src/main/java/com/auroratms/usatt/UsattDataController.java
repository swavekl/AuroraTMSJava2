package com.auroratms.usatt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class UsattDataController {

    @Autowired
    private UsattDataService usattDataService;

    /**
     * search players
     * @param params
     * @param pageable
     * @return
     */
    @GetMapping("/usattplayers")
    public List<UsattPlayerRecord> listPlayers (@RequestParam Map<String,String> params, Pageable pageable) {
        if (params.containsKey("firstName") && params.containsKey("lastName")) {
            String firstName = params.get("firstName");
            String lastName = params.get("lastName");
            return this.usattDataService.findAllPlayersByNames(firstName, lastName, pageable);
        }
        return Collections.emptyList();
    }

    /**
     * find one player
     * @param params
     * @return
     */
    @GetMapping("/usattplayer")
    public UsattPlayerRecord getPlayer (@RequestParam Map<String,String> params) {
        if (params.containsKey("firstName") && params.containsKey("lastName")) {
            String firstName = params.get("firstName");
            String lastName = params.get("lastName");
            return this.usattDataService.getPlayerByNames(firstName, lastName);
        } else if (params.containsKey("membershipId")) {
            String strMembershipId = params.get("membershipId");
            Long membershipId = Long.parseLong(strMembershipId);
            return this.usattDataService.getPlayerByMembershipId(membershipId);
        } else {
            throw new RuntimeException("Player not found");
        }
    }

//    @PreAuthorize("hasAuthority('Admins')")
    public void processFile(String filename) {
        List<UsattPlayerRecord> usattPlayerRecords = this.usattDataService.readAllPlayersFromFile(filename);
        if (usattPlayerRecords.size() > 0) {
            this.usattDataService.insertPlayerData(usattPlayerRecords);
        }
    }
}
