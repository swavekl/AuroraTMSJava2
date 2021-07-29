package com.auroratms.event;

import com.auroratms.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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

    /**
     * Gets all events for this tournament which take place on the specified day of this tournament
     * @param tournamentId
     * @param day day 1, 2, 3 etc.
     * @return
     */
    public List<TournamentEventEntity> listDaysEvents(long tournamentId, int day) {
        // get all events and find those that will have match on this day
        List<TournamentEventEntity> daysEvents = new ArrayList<>();
        Collection<TournamentEventEntity> tournamentEvents = list(tournamentId, Pageable.unpaged());
        for (TournamentEventEntity tournamentEvent : tournamentEvents) {
            if (tournamentEvent.getDay() == day) {
                daysEvents.add(tournamentEvent);
            }
            // todo - if later rounds are held on other days then preliminary round find those rounds too
            // but it will require adding that in event configuration - e.g. R of 16 same day or day + 1, R of 8 day + 2 etc.
        }

        // sort events by starting time - earliest starting first
        daysEvents.sort(new Comparator<TournamentEventEntity>() {
            @Override
            public int compare(TournamentEventEntity tee1, TournamentEventEntity tee2) {
                int result = Double.compare(tee1.getStartTime(), tee2.getStartTime());
                if (result == 0) {
                    // if they start at the same time on this day schedule higher rated event first
                    if (tee1.getGenderRestriction().equals(GenderRestriction.NONE)) {
                        result = Integer.compare(tee1.getMaxPlayerRating(), tee2.getMaxPlayerRating());
                    }
                }
                return result;
            }
        });
        return daysEvents;
    }

    /**
     * Gets list of doubles events for this tournament
     * @param tournamentId
     * @return
     */
    public List<TournamentEventEntity> listDoublesEvents(long tournamentId) {
        return repository.findByTournamentFkAndDoublesIsTrue(tournamentId);
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
