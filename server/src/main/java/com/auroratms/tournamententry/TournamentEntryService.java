package com.auroratms.tournamententry;

import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.tournament.TournamentService;
import com.auroratms.utils.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@CacheConfig (cacheNames = {"tournament-entries"})
@Transactional
public class TournamentEntryService {
    
    @Autowired
    TournamentEntryRepository repository;

    @Autowired
    TournamentService tournamentService;

    @Autowired
    SecurityService securityService;

    @CachePut(key = "#result.id")
    public TournamentEntry create(TournamentEntry tournamentEntry) {
        TournamentEntry savedTournamentEntry = repository.save(tournamentEntry);
        securityService.provideAccessToAdmin(savedTournamentEntry.getId(), TournamentEntry.class);
        String ownerId = tournamentService.getTournamentOwner(tournamentEntry.getTournamentFk());
        securityService.provideAccessToTournamentDirector(savedTournamentEntry.getId(), ownerId, TournamentEntry.class);
        return savedTournamentEntry;
    }

    @Cacheable(key = "#entryId")
    public TournamentEntry get(Long entryId) {
        return repository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("TournamentEntry " + entryId + " not found"));
    }

    public List<TournamentEntry> listForTournamentAndUser(Long tournamentId, String profileId) {
        List<TournamentEntry> entries = repository.findByTournamentFkAndProfileId(tournamentId, profileId);
        if (entries.size() > 0) {
            cacheIt(entries.get(0));
        }
        return entries;
    }

    @CachePut(key = "#entry.id")
    public void cacheIt (TournamentEntry entry) {
        // do nothing just cache it
    }

    @CachePut(key = "#result.id")
    public TournamentEntry update(TournamentEntry TournamentEntry) {
        if (repository.existsById(TournamentEntry.getId())) {
            return repository.save(TournamentEntry);
        } else {
            throw new ResourceNotFoundException("TournamentEntry " + TournamentEntry.getId() + " not found");
        }
    }

    @CacheEvict(key = "#id")
    public void delete(long id) {
        repository.deleteById(id);
    }

    /**
     * Gets count of entries for this tournament
     * @param tournamentFk
     * @return
     */
    public int getCountOfEntries(long tournamentFk) {
        return this.repository.countTournamentEntryByTournamentFk(tournamentFk);
    }
}
