package com.auroratms.tournamentevententry.doubles;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
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
     * @param doublesPair
     * @return
     */
    public DoublesPair save(DoublesPair doublesPair) {
        return this.doublesPairRepository.save(doublesPair);
    }

    /**
     * Brakes apart a pair
     * @param doublesPairId
     */
    public void deletePair(long doublesPairId) {
        this.doublesPairRepository.deleteById(doublesPairId);
    }

    /**
     * Gets one specific pair for this event
     * @param doublesPairId
     * @return
     */
    public DoublesPair get (long doublesPairId) {
        return this.doublesPairRepository.getOne(doublesPairId);
    }

    /**
     * Gets all doubles pairs for given event
     * @param tournamentEventFk
     * @return
     */
    public List<DoublesPair> findDoublesPairsForEvent(long tournamentEventFk) {
        return doublesPairRepository.findDoublesPairsByTournamentEventFk(tournamentEventFk);
    }

    /**
     *
     * @param playerTournamentEntryId
     * @return
     */
    public List<DoublesPair> makeRequestedPairs(long playerTournamentEntryId) {
        List<DoublesPair> doublesPairList = new ArrayList<>();

        // find doubles events in this tournament
        TournamentEntry tournamentEntry = tournamentEntryService.get(playerTournamentEntryId);
        long tournamentFk = tournamentEntry.getTournamentFk();
        List<Long> doublesEventIds = new ArrayList<>();
        Collection<TournamentEventEntity> events = tournamentEventEntityService.list(tournamentFk, Pageable.unpaged());
        for (TournamentEventEntity event : events) {
            if (event.isDoubles()) {
                doublesEventIds.add(event.getId());
            }
        }

        // get this players entries into doubles events
        List<TournamentEventEntry> playerEventEntries = tournamentEventEntryService.getEntries(playerTournamentEntryId);
        for (TournamentEventEntry playerEventEntry : playerEventEntries) {
            // if player entered a doubles event
            if (doublesEventIds.contains(playerEventEntry.getTournamentEventFk())) {
                String doublesPartnerProfileId = playerEventEntry.getDoublesPartnerProfileId();
                if (doublesPartnerProfileId != null) {
                    // get his partners entry into this tournament
                    List<TournamentEntry> partnerTournamentEntries = tournamentEntryService.listForTournamentAndUser(tournamentFk, doublesPartnerProfileId);
                    for (TournamentEntry partnerTournamentEntry : partnerTournamentEntries) {
                        List<TournamentEventEntry> partnerEventEntries = tournamentEventEntryService.getEntries(partnerTournamentEntry.getId());
                        for (TournamentEventEntry partnerEventEntry : partnerEventEntries) {
                            if (partnerEventEntry.getTournamentEventFk() == playerEventEntry.getTournamentEventFk()) {
                                DoublesPair doublesPair = null;
                                // find existing entry in case they are switching partners
                                List<DoublesPair> playerDoublesEntry = this.doublesPairRepository.findPlayerDoublesEntry(playerEventEntry.getTournamentEventFk(), playerEventEntry.getId());
                                if (playerDoublesEntry.size() == 0) {
                                    // create new one
                                    doublesPair = new DoublesPair();
                                    doublesPair.setTournamentEventFk(playerEventEntry.getTournamentEventFk());
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
                                doublesPair = this.doublesPairRepository.saveAndFlush(doublesPair);

                                doublesPairList.add(doublesPair);
                            }
                        }
                    }
                }
            }
        }

        return doublesPairList;
    }

    /**
     *
     * @param doublesEventId
     * @param playerEventEntryId
     * @return
     */
    public List<DoublesPair> findPlayerDoublesEntry(long doublesEventId, long playerEventEntryId) {
        return doublesPairRepository.findPlayerDoublesEntry(doublesEventId, playerEventEntryId);
    }
}
