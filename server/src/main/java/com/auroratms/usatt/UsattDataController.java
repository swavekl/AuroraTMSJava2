package com.auroratms.usatt;

import com.auroratms.justgo.ApiPlayerDto;
import com.auroratms.justgo.JustGoRatingsService;
import com.auroratms.ratingsprocessing.RatingsProcessorStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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
            if (!this.usattDataService.existsPlayerByName(firstName, lastName)) {
                // possibly new member so
                ApiPlayerDto playerRecordByName = this.justGoRatingsService.findPlayerRecordByName(firstName, lastName);
                if (playerRecordByName != null) {
                    UsattPlayerRecord newUsattPlayerRecord = this.toUsattPlayerRecord(playerRecordByName);
                    this.usattDataService.saveAllAndFlush(List.of(newUsattPlayerRecord));
                }
            }

            UsattPlayerRecord playerByNames = this.usattDataService.getPlayerByNames(firstName, lastName);
            if (playerByNames != null && playerByNames.getMemberGuid() == null) {
                try {
                    // update justgo UUID
                    Long membershipId = playerByNames.getMembershipId();
                    ApiPlayerDto playerRecordByMembershipId = this.justGoRatingsService.findPlayerRecordByMembershipId(membershipId);
                    if (playerRecordByMembershipId != null) {
                        playerByNames.setMemberGuid(playerRecordByMembershipId.getId());
                        this.usattDataService.saveAllAndFlush(List.of(playerByNames));
                    }
                } catch (Exception e) {
                    // ignore it maybe there is no player like that at all - like our test players.
                }
            }
            return playerByNames;
        } else if (params.containsKey("membershipId")) {
            String strMembershipId = params.get("membershipId");
            Long membershipId = Long.parseLong(strMembershipId);
            UsattPlayerRecord usattPlayerRecord = this.usattDataService.getPlayerByMembershipId(membershipId);
            if (usattPlayerRecord != null) {
                // we may have a USATT player record in our database but may not have the GUID
                // get it first so we can get the latest membership information
                String memberGuid = usattPlayerRecord.getMemberGuid();
                if (StringUtils.isEmpty(memberGuid)) {
                    try {
                        ApiPlayerDto justGoPlayerRecordByMembershipId = this.justGoRatingsService.findPlayerRecordByMembershipId(membershipId);
                        if (justGoPlayerRecordByMembershipId != null) {
                            memberGuid = justGoPlayerRecordByMembershipId.getId();
                            usattPlayerRecord.setMemberGuid(memberGuid);
                            this.usattDataService.saveAllAndFlush(List.of(usattPlayerRecord));
                        }
                    } catch (Exception e) {
                        // ignore it maybe there is no player like that at all - like our test players.
                    }
                }
                // check if perhaps they updated their membership just now
                ApiPlayerDto justGoPlayerRecord = StringUtils.isNotEmpty(memberGuid)
                        ? this.justGoRatingsService.findPlayerRecordByGUID(memberGuid) : null;
                if (justGoPlayerRecord != null) {
                    String newMembershipExpirationDate = justGoPlayerRecord.getMembershipExpirationDate();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String strExpirationDate = dateFormat.format(usattPlayerRecord.getMembershipExpirationDate());
                    String newMembershipType = justGoPlayerRecord.getMembershipType();
                    if ((!StringUtils.equalsIgnoreCase(usattPlayerRecord.getMembershipType(), newMembershipType) ||
                        !StringUtils.equals(strExpirationDate, newMembershipExpirationDate)) &&
                         StringUtils.isNotEmpty(newMembershipExpirationDate)) {
                        usattPlayerRecord.setMembershipType(newMembershipType);
                        try {
                            Date newExpirationDate = dateFormat.parse(newMembershipExpirationDate);
                            usattPlayerRecord.setMembershipExpirationDate(newExpirationDate);
                            this.usattDataService.saveAllAndFlush(List.of(usattPlayerRecord));
                        } catch (ParseException e) {

                        }
                    }
                }
            }
            return usattPlayerRecord;
        } else {
            throw new RuntimeException("Player not found");
        }
    }

    /**
     *
     * @param apiPlayerDto
     * @return
     */
    private UsattPlayerRecord toUsattPlayerRecord(ApiPlayerDto apiPlayerDto) {
        UsattPlayerRecord usattPlayerRecord = new UsattPlayerRecord();

        usattPlayerRecord.setMembershipId(Long.parseLong(apiPlayerDto.getMemberNumber()));
        usattPlayerRecord.setFirstName(apiPlayerDto.getFirstName());
        usattPlayerRecord.setLastName(apiPlayerDto.getLastName());
        usattPlayerRecord.setGender(apiPlayerDto.getGender());
        usattPlayerRecord.setCity(apiPlayerDto.getTown());
        usattPlayerRecord.setState(apiPlayerDto.getCounty());
        usattPlayerRecord.setZip(apiPlayerDto.getPostCode());
        usattPlayerRecord.setCountry(apiPlayerDto.getCountry());
        usattPlayerRecord.setHomeClub(apiPlayerDto.getClubName());

        if (apiPlayerDto.getDob() != null) {
            try {
                // "dob": "1990-03-20",
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date dateOfBirth = formatter.parse(apiPlayerDto.getDob());
                usattPlayerRecord.setDateOfBirth(dateOfBirth);
            } catch (ParseException e) {
                System.out.println("unable to parse date of birth = " + e);
            }
        }
        if (apiPlayerDto.getMembershipExpirationDate() != null) {
            try {
                // "endDate": "2025-11-24",
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date expirationDate = formatter.parse(apiPlayerDto.getMembershipExpirationDate());
                usattPlayerRecord.setMembershipExpirationDate(expirationDate);
            } catch (ParseException e) {
                System.out.println("unable to parse date of birth = " + e);
            }
        }
        usattPlayerRecord.setMembershipType(apiPlayerDto.getMembershipType());
        usattPlayerRecord.setMemberGuid(apiPlayerDto.getId());
        return usattPlayerRecord;
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
