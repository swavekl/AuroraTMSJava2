package com.auroratms.event;

import com.auroratms.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = {"events"})
@Transactional
public class TournamentEventEntityService {

    @Autowired
    TournamentEventEntityRepository repository;

    public Collection<TournamentEventEntity> list(long tournamentId, Pageable pageable) {
        Page<TournamentEventEntity> result = repository.findByTournamentFk(tournamentId, pageable);
        return result.get().collect(Collectors.toList());
    }

    @CachePut(key = "#result.id")
    public TournamentEventEntity create(TournamentEventEntity tournamentEventEntity) {
        return repository.save(tournamentEventEntity);
    }

    @Cacheable(key = "#eventId")
    public TournamentEventEntity get(Long eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("TournamentEventEntity " + eventId + " not found"));
    }

    @CachePut(key = "#result.id")
    public TournamentEventEntity update(TournamentEventEntity tournamentEventEntity) {
        if (repository.existsById(tournamentEventEntity.getId())) {
            return repository.save(tournamentEventEntity);
        } else {
            throw new ResourceNotFoundException("TournamentEventEntity " + tournamentEventEntity.getId() + " not found");
        }
    }

    @CacheEvict(key = "#id")
    public void delete(long id) {
        repository.deleteById(id);
    }
}
