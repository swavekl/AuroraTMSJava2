package com.auroratms.reports;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamententry.MembershipType;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller for loading information about all purchased USATT memberships to
 * find out if anyone avoided paying membership
 */
@RestController
@RequestMapping("api/membershipinfo")
@PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
@Transactional
public class MembershipInfoController {

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private UsattDataService usattDataService;

    /**
     * Gets a list of all membership infos with membership expiration dates and type of membership purchased if any
     *
     * @param tournamentId
     * @return
     */
    @GetMapping("/list/{tournamentId}")
    @ResponseBody
    public ResponseEntity<List<MembershipInfo>> list(@PathVariable long tournamentId) {

        // build map of entry ids showing which have any confirmed entries
        // we want to exclude the players who withdrew
        Map<Long, Boolean> entryIdToEnteredMap = new HashMap<>();
        List<TournamentEventEntry> tournamentEventEntries = tournamentEventEntryService.listAllForTournament(tournamentId);
        for (TournamentEventEntry tournamentEventEntry : tournamentEventEntries) {
            EventEntryStatus status = tournamentEventEntry.getStatus();
            if (status == EventEntryStatus.ENTERED) {
                entryIdToEnteredMap.put(tournamentEventEntry.getTournamentEntryFk(), Boolean.TRUE);
            }
        }

        // get all tournament entries for this tournament and collect player profiles ids
        List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournament(tournamentId);
        Set<String> uniqueProfileIdsSet = new HashSet<>();
        Map<String, MembershipType> profileIdToPurchasedMembershipTypesMap = new HashMap<>();
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            if (entryIdToEnteredMap.containsKey(tournamentEntry.getId())) {
                MembershipType membershipOption = tournamentEntry.getMembershipOption();
                uniqueProfileIdsSet.add(tournamentEntry.getProfileId());
                profileIdToPurchasedMembershipTypesMap.put(tournamentEntry.getProfileId(), membershipOption);
            }
        }

        // get profile information for those who bought it
        List<String> profileIds = new ArrayList<>(uniqueProfileIdsSet);
        Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIds);

        List<UserProfile> userProfileList = new ArrayList<>(userProfiles);
        Comparator<UserProfile> comparator = Comparator.comparing(UserProfile::getLastName)
                .thenComparing(UserProfile::getFirstName);
        Collections.sort(userProfileList, comparator);

        // get profile id to membership id map
        Map<String, UserProfileExt> profileIdToUserExtProfileMap = userProfileExtService.findByProfileIds(profileIds);

        List<Long> membershipIds = new ArrayList<>(userProfileList.size());
        for (UserProfileExt userProfileExt : profileIdToUserExtProfileMap.values()) {
            membershipIds.add(userProfileExt.getMembershipId());
        }

        // get expiration dates
        List<UsattPlayerRecord> usattPlayerRecordList = usattDataService.findAllByMembershipIdIn(membershipIds);

        // build membership infos
        List<MembershipInfo> membershipInfoList = new ArrayList<>(tournamentEntries.size());
        for (UserProfile userProfile : userProfileList) {
            MembershipInfo membershipInfo = new MembershipInfo();
            membershipInfoList.add(membershipInfo);
            String fullName = String.format("%s, %s", userProfile.getLastName(), userProfile.getFirstName());
            membershipInfo.setPlayerName(fullName);
            String profileId = userProfile.getUserId();
            membershipInfo.setProfileId(profileId);
            UserProfileExt userProfileExt = profileIdToUserExtProfileMap.get(profileId);
            membershipInfo.setMembershipId(userProfileExt != null ? userProfileExt.getMembershipId() : null);
            MembershipType membershipType = profileIdToPurchasedMembershipTypesMap.get(profileId);
            membershipInfo.setMembershipType(membershipType);
            for (UsattPlayerRecord usattPlayerRecord : usattPlayerRecordList) {
                if (usattPlayerRecord.getMembershipId().equals(membershipInfo.getMembershipId())) {
                    membershipInfo.setExpirationDate(usattPlayerRecord.getMembershipExpirationDate());
                    break;
                }
            }

            for (TournamentEntry tournamentEntry : tournamentEntries) {
                if (tournamentEntry.getProfileId().equals(profileId)) {
                    membershipInfo.setEntryId(tournamentEntry.getId());
                    break;
                }
            }
        }

        return ResponseEntity.ok(membershipInfoList);
    }
}
