package com.auroratms.profile;

import com.auroratms.AbstractOktaController;
import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.auroratms.usatt.UsattPlayerRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for managing user profile
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Slf4j
public class UserProfileController extends AbstractOktaController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private UsattPlayerRecordRepository playerRecordRepository;

    @Autowired
    private ClubService clubService;

    /**
     * Gets user profile
     *
     * @param userId
     * @return
     */
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping("/profiles/{userId}")
    @ResponseBody
    public ResponseEntity<UserProfile> getProfile(@PathVariable String userId) {
        try {
            UserProfile userProfile = userProfileService.getProfile(userId);
            if (userProfile != null) {
                UserProfileExt userProfileExt = userProfileExtService.getByProfileId(userId);
                if (userProfileExt != null) {
                    fromUserProfileExt(userProfile, userProfileExt);
                    // get membership expiration date & current rating
                    UsattPlayerRecord userPlayerRecord = this.playerRecordRepository.getFirstByMembershipId(userProfileExt.getMembershipId());
                    if (userPlayerRecord != null) {
                        userProfile.setMembershipExpirationDate(userPlayerRecord.getMembershipExpirationDate());
                        userProfile.setTournamentRating(userPlayerRecord.getTournamentRating());
                    }

                    if (userProfile.getHomeClubId() != null && userProfile.getHomeClubId() != 0) {
                        ClubEntity club = this.clubService.findById(userProfile.getHomeClubId());
                        userProfile.setHomeClubName(club.getClubName());
                    }
                }
            }
            return new ResponseEntity<UserProfile>(userProfile, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<UserProfile>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Updates user profile
     *
     * @param userProfile
     * @param userId
     * @return
     */
    @PutMapping("/profiles/{userId}")
    public ResponseEntity update(@RequestBody UserProfile userProfile, @PathVariable String userId) {
        try {
            userProfileService.updateProfile(userProfile);
            // initial save maybe during registration and is without USATT membership id
            if (userProfile.getMembershipId() != null && userProfile.getUserId() != null) {
                // save mapping from Okta user profile id to USATT membership id
                UserProfileExt userProfileExt = toUserProfileExt(userProfile);
                userProfileExtService.save(userProfileExt);

                UsattPlayerRecord usattPlayerRecord = playerRecordRepository.getFirstByMembershipId(userProfile.getMembershipId());
                if (usattPlayerRecord != null) {
                    usattPlayerRecord.setState(userProfile.getState());
                    playerRecordRepository.save(usattPlayerRecord);
                }
            }
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Creates user profile
     *
     * @param userProfile
     * @return
     */
    @PostMapping("/profiles")
    @ResponseBody
    @PreAuthorize("hasAuthority('Admins') or hasAuthority('USATTMatchOfficialsManagers') or hasAuthority('TournamentDirectors')")
    public ResponseEntity<UserProfile> create(@RequestBody UserProfile userProfile) {
        try {
            UserProfile createdProfile = userProfileService.createProfile(userProfile);
            // initial save maybe during registration and is without USATT membership id
            if (userProfile.getMembershipId() != null) {
                createdProfile.setMembershipId(userProfile.getMembershipId());
                // save mapping from Okta user profile id to USATT membership id
                if (!userProfileExtService.existsByMembershipId(userProfile.getMembershipId())) {
                    UserProfileExt userProfileExt = toUserProfileExt(createdProfile);
                    userProfileExtService.save(userProfileExt);
                }

                UsattPlayerRecord usattPlayerRecord = playerRecordRepository.getFirstByMembershipId(userProfile.getMembershipId());
                if (usattPlayerRecord != null) {
                    usattPlayerRecord.setState(userProfile.getState());
                    playerRecordRepository.save(usattPlayerRecord);
                }
            }
            return new ResponseEntity<UserProfile> (createdProfile, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating profile", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/profiles")
    @PreAuthorize("hasAuthority('TournamentDirectors')")
    public ResponseEntity<Collection<UserProfile>> list() {
        try {
            Collection<UserProfile> userProfiles = userProfileService.list();
            getExtendedProfileInformation(userProfiles);
            return new ResponseEntity(userProfiles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/profilessearch")
    public ResponseEntity<Collection<UserProfile>> list(@RequestParam(name = "firstName", required = false) String firstName,
                                                        @RequestParam(name = "lastName", required = false) String lastName) {
        try {
            Collection<UserProfile> userProfiles = userProfileService.list(firstName, lastName);
            getExtendedProfileInformation(userProfiles);
            return new ResponseEntity(userProfiles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets membership id information which resides in our database not in Okta
     * @param userProfiles
     */
    private void getExtendedProfileInformation(Collection<UserProfile> userProfiles) {
        if (userProfiles.size() > 0) {
            // get USATT membership information from the UserProfileExts
            List<String> profileIds = new ArrayList<>(userProfiles.size());
            for (UserProfile userProfile : userProfiles) {
                profileIds.add(userProfile.getUserId());
            }
            Map<String, UserProfileExt> userProfileExtMap = userProfileExtService.findByProfileIds(profileIds);
            List<Long> membershipIds = new ArrayList<>();
            for (UserProfile userProfile : userProfiles) {
                String profileId = userProfile.getUserId();
                UserProfileExt userProfileExt = userProfileExtMap.get(profileId);
                if (userProfileExt != null) {
                    fromUserProfileExt(userProfile, userProfileExt);
                    membershipIds.add(userProfileExt.getMembershipId());
                }
            }

            /**
             * now find the latest rating from the table
             */
            List<UsattPlayerRecord> playerRecords = this.playerRecordRepository.findAllByMembershipIdIn(membershipIds);
            for (UserProfile userProfile : userProfiles) {
                Long membershipId = userProfile.getMembershipId();
                if (membershipId != null) {
                    for (UsattPlayerRecord playerRecord : playerRecords) {
                        if (membershipId.equals(playerRecord.getMembershipId())) {
                            userProfile.setTournamentRating(playerRecord.getTournamentRating());
                            break;
                        }
                    }
                } else {
                    UsattPlayerRecord playerRecord = this.playerRecordRepository.getFirstByFirstNameAndLastName(userProfile.getFirstName(), userProfile.getLastName());
                    if (playerRecord != null) {
                        userProfile.setMembershipId(playerRecord.getMembershipId());
                        userProfile.setTournamentRating(playerRecord.getTournamentRating());
                        userProfile.setMembershipExpirationDate(playerRecord.getMembershipExpirationDate());
                    }
                }
            }
        }
    }

    /**
     *
     * @param userProfile
     * @return
     */
    private UserProfileExt toUserProfileExt(UserProfile userProfile) {
        UserProfileExt userProfileExt = new UserProfileExt();
        userProfileExt.setProfileId(userProfile.getUserId());
        userProfileExt.setMembershipId(userProfile.getMembershipId());
        userProfileExt.setClubFk(userProfile.getHomeClubId());
        return  userProfileExt;
    }

    /**
     *
     * @param userProfile
     * @param userProfileExt
     * @return
     */
    private UserProfile fromUserProfileExt(UserProfile userProfile, UserProfileExt userProfileExt) {
        userProfile.setMembershipId(userProfileExt.getMembershipId());
        userProfile.setHomeClubId(userProfileExt.getClubFk());
        return userProfile;
    }
}
