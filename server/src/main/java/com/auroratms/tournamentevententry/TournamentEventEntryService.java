package com.auroratms.tournamentevententry;

import com.auroratms.event.TournamentEventEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = {"tournament-event-entries"})
public class TournamentEventEntryService {

    @Autowired
    private TournamentEventEntryRepository repository;

    List<TournamentEventEntry> getEntries(Long tournamentEntryId) {
        TournamentEventEntry tee = new TournamentEventEntry();
        tee.setTournamentEntryFk(tournamentEntryId);
        return repository.findAll(Example.of(tee));
//
//       return repository.findByTournamentEntryFk(tournamentEntryFk);
    }
}
