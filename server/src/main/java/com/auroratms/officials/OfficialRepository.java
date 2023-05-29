package com.auroratms.officials;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Repository for storing & retrieving officials objects
 */
@RepositoryRestResource
public interface OfficialRepository extends JpaRepository<Official, Long> {

    Page<Official> findAllByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(String firstNameLike, String lastNameLike, Pageable pageable);

}
