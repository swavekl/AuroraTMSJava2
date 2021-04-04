package com.auroratms.tournamententry;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.auroratms.usatt.UsattPlayerRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class TournamentEntryInfoService {

    private final TournamentEntryService tournamentEntryService;

    private final TournamentEventEntryService tournamentEventEntryService;

    private final UserProfileExtService userProfileExtService;

    private final UsattDataService usattDataService;
//    private final UsattPlayerRecordRepository playerRecordRepository;

    public TournamentEntryInfoService(TournamentEntryService tournamentEntryService,
                                      TournamentEventEntryService tournamentEventEntryService,
                                      UserProfileExtService userProfileExtService,
                                      UsattDataService usattDataService) {
        this.tournamentEntryService = tournamentEntryService;
        this.tournamentEventEntryService = tournamentEventEntryService;
        this.userProfileExtService = userProfileExtService;
        this.usattDataService = usattDataService;
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
}
