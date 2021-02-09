package com.auroratms.tournamentevententry;

import com.auroratms.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@CacheConfig(cacheNames = {"tournament-event-entries"})
@Transactional
public class TournamentEventEntryService {

    @Autowired
    private TournamentEventEntryRepository repository;

    List<TournamentEventEntry> getEntries(Long tournamentEntryId) {
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
        repository.deleteById(id);
    }

}
