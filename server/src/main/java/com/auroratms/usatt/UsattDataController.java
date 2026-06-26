package com.auroratms.usatt;

import com.auroratms.justgo.JustGoRatingsService;
import com.auroratms.ratingsprocessing.RatingsProcessorStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class UsattDataController {

    @Autowired
    private UsattDataService usattDataService;

    @Autowired
    private JustGoRatingsService justGoRatingsService;

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
//            return this.justGoRatingsService.findPlayerRecordByName(firstName, lastName);
            return this.usattDataService.getPlayerByNames(firstName, lastName);
        } else if (params.containsKey("membershipId")) {
            String strMembershipId = params.get("membershipId");
            Long membershipId = Long.parseLong(strMembershipId);
//            return this.justGoRatingsService.findPlayerRecordByMembershipId(membershipId);
            return this.usattDataService.getPlayerByMembershipId(membershipId);
        } else {
            throw new RuntimeException("Player not found");
        }
    }

    /**
     * Links Okta profile id with USATT membership id
     * @param usattPlayerRecord
     * @param profileId
     * @return
     */
    @PostMapping("/usattplayer/{profileId}")
    public UsattPlayerRecord linkPlayerToProfile(@RequestBody UsattPlayerRecord usattPlayerRecord,
                                                 @PathVariable String profileId) {
        return this.usattDataService.linkPlayerToProfile(usattPlayerRecord, profileId);
    }

//    @PreAuthorize("hasAuthority('Admins')")
    public void processFile(String filename) {
        RatingsProcessorStatus ratingsProcessorStatus = new RatingsProcessorStatus();
        List<UsattPlayerRecord> usattPlayerRecords = this.usattDataService.readAllPlayersFromFile(filename, ratingsProcessorStatus);
        if (usattPlayerRecords.size() > 0) {
            this.usattDataService.insertPlayerData(usattPlayerRecords, ratingsProcessorStatus);
        }
    }

    /**
     * Checks the availability of a USATT membership ID to determine whether it can be mapped
     * to the currently logged-in user's profile ID.
     *
     * @param membershipId the USATT membership ID to check for availability
     * @param profileId the profile ID of the currently logged-in user
     * @return a ResponseEntity containing a map with a single key "isAvailable".
     *         The value is {@code true} if the membership ID is available (either unmapped or mapped
     *         to the current profile ID), and {@code false} if it is already mapped to another profile ID.
     */
    @GetMapping("/usattplayer/checkavailability")
    public ResponseEntity<Map<String, String>> checkAvailability(
            @RequestParam Long membershipId,
            @RequestParam String profileId) {

        Map<String, String> result = this.usattDataService.checkMembershipAvailability(membershipId, profileId);
        return ResponseEntity.ok(result);
    }
}
