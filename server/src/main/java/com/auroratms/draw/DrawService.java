package com.auroratms.draw;

import com.auroratms.draw.generation.*;
import com.auroratms.draw.notification.DrawsEventPublisher;
import com.auroratms.draw.notification.event.DrawAction;
import com.auroratms.error.ResourceNotFoundException;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.doubles.DoublesPair;
import com.auroratms.tournamentevententry.doubles.DoublesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service for draws functions
 */
@Service
@Transactional
public class DrawService {

    private DrawRepository drawRepository;

    private DoublesService doublesService;

    private DrawsEventPublisher drawsEventPublisher;

    public DrawService(DrawRepository drawRepository,
                       DoublesService doublesService,
                       DrawsEventPublisher drawsEventPublisher) {
        this.drawRepository = drawRepository;
        this.doublesService = doublesService;
        this.drawsEventPublisher = drawsEventPublisher;
    }

    /**
     *
     * @param tournamentEvent
     * @param drawType
     * @param eventEntries
     * @param existingDrawItems
     * @param entryIdToPlayerDrawInfo
     * @return
     */
    public List<DrawItem> generateDraws(TournamentEventEntity tournamentEvent,
                                        DrawType drawType,
                                        List<TournamentEventEntry> eventEntries,
                                        List<DrawItem> existingDrawItems,
                                        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        List<DrawItem> drawItemList = Collections.emptyList();
        try {
            IDrawsGenerator generator = DrawGeneratorFactory.makeGenerator(tournamentEvent, drawType);
            if (generator != null) {
                if (tournamentEvent.isDoubles()) {
                    // pass doubles pairs to generator
                    List<DoublesPair> doublesPairsForEvent = this.doublesService.findDoublesPairsForEvent(tournamentEvent.getId());
                    if (generator instanceof DoublesSnakeDrawsGenerator) {
                        ((DoublesSnakeDrawsGenerator)generator).setDoublesPairs(doublesPairsForEvent);
                    } else if (generator instanceof DoublesSingleEliminationDrawsGenerator) {
                        ((DoublesSingleEliminationDrawsGenerator)generator).setDoublesPairs(doublesPairsForEvent);
                    }
                }
                drawItemList = generator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDrawItems);
            }
            // save the list
            if (drawItemList.size() > 0) {
                this.drawRepository.saveAll(drawItemList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        this.drawsEventPublisher.publishEvent(tournamentEvent.getId(), DrawAction.GENERATED, drawType, null);

        return drawItemList;
    }

    /**
     * List all draw items for one event and draw type
     * @param eventId
     * @param drawType
     * @return
     */
    @Transactional(readOnly = true)
    public List<DrawItem> list(long eventId, DrawType drawType) {
        if (drawType.equals(DrawType.ROUND_ROBIN)) {
            return this.drawRepository.findAllByEventFkAndDrawTypeOrderByGroupNumAscPlaceInGroupAsc(eventId, drawType);
        } else {
            return this.drawRepository.findAllByEventFkAndDrawTypeOrderBySingleElimLineNumAsc(eventId, drawType);
        }
    }

    @Transactional(readOnly = true)
    public boolean existsByEventFkAndDrawTypeAndRoundAndSingleElimLineNum(long eventId, DrawType drawType, int roundOf, int singleElimLineNum) {
        return this.drawRepository.existsByEventFkAndDrawTypeAndRoundAndSingleElimLineNum(eventId, drawType, roundOf, singleElimLineNum);
    }

    @Transactional(readOnly = true)
    public DrawItem findByEventFkAndDrawTypeAndRoundAndSingleElimLineNum(long eventId, DrawType drawType, int roundOf, int singleElimLineNum) {
        return this.drawRepository.findByEventFkAndDrawTypeAndRoundAndSingleElimLineNum(eventId, drawType, roundOf, singleElimLineNum)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find draw item"));
    }

    /**
     *
     * @param eventIds
     * @return
     */
    @Transactional(readOnly = true)
    public List<DrawItem> listAllDrawsForTournament(List<Long> eventIds) {
        return this.drawRepository.findAllByEventFkInOrderByEventFkAscGroupNumAscPlaceInGroupAsc(eventIds);
    }

    /**
     * Saves newly created item
     * @param drawItem
     * @return
     */
    public DrawItem save(DrawItem drawItem) {
        DrawItem savedItem = this.drawRepository.save(drawItem);
        List<DrawItem> updatedDrawItems = Arrays.asList(drawItem);
        this.drawsEventPublisher.publishEvent(savedItem.getEventFk(), DrawAction.UPDATED, savedItem.getDrawType(), updatedDrawItems);
        return savedItem;
    }

    /**
     *
     * @param drawItems
     */
    public void updateDraws(List<DrawItem> drawItems) {
        if (drawItems.size() > 0) {
            this.drawRepository.saveAll(drawItems);
            DrawItem drawItem = drawItems.get(0);
            this.drawsEventPublisher.publishEvent(drawItem.getEventFk(), DrawAction.UPDATED, drawItem.getDrawType(), drawItems);
        }
    }

    /**
     *
     * @param eventId
     * @param drawType
     */
    public void deleteDraws(long eventId, DrawType drawType) {
        this.drawRepository.deleteAllByEventFkAndDrawType(eventId, drawType);
        this.drawsEventPublisher.publishEvent(eventId, DrawAction.DELETED, drawType, null);
    }
}
