package com.auroratms.draw;

import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubService;
import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Rest API controller for manipulating event draws
 */
@RestController
@RequestMapping("api/draws")
@PreAuthorize("isAuthenticated()")
@Transactional
public class DrawController {

    private DrawService drawService;

    private TournamentEventEntityService eventService;

    private TournamentEventEntryService eventEntryService;

    private TournamentEntryService entryService;

    private UserProfileExtService userProfileExtService;

    private UserProfileService userProfileService;

    private UsattDataService usattDataService;

    private ClubService clubService;

    public DrawController(DrawService drawService,
                          TournamentEventEntityService eventService,
                          TournamentEventEntryService eventEntryService,
                          TournamentEntryService entryService,
                          UserProfileExtService userProfileExtService,
                          UserProfileService userProfileService,
                          UsattDataService usattDataService,
                          ClubService clubService) {
        this.drawService = drawService;
        this.eventService = eventService;
        this.eventEntryService = eventEntryService;
        this.entryService = entryService;
        this.userProfileExtService = userProfileExtService;
        this.userProfileService = userProfileService;
        this.usattDataService = usattDataService;
        this.clubService = clubService;
    }

    /**
     * gets list of all draw items for given event
     *
     * @param eventId
     * @param drawType
     * @return
     */
    @GetMapping("")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    public ResponseEntity<List<DrawItem>> listAll(@RequestParam long eventId,
                                                  @RequestParam(required = false) DrawType drawType) {
        List<DrawItem> drawItems = this.drawService.list(eventId, drawType);

        // now enhance this information with player name, club name and state
        // get profiles of players in this event
        List<String> profileIds = new ArrayList<>(drawItems.size());
        for (DrawItem drawItem : drawItems) {
            profileIds.add(drawItem.getPlayerId());
        }


        // if this event has more than one round RR followed by SE then delete that too
        TournamentEventEntity thisEvent = this.eventService.get(eventId);
        if (!thisEvent.isSingleElimination() && thisEvent.getPlayersToAdvance() > 0) {
            List<DrawItem> seRoundDrawItems = this.drawService.list(eventId, DrawType.SINGLE_ELIMINATION);
            drawItems.addAll(seRoundDrawItems);
        }

        // get their profile exts containing club ids
        // collect them in unique set
        Map<String, UserProfileExt> userProfileExtMap = userProfileExtService.findByProfileIds(profileIds);
        Set<Long> clubIdsSet = new HashSet<>();
        Map<Long, String> mapMembershipToProfileId = new HashMap<>();
        for (UserProfileExt userProfileExt : userProfileExtMap.values()) {
            if (userProfileExt.getClubFk() != null) {
                clubIdsSet.add(userProfileExt.getClubFk());
            }
            mapMembershipToProfileId.put(userProfileExt.getMembershipId(), userProfileExt.getProfileId());
        }

        List<ClubEntity> clubEntityList = this.clubService.findAllByIdIn(new ArrayList<Long>(clubIdsSet));

        // set club name for player
        for (DrawItem drawItem : drawItems) {
            String playerId = drawItem.getPlayerId();
            UserProfileExt userProfileExt = userProfileExtMap.get(playerId);
            if (userProfileExt != null) {
                Long clubFk = userProfileExt.getClubFk();
                if (clubFk != null) {
                    for (ClubEntity clubEntity : clubEntityList) {
                        if (clubEntity.getId() == clubFk) {
                            drawItem.setClubName(clubEntity.getClubName());
                            break;
                        }
                    }
                }
            }
        }

        /**
         * Get personal player information
         */
        // get all player USATT records containing name and state
        List<Long> membershipIds = new ArrayList<>(mapMembershipToProfileId.keySet());
        List<UsattPlayerRecord> usattPlayerRecordList = this.usattDataService.findAllByMembershipIdIn(membershipIds);
        for (DrawItem drawItem : drawItems) {
            String profileId = drawItem.getPlayerId();
            UserProfileExt userProfileExt = userProfileExtMap.get(profileId);
            if (userProfileExt != null) {
                Long membershipId = userProfileExt.getMembershipId();
                for (UsattPlayerRecord usattPlayerRecord : usattPlayerRecordList) {
                    if (membershipId.equals(usattPlayerRecord.getMembershipId())) {
                        // todo state may be obsolete - we should be getting it from Okta
                        drawItem.setState(usattPlayerRecord.getState());
                        String fullName = usattPlayerRecord.getLastName() + ", " + usattPlayerRecord.getFirstName();
                        drawItem.setPlayerName(fullName);
                        break;
                    }
                }
            }
        }

        return new ResponseEntity<List<DrawItem>>(drawItems, HttpStatus.OK);
    }

    /**
     * Generates draws from scratch for given event
     *
     * @param eventId  event id
     * @param drawType draw type
     * @return
     */
    @PostMapping("")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    public ResponseEntity<List<DrawItem>> generateAll(@RequestParam long eventId,
                                                      @RequestParam DrawType drawType) {
        try {
            TournamentEventEntity thisEvent = this.eventService.get(eventId);

            // remove existing draw if any
            this.drawService.deleteDraws(eventId, drawType);

            // if this event has more than one round RR followed by SE then delete that too
            if (!thisEvent.isSingleElimination() && thisEvent.getPlayersToAdvance() > 0) {
                this.drawService.deleteDraws(eventId, DrawType.SINGLE_ELIMINATION);
            }

            // get all event entries into this event
            List<TournamentEventEntry> eventEntries = this.eventEntryService.listAllForEvent(thisEvent.getId());

            long tournamentFk = thisEvent.getTournamentFk();

            // get all players who entered tournament
            List<TournamentEntry> tournamentEntries = this.entryService.listForTournament(tournamentFk);
            Map<String, PlayerDrawInfo> profileIdToPlayerDrawInfo = new HashMap<>();
            Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = new HashMap<>();
            for (TournamentEntry tournamentEntry : tournamentEntries) {
                PlayerDrawInfo playerDrawInfo = new PlayerDrawInfo();
                playerDrawInfo.setProfileId(tournamentEntry.getProfileId());
                playerDrawInfo.setRating(tournamentEntry.getSeedRating());
                profileIdToPlayerDrawInfo.put(playerDrawInfo.getProfileId(), playerDrawInfo);
                entryIdToPlayerDrawInfo.put(tournamentEntry.getId(), playerDrawInfo);
            }

            // get additional player information - club id
            Set<Long> clubIdsSet = new HashSet<>();

            List<String> profileIds = new ArrayList<>(profileIdToPlayerDrawInfo.keySet());
            Map<String, UserProfileExt> userProfileExtMap = userProfileExtService.findByProfileIds(profileIds);
            Map<Long, PlayerDrawInfo> membershipIdToPlayerDrawInfo = new HashMap<>();
            for (UserProfileExt userProfileExt : userProfileExtMap.values()) {
                String profileId = userProfileExt.getProfileId();
                PlayerDrawInfo playerDrawInfo = profileIdToPlayerDrawInfo.get(profileId);
                if (playerDrawInfo != null) {
                    if (userProfileExt.getClubFk() != null) {
                        playerDrawInfo.setClubId(userProfileExt.getClubFk());
                        // collect club ids for quick name lookup
                        clubIdsSet.add(userProfileExt.getClubFk());
                    }
                    membershipIdToPlayerDrawInfo.put(userProfileExt.getMembershipId(), playerDrawInfo);
                }
            }

            // get all club names into a map of club id to club name
            List<ClubEntity> clubList = this.clubService.findAllByIdIn(new ArrayList<Long>(clubIdsSet));
            Map<Long, String> clubIdToClubNameMap = new HashMap<>();
            for (ClubEntity clubEntity : clubList) {
                clubIdToClubNameMap.put(clubEntity.getId(), clubEntity.getClubName());
            }
            for (PlayerDrawInfo playerDrawInfo : profileIdToPlayerDrawInfo.values()) {
                Long clubId = playerDrawInfo.getClubId();
                if (clubId != null) {
                    String clubName = clubIdToClubNameMap.get(clubId);
                    playerDrawInfo.setClubName(clubName);
                }
            }

            // todo make this better
            // get the state from player record - instead of going to Okta
            // get state and name
            List<Long> membershipIds = new ArrayList<>(membershipIdToPlayerDrawInfo.keySet());
            List<UsattPlayerRecord> usattPlayerRecordList = this.usattDataService.findAllByMembershipIdIn(membershipIds);
            for (UsattPlayerRecord usattPlayerRecord : usattPlayerRecordList) {
                Long membershipId = usattPlayerRecord.getMembershipId();
                PlayerDrawInfo playerDrawInfo = membershipIdToPlayerDrawInfo.get(membershipId);
                if (playerDrawInfo != null) {
                    playerDrawInfo.setState(usattPlayerRecord.getState());
                    String fullName = usattPlayerRecord.getLastName() + ", " + usattPlayerRecord.getFirstName();
                    playerDrawInfo.setPlayerName(fullName);
                }
            }

            // get list of other event entries
            List<Long> otherEventIds = new ArrayList<>();
            Collection<TournamentEventEntity> tournamentEventEntities = this.eventService.list(tournamentFk, Pageable.unpaged());
            for (TournamentEventEntity event : tournamentEventEntities) {
                if (!event.getId().equals(thisEvent.getId())) {
                    otherEventIds.add(event.getId());
                }
            }

            // get draws for these other events so we can find and mitigate conflicts
            List<DrawItem> existingDrawItems = this.drawService.listAllDrawsForTournament(otherEventIds);

            // finally make the draws and save them
            List<DrawItem> drawItems = this.drawService.generateDraws(thisEvent, drawType, eventEntries,
                    existingDrawItems, entryIdToPlayerDrawInfo);

            // see if we need to create draw for single elimination round
            if (thisEvent.getPlayersToAdvance() > 0) {
                List<TournamentEventEntry> seSimulatedEventEntries = generateSEEventEntriesFromDraws(
                        drawItems, eventEntries, thisEvent, entryIdToPlayerDrawInfo);
                fillCityStateCountryForSEPlayers (seSimulatedEventEntries, entryIdToPlayerDrawInfo);
                List<DrawItem> seDrawItems = this.drawService.generateDraws(thisEvent, DrawType.SINGLE_ELIMINATION,
                        seSimulatedEventEntries, existingDrawItems, entryIdToPlayerDrawInfo);
                drawItems.addAll(seDrawItems);
            }
            
            // response
            return new ResponseEntity<List<DrawItem>>(drawItems, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<List<DrawItem>>(Collections.EMPTY_LIST, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     *
     * @param seSimulatedEventEntries
     * @param entryIdToPlayerDrawInfo
     */
    private void fillCityStateCountryForSEPlayers(List<TournamentEventEntry> seSimulatedEventEntries, Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        long start = System.currentTimeMillis();
        Map<String, PlayerDrawInfo> profileIdToInfoMap = new HashMap<>();
        List<String> profileIds = new ArrayList<>(seSimulatedEventEntries.size());
        for (TournamentEventEntry seSimulatedEventEntry : seSimulatedEventEntries) {
            long tournamentEntryFk = seSimulatedEventEntry.getTournamentEntryFk();
            PlayerDrawInfo playerDrawInfo = entryIdToPlayerDrawInfo.get(tournamentEntryFk);
            if (playerDrawInfo != null) {
                String profileId = playerDrawInfo.getProfileId();
                profileIds.add(profileId);
                profileIdToInfoMap.put(profileId, playerDrawInfo);
            }
        }

        // request user profiles in bulk
        Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIds);
        // enrich the information in the player draw info
        for (UserProfile userProfile : userProfiles) {
            String profileId = userProfile.getUserId();
            PlayerDrawInfo playerDrawInfo = profileIdToInfoMap.get(profileId);
            if (playerDrawInfo != null) {
                playerDrawInfo.setCity(userProfile.getCity());
                playerDrawInfo.setState(userProfile.getState());
                playerDrawInfo.setCountry(userProfile.getCountryCode());
//                System.out.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\"",
//                        playerDrawInfo.getPlayerName(), playerDrawInfo.getClubName(), playerDrawInfo.getCity(), playerDrawInfo.getState(), playerDrawInfo.getCountry()));
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Fetch profiles for " + userProfiles.size() + " players took " + (end - start) + " ms");
    }


    /**
     *
     * @param rrDrawItems
     * @param rrEventEntries
     * @param tournamentEventEntity
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    private List<TournamentEventEntry> generateSEEventEntriesFromDraws(List<DrawItem> rrDrawItems,
                                                                      List<TournamentEventEntry> rrEventEntries,
                                                                      TournamentEventEntity tournamentEventEntity,
                                                                      Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        List<TournamentEventEntry> seEventEntries = new ArrayList<>();
        int playersToAdvance = tournamentEventEntity.getPlayersToAdvance();
        for (DrawItem rrDrawItem : rrDrawItems) {
            if (rrDrawItem.getPlaceInGroup() <= playersToAdvance) {
                String playerId = rrDrawItem.getPlayerId();
                TournamentEventEntry eventEntry = makeEventEntry(playerId, rrEventEntries, entryIdToPlayerDrawInfo);
                if (eventEntry != null) {
                    seEventEntries.add(eventEntry);
                }
            }
        }

        return seEventEntries;
    }

    /**
     *
     * @param playerId
     * @param rrEventEntries
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    private TournamentEventEntry makeEventEntry(String playerId,
                                                List<TournamentEventEntry> rrEventEntries,
                                                Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        for (Map.Entry<Long, PlayerDrawInfo> playerDrawInfoEntry : entryIdToPlayerDrawInfo.entrySet()) {
            PlayerDrawInfo playerDrawInfo = playerDrawInfoEntry.getValue();
            if (playerDrawInfo.getProfileId().equals(playerId)) {
                Long entryId = playerDrawInfoEntry.getKey();
//                System.out.println(
//                        String.format("map.put(%d, makePlayerDrawInfo(\"%s\", \"%s\", \"%s\", \"%s\", %d));",
//                        entryId, playerDrawInfo.getPlayerName(), playerDrawInfo.getClubName(), playerDrawInfo.getCity(),
//                                playerDrawInfo.getState(), playerDrawInfo.getRating()));
                for (TournamentEventEntry rrEventEntry : rrEventEntries) {
                    if (rrEventEntry.getTournamentEntryFk() == entryId) {
//                        System.out.println(String.format("makeTournamentEventEntry(%d, %d, %d);",
//                                rrEventEntry.getId(), rrEventEntry.getTournamentEventFk(), rrEventEntry.getTournamentEntryFk()));
                        return rrEventEntry;
                    }
                }
            }
        }
        return null;
    }



    /**
     * Updates existing draw
     *
     * @param eventId
     * @param drawType
     * @param updatedDrawItems
     */
    @PatchMapping("")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    public void update(@RequestParam long eventId,
                       @RequestParam DrawType drawType,
                       @RequestBody List<DrawItem> updatedDrawItems) {
        this.drawService.updateDraws(updatedDrawItems);
    }

    /**
     * Deletes the draw
     * @param eventId
     */
    @DeleteMapping("")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    public void deleteAll(@RequestParam long eventId) {
        TournamentEventEntity thisEvent = this.eventService.get(eventId);
        boolean singleElimination = thisEvent.isSingleElimination();
        if (singleElimination) {
            this.drawService.deleteDraws(eventId, DrawType.SINGLE_ELIMINATION);
        } else {
            // if RR is followed by Single elimination then delte that too
            this.drawService.deleteDraws(eventId, DrawType.ROUND_ROBIN);
            if (thisEvent.getPlayersToAdvance() > 0) {
                this.drawService.deleteDraws(eventId, DrawType.SINGLE_ELIMINATION);
            }
        }
    }
}