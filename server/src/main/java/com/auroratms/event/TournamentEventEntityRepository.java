package com.auroratms.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface TournamentEventEntityRepository extends JpaRepository<TournamentEventEntity, Long>  {

    // find page worth of events
    Page<TournamentEventEntity> findByTournamentFk(Long tournamentId, Pageable pageable);

}
