package com.auroratms.tournamentevententry.doubles;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing doubles pair information
 */
@Service
@Transactional
public class DoublesService {

    @Autowired
    private DoublesRepository doublesPairRepository;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    /**
     * Makes a doubles pair
     *
     * @param doublesPair
     * @return
     */
    public DoublesPair save(DoublesPair doublesPair) {
        return this.doublesPairRepository.save(doublesPair);
    }

    /**
     * Brakes apart a pair
     *
     * @param doublesPairId
     */
    public void deletePair(long doublesPairId) {
        this.doublesPairRepository.deleteById(doublesPairId);
    }

    /**
     * Gets one specific pair for this event
     *
     * @param doublesPairId
     * @return
     */
    public DoublesPair get(long doublesPairId) {
        return this.doublesPairRepository.getOne(doublesPairId);
    }

    /**
     * Gets all doubles pairs for given event
     *
     * @param tournamentEventFk
     * @return
     */
    public List<DoublesPair> findDoublesPairsForEvent(long tournamentEventFk) {
        return doublesPairRepository.findDoublesPairsByTournamentEventFk(tournamentEventFk);
    }

    /**
     * @param doublesEventId
     * @param playerEventEntryId
     * @return
     */
    public List<DoublesPair> findPlayerDoublesEntry(long doublesEventId, long playerEventEntryId) {
        return doublesPairRepository.findPlayerDoublesEntry(doublesEventId, playerEventEntryId);
    }

    /**
     * Makes pairs for all doubles events entered by this player
     *
     * @param tournamentEntryId players tournament entry id
     * @return
     */
    public List<DoublesPair> makeRequestedPairs(long tournamentEntryId) {
        List<DoublesPair> doublesPairList = new ArrayList<>();

        // find doubles events in this tournament
        TournamentEntry tournamentEntry = tournamentEntryService.get(tournamentEntryId);
        long tournamentFk = tournamentEntry.getTournamentFk();
        List<Long> doublesEventIds = new ArrayList<>();
        List<TournamentEventEntity> doublesEvents = tournamentEventEntityService.listDoublesEvents(tournamentFk);
        for (TournamentEventEntity event : doublesEvents) {
            doublesEventIds.add(event.getId());
        }
        // get this players entries into doubles events
        List<TournamentEventEntry> playerEventEntries = tournamentEventEntryService.getEntries(tournamentEntryId);
        for (TournamentEventEntry playerEventEntry : playerEventEntries) {
            // if player entered a doubles event
            if (doublesEventIds.contains(playerEventEntry.getTournamentEventFk())) {
                DoublesPair doublesPair = this.makeRequestedPair(tournamentEntryId,
                        playerEventEntry.getTournamentEventFk(), playerEventEntry.getId());
                if (doublesPair != null) {
                    doublesPairList.add(doublesPair);
                }
            }
        }

        return doublesPairList;
    }

    /**
     * Makes a single pair of a doubles event
     *
     * @param tournamentEntryId
     * @param eventFk
     * @param eventEntryFk
     * @return
     */
    public DoublesPair makeRequestedPair(long tournamentEntryId, long eventFk, long eventEntryFk) {
        DoublesPair doublesPair = null;
        TournamentEntry tournamentEntry = tournamentEntryService.get(tournamentEntryId);
        long tournamentFk = tournamentEntry.getTournamentFk();
        String profileId = tournamentEntry.getProfileId();
        TournamentEventEntry playerEventEntry = tournamentEventEntryService.get(eventEntryFk);
        if (playerEventEntry != null) {
            String doublesPartnerProfileId = playerEventEntry.getDoublesPartnerProfileId();
            if (doublesPartnerProfileId != null) {
                // get the partner's entry into this tournament to get his profile id
                List<TournamentEntry> partnerTournamentEntries = tournamentEntryService.listForTournamentAndUser(tournamentFk, doublesPartnerProfileId);
                for (TournamentEntry partnerTournamentEntry : partnerTournamentEntries) {
                    // get partner's entries into all events and find the entry into the same doubles event
                    List<TournamentEventEntry> partnerEventEntries = tournamentEventEntryService.getEntries(partnerTournamentEntry.getId());
                    for (TournamentEventEntry partnerEventEntry : partnerEventEntries) {
                        if (partnerEventEntry.getTournamentEventFk() == playerEventEntry.getTournamentEventFk()) {
                            // if this doubles partner wants to play with this player
                            if (partnerEventEntry.getDoublesPartnerProfileId().equals(profileId)) {
                                // find existing entry in case they are switching partners
                                List<DoublesPair> playerDoublesEntry = this.doublesPairRepository.findPlayerDoublesEntry(playerEventEntry.getTournamentEventFk(), playerEventEntry.getId());
                                if (playerDoublesEntry.size() == 0) {
                                    // create new doubles pair record
                                    doublesPair = new DoublesPair();
                                    doublesPair.setTournamentEventFk(eventFk);
                                    doublesPair.setPlayerAEventEntryFk(playerEventEntry.getId());
                                    doublesPair.setPlayerBEventEntryFk(partnerEventEntry.getId());
                                    int eligibilityRating = tournamentEntry.getEligibilityRating() + partnerTournamentEntry.getEligibilityRating();
                                    int seedRating = tournamentEntry.getSeedRating() + partnerTournamentEntry.getSeedRating();
                                    doublesPair.setEligibilityRating(eligibilityRating);
                                    doublesPair.setSeedRating(seedRating);
                                } else {
                                    // fix existing entry
                                    doublesPair = playerDoublesEntry.get(0);
                                    if (doublesPair.getPlayerAEventEntryFk() == playerEventEntry.getId()) {
                                        doublesPair.setPlayerBEventEntryFk(partnerEventEntry.getId());
                                    } else if (doublesPair.getPlayerBEventEntryFk() == playerEventEntry.getId()) {
                                        doublesPair.setPlayerAEventEntryFk(partnerEventEntry.getId());
                                    }
                                    int eligibilityRating = tournamentEntry.getEligibilityRating() + partnerTournamentEntry.getEligibilityRating();
                                    int seedRating = tournamentEntry.getSeedRating() + partnerTournamentEntry.getSeedRating();
                                    doublesPair.setEligibilityRating(eligibilityRating);
                                    doublesPair.setSeedRating(seedRating);
                                }
                                doublesPair = this.doublesPairRepository.save(doublesPair);
                            }
                        }
                    }
                }
            }
        }
        return doublesPair;
    }

    /**
     * Breaks up a doubles pair after withdrawal
     *
     * @param tournamentEntryId players tournament entry id
     * @param eventFk           doubles event id
     * @param eventEntryFk      players doubles event entry id
     */
    public void breakUpPair(long tournamentEntryId, long eventFk, long eventEntryFk) {
        TournamentEntry tournamentEntry = tournamentEntryService.get(tournamentEntryId);
        String profileId = tournamentEntry.getProfileId();
        // remove the doubles pairs which are not needed after withdrawal
        List<DoublesPair> playerDoublesEntry = this.doublesPairRepository.findPlayerDoublesEntry(eventFk, eventEntryFk);
        for (DoublesPair doublesPair : playerDoublesEntry) {
            // remove this player as partner from former team member event entry
            // find partners entry into this doubles event
            long partnersEventEntryId = (doublesPair.getPlayerAEventEntryFk() == eventEntryFk)
                    ? doublesPair.getPlayerBEventEntryFk()
                    : doublesPair.getPlayerAEventEntryFk();
            TournamentEventEntry partnersEventEntry = tournamentEventEntryService.get(partnersEventEntryId);
            if (partnersEventEntry != null) {
                if (profileId.equals(partnersEventEntry.getDoublesPartnerProfileId())) {
                    partnersEventEntry.setDoublesPartnerProfileId(null);
                    tournamentEventEntryService.update(partnersEventEntry);
                }
            }

            // finally remove this doubles entry
            this.doublesPairRepository.deleteById(doublesPair.getId());
        }
    }
}
