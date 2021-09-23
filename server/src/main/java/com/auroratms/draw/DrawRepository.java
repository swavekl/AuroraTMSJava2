package com.auroratms.draw;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

/**
 * Repository for persisting draws information in the database
 */
@RepositoryRestResource
public interface DrawRepository extends JpaRepository<DrawItem, Long> {

    List<DrawItem> findAllByEventFkAndDrawTypeOrderByGroupNumAscPlaceInGroupAsc(long eventFk, DrawType drawType);

    List<DrawItem> findAllByEventFkAndDrawTypeOrderBySingleElimLineNumAsc(long eventFk, DrawType drawType);

    // list all draws for all events in this tournament
    List<DrawItem> findAllByEventFkInOrderByEventFkAscGroupNumAscPlaceInGroupAsc(List<Long> eventFkList);

    void deleteAllByEventFkAndDrawType(long eventFk, DrawType drawType);

    boolean existsByEventFkAndDrawTypeAndRoundAndSingleElimLineNum(long eventId, DrawType drawType, int roundOf, int singleElimLinNum);

    Optional<DrawItem> findByEventFkAndDrawTypeAndRoundAndSingleElimLineNum(long eventId, DrawType drawType, int roundOf, int singleElimLineNum);

}
