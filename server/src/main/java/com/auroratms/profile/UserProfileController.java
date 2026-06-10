package com.auroratms.profile;

import com.auroratms.AbstractOktaController;
import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubRepository;
import com.auroratms.club.ClubService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.auroratms.usatt.UsattPlayerRecordRepository;
import com.auroratms.utils.ImportTournamentService;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * REST API controller for managing user profile
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Slf4j
public class UserProfileController extends AbstractOktaController {
    public static final String BIRTHDAY_DATE_FORMAT = UserProfileService.DATE_FORMAT;
    @Autowired
    private ClubRepository clubRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private UsattPlayerRecordRepository playerRecordRepository;

    @Autowired
    private ClubService clubService;

    @Autowired
    private ImportTournamentService importTournamentService;
    @Autowired
    private TournamentEntryService tournamentEntryService;
    @Autowired
    private TournamentService tournamentService;

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
                } else {
                    userProfile.setTournamentRating(0);
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
                    if (usattPlayerRecord.getTournamentRating() != userProfile.getTournamentRating()) {
                        usattPlayerRecord.setTournamentRating(userProfile.getTournamentRating());
                    }
                    // update names if spelling changed
                    if (!StringUtils.equals(userProfile.getFirstName(), usattPlayerRecord.getFirstName()) ||
                            !StringUtils.equals(userProfile.getLastName(), usattPlayerRecord.getLastName())) {
                        usattPlayerRecord.setFirstName(userProfile.getFirstName());
                        usattPlayerRecord.setLastName(userProfile.getLastName());
                    }
                    playerRecordRepository.save(usattPlayerRecord);
                }
            }
        } catch (Exception e) {
            log.error("Error updating profile", e);
            String message = "{\"error\": \"%s\"}".formatted(e.getMessage());
            return new ResponseEntity(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Deleted unwated user profile
     *
     * @param userId user profile id
     * @return
     */
    @DeleteMapping("/profiles/{userId}")
    @PreAuthorize("hasAuthority('Admins') or hasAuthority('TournamentDirectors')")
    public ResponseEntity<Void> delete(@PathVariable String userId) {
        try {
            userProfileService.deleteProfile(userId);
            userProfileExtService.delete(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
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
    public ResponseEntity<Collection<UserProfile>> list(@RequestParam(required = false) String firstName,
                                                        @RequestParam(required = false) String lastName) {
        try {
            Collection<UserProfile> userProfiles = userProfileService.list(firstName, lastName);
            getExtendedProfileInformation(userProfiles);
            return new ResponseEntity(userProfiles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/profileslist")
    @PreAuthorize("hasAuthority('Admins')")
    public ResponseEntity<Map<String, Object>> listPaged(@RequestParam Integer limit,
                                                         @RequestParam(required = false) String after,
                                                         @RequestParam(required = false) String lastName,
                                                         @RequestParam(required = false) String status,
                                                         @RequestParam(required = false) String email) {
        try {
            Map<String, Object> responseMap = userProfileService.listPaged(limit, after, lastName, status, email);
            return new ResponseEntity(responseMap, HttpStatus.OK);
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

    @PutMapping("/profiles/{userId}/unlock")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public ResponseEntity<UserProfile> unlock(@RequestBody UserProfile userProfile,
                                         @PathVariable String userId) {
        try {
            logger.info("Unlocking user named " + userProfile.getFirstName() + " " + userProfile.getLastName() + " with profileId " + userId );
            String unlockUrl = oktaServiceBase + "/api/v1/users/" + userId + "/lifecycle/unlock";
            String response = makePostRequest(unlockUrl, null);
            // update profile so the cache gets evicted
            UserProfile updatedProfile = userProfileService.updateProfile(userProfile);
            return ResponseEntity.ok(updatedProfile);
        } catch (IOException e) {
            logger.error("Error unlocking user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("profiles/{userId}/roles")
    @PreAuthorize("hasAuthority('Admins')")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable String userId) {
        try {
            List<String> roles = this.userProfileService.getUserRoles(userId);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("profiles/{userId}/roles")
    @PreAuthorize("hasAuthority('Admins')")
    public ResponseEntity<Void> updateUserRoles(@PathVariable String userId,
                                                @RequestBody List<String> updatedRoles) {
        try {
            this.userProfileService.updateUserRoles(userId, updatedRoles);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/profiles/{userId}/activate")
    @ResponseBody
    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    public ResponseEntity<UserProfile> activate(@RequestBody UserProfile userProfile,
                                                @PathVariable String userId) {
        try {
            logger.info("Activating user named " + userProfile.getFirstName() + " " + userProfile.getLastName() + " with profileId " + userId );
            String unlockUrl = oktaServiceBase + "/api/v1/users/" + userId + "/lifecycle/unsuspend";
            String response = makePostRequest(unlockUrl, null);
            // update profile so the cache gets evicted
            UserProfile updatedProfile = userProfileService.updateProfile(userProfile);
            return ResponseEntity.ok(updatedProfile);
        } catch (IOException e) {
            logger.error("Error activating user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/profiles/repair")
    @PreAuthorize("hasAuthority('Admins')")
    public ResponseEntity<Void> repairProfiles(@RequestParam String fromUrl) {
        try {

            File allProfilesFile = new File("C:\\myprojects\\clubimports\\alluserprofiles.csv");
            Collection<UserProfile> userProfiles = null;
            if (!allProfilesFile.exists() || allProfilesFile.length() == 0) {
                userProfiles = this.userProfileService.list();
                saveListToCSVFile(allProfilesFile, userProfiles);
            } else {
                logger.info("All profiles file already exists");
                userProfiles = readListFromCSVFile(allProfilesFile);
            }

            List<UserProfile> registeredUserProfilesList = userProfiles.stream()
                    .filter(up -> !up.getLogin().startsWith("swaveklorenc+"))
                    .toList();
            log.info("Number of self registered profiles " + registeredUserProfilesList.size() + " out of " + userProfiles.size() );

            Set<String> profileIdsFromEntriesIntoTournaments = new HashSet<>();
            long [] myTournamentIds = {895, 897, 899, 900, 903};
            for (long tournamentId : myTournamentIds) {
                List<TournamentEntry> tournamentEntriesFromMyTournaments = tournamentEntryService.listForTournament(tournamentId);
                tournamentEntriesFromMyTournaments.stream()
                        .map(TournamentEntry::getProfileId)
                        .forEach(profileIdsFromEntriesIntoTournaments::add);
            }

            log.info("Number of unique profileIds from entries into " + myTournamentIds.length + " tournaments is " + profileIdsFromEntriesIntoTournaments.size());
            addMissingUserProfileExts(profileIdsFromEntriesIntoTournaments, registeredUserProfilesList);

            fixProfileAddresses(registeredUserProfilesList);

            // now try to set the club
            List<UserProfileExt> playerProfileExtWithoutClub = this.userProfileExtService.findAllByClubFkIsNull();
            List<String> profileIdsWithoutClub = playerProfileExtWithoutClub.stream()
                    .map(UserProfileExt::getProfileId)
                    .toList();
            log.info("Number of profiles without club " + profileIdsWithoutClub.size() + " out of " + registeredUserProfilesList.size() );

            Map<String, UserProfileExt> extProfileIds = this.userProfileExtService.findByProfileIds(profileIdsWithoutClub);

            List<Map<String, Object>> tournamentEntries = importTournamentService.readEntries(fromUrl);
            log.info("Number of tournament entries from OmniPong tournament: " + tournamentEntries.size() );

            List<ClubEntity> allClubs = clubService.findAll();
            log.info("Number of clubs " + allClubs.size());

            fixClubs(extProfileIds, registeredUserProfilesList, tournamentEntries, allClubs);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     *
     * @param registeredUserProfilesList
     */
    private void fixProfileAddresses(List<UserProfile> registeredUserProfilesList) {
        long count = 0;
        for (UserProfile userProfile : registeredUserProfilesList) {
            if (StringUtils.isEmpty(userProfile.getCity()) ||
                    StringUtils.isEmpty(userProfile.getState()) ||
                    StringUtils.isEmpty(userProfile.getZipCode())) {
                UsattPlayerRecord playerRecord = playerRecordRepository.getFirstByFirstNameAndLastName(userProfile.getFirstName(), userProfile.getLastName());
                if (playerRecord != null) {
                    Date dateOfBirth = userProfile.getDateOfBirth();
                    Date dateOfBirth1 = playerRecord.getDateOfBirth();
                    log.info("Date of birth of player " + userProfile.getFirstName() + " " + userProfile.getLastName() + " is " + dateOfBirth + " and in player record is " + dateOfBirth1);
                    if (dateOfBirth != null && dateOfBirth1 != null && dateOfBirth.equals(dateOfBirth1)) {
                        boolean changed = false;
                        if (StringUtils.isEmpty(userProfile.getCity()) && playerRecord.getCity() != null) {
                            userProfile.setCity(playerRecord.getCity());
                            changed = true;
                        }
                        if (StringUtils.isEmpty(userProfile.getState()) && playerRecord.getState() != null) {
                            userProfile.setState(playerRecord.getState());
                            changed = true;
                        }
                        if (StringUtils.isEmpty(userProfile.getZipCode()) && playerRecord.getZip() != null) {
                            userProfile.setZipCode(playerRecord.getZip());
                            changed = true;
                        }
                        if (changed) {
                            count++;
                            log.info("updating player profile address parts " + userProfile.getFirstName() + " " + userProfile.getLastName());
                            userProfileService.updateProfile(userProfile);
                        }
                    }
                }
            }
        }
        log.info("Number of profiles with missing address parts updated " + count);
    }

    /**
     *
     * @param profileIdsToFind
     * @param registeredUserProfilesList
     */
    private void addMissingUserProfileExts(Set<String> profileIdsToFind, List<UserProfile> registeredUserProfilesList) {
        Map<String, UserProfileExt> extProfileIds = this.userProfileExtService.findByProfileIds(new ArrayList<>(profileIdsToFind));
        Set<String> profileIdsToCreate = new HashSet<>();
        for (String profileId : profileIdsToFind) {
            UserProfileExt userProfileExt = extProfileIds.get(profileId);
            if (userProfileExt == null) {
                profileIdsToCreate.add(profileId);
            }
        }

        if (!profileIdsToCreate.isEmpty()) {
            AtomicInteger created = new AtomicInteger();
            registeredUserProfilesList.stream()
                    .filter(up -> profileIdsToCreate.contains(up.getUserId()))
                    .forEach(up -> {
                        UsattPlayerRecord playerRecord = playerRecordRepository.getFirstByFirstNameAndLastName(up.getFirstName(), up.getLastName());
                        if (playerRecord != null) {
                            if (StringUtils.equals(playerRecord.getState(), up.getState())) {
                                boolean existsAlready = userProfileExtService.existsByMembershipId(playerRecord.getMembershipId());
                                if (!existsAlready) {
                                    UserProfileExt userProfileExt = new UserProfileExt();
                                    userProfileExt.setProfileId(up.getUserId());
                                    userProfileExt.setMembershipId(playerRecord.getMembershipId());
                                    log.info("Creating missing user profile ext for player " + playerRecord.getFirstName() + " " + playerRecord.getLastName() + ", " + playerRecord.getFirstName() + " profileId: " + up.getUserId() + " membership id: " + playerRecord.getMembershipId());
                                    created.getAndIncrement();
                                    userProfileExtService.save(userProfileExt);
                                } else {
                                    log.info("User profile ext already exists for player " + playerRecord.getFirstName() + " " + playerRecord.getLastName() + ", " + playerRecord.getFirstName() + " profileId: " + up.getUserId() + " membership id: " + playerRecord.getMembershipId());
                                }
                            }
                        }
                    });
            log.info("Created " + created + " missing user profile exts");
        }
    }

    private void fixClubs(Map<String, UserProfileExt> extProfileIds, List<UserProfile> registeredUserProfilesList, List<Map<String, Object>> tournamentEntries, List<ClubEntity> allClubs) {
        for (UserProfileExt userProfileExt : extProfileIds.values()) {
            if (userProfileExt.getClubFk() == null) {
                String searchForUserProfileId = userProfileExt.getProfileId();
                UserProfile userProfile = registeredUserProfilesList.stream()
                        .filter(up -> up.getUserId().equals(searchForUserProfileId))
                        .findFirst()
                        .orElse(null);
                if (userProfile != null) {
                    String firstName = userProfile.getFirstName();
                    String lastName = userProfile.getLastName();
                    String playerName = lastName + ", " + firstName;
                    List<Map<String, Object>> playerTournamentEntries = tournamentEntries.stream()
                            .filter(entry ->
                                    StringUtils.equals(entry.get("playerName"), playerName))
                            .toList();
                    String clubName = null;
                    String state = (String) userProfile.getState();
                    if (playerTournamentEntries.size() == 1) {
                        clubName = (String) playerTournamentEntries.get(0).get("clubName");
                    } else if (playerTournamentEntries.size() > 1) {
                        // more than one player with same last name
                        playerTournamentEntries = playerTournamentEntries.stream()
                                .filter(entry -> StringUtils.equals(entry.get("state"), userProfile.getState()))
                                .toList();
                        if (playerTournamentEntries.size() == 1) {
                            clubName = (String) playerTournamentEntries.get(0).get("clubName");
                        } else {
                            logger.error("Found no entry for " + lastName + ", " + firstName + " userProfileId " + userProfile.getUserId() + " in tournament entries");
                        }
                    } else {
                        logger.error("Found no entry for " + lastName + ", " + firstName + " userProfileId " + userProfile.getUserId() + " in tournament entries");
                    }

                    if (clubName != null && !clubName.equalsIgnoreCase("none")) {
                        logger.info("Searching for club for " + lastName + ", " + firstName + " named " + clubName + " in state " + state);
                        final String finalClubName = clubName;
                        List<ClubEntity> clubsByName = allClubs.stream()
                                .filter(clubEntity -> clubNameMatches(clubEntity, finalClubName))
                                .toList();
                        Long clubFk = null;
                        if (clubsByName.size() == 1) {
                            clubFk = clubsByName.get(0).getId();
                        } else if (clubsByName.size() > 1) {
                            clubFk = clubsByName.stream()
                                    .filter(clubEntity -> clubEntity.getState().equals(state))
                                    .map(ClubEntity::getId)
                                    .findFirst()
                                    .orElse(null);
                        }
                        if (clubFk != null) {
                            log.info("Club found for " + playerName + " club: " + finalClubName + " in state " + state + " with id " + clubFk);
                            userProfileExt.setClubFk(clubFk);
                            userProfileExtService.save(userProfileExt);
                        } else {
                            logger.error("Club not found for " + finalClubName + " in state " + state);
                        }
                    } else {
                        logger.error("Club " + clubName + " not found for " + lastName + ", " + firstName);
                    }
                } else {
                    logger.error("User profile not found for " + searchForUserProfileId + " can't fill club information");
                }
            }
        }
    }

    /**
     *
     * @param clubEntity
     * @param clubNameToFind
     * @return
     */
    private boolean clubNameMatches(ClubEntity clubEntity, String clubNameToFind) {
        boolean clubNameMatches = StringUtils.equalsIgnoreCase(clubEntity.getClubName(), clubNameToFind);
        if (!clubNameMatches && !StringUtils.isEmpty(clubEntity.getAlternateClubNames())) {
            String[] alternateNames = clubEntity.getAlternateClubNames().split(",");
            for (String clubName : alternateNames) {
                if (StringUtils.equalsIgnoreCase(clubName.trim(), clubNameToFind)) {
                    clubNameMatches = true;
                    break;
                }
            }
        }
        return clubNameMatches;
    }

    /**
     *
     * @param allProfilesFile
     * @return
     */
    private List<UserProfile> readListFromCSVFile(File allProfilesFile) {
        List<UserProfile> userProfiles = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat(BIRTHDAY_DATE_FORMAT);
        int rowNumber = 0;
        try (CSVReader csvReader = new CSVReader(new FileReader(allProfilesFile));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                rowNumber++;
                if (rowNumber == 1) {
                    continue;
                }
                UserProfile userProfile = new UserProfile();
                userProfiles.add(userProfile);
                // userId,loginid,email,lastName,firstName,gender,streetaddress,city,state,zipcode,countrycode,dateofbirth,division
                //00uy334yckW23VIdP0h7,swaveklorenc+1@gmail.com,swaveklorenc+1@gmail.com,Aguilera,Frank,Male,null,null,IN,46311,US,10/30/1944,null
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    value = (value.equals("null")) ? null : value;
                    switch (i) {
                        case 0:
                            userProfile.setUserId(value);
                            break;
                        case 1:
                            userProfile.setLogin(value);
                            break;
                        case 2:
                            userProfile.setEmail(value);
                            break;
                        case 3:
                            userProfile.setLastName(value);
                            break;
                        case 4:
                            userProfile.setFirstName(value);
                            break;
                        case 5:
                            userProfile.setGender(value);
                            break;
                        case 6:
                            userProfile.setStreetAddress(value);
                            break;
                        case 7:
                            userProfile.setCity(value);
                            break;
                        case 8:
                            userProfile.setState(value);
                            break;
                        case 9:
                            userProfile.setZipCode(value);
                            break;
                        case 10:
                            userProfile.setCountryCode(value);
                            break;
                        case 11:
                            Date birthday = (value != null) ? dateFormat.parse(value) : null;
                            userProfile.setDateOfBirth(birthday);
                            break;
                        case 12:
                            userProfile.setDivision(value);
                            break;
                        default:
                        logger.error("Unknown column number " + i);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error reading from file at row " + rowNumber, e);
        }
        log.info("Number of profiles read from file " + userProfiles.size());
        return userProfiles;
    }
    /**
     *
     * @param allProfilesFile
     * @param userProfiles
     */
    private void saveListToCSVFile(File allProfilesFile, Collection<UserProfile> userProfiles) {
        try {
            log.info("Saving user profiles to file " + allProfilesFile.getAbsolutePath() + " with " + userProfiles.size() + " profiles");
            FileWriter fileWriter = new FileWriter(allProfilesFile);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.append("userId,loginid,email,lastName,firstName,gender,streetaddress,city,state,zipcode,countrycode,dateofbirth,division\n");
            DateFormat dateFormat = new SimpleDateFormat(BIRTHDAY_DATE_FORMAT);
            int writtenRows = 0;
            for (UserProfile up : userProfiles) {
                String formattedBirthDate = (up.getDateOfBirth() != null) ? dateFormat.format(up.getDateOfBirth()) : null;
                String cleanedStreetAddress =  (up.getStreetAddress() != null) ? up.getStreetAddress().replaceAll(",", " ") : null;
                String playerRecord = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", up.getUserId(),
                        up.getLogin(), up.getEmail(), up.getLastName(), up.getFirstName(), up.getGender(),
                        cleanedStreetAddress, up.getCity(), up.getState(), up.getZipCode(), up.getCountryCode(),
                        formattedBirthDate, up.getDivision());
                writer.write(playerRecord);
                writer.newLine();
                writtenRows++;
                if (writtenRows % 100 == 0) {
                    writer.flush();
                    logger.info("Written " + writtenRows + " rows");
                }

            }
            writer.flush();
            writer.close();
            logger.info("Written total " + writtenRows + " rows");
        } catch (IOException e) {
            throw new RuntimeException("Unable to ", e);
        }
    }

}
