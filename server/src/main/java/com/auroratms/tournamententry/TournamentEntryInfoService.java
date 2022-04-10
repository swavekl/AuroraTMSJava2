package com.auroratms.tournamententry;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class TournamentEntryInfoService {

    private final TournamentEntryService tournamentEntryService;

    private final TournamentEventEntryService tournamentEventEntryService;

    private final UserProfileExtService userProfileExtService;

    private final UserProfileService userProfileService;

    private final UsattDataService usattDataService;

    public TournamentEntryInfoService(TournamentEntryService tournamentEntryService,
                                      TournamentEventEntryService tournamentEventEntryService,
                                      UserProfileExtService userProfileExtService,
                                      UsattDataService usattDataService,
                                      UserProfileService userProfileService) {
        this.tournamentEntryService = tournamentEntryService;
        this.tournamentEventEntryService = tournamentEventEntryService;
        this.userProfileExtService = userProfileExtService;
        this.usattDataService = usattDataService;
        this.userProfileService = userProfileService;
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
        Map<Long, String> reverseMapMembershipIdToProfileId = new HashMap<>();
        for (Map.Entry<String, UserProfileExt> entry : userProfileExtMap.entrySet()) {
            UserProfileExt userProfileExt = entry.getValue();
            membershipIds.add(userProfileExt.getMembershipId());
            reverseMapMembershipIdToProfileId.put(userProfileExt.getMembershipId(), userProfileExt.getProfileId());
        }

        // pull player records for first and last name
        List<UsattPlayerRecord> playerRecords = usattDataService.findAllByMembershipIdIn(membershipIds);
        for (UsattPlayerRecord playerRecord : playerRecords) {
            Long membershipId = playerRecord.getMembershipId();
            String profileId = reverseMapMembershipIdToProfileId.get(membershipId);
            if (profileId != null) {
                TournamentEntryInfo tournamentEntryInfo = mapProfileIdToInfo.get(profileId);
                if (tournamentEntryInfo != null) {
                    tournamentEntryInfo.setFirstName(playerRecord.getFirstName());
                    tournamentEntryInfo.setLastName(playerRecord.getLastName());
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
