package com.auroratms.draw;

import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
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

    private UsattDataService usattDataService;

    public DrawController(DrawService drawService,
                          TournamentEventEntityService eventService,
                          TournamentEventEntryService eventEntryService,
                          TournamentEntryService entryService,
                          UserProfileExtService userProfileExtService,
                          UsattDataService usattDataService) {
        this.drawService = drawService;
        this.eventService = eventService;
        this.eventEntryService = eventEntryService;
        this.entryService = entryService;
        this.userProfileExtService = userProfileExtService;
        this.usattDataService = usattDataService;
    }

    @GetMapping("")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    public ResponseEntity<List<Draw>> listAll(@RequestParam long eventId,
                                              @RequestParam DrawType drawType) {
        List<Draw> draws = this.drawService.list(eventId, drawType);
        return new ResponseEntity<List<Draw>>(draws, HttpStatus.OK);
    }

    @PostMapping("")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    public ResponseEntity<List<Draw>> generateAll(@RequestParam long eventId,
                                                  @RequestParam DrawType drawType) {
        try {
            // get all event entries into this event
            TournamentEventEntity thisEvent = this.eventService.get(eventId);
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
                entryIdToPlayerDrawInfo.put (tournamentEntry.getId(), playerDrawInfo);
            }

            // get additional player information - club id
            List<String> profileIds = new ArrayList<>(profileIdToPlayerDrawInfo.keySet());
            Map<String, UserProfileExt> userProfileExtMap = userProfileExtService.findByProfileIds(profileIds);
            Map<Long, PlayerDrawInfo> membershipIdToPlayerDrawInfo = new HashMap<>();
            for (UserProfileExt userProfileExt : userProfileExtMap.values()) {
                String profileId = userProfileExt.getProfileId();
                PlayerDrawInfo playerDrawInfo = profileIdToPlayerDrawInfo.get(profileId);
                if (playerDrawInfo != null) {
                    playerDrawInfo.setClubId(userProfileExt.getClubId());
                    membershipIdToPlayerDrawInfo.put(userProfileExt.getMembershipId(), playerDrawInfo);
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
            List<Draw> existingDraws = this.drawService.listAllDrawsForTournament(otherEventIds);

            // finally make the draws
            List<Draw> draws = this.drawService.generateDraws(thisEvent, drawType, eventEntries, existingDraws, entryIdToPlayerDrawInfo);

            // response
            return new ResponseEntity<List<Draw>>(draws, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<List<Draw>>(Collections.EMPTY_LIST, HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    public void update(@RequestParam long eventId,
                       @RequestParam DrawType drawType,
                       @RequestBody List<Draw> updatedDraws) {
        this.drawService.updateDraws(updatedDraws);
    }

    @DeleteMapping("")
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins') or hasAuthority('Referees')")
    public void deleteAll(@RequestParam long eventId) {
        this.drawService.deleteDraws(eventId, DrawType.ROUND_ROBIN);
    }
}
