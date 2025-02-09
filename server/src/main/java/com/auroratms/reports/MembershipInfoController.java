package com.auroratms.reports;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.MembershipType;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.auroratms.utils.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
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

    private static final Logger log = LoggerFactory.getLogger(MembershipInfoController.class);

    private TournamentEntryService tournamentEntryService;

    private TournamentEventEntryService tournamentEventEntryService;

    private UserProfileService userProfileService;

    private UserProfileExtService userProfileExtService;

    private UsattDataService usattDataService;

    private TournamentService tournamentService;

    private EmailService emailService;

    @Value("${client.host.url}")
    private String clientHostUrl;

    public MembershipInfoController(TournamentEntryService tournamentEntryService,
                                    TournamentEventEntryService tournamentEventEntryService,
                                    UserProfileService userProfileService,
                                    UserProfileExtService userProfileExtService,
                                    UsattDataService usattDataService,
                                    TournamentService tournamentService,
                                    EmailService emailService) {
        this.tournamentEntryService = tournamentEntryService;
        this.tournamentEventEntryService = tournamentEventEntryService;
        this.userProfileService = userProfileService;
        this.userProfileExtService = userProfileExtService;
        this.usattDataService = usattDataService;
        this.tournamentService = tournamentService;
        this.emailService = emailService;
    }

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

    @PostMapping("/contactplayers/{tournamentId}")
    public ResponseEntity<Boolean> contactPlayers(@RequestBody List<MembershipInfo> membershipInfos,
                                                  @PathVariable long tournamentId) {
        try {
            Tournament tournament = this.tournamentService.getByKey(tournamentId);
            // get player profiles which contain email addresses
            final List<String> playerProfileIds = new ArrayList<>(membershipInfos.size());
            for (MembershipInfo membershipInfo : membershipInfos) {
                playerProfileIds.add(membershipInfo.getProfileId());
            }

            final String clientHostUrl = this.clientHostUrl;

            Runnable emailSendingTask = new Runnable() {
                @Override
                @Transactional
                public void run() {
                    Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(playerProfileIds);
                    log.info("Sending email to " + userProfiles.size() + " players about the need to pay/refund their USATT membership");
                    int countSent = 0;
                    for (UserProfile userProfile : userProfiles) {
                        for (MembershipInfo membershipInfo : membershipInfos) {
                            String profileId = membershipInfo.getProfileId();
                            if (profileId.equals(userProfile.getUserId())) {
                                // create a link to this player's entry
                                String entryURL = String.format("%s/ui/entries/entryview/%d/edit/%d",
                                        clientHostUrl, tournamentId, membershipInfo.getEntryId());
                                sendEmail(membershipInfo, userProfile, entryURL, tournament);
                                // throttle sending so we get fewer errors
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException ignored) {

                                }
                                countSent++;
                                if (countSent % 5 == 0) {
                                    log.info("Sent " + countSent + " emails");
                                }
                                break;
                            }
                        }
                    }
                    log.info("Finished sending emails");
                }
            };
            Thread thread = new Thread(emailSendingTask);
            thread.setName("usatt-membership-email-sending-" + tournamentId);
            thread.start();

            return ResponseEntity.ok(Boolean.TRUE);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Boolean.FALSE);
        }
    }

    /**
     * Sends email to players affected by expired/updated membership
     *
     * @param membershipInfo
     * @param userProfile
     * @param entryURL
     * @param tournament
     */
    private void sendEmail(MembershipInfo membershipInfo, UserProfile userProfile, String entryURL, Tournament tournament) {
        try {
            String email = userProfile.getEmail();
            Date expirationDate = membershipInfo.getExpirationDate();
            Boolean willExpire = tournament.getStartDate().after(expirationDate);
            Map<String, Object> templateModel = new HashMap<>();
            String reason = willExpire ? "USATT membership needs a payment" : "USATT membership is eligible for a refund";
            String subject = tournament.getName() + " - " + reason;
            templateModel.put("tournamentName", tournament.getName());
            templateModel.put("tournamentDirectorName", tournament.getContactName());
            templateModel.put("tournamentDate", tournament.getStartDate());
            templateModel.put("playerFirstName", userProfile.getFirstName());
            templateModel.put("entryURL", entryURL);
            templateModel.put("expirationDate", expirationDate);
            templateModel.put("willExpire", willExpire);

            this.emailService.sendMessageUsingThymeleafTemplate(email, null,
                    subject, "tournament-entry/membership-expired-or-unnecessary.html", templateModel);
        } catch (MessagingException e) {
            log.error("Unable to send email", e);
        }
    }
}
