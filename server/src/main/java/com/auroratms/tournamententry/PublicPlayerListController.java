package com.auroratms.tournamententry;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentInfo;
import com.auroratms.tournament.TournamentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * Controller which allows to get data used on player list screen without being logged in
 */
@RestController
@RequestMapping("publicapi")
@Transactional
public class PublicPlayerListController {
    private static final Logger logger = LoggerFactory.getLogger(PublicPlayerListController.class);

    @Autowired
    private TournamentEntryInfoService tournamentEntryInfoService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping("/tournamentplayers/{tournamentId}")
    public ResponseEntity<List<TournamentEntryInfo>> getAllEntryInfosForTournament(@PathVariable Long tournamentId) {
        try {
            List<TournamentEntryInfo> entryInfos = this.tournamentEntryInfoService.getAllEntryInfosForTournament(tournamentId);
            return ResponseEntity.ok(entryInfos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tournament/{tournamentId}/tournamentevents")
    public Collection<TournamentEvent> list(@PathVariable Long tournamentId,
                                            Pageable pageable,
                                            @RequestParam(required = false) Boolean doublesOnly) {
        if (Boolean.TRUE.equals(doublesOnly)) {
            return tournamentEventEntityService.listDoublesEvents(tournamentId);
        } else {
            return tournamentEventEntityService.list(tournamentId, pageable);
        }
    }

    @GetMapping("/tournamentinfo/{id}")
    public TournamentInfo getByKey(@PathVariable Long id) {
        return toTournamentInfo(tournamentService.getByKey(id));
    }

    @GetMapping("/profiles/unsubscribe/{email}")
    public ResponseEntity<Void> unsubscribeByEmail(@PathVariable String email) {
        return unsubscribeByEmailInternal(email);
    }

    @GetMapping("/profiles/unsubscribe")
    public ResponseEntity<Void> unsubscribeByEmailRequestParam(@RequestParam String email) {
        return unsubscribeByEmailInternal(email);
    }

    private ResponseEntity<Void> unsubscribeByEmailInternal(String email) {
        String encodedEmail = URLEncoder.encode(email != null ? email : "", StandardCharsets.UTF_8);
        String redirectUrl = "/ui/email/unsubscribe?email=" + encodedEmail;
        try {
            if (email == null || email.isBlank()) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(redirectUrl + "&status=error"))
                        .build();
            }
            UserProfile userProfile = userProfileService.getUserProfileForEmail(email);
            if (userProfile == null) {
                userProfile = userProfileService.getUserProfileForLoginId(email);
            }
            if (userProfile == null) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(redirectUrl + "&status=not_found"))
                        .build();
            }
            userProfile.setEmailSubscribed(false);
            userProfileService.updateProfile(userProfile);
            logger.info("User with email {} unsubscribed from marketing emails", email);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl + "&status=success"))
                    .build();
        } catch (Exception e) {
            logger.error("Error unsubscribing user with email {}", email, e);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl + "&status=error"))
                    .build();
        }
    }

    private TournamentInfo toTournamentInfo(Tournament tournament) {
        TournamentInfo tournamentInfo = new TournamentInfo();
        tournamentInfo.setId(tournament.getId());
        tournamentInfo.setName(tournament.getName());
        tournamentInfo.setVenueName(tournament.getVenueName());
        tournamentInfo.setStreetAddress(tournament.getStreetAddress());
        tournamentInfo.setCity(tournament.getCity());
        tournamentInfo.setState(tournament.getState());
        tournamentInfo.setStartDate(tournament.getStartDate());
        tournamentInfo.setEndDate(tournament.getEndDate());
        tournamentInfo.setStarLevel(tournament.getStarLevel());
        return tournamentInfo;
    }
}
