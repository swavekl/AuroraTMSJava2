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

    public Collection<TournamentEvent> list(long tournamentId, Pageable pageable) {
        Page<TournamentEventEntity> result = repository.findByTournamentFk(tournamentId, pageable);
        List<TournamentEventEntity> tournamentEventEntities = result.get().collect(Collectors.toList());
        return convertFromEntities(tournamentEventEntities);
    }

    /**
     * Converts a collection of tournament event entities into collection of tournament events
     * @param tournamentEventEntities
     * @return
     */
    private Collection<TournamentEvent> convertFromEntities(List<TournamentEventEntity> tournamentEventEntities) {
        List<TournamentEvent> tournamentEvents = new ArrayList<>(tournamentEventEntities.size());
        for (TournamentEventEntity tournamentEventEntity : tournamentEventEntities) {
            TournamentEvent tournamentEvent = TournamentEvent.fromEntity(tournamentEventEntity);
            tournamentEvents.add(tournamentEvent);
        }
        return tournamentEvents;
    }

    /**
     * Gets all events for this tournament which take place on the specified day of this tournament
     * @param tournamentId
     * @param day day 1, 2, 3 etc.
     * @return
     */
    public List<TournamentEvent> listDaysEvents(long tournamentId, int day) {
        // get all events and find those that will have match on this day
        List<TournamentEvent> daysEvents = new ArrayList<>();
        Collection<TournamentEvent> tournamentEvents = list(tournamentId, Pageable.unpaged());
        for (TournamentEvent tournamentEvent : tournamentEvents) {
            if (tournamentEvent.getDay() == day) {
                daysEvents.add(tournamentEvent);
            }
            // todo - if later rounds are held on other days then preliminary round find those rounds too
            // but it will require adding that in event configuration - e.g. R of 16 same day or day + 1, R of 8 day + 2 etc.
        }

        // sort events by starting time - earliest starting first
        daysEvents.sort(new Comparator<TournamentEvent>() {
            @Override
            public int compare(TournamentEvent tee1, TournamentEvent tee2) {
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
     * Gets all specified events
     * @param eventIds
     * @return
     */
    public List<TournamentEvent> findAllById(List<Long> eventIds) {
        return (List<TournamentEvent>) convertFromEntities(repository.findAllById(eventIds));
    }

    /**
     * Gets list of doubles events for this tournament
     * @param tournamentId
     * @return
     */
    public List<TournamentEvent> listDoublesEvents(long tournamentId) {
        return (List<TournamentEvent>) convertFromEntities(repository.findByTournamentFkAndDoublesIsTrue(tournamentId));
    }

    @CachePut(key = "#result.id")
    public TournamentEvent create(TournamentEvent tournamentEvent) {
        TournamentEventEntity savedEntity = repository.save(tournamentEvent.toEntity());
        return TournamentEvent.fromEntity(savedEntity);
    }

    @Cacheable(key = "#eventId")
    public TournamentEvent get(Long eventId) {
        TournamentEventEntity tournamentEventEntity = repository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("TournamentEventEntity " + eventId + " not found"));
        return TournamentEvent.fromEntity(tournamentEventEntity);
    }

    @CachePut(key = "#result.id")
    public TournamentEvent update(TournamentEvent tournamentEvent) {
        if (repository.existsById(tournamentEvent.getId())) {
            TournamentEventEntity toSave = tournamentEvent.toEntity();
            TournamentEventEntity saved = repository.save(toSave);
            return TournamentEvent.fromEntity(saved);
        } else {
            throw new ResourceNotFoundException("TournamentEventEntity " + tournamentEvent.getId() + " not found");
        }
    }

    @CacheEvict(key = "#id")
    public void delete(long id) {
        repository.deleteById(id);
    }
}
