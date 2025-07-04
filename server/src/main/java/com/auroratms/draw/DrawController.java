package com.auroratms.draw;

import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubService;
import com.auroratms.draw.conflicts.ConflictFinder;
import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.draw.generation.singleelim.SingleEliminationEntriesConverter;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Rest API controller for manipulating event draws
 */
@Slf4j
@RestController
@RequestMapping("api/draws")
@PreAuthorize("isAuthenticated()")
@Transactional
public class DrawController {

    public static final String PLAYER_A_SIDE = "A";
    public static final String PLAYER_B_SIDE = "B";

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
//    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    public ResponseEntity<List<DrawItem>> listAll(@RequestParam long eventId,
                                                  @RequestParam(required = false) DrawType drawType) {
        List<DrawItem> drawItems = this.drawService.list(eventId, drawType);

        TournamentEvent thisEvent = this.eventService.get(eventId);

        // now enhance this information with player name, club name and state
        // get profiles of players in this event
        List<String> profileIds = new ArrayList<>(drawItems.size());
        for (DrawItem drawItem : drawItems) {
            String profileId = drawItem.getPlayerId();
            // doubles event has playerA/playerB profile ids
            if (thisEvent.isDoubles()) {
                String[] playersProfileIds = profileId.split(";");
                profileIds.addAll(Arrays.asList(playersProfileIds));
            } else {
                profileIds.add(profileId);
            }
        }


        // if this event has more than one round RR followed by SE then get those entries too
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

        // set club name for player(s)
        for (DrawItem drawItem : drawItems) {
            String profileId = drawItem.getPlayerId();
            if (!profileId.equals(DrawItem.TBD_PROFILE_ID) && !StringUtils.isEmpty(profileId)) {
                // doubles event has playerA/playerB profile ids
                if (thisEvent.isDoubles()) {
                    String[] playersProfileIds = profileId.split(";");
                    fillDoublesTeamClubNames(userProfileExtMap, clubEntityList, drawItem, playersProfileIds);
                } else {
                    fillClubName(userProfileExtMap, clubEntityList, drawItem, profileId);
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
            if (!profileId.equals(DrawItem.TBD_PROFILE_ID) && !StringUtils.isEmpty(profileId)) {
                if (thisEvent.isDoubles()) {
                    String[] playersProfileIds = profileId.split(";");
                    fillDoublesTeamPlayerNames(userProfileExtMap, usattPlayerRecordList, drawItem, playersProfileIds);
                } else {
                    fillPlayerNameAndState(userProfileExtMap, usattPlayerRecordList, drawItem, profileId);
                }
            }
        }

        return new ResponseEntity<List<DrawItem>>(drawItems, HttpStatus.OK);
    }

    /**
     * Helper function for setting player name in draw item
     * @param userProfileExtMap
     * @param usattPlayerRecordList
     * @param drawItem
     * @param profileId
     */
    private void fillPlayerNameAndState(Map<String, UserProfileExt> userProfileExtMap, List<UsattPlayerRecord> usattPlayerRecordList, DrawItem drawItem, String profileId) {
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

    /**
     * Helper function for setting player name in draw item
     * @param userProfileExtMap
     * @param usattPlayerRecordList
     * @param drawItem
     * @param profileIds
     */
    private void fillDoublesTeamPlayerNames(Map<String, UserProfileExt> userProfileExtMap, List<UsattPlayerRecord> usattPlayerRecordList, DrawItem drawItem, String [] profileIds) {
        String playerAProfileId = (profileIds.length >= 1) ? profileIds[0] : DrawItem.TBD_PROFILE_ID;
        String playerBProfileId = (profileIds.length == 2) ? profileIds[1] : DrawItem.TBD_PROFILE_ID;
        fillPlayerNameAndState(userProfileExtMap, usattPlayerRecordList, drawItem, playerAProfileId);
        String playerAState = (!StringUtils.isEmpty(drawItem.getState())) ? drawItem.getState() : "N/A";
        String playerAName = drawItem.getPlayerName();
        if (profileIds.length == 2) {
            fillPlayerNameAndState(userProfileExtMap, usattPlayerRecordList, drawItem, playerBProfileId);
        }
        String playerBState = (profileIds.length == 2 && !StringUtils.isEmpty(drawItem.getState())) ? drawItem.getState() : "N/A";
        String playerBName = (profileIds.length == 2) ? drawItem.getPlayerName() : "";

        String teamPlayerNames = playerAName + " / " + playerBName;
        drawItem.setPlayerName(teamPlayerNames);

        String teamPlayerStates = playerAState + " / " + playerBState;
        drawItem.setState(teamPlayerStates);
    }

    /**
     * Helper function for filling club name in draw item
     * @param userProfileExtMap
     * @param clubEntityList
     * @param drawItem
     * @param profileId
     */
    private void fillClubName(Map<String, UserProfileExt> userProfileExtMap, List<ClubEntity> clubEntityList, DrawItem drawItem, String profileId) {
        UserProfileExt userProfileExt = userProfileExtMap.get(profileId);
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
     *
     * @param userProfileExtMap
     * @param clubEntityList
     * @param drawItem
     * @param profileIds
     */
    private void fillDoublesTeamClubNames(Map<String, UserProfileExt> userProfileExtMap, List<ClubEntity> clubEntityList, DrawItem drawItem, String [] profileIds) {
        String playerAProfileId = (profileIds.length >= 1) ? profileIds[0] : DrawItem.TBD_PROFILE_ID;
        String playerBProfileId = (profileIds.length == 2) ? profileIds[1] : DrawItem.TBD_PROFILE_ID;
        fillClubName(userProfileExtMap, clubEntityList, drawItem, playerAProfileId);
        String playerAClubName = (!StringUtils.isEmpty(drawItem.getClubName())) ? drawItem.getClubName() : "N/A";
        if (profileIds.length == 2) {
            fillClubName(userProfileExtMap, clubEntityList, drawItem, playerBProfileId);
        }
        String playerBClubName = (profileIds.length == 2 && !StringUtils.isEmpty(drawItem.getClubName())) ? drawItem.getClubName() : "N/A";
        String teamClubNames = playerAClubName + " / " + playerBClubName;
        drawItem.setClubName(teamClubNames);
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
            TournamentEvent thisEvent = this.eventService.get(eventId);

            // remove existing draw if any
            this.drawService.deleteDraws(eventId, drawType);

            // if this event has more than one round RR followed by SE then delete that too
            if (!thisEvent.isSingleElimination() && thisEvent.getPlayersToAdvance() > 0) {
                this.drawService.deleteDraws(eventId, DrawType.SINGLE_ELIMINATION);
            }

            // get all event entries into this event
            List<TournamentEventEntry> eventEntries = this.eventEntryService.listAllForEvent(thisEvent.getId());

            long tournamentFk = thisEvent.getTournamentFk();

            Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = getPlayerDrawInfo(tournamentFk);

            List<DrawItem> existingDrawItems = getOtherEventDrawItems(tournamentFk, thisEvent);

            // finally make the draws and save them
            List<DrawItem> drawItems = this.drawService.generateDraws(thisEvent, drawType, eventEntries,
                        existingDrawItems, entryIdToPlayerDrawInfo);

            // see if we need to create draw for single elimination round
            if (!thisEvent.isSingleElimination() && thisEvent.getPlayersToAdvance() > 0) {
                List<TournamentEventEntry> seSimulatedEventEntries = SingleEliminationEntriesConverter.generateSEEventEntriesFromDraws(
                        drawItems, eventEntries, thisEvent, entryIdToPlayerDrawInfo);
                fillCityStateCountryForSEPlayers (seSimulatedEventEntries, entryIdToPlayerDrawInfo);
                SingleEliminationEntriesConverter.fillRRGroupNumberForSEPlayers(drawItems, entryIdToPlayerDrawInfo, thisEvent);
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

    private List<DrawItem> getOtherEventDrawItems(long tournamentFk, TournamentEvent thisEvent) {
        // get list of other event entries
        List<Long> otherEventIds = new ArrayList<>();
        Collection<TournamentEvent> tournamentEventEntities = this.eventService.list(tournamentFk, Pageable.unpaged());
        for (TournamentEvent event : tournamentEventEntities) {
            if (!event.getId().equals(thisEvent.getId())) {
                otherEventIds.add(event.getId());
            }
        }

        // get draws for these other events so we can find and mitigate conflicts
        List<DrawItem> existingDrawItems = this.drawService.listAllDrawsForTournament(otherEventIds);
        return existingDrawItems;
    }

    /**
     *
     * @param tournamentFk
     * @return
     */
    private Map<Long, PlayerDrawInfo> getPlayerDrawInfo(long tournamentFk) {
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
        return entryIdToPlayerDrawInfo;
    }

    /**
     *
     * @param seSimulatedEventEntries
     * @param entryIdToPlayerDrawInfo
     */
    private void fillCityStateCountryForSEPlayers(List<TournamentEventEntry> seSimulatedEventEntries,
                                                  Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
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
     * Updates existing draw items
     *
     * @param drawItemsList
     */
    @PutMapping("")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    public void update(@RequestBody List<DrawItem> drawItemsList) {

        if (!drawItemsList.isEmpty()) {
            DrawItem drawItem = drawItemsList.get(0);

            List<TournamentEventEntry> thisEventEntries = eventEntryService.listAllForEvent(drawItem.getEventFk());
            // only two items were exchanged, but we need to get the full groups to determine conflicts
            List<DrawItem> groupsDrawItems = getFullGroupDrawItems(drawItemsList, thisEventEntries);

            TournamentEvent thisEvent = this.eventService.get(drawItem.getEventFk());

            long tournamentFk = thisEvent.getTournamentFk();

            Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo = getPlayerDrawInfo(tournamentFk);

            List<DrawItem> existingDrawItems = getOtherEventDrawItems(tournamentFk, thisEvent);

            ConflictFinder conflictFinder = new ConflictFinder(drawItem.getDrawType(),
                    entryIdToPlayerDrawInfo, existingDrawItems, thisEvent);
            conflictFinder.identifyConflicts(groupsDrawItems);

            // update all groups draw items since their conflicts may have changed
            this.drawService.updateDraws(groupsDrawItems);
        }
    }

    /**
     * Gets the draw items for both groups which have been updated
     *
     * @param drawItemsList    updated draw items
     * @param thisEventEntries
     * @return group draw items
     */
    private List<DrawItem> getFullGroupDrawItems(List<DrawItem> drawItemsList, List<TournamentEventEntry> thisEventEntries) {
        DrawItem drawItem = drawItemsList.get(0);
        // get which group numbers were affected by this change
        Set<Integer> groupNumbers = new HashSet<>();
        Set<Long> updatedDrawItemsIds = new HashSet<>();
        for (DrawItem item : drawItemsList) {
            groupNumbers.add(item.getGroupNum());
            updatedDrawItemsIds.add(item.getId());
        }

        // get all draw items and filter out those which are not in these groups or the updated ones
        List<DrawItem> otherDrawItemsInEvent = drawService.list(drawItem.getEventFk(), drawItem.getDrawType());
        List<DrawItem> groupsDrawItems = new ArrayList<>();
        for (DrawItem otherDrawItem : otherDrawItemsInEvent) {
            if (groupNumbers.contains(otherDrawItem.getGroupNum())) {
                if (!updatedDrawItemsIds.contains(otherDrawItem.getId())) {
                    groupsDrawItems.add(otherDrawItem);
                }
            }
        }

        // add the updated ones and sort them
        groupsDrawItems.addAll(drawItemsList);
        groupsDrawItems.sort(Comparator.comparing(DrawItem::getGroupNum)
                .thenComparing(DrawItem::getPlaceInGroup));

        // fill in the tournament entry ids
        List<Long> tournamentEntryFkList = new ArrayList<>(groupsDrawItems.size());
        for (TournamentEventEntry eventEntry : thisEventEntries) {
            tournamentEntryFkList.add(eventEntry.getTournamentEntryFk());
        }

        List<TournamentEntry> tournamentEntries = entryService.listEntries(tournamentEntryFkList);
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            String profileId = tournamentEntry.getProfileId();
            for (DrawItem groupsDrawItem : groupsDrawItems) {
                if (groupsDrawItem.getPlayerId().equals(profileId)) {
                    groupsDrawItem.setEntryId(tournamentEntry.getId());
                    break;
                }
            }
        }

        return groupsDrawItems;
    }

    /**
     * Deletes the draw
     * @param eventId
     */
    @DeleteMapping("")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    public void deleteAll(@RequestParam long eventId) {
        TournamentEvent thisEvent = this.eventService.get(eventId);
        boolean singleElimination = thisEvent.isSingleElimination();
        if (singleElimination) {
            this.drawService.deleteDraws(eventId, DrawType.SINGLE_ELIMINATION);
        } else {
            // if RR is followed by Single elimination then delete that too
            this.drawService.deleteDraws(eventId, DrawType.ROUND_ROBIN);
            if (thisEvent.getPlayersToAdvance() > 0) {
                this.drawService.deleteDraws(eventId, DrawType.SINGLE_ELIMINATION);
            }
        }
    }

    @PutMapping("/replace")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    public @ResponseBody ResponseEntity
    replaceDrawItem(@RequestBody DrawItem drawItem,
                    @RequestParam(name = "playerToAddEntryId") Long playerToAddEntryId,
                    @RequestParam(name = "tournamentId") Long tournamentId,
                    @RequestParam(name = "playerSideToRemove", required = false) String playerSideToRemove) {
        try {
            // find player to remove entry so we can remove him from the event confirmed state
            // to waiting list - needed to keep the payment ?
            boolean replacingEmptySpot = drawItem.getId() == 0;
            if (!replacingEmptySpot) {
                String profileId = drawItem.getPlayerId();
                List<TournamentEntry> tournamentEntries = entryService.listForTournamentAndUser(tournamentId, profileId);
                if (!tournamentEntries.isEmpty()) {
                    TournamentEntry tournamentEntry = tournamentEntries.get(0);
                    TournamentEventEntry tournamentEventEntry = eventEntryService.getByTournamentEventIdAndTournamentEntryId(
                            drawItem.getEventFk(), tournamentEntry.getId());
                    tournamentEventEntry.setStatus(EventEntryStatus.ENTERED_WAITING_LIST);
                    tournamentEventEntry.setDateEntered(new Date());
                    log.info("moved player event entry onto waiting list " + tournamentEventEntry);
                    eventEntryService.update(tournamentEventEntry);
                }
            }

            long eventFk = drawItem.getEventFk();
            // find out if the added player is possibly on the waiting list
            List<TournamentEventEntry> tournamentEventEntries = eventEntryService.listAllForTournamentEntry(playerToAddEntryId);
            TournamentEventEntry eventEntryToAdd = null;
            for (TournamentEventEntry tournamentEventEntry : tournamentEventEntries) {
                if (tournamentEventEntry.getTournamentEventFk() == drawItem.getEventFk()) {
                    eventEntryToAdd = tournamentEventEntry;
                    log.info("found entry into this event on the waiting list: " + eventEntryToAdd);
                    break;
                }
            }

            if (eventEntryToAdd == null) {
                eventEntryToAdd = new TournamentEventEntry();
            }

            TournamentEntry tournamentEntry = entryService.get(playerToAddEntryId);
            TournamentEvent tournamentEvent = eventService.get(eventFk);
            double feeAdult = tournamentEvent.getFeeAdult();
            eventEntryToAdd.setTournamentFk(tournamentEntry.getTournamentFk());
            eventEntryToAdd.setTournamentEntryFk(playerToAddEntryId);
            eventEntryToAdd.setTournamentEventFk(eventFk);
            eventEntryToAdd.setDateEntered(new Date());
            eventEntryToAdd.setStatus(EventEntryStatus.ENTERED);
            eventEntryToAdd.setCartSessionId(null);
            eventEntryToAdd.setPrice(feeAdult);
            eventEntryToAdd.setDoublesPartnerProfileId(null);
            if (eventEntryToAdd.getId() == null) {
                TournamentEventEntry tournamentEventEntry = eventEntryService.create(eventEntryToAdd);
                log.info("Created entry " + tournamentEventEntry);
            } else {
                eventEntryService.update(eventEntryToAdd);
                log.info("Updated event entry: " + eventEntryToAdd);
            }

            if (replacingEmptySpot) {
                log.info("Updating count of players in the event");
                int numEntries = tournamentEvent.getNumEntries();
                tournamentEvent.setNumEntries(numEntries + 1);
                eventService.update(tournamentEvent);
            }

            List<DrawItem> existingDrawItems = this.drawService.list(tournamentEvent.getId(), drawItem.getDrawType());
            // get only the first round of single elimination round
            if (drawItem.getDrawType() == DrawType.SINGLE_ELIMINATION) {
                int firstRoundNum = existingDrawItems.stream()
                        .mapToInt(DrawItem::getRound)
                        .max()
                        .orElseThrow(NoSuchElementException::new);
                existingDrawItems = existingDrawItems.stream()
                        .filter(drawItem1 -> drawItem1.getRound() == firstRoundNum)
                        .collect(Collectors.toList());
            }
            DrawItem drawItemToDelete = null;
            for (DrawItem existingDrawItem : existingDrawItems) {
                if (drawItem.getDrawType() == DrawType.ROUND_ROBIN) {
                    if (existingDrawItem.getGroupNum() == drawItem.getGroupNum() &&
                            existingDrawItem.getPlaceInGroup() == drawItem.getPlaceInGroup()) {
                        drawItemToDelete = existingDrawItem;
                    }
                } else {
                    // single elimination type
                    if (drawItem.getSingleElimLineNum() == existingDrawItem.getSingleElimLineNum()) {
                        drawItemToDelete = existingDrawItem;
                    }
                }

                if (drawItemToDelete != null) {
                    break;
                }
            }

            // finally update the draw item
            String profileId = tournamentEntry.getProfileId();
            int rating = tournamentEntry.getSeedRating();
            if (tournamentEvent.isDoubles()) {
                String teamPlayerName = drawItem.getPlayerName();
                String playerAProfileId = "";
                String playerBProfileId = "";
                if (PLAYER_A_SIDE.equals(playerSideToRemove)) {
                    playerAProfileId = tournamentEntry.getProfileId();
                } else if (PLAYER_B_SIDE.equals(playerSideToRemove)) {
                    playerBProfileId = tournamentEntry.getProfileId() ;
                }

                if (drawItemToDelete != null) {
                    String oldPlayerId = drawItemToDelete.getPlayerId();
                    String[] oldPlayerIds = oldPlayerId.split(";");
                    String partnerProfileId = null;
                    if (PLAYER_A_SIDE.equals(playerSideToRemove)) {
                        playerBProfileId = oldPlayerIds[1];
                        partnerProfileId = playerBProfileId;
                    } else if (PLAYER_B_SIDE.equals(playerSideToRemove)) {
                        playerAProfileId = oldPlayerIds[0];
                        partnerProfileId = playerAProfileId;
                    }
                    int partnerSeedRating = 0;
                    if (!StringUtils.isEmpty(partnerProfileId)) {
                        List<TournamentEntry> tournamentEntries = entryService.listForTournamentAndUser(tournamentId, partnerProfileId);
                        if (!tournamentEntries.isEmpty()) {
                            TournamentEntry partnerTournamentEntry = tournamentEntries.get(0);
                            partnerSeedRating = partnerTournamentEntry.getSeedRating();
                        }
                    }
                    rating = tournamentEntry.getSeedRating() + partnerSeedRating;
                }
                profileId = String.format("%s;%s", playerAProfileId, playerBProfileId);
            }
            drawItem.setPlayerId(profileId);
            drawItem.setRating(rating);
            if (drawItemToDelete != null) {
                log.info("Deleting existing draw item: " + drawItemToDelete);
                this.drawService.deleteDrawItem(drawItemToDelete);
            }
            DrawItem updatedDrawItem = this.drawService.save(drawItem);
            log.info("Updated draw item " + updatedDrawItem);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error during replace player: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }
}
