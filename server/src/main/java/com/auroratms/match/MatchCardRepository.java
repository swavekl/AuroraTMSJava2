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
    Optional<MatchCard> findMatchCardByEventFkAndGroupNum(long eventId, int groupNum);

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
    List<MatchCard> findMatchCardByEventFkAndDrawTypeOrderByGroupNum(long eventId, DrawType drawType);

    /**
     * Finds all match cards for given day for events in the list of event ids
     * @param eventFks
     * @param day
     * @return
     */
    List<MatchCard> findMatchCardByEventFkInAndDayOrderByEventFkAscStartTimeAsc(List<Long> eventFks, int day);
}
