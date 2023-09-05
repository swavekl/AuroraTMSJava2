package com.auroratms.tournamententry.notification;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.ratingsprocessing.notification.event.RatingsProcessingEndEvent;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.tournamentevententry.policy.RatingRestrictionEventPolicy;
import com.auroratms.usatt.RatingHistoryRecord;
import com.auroratms.usatt.RatingHistoryRecordRepository;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.auroratms.utils.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.mail.MessagingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Listener which will be invoked after USATT ratings file is processed:
 * 1) to find players who are not eligible for event they signed up for e.g. rating is too high
 * 2) to update their seed ratings
 *
 */
@Component
@Slf4j
public class RatingsProcessingEventListener implements ApplicationListener<RatingsProcessingEndEvent> {

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentEventEntityService tournamentEventService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private UsattDataService usattDataService;

    @Autowired
    private RatingHistoryRecordRepository ratingHistoryRecordRepository;

    @Autowired
    private EmailService emailService;

    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    @Override
    public void onApplicationEvent(RatingsProcessingEndEvent event) {
        // run this task as system principal so we have access to various services
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            @Transactional
            protected void taskBody() {
                updateEventEntriesRatings();
            }
        };
        task.execute();
    }

    /**
     *
     */
    public void updateEventEntriesRatings() {
        log.info("updateEventEntriesRatings - BEGIN");
        Date today = new Date();
        Collection<Tournament> tournaments = tournamentService.listTournamentsAfterDate(today);
        for (Tournament tournament : tournaments) {
            try {
                if (!tournament.isReady()) {
                    continue;
                }
                updateEventEntriesRatingsForTournament(tournament, today);
            } catch (Exception e) {
                log.error("Error updating ratings for tournament " + tournament.getName(), e);
            }
        }
        log.info("updateEventEntriesRatings - END");
    }

    /**
     *
     * @param tournament
     * @param today
     */
    public void updateEventEntriesRatingsForTournament(Tournament tournament, Date today) {
        Date eligibilityDate = tournament.getConfiguration().getEligibilityDate();
        boolean beforeEligibilityDate = today.before(eligibilityDate);
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        log.info("===== Updating eligibility ratings for tournament " + tournament.getName() + " with eligibility date " + dateFormat.format(eligibilityDate));
        Collection<TournamentEvent> eventEntityCollection = tournamentEventService.list(tournament.getId(), Pageable.unpaged());
        List<TournamentEvent> eventList = new ArrayList<>(eventEntityCollection);
        Map<Long, TournamentEvent> eventIdToEventMap = new HashMap<>();
        for (TournamentEvent tournamentEvent : eventList) {
            eventIdToEventMap.put(tournamentEvent.getId(), tournamentEvent);
        }

        // get all entries in the tournament. stop if there are none
        List<TournamentEntry> entries = tournamentEntryService.listForTournament(tournament.getId());
        if (entries.isEmpty()) {
            return;
        }
        List<String> profileIds = new ArrayList<>(entries.size());
        Map<String, TournamentEntry> profileIdToEntryMap = new HashMap<>();
        for (TournamentEntry entry : entries) {
            String profileId = entry.getProfileId();
            profileIds.add(profileId);
            profileIdToEntryMap.put(profileId, entry);
        }

        // get profile data to get membership id
        Map<String, UserProfileExt> profileIdToExtMap = userProfileExtService.findByProfileIds(profileIds);
        Collection<UserProfileExt> userProfileExtColl = profileIdToExtMap.values();
        List<Long> membershipIds = new ArrayList<>(userProfileExtColl.size());
        Map<Long, String> membershipIdToProfileMap = new HashMap<>();
        for (UserProfileExt userProfileExt : userProfileExtColl) {
            Long membershipId = userProfileExt.getMembershipId();
            membershipIds.add(membershipId);
            membershipIdToProfileMap.put(membershipId, userProfileExt.getProfileId());
        }

        // get all latest usatt records with latest tournament ratings
        Map<String, List<String>> playerProfileIdToDisqualifiedEventsMap = new HashMap<>();
        Map<String, Integer> playerProfileIdToEligibitiyRatingMap = new HashMap<>();
        List<UsattPlayerRecord> allLatestRecords = usattDataService.findAllByMembershipIdIn(membershipIds);
        List<RatingHistoryRecord> eligibilityRatingsList = ratingHistoryRecordRepository.getBatchPlayerRatingsAsOfDate(membershipIds, eligibilityDate);
        log.info ("Got " + allLatestRecords.size() + " latest ratings records and " + eligibilityRatingsList.size() + " history ratings records");
        for (UsattPlayerRecord usattPlayerRecord : allLatestRecords) {
            long membershipId = usattPlayerRecord.getMembershipId();
            String profileId = membershipIdToProfileMap.get(membershipId);
            if (profileId != null) {
                String playerFullName = usattPlayerRecord.getLastName() + ", " + usattPlayerRecord.getFirstName();
                TournamentEntry tournamentEntry = profileIdToEntryMap.get(profileId);
                if (tournamentEntry != null) {
                    // update rating
                    int seedRating = usattPlayerRecord.getTournamentRating();
                    boolean seedRatingChanged = (tournamentEntry.getSeedRating() != seedRating);
                    int eligibilityRating = tournamentEntry.getEligibilityRating();
                    int oldEligibilityRating = eligibilityRating;
                    boolean eligibilityRatingChanged = false;
                    if (beforeEligibilityDate) {
                        eligibilityRatingChanged = (eligibilityRating != seedRating);
                        eligibilityRating = seedRating;
                    } else {
                        // today is after eligibility date so look through historical records - they may have changed
                        boolean eligibilityRatingFound = false;
                        for (RatingHistoryRecord ratingHistoryRecord : eligibilityRatingsList) {
                            if (ratingHistoryRecord.getMembershipId() == membershipId) {
                                eligibilityRatingFound = true;
                                if (ratingHistoryRecord.getFinalRating() != eligibilityRating) {
                                    eligibilityRating = ratingHistoryRecord.getFinalRating();
                                    eligibilityRatingChanged = true;
                                }
                                break;
                            }
                        }
                        // if we didn't find history records then let's use the seed rating
                        if (!eligibilityRatingFound) {
                            log.warn("Eligibility rating not found in historical records for player " + playerFullName + ". Using seed rating.");
                            eligibilityRatingChanged = (seedRating != eligibilityRating);
                            eligibilityRating = seedRating;
                        }
                    }

                    // persist updated ratings
                    if (seedRatingChanged || eligibilityRatingChanged) {
                        tournamentEntry.setSeedRating(seedRating);
                        tournamentEntry.setEligibilityRating(eligibilityRating);
                        tournamentEntryService.update(tournamentEntry);
                    }

                    // check if this player is still eligible for all events he/she entered, if not notify player and TD
                    if (eligibilityRatingChanged) {
                        log.info("Eligibility rating for player " + playerFullName + " changed from " + oldEligibilityRating + " to " + eligibilityRating + ". Checking events eligibility...");
                        List<TournamentEventEntry> tournamentEventEntries = tournamentEventEntryService.listAllForTournamentEntry(tournamentEntry.getId());
                        for (TournamentEventEntry tournamentEventEntry : tournamentEventEntries) {
                            TournamentEvent tournamentEvent = eventIdToEventMap.get(tournamentEventEntry.getTournamentEventFk());
                            int effectiveEligibilityRating = eligibilityRating;
                            if (tournamentEvent.isDoubles()) {
                                // find doubles partner eligibility rating
                                String doublesPartnerProfileId = tournamentEventEntry.getDoublesPartnerProfileId();
                                UserProfileExt profileExt = profileIdToExtMap.get(doublesPartnerProfileId);
                                if (profileExt != null) {
                                    int partnerEligibilityRating = 0;
                                    for (RatingHistoryRecord ratingHistoryRecord : eligibilityRatingsList) {
                                        if(ratingHistoryRecord.getMembershipId() == profileExt.getMembershipId()) {
                                            effectiveEligibilityRating = seedRating + partnerEligibilityRating;
                                            break;
                                        }
                                    }
                                }
                            }

                            // check rating restriction and record disqualification if any
                            RatingRestrictionEventPolicy ratingRestrictionEventPolicy = new RatingRestrictionEventPolicy(effectiveEligibilityRating);
                            if (ratingRestrictionEventPolicy.isEntryDenied(tournamentEvent)) {
                                log.info("Player " + playerFullName + " (" + eligibilityRating + ") is not eligible for event " + tournamentEvent.getName());
                                List<String> disqualifiedEventNames = playerProfileIdToDisqualifiedEventsMap.computeIfAbsent(profileId, k -> new ArrayList<>());
                                disqualifiedEventNames.add(tournamentEvent.getName());
                                playerProfileIdToEligibitiyRatingMap.put(profileId, eligibilityRating);
                            }
                        }
                    }
                }
            }
        }

        // check if any players were disqualified
        if (!playerProfileIdToDisqualifiedEventsMap.isEmpty()) {
            // get all profiles in one shot
            sendDisqualificationsEmails(playerProfileIdToDisqualifiedEventsMap, playerProfileIdToEligibitiyRatingMap, tournament, beforeEligibilityDate);
        }
    }

    /**
     * @param playerProfileIdToDisqualifiedEventsMap
     * @param playerProfileIdToEligibitiyRatingMap
     * @param tournament
     * @param beforeEligibilityDate
     */
    private void sendDisqualificationsEmails(Map<String, List<String>> playerProfileIdToDisqualifiedEventsMap,
                                             Map<String, Integer> playerProfileIdToEligibitiyRatingMap,
                                             Tournament tournament, boolean beforeEligibilityDate) {
        Set<String> disqualifiedProfileIds = playerProfileIdToDisqualifiedEventsMap.keySet();
        List<String> profileIdsToFetch = new ArrayList<>(disqualifiedProfileIds);
        Collection<UserProfile> playerUserProfiles = userProfileService.listByProfileIds(profileIdsToFetch);

        UserProfile tdUserProfile = null;
        String tournamentOwnerLoginId = tournamentService.getTournamentOwner(tournament.getId());
        if (tournamentOwnerLoginId != null) {
            String tournamentDirectorProfileId = this.userProfileService.getProfileByLoginId(tournamentOwnerLoginId);
            tdUserProfile = userProfileService.getProfile(tournamentDirectorProfileId);
        }

        for (String profileId : disqualifiedProfileIds) {
            for (UserProfile userProfile : playerUserProfiles) {
                if (userProfile.getUserId().equals(profileId)) {
                    List<String> eventNames = playerProfileIdToDisqualifiedEventsMap.get(profileId);
                    Integer eligibilityRating = playerProfileIdToEligibitiyRatingMap.get(profileId);
                    sendEmailToPlayerAndTD(userProfile, tdUserProfile, eventNames, tournament, eligibilityRating, beforeEligibilityDate);
                }
            }
        }
    }

    /**
     * @param userProfile
     * @param tdUserProfile
     * @param eventNames
     * @param tournament
     * @param eligibilityRating
     * @param beforeEligibilityDate
     */
    private void sendEmailToPlayerAndTD(UserProfile userProfile, UserProfile tdUserProfile, List<String> eventNames,
                                        Tournament tournament, Integer eligibilityRating, boolean beforeEligibilityDate) {
        try {
            log.info ("Sending email to " + userProfile.getFirstName() + " " + userProfile.getLastName());
            String tournamentName = tournament.getName();
            Date eligibilityDate = tournament.getConfiguration().getEligibilityDate();
            String subject = tournamentName + " - event disqualification";
            String tournamentDirectorEmail = tdUserProfile.getEmail();
            String playerEmail = userProfile.getEmail();
//playerEmail = "swaveklorenc+mirek@gmail.com";
            String playerFirstName = userProfile.getFirstName();
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put ("tournamentName", tournamentName);
            templateModel.put ("eligibilityDate", eligibilityDate);
            templateModel.put ("playerFirstName", playerFirstName);
            templateModel.put ("eventNames", eventNames);
            templateModel.put ("eligibilityRating", eligibilityRating);
            templateModel.put ("beforeEligibilityDate", beforeEligibilityDate);
//templateModel.put ("beforeEligibilityDate", false);

            emailService.sendMessageUsingThymeleafTemplate(playerEmail, tournamentDirectorEmail,
                    subject, "tournament-entry/event-disqualifications.html", templateModel);
        } catch (MessagingException e) {
            log.error("Error sending event disqualification email", e);
            throw new RuntimeException(e);
        }
    }
}
