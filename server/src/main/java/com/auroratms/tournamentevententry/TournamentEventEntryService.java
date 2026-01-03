package com.auroratms.tournamentevententry;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.tournamentevententry.notification.EventEntryChangePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@CacheConfig(cacheNames = {"tournament-event-entries"})
@Transactional
public class TournamentEventEntryService {

    @Autowired
    private TournamentEventEntryRepository repository;

    @Autowired
    private EventEntryChangePublisher eventPublisher;

    // list of event entry statuses considered to be 'taken'
    private static List<EventEntryStatus> TAKEN_EVENTS_STATUS = Arrays.asList(
            EventEntryStatus.ENTERED, EventEntryStatus.PENDING_CONFIRMATION, EventEntryStatus.PENDING_DELETION);

    public List<TournamentEventEntry> getEntries(Long tournamentEntryId) {
        return repository.findByTournamentEntryFk(tournamentEntryId);
    }

    @CachePut(key = "#result.id")
    public TournamentEventEntry create(TournamentEventEntry tournamentEventEntry) {
        return repository.save(tournamentEventEntry);
    }

    @Cacheable(key = "#eventEntryId")
    public TournamentEventEntry get(Long eventEntryId) {
        return repository.findById(eventEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("TournamentEventEntry " + eventEntryId + " not found"));
    }

    @CachePut(key = "#result.id")
    public TournamentEventEntry update(TournamentEventEntry tournamentEventEntry) {
        if (repository.existsById(tournamentEventEntry.getId())) {
            return repository.save(tournamentEventEntry);
        } else {
            throw new ResourceNotFoundException("TournamentEventEntry " + tournamentEventEntry.getId() + " not found");
        }
    }

    @CacheEvict(key = "#id")
    public void delete(long id) {
        TournamentEventEntry tournamentEventEntry = this.get(id);
        long tournamentId = tournamentEventEntry.getTournamentFk();
        long tournamentEventId = tournamentEventEntry.getTournamentEventFk();
        EventEntryStatus status = tournamentEventEntry.getStatus();

        repository.deleteById(id);

        this.eventPublisher.publishTournamentEventDeletedEvent(tournamentId, tournamentEventId, status);
    }

    public void deleteByIds(List<Long> ids) {
        repository.deleteAllById(ids);
        for (Long id : ids) {
            clearFromCache(id);
        }
    }

    @CacheEvict(key = "#id")
    public void clearFromCache(Long id) {
        // do nothing - just clear cache
    }

    /**
     * Count of entries in one event
     *
     * @param eventId event id
     * @return
     */
    public long getCountValidEntriesInEvent(Long eventId) {
        return repository.countByTournamentEventFkEqualsAndStatusIn(eventId, TAKEN_EVENTS_STATUS);
    }

    /**
     * Gets count of entries in all events for specified tournament
     *
     * @param tournamentId tournament id
     * @return
     */
    public int getCountOfValidEntriesInAllEvents(long tournamentId) {
        return repository.countByTournamentFkEqualsAndStatusIn(
                tournamentId, TAKEN_EVENTS_STATUS);
    }

    /**
     * Gets count of tournament entries that are in a specific status making them valid (i.e. not pending)
     *
     * @param tournamentId tournament id
     * @return
     */
    public int getCountOfEntries(long tournamentId) {
        return repository.countTournamentEntries(tournamentId, TAKEN_EVENTS_STATUS);
    }

    /**
     * Gets all tournament entries for a tournament
     *
     * @param tournamentId tournament id
     * @return
     */
    public List<TournamentEventEntry> listAllForTournament(long tournamentId) {
        return repository.findAllByTournamentFkAndStatusInOrderByTournamentEntryFkAscTournamentEventFkAsc(
                tournamentId, TAKEN_EVENTS_STATUS);
    }

    /**
     * Lists all event entries for one event that are confirmed
     *
     * @param eventId event id
     * @return list of event entries
     */
    public List<TournamentEventEntry> listAllForEvent(Long eventId) {
        return repository.findAllByTournamentEventFkEqualsAndStatusEquals(eventId, EventEntryStatus.ENTERED);
    }

    /**
     * Finds all entries associated with one tournament entry i.e. player entry
     *
     * @param tournamentEntryId
     * @return
     */
    public List<TournamentEventEntry> listAllForTournamentEntry(Long tournamentEntryId) {
        return repository.findAllByTournamentEntryFk(tournamentEntryId);
    }

    /**
     * Gets single event entry
     *
     * @param tournamentEventId
     * @param tournamentEntryId
     * @return
     */
    public TournamentEventEntry getByTournamentEventIdAndTournamentEntryId(Long tournamentEventId, Long tournamentEntryId) {
        return repository.getTournamentEventEntryByTournamentEventFkEqualsAndTournamentEntryFkEquals(tournamentEventId, tournamentEntryId);
    }

    public List<TournamentEventEntry> listAllForCartSession(String cartSessionUUID) {
        return repository.findAllByCartSessionId(cartSessionUUID);
    }

    public List<TournamentEventEntry> listWaitingListEntriesForEvent(long eventId) {
        List<TournamentEventEntry> waitingListEntries = repository.findAllByTournamentEventFkEqualsAndStatusEquals(eventId, EventEntryStatus.ENTERED_WAITING_LIST);
        // sort them so that earliest entries are first
        waitingListEntries.sort(Comparator.comparing(TournamentEventEntry::getDateEntered));
        return waitingListEntries;
    }

    public List<TournamentEventEntry> findAllEntriesByTournamentFkWithWaitingListEntries(Long tournamentId) {
        return repository.findAllEntriesByTournamentFkWithEventStatus(tournamentId, EventEntryStatus.ENTERED_WAITING_LIST);
    }

    /**
     * Finds all tournament entry ids corresponding to tournament event entries
     *
     * @param tournamentId
     * @return
     */
    public List<Long> findAllTournamentEntryIds(Long tournamentId) {
        return repository.findAllTournamentEntryIds(tournamentId);
    }

    public int countValidTournamentEntries(long tournamentId) {
        return repository.countDistinctByTournamentEntryFk(tournamentId);
    }
}
