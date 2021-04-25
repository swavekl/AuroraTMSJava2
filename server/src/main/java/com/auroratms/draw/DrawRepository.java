package com.auroratms.draw;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Repository for persisting draws informaiton in the database
 */
@RepositoryRestResource
public interface DrawRepository extends JpaRepository<Draw, Long> {

    List<Draw> findAllByEventFkAndDrawTypeOrderByGroupNumAscPlaceInGroupAsc(long eventFk, DrawType drawType);

    // list all draws for all events in this tournament
    List<Draw> findAllByEventFkInOrderByEventFkAscGroupNumAscPlaceInGroupAsc(List<Long> eventFkList);

    void deleteAllByEventFkAndDrawType(long eventFk, DrawType drawType);

}
