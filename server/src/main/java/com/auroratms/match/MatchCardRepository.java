package com.auroratms.match;

import com.auroratms.draw.DrawType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for persisting match cards containing individual matches
 */
public interface MatchCardRepository extends JpaRepository<MatchCard, Long> {

    /**
     * Find one match card for this event and group number
     * @param eventId
     * @param groupNum
     * @return
     */
    Optional<MatchCard> findMatchCardByEventFkAndRoundAndGroupNum(long eventId, int round, int groupNum);

    /**
     * Finds match card for given round of event and draw type (used for single elimination round)
     * @param eventId
     * @param drawType
     * @param round
     * @return
     */
    List<MatchCard> findMatchCardByEventFkAndDrawTypeAndRound(long eventId, DrawType drawType, int round);

    /**
     * Find all match cards for this event
     * @param eventId
     * @return
     */
    List<MatchCard> findMatchCardByEventFkOrderByDrawTypeDescGroupNumAsc(long eventId);

    /**
     * Find all match cards for this event and draw type
     * @param eventId
     * @return
     */
    List<MatchCard> findMatchCardByEventFkAndDrawTypeOrderByRoundDescGroupNumAsc(long eventId, DrawType drawType);

    /**
     * Finds all match cards for given day for events in the list of event ids
     * @param eventFks
     * @param day
     * @return
     */
    List<MatchCard> findMatchCardByEventFkInAndDayOrderByEventFkAscStartTimeAsc(List<Long> eventFks, int day);

    /**
     * Finds all match cards for given day for events in the list of event ids which are played on given table
     * @param eventFks
     * @param day
     * @return
     */
    List<MatchCard> findMatchCardByEventFkInAndDayAndAssignedTablesContainsOrderByEventFkAscStartTimeAsc(List<Long> eventFks, int day, String assignedTableContains);

    /**
     * Deletes all match cards for this event and draw type
     * @param eventId
     * @param drawType
     */
    void deleteAllByEventFkAndDrawType(long eventId, DrawType drawType);
}
