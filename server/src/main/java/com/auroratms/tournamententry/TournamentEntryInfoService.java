package com.auroratms.tournamententry;

import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubService;
import com.auroratms.event.AgeRestrictionType;
import com.auroratms.event.GenderRestriction;
import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.EligibilityRestriction;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.tournamentevententry.policy.*;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.auroratms.usatt.UsattPlayerRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
public class TournamentEntryInfoService {

    private final TournamentEntryService tournamentEntryService;

    private final TournamentEventEntryService tournamentEventEntryService;

    private final UserProfileExtService userProfileExtService;

    private final UserProfileService userProfileService;

    private final UsattDataService usattDataService;
    private final UsattPlayerRecordRepository usattPlayerRecordRepository;
    private final ClubService clubService;
    private final TournamentEventEntityService tournamentEventEntityService;
    private final TournamentService tournamentService;

    public TournamentEntryInfoService(TournamentEntryService tournamentEntryService,
                                      TournamentEventEntryService tournamentEventEntryService,
                                      UserProfileExtService userProfileExtService,
                                      UsattDataService usattDataService,
                                      UserProfileService userProfileService,
                                      UsattPlayerRecordRepository usattPlayerRecordRepository, ClubService clubService, TournamentEventEntityService tournamentEventEntityService, TournamentService tournamentService) {
        this.tournamentEntryService = tournamentEntryService;
        this.tournamentEventEntryService = tournamentEventEntryService;
        this.userProfileExtService = userProfileExtService;
        this.usattDataService = usattDataService;
        this.userProfileService = userProfileService;
        this.usattPlayerRecordRepository = usattPlayerRecordRepository;
        this.clubService = clubService;
        this.tournamentEventEntityService = tournamentEventEntityService;
        this.tournamentService = tournamentService;
    }

    /**
     * Gets all tournament entry infos for a given tournament
     * @param tournamentId
     * @return
     */
    public List<TournamentEntryInfo> getAllEntryInfosForTournament(long tournamentId) {
        // get all entries for this tournament
        List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournament(tournamentId);
        List<TournamentEntryInfo> tournamentEntryInfos = new ArrayList<>(tournamentEntries.size());

        // make these maps for efficient joining in memory - we may have to do it in database if it is too slow
        Map<String, TournamentEntryInfo> mapProfileIdToInfo = new HashMap<>();
        Map<Long, TournamentEntryInfo> mapEntryIdToInfo = new HashMap<>();
        // translate partially into infos
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            TournamentEntryInfo info = new TournamentEntryInfo();
            info.setEntryId(tournamentEntry.getId());
            info.setProfileId(tournamentEntry.getProfileId());
            info.setEligibilityRating(tournamentEntry.getEligibilityRating());
            info.setSeedRating(tournamentEntry.getSeedRating());
            tournamentEntryInfos.add(info);

            // collect for efficient matching below
            mapProfileIdToInfo.put(tournamentEntry.getProfileId(), info);
            mapEntryIdToInfo.put(tournamentEntry.getId(), info);
        }

        // get player names by profile id
        // first find all member ids so we can pull up player records with first and last name
        Set<String> profileIdsSet = mapProfileIdToInfo.keySet();
        List<String> profileIds = new ArrayList<>(profileIdsSet);

        // collect membership ids
        Map<String, UserProfileExt> userProfileExtMap = userProfileExtService.findByProfileIds(profileIds);
        List<Long> membershipIds = new ArrayList<>(profileIdsSet.size());
        Map<Long, String> clubInfoMap = new HashMap<>();
        Map<Long, String> reverseMapMembershipIdToProfileId = new HashMap<>();
        for (Map.Entry<String, UserProfileExt> entry : userProfileExtMap.entrySet()) {
            UserProfileExt userProfileExt = entry.getValue();
            membershipIds.add(userProfileExt.getMembershipId());
            reverseMapMembershipIdToProfileId.put(userProfileExt.getMembershipId(), userProfileExt.getProfileId());
            if (userProfileExt.getClubFk() != null) {
                clubInfoMap.put(userProfileExt.getClubFk(), "");
            }
        }

        // get club infos
        List<Long> clubIds = new ArrayList<>(clubInfoMap.keySet());
        List<ClubEntity> clubList = clubService.findAllByIdIn(clubIds);
        for (ClubEntity clubEntity : clubList) {
            clubInfoMap.put(clubEntity.getId(), clubEntity.getClubName());
        }

        // pull player records for first and last name
        List<UsattPlayerRecord> playerRecords = usattDataService.findAllByMembershipIdIn(membershipIds);
        Set<String> missingStateProfileIdsSet = new HashSet<>();
        for (UsattPlayerRecord playerRecord : playerRecords) {
            Long membershipId = playerRecord.getMembershipId();
            String profileId = reverseMapMembershipIdToProfileId.get(membershipId);
            if (profileId != null) {
                TournamentEntryInfo tournamentEntryInfo = mapProfileIdToInfo.get(profileId);
                if (tournamentEntryInfo != null) {
                    tournamentEntryInfo.setFirstName(playerRecord.getFirstName());
                    tournamentEntryInfo.setLastName(playerRecord.getLastName());
                    tournamentEntryInfo.setGender(playerRecord.getGender() != null ? playerRecord.getGender() : "M");
                    // fill club name
                    UserProfileExt userProfileExt = userProfileExtMap.get(profileId);
                    if (userProfileExt != null && userProfileExt.getClubFk() != null) {
                        String clubName = clubInfoMap.get(userProfileExt.getClubFk());
                        tournamentEntryInfo.setClubName(clubName);
                    }
                    // fill state
                    if (StringUtils.isNotEmpty(playerRecord.getState())) {
                        tournamentEntryInfo.setState(playerRecord.getState());
                    } else {
                        missingStateProfileIdsSet.add(profileId);
                    }
                }
            } else {
                System.out.println("didn't find profile id for membership " + membershipId);
            }
        }

        if (profileIdsSet.size() != playerRecords.size()) {
            Set<String> foundProfileIds = userProfileExtMap.keySet();
            List<String> missingProfileIds = new ArrayList<>(CollectionUtils.removeAll(profileIdsSet, foundProfileIds));
            Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(missingProfileIds);
            for (UserProfile userProfile : userProfiles) {
                TournamentEntryInfo tournamentEntryInfo = mapProfileIdToInfo.get(userProfile.getUserId());
                if (tournamentEntryInfo != null) {
                    tournamentEntryInfo.setFirstName(userProfile.getFirstName());
                    tournamentEntryInfo.setLastName(userProfile.getLastName());
                    tournamentEntryInfo.setGender(userProfile.getGender());
                    // find rating if it is missing
                    UsattPlayerRecord playerRecord = usattPlayerRecordRepository.getFirstByFirstNameAndLastName(
                                    userProfile.getFirstName(), userProfile.getLastName());
                    if (playerRecord != null) {
                        tournamentEntryInfo.setSeedRating(playerRecord.getTournamentRating());
                        tournamentEntryInfo.setEligibilityRating(playerRecord.getTournamentRating());
                    }
                }
            }
        }

        // fill missing states
        List<String> missingProfileIds2 = new ArrayList<>(missingStateProfileIdsSet);
        Collection<UserProfile> userProfilesWithState = userProfileService.listByProfileIds(missingProfileIds2);
        for (UserProfile userProfile : userProfilesWithState) {
            TournamentEntryInfo tournamentEntryInfo = mapProfileIdToInfo.get(userProfile.getUserId());
            if (tournamentEntryInfo != null) {
                if (StringUtils.isNotEmpty(userProfile.getState())) {
                    tournamentEntryInfo.setState(userProfile.getState());
                }
            }
        }

        // get entered event ids
        List<TournamentEventEntry> tournamentEventEntries = tournamentEventEntryService.listAllForTournament(tournamentId);
        for (TournamentEventEntry tournamentEventEntry : tournamentEventEntries) {
            Long entryId = tournamentEventEntry.getTournamentEntryFk();
            TournamentEntryInfo tournamentEntryInfo = mapEntryIdToInfo.get(entryId);
            if (tournamentEntryInfo != null) {
                List<Long> eventIds = tournamentEntryInfo.getEventIds();
                if (eventIds == null) {
                    eventIds = new ArrayList<>();
                    tournamentEntryInfo.setEventIds(eventIds);
                }
                eventIds.add(tournamentEventEntry.getTournamentEventFk());
            }
        }

        return tournamentEntryInfos;
    }

    /**
     * Gets entry infos for players who qualify for this specific event in this event's tournament
     * @param eventId
     * @return
     */
    public List<TournamentEntryInfo> getEntryInfosForReplacementInEvent(Long eventId) {
        log.info("in getEntryInfosForReplacementInEvent");
        final TournamentEvent tournamentEvent = tournamentEventEntityService.get(eventId);
        final Tournament tournament = tournamentService.getByKey(tournamentEvent.getTournamentFk());
        List<TournamentEventEntry> tournamentEventEntries = tournamentEventEntryService.listAllForEvent(eventId);
        log.info("Found " + tournamentEventEntries.size() + " event entries");
        List<Long> entryIds = tournamentEventEntries.stream().map(TournamentEventEntry::getTournamentEntryFk)
                .collect(Collectors.toList());
        List<TournamentEntry> allTournamentEntries = tournamentEntryService.listForTournament(tournamentEvent.getTournamentFk());
        log.info("Found " + allTournamentEntries.size() + " all tournament entries");
        // filter out those already in the event
        List<TournamentEntry> otherTournamentEntries = allTournamentEntries.stream().filter(tournamentEntry ->
            !entryIds.contains(tournamentEntry.getId())
        ).collect(Collectors.toList());
        log.info ("remaining " + otherTournamentEntries.size() + " other entries after removing those already in the event");

        if (tournamentEvent.getMinPlayerRating() > 0 || tournamentEvent.getMaxPlayerRating() > 0) {
            otherTournamentEntries = otherTournamentEntries.stream().filter(tournamentEntry ->
                            !RatingRestrictionEventPolicy.isEntryDenied(tournamentEntry.getEligibilityRating(), tournamentEvent))
                    .collect(Collectors.toList());
        }
        log.info ("Remaining entries after rating restriction " + otherTournamentEntries.size());
        long start = System.currentTimeMillis();
        // get profile ids of all remaining players
        List<String> profileIds = otherTournamentEntries.stream().map(TournamentEntry::getProfileId)
                .collect(Collectors.toList());

        // get full user profiles - this is slow
        // TODO - make it faster
        Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIds);
        long duration = System.currentTimeMillis() - start;
        log.info("Reading user profiles took " + duration + " ms");
        log.info ("Got " + userProfiles.size() + " user profiles");

        if (tournamentEvent.getGenderRestriction() != GenderRestriction.NONE ||
                tournamentEvent.getAgeRestrictionType() != AgeRestrictionType.NONE ||
                tournamentEvent.getEligibilityRestriction() != EligibilityRestriction.OPEN) {
            // filter them by gender
            if (tournamentEvent.getGenderRestriction() != GenderRestriction.NONE) {
                userProfiles = userProfiles.stream().filter(
                                userProfile -> !GenderRestrictedEventPolicy.isEntryDenied(
                                        "M".equals(userProfile.getGender()) ? "MALE" : "FEMALE", tournamentEvent))
                        .collect(Collectors.toList());
                log.info("User profiles after gender filtering " + userProfiles.size());
            }

            if (tournamentEvent.getEligibilityRestriction() != EligibilityRestriction.OPEN) {
                String tournamentState = tournament.getState();
                String tournamentCountry = "US";  // TODO get it from address
                userProfiles = userProfiles.stream().filter(
                                userProfile -> !ClosedOpenRestrictionEventPolicy.isEntryDenied(
                                        tournamentState, userProfile.getState(),
                                        tournamentCountry, userProfile.getCountryCode(),
                                        tournamentEvent))
                        .collect(Collectors.toList());
                log.info("User profiles after eligibility filtering " + userProfiles.size());
            }

            if (tournamentEvent.getAgeRestrictionType() != AgeRestrictionType.NONE) {
                Date tournamentStartDate = tournament.getStartDate();
                userProfiles = userProfiles.stream().filter(
                                userProfile -> !AgeRestrictionEventPolicy.isEntryDenied(
                                        tournamentStartDate, userProfile.getDateOfBirth(), tournamentEvent))
                        .collect(Collectors.toList());
                log.info("User profiles after age restriction filtering " + userProfiles.size());
            }

            List<String> eligibleProfileIds = userProfiles.stream().map(UserProfile::getUserId).collect(Collectors.toList());
            otherTournamentEntries = otherTournamentEntries.stream().filter(
                    tournamentEntry -> eligibleProfileIds.contains(tournamentEntry.getProfileId())
            ).collect(Collectors.toList());
        }
        log.info ("otherTournamentEntries after all filtering " + otherTournamentEntries.size());

        // convert to entry infos
        List<TournamentEntryInfo> tournamentEntryInfos = new ArrayList<>(otherTournamentEntries.size());
        for (TournamentEntry tournamentEntry : otherTournamentEntries) {
            Optional<UserProfile> result = userProfiles.stream().filter(
                    userProfile -> userProfile.getUserId().equals(tournamentEntry.getProfileId()))
                    .findFirst();
            if (result.isPresent()) {
                UserProfile userProfile = result.get();

                TournamentEntryInfo tournamentEntryInfo = new TournamentEntryInfo();
                tournamentEntryInfo.setEntryId(tournamentEntry.getId());
                tournamentEntryInfo.setProfileId(tournamentEntry.getProfileId());
                tournamentEntryInfo.setEligibilityRating(tournamentEntry.getEligibilityRating());
                tournamentEntryInfo.setSeedRating(tournamentEntry.getSeedRating());

                tournamentEntryInfo.setFirstName(userProfile.getFirstName());
                tournamentEntryInfo.setLastName(userProfile.getLastName());
                tournamentEntryInfo.setGender(userProfile.getGender());
                tournamentEntryInfo.setState(userProfile.getState());
                tournamentEntryInfos.add(tournamentEntryInfo);
            }
        }
        log.info ("tournamentEntryInfos " + tournamentEntryInfos.size());

        return tournamentEntryInfos;
    }

    /**
     * Gets entry infos for just one event
     * @param eventId
     * @return
     */
    public List<TournamentEntryInfo> getDoublesEntryInfosForEvent(Long eventId) {
        // get all entries into this event
        List<TournamentEventEntry> eventEntryList = tournamentEventEntryService.listAllForEvent(eventId);
        List<TournamentEntryInfo> tournamentEntryInfos = convertToTournamentEntryInfos(eventEntryList);
        // set the events array to just this event
        List<Long> eventIds = new ArrayList<>(1);
        eventIds.add(eventId);
        for (TournamentEntryInfo tournamentEntryInfo : tournamentEntryInfos) {
            tournamentEntryInfo.setEventIds(eventIds);
        }
        return tournamentEntryInfos;
    }

    /**
     * Gets all entries for players with entries on the waiting list in the specified tournament
     *
     * @param tournamentId id of tournament to search entries for
     * @return list of infos
     */
    public List<TournamentEntryInfo> getPlayerEntriesWithWaitingListEntries(Long tournamentId) {
        List<TournamentEventEntry> playerEventEntries = tournamentEventEntryService.findAllEntriesByTournamentFkWithWaitingListEntries(tournamentId);
        // convert to infos
        List<TournamentEntryInfo> infos = convertToTournamentEntryInfos(playerEventEntries);

        // populate event array with ids of events on which they wait
        for (TournamentEventEntry eventEntry : playerEventEntries) {
            long eventFk = eventEntry.getTournamentEventFk();
            long tournamentEntryFk = eventEntry.getTournamentEntryFk();
            for (TournamentEntryInfo info : infos) {
                if (info.getEntryId() == tournamentEntryFk) {
                    // separate waiting list entries from other entries
                    if (eventEntry.getStatus() == EventEntryStatus.ENTERED_WAITING_LIST ||
                        eventEntry.getStatus() == EventEntryStatus.PENDING_WAITING_LIST) {
                        List<Long> waitingListEventIds = info.getWaitingListEventIds();
                        if (waitingListEventIds == null) {
                            waitingListEventIds = new ArrayList<>();
                            info.setWaitingListEventIds(waitingListEventIds);
                        }
                        waitingListEventIds.add(eventFk);
                        // get the dates too so we can sort in the order in which they entered waiting list
                        List<Date> waitingListEnteredDates = info.getWaitingListEnteredDates();
                        if (waitingListEnteredDates == null) {
                            waitingListEnteredDates = new ArrayList<>();
                            info.setWaitingListEnteredDates(waitingListEnteredDates);
                        }
                        Date dateEntered = eventEntry.getDateEntered() != null ? eventEntry.getDateEntered() : new Date();
                        waitingListEnteredDates.add(dateEntered);
                    } else if (eventEntry.getStatus() == EventEntryStatus.PENDING_CONFIRMATION){
                        List<Long> pendingList = info.getPendingEventIds();
                        if (pendingList == null) {
                            pendingList = new ArrayList<>();
                            info.setPendingEventIds(pendingList);
                        }
                        pendingList.add(eventFk);
                    } else {
                        List<Long> eventIds = info.getEventIds();
                        if (eventIds == null) {
                            eventIds = new ArrayList<>();
                            info.setEventIds(eventIds);
                        }
                        eventIds.add(eventFk);
                    }
                }
            }
        }

        infos.sort(Comparator.comparing(TournamentEntryInfo::getLastName)
                .thenComparing(TournamentEntryInfo::getFirstName));

        return infos;
    }

    /**
     * Converts event entry list into TournamentEntryInfos with player information and events they participate in
     * @param eventEntryList
     * @return
     */
    private List<TournamentEntryInfo> convertToTournamentEntryInfos(List<TournamentEventEntry> eventEntryList) {
        List<TournamentEntryInfo> tournamentEntryInfos = new ArrayList<>(eventEntryList.size());
        // get unique tournament entry ids
        List<Long> tournamentEntryIds = new ArrayList<>(eventEntryList.size());
        for (TournamentEventEntry entry : eventEntryList) {
            if (!tournamentEntryIds.contains(entry.getTournamentEntryFk())) {
                tournamentEntryIds.add(entry.getTournamentEntryFk());
            }
        }

        // get all entries which contain profile ids
        List<TournamentEntry> tournamentEntries = tournamentEntryService.listEntries(tournamentEntryIds);
        // make these maps for efficient joining in memory - we may have to do it in database if it is too slow
        Map<String, TournamentEntryInfo> mapProfileIdToInfo = new HashMap<>();
        // translate partially into infos
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            TournamentEntryInfo info = new TournamentEntryInfo();
            info.setEntryId(tournamentEntry.getId());
            info.setProfileId(tournamentEntry.getProfileId());
            info.setEligibilityRating(tournamentEntry.getEligibilityRating());
            info.setSeedRating(tournamentEntry.getSeedRating());
            tournamentEntryInfos.add(info);

            // collect for efficient matching below
            mapProfileIdToInfo.put(tournamentEntry.getProfileId(), info);
        }

        // get player profiles in one list
        Set<String> profileIdsSet = mapProfileIdToInfo.keySet();
        List<String> profileIds = new ArrayList<>(profileIdsSet);
        Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIds);

        for (UserProfile userProfile : userProfiles) {
            String profileId = userProfile.getUserId();
            TournamentEntryInfo tournamentEntryInfo = mapProfileIdToInfo.get(profileId);
            if (tournamentEntryInfo != null) {
                tournamentEntryInfo.setFirstName(userProfile.getFirstName());
                tournamentEntryInfo.setLastName(userProfile.getLastName());
            }
        }

        return tournamentEntryInfos;
    }
}
