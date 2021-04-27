package com.auroratms.club;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * JPA repository for getting clubs
 */
@RepositoryRestResource
public interface ClubRepository extends JpaRepository<ClubEntity, Long> {

    // get a bunch of them by ids
    List<ClubEntity> findAllByIdIn (List<Long> clubIdsList);
}
