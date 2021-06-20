package com.auroratms.draw;

import com.auroratms.draw.generation.*;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.doubles.DoublesPair;
import com.auroratms.tournamentevententry.doubles.DoublesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public DrawService(DrawRepository drawRepository, DoublesService doublesService) {
        this.drawRepository = drawRepository;
        this.doublesService = doublesService;
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
    public List<DrawItem> generateDraws(TournamentEventEntity tournamentEvent, DrawType drawType,
                                        List<TournamentEventEntry> eventEntries, List<DrawItem> existingDrawItems,
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

        return drawItemList;
    }

    public List<DrawItem> list(long eventId, DrawType drawType) {
        if (drawType.equals(DrawType.ROUND_ROBIN)) {
            return this.drawRepository.findAllByEventFkAndDrawTypeOrderByGroupNumAscPlaceInGroupAsc(eventId, drawType);
        } else {
            return this.drawRepository.findAllByEventFkAndDrawTypeOrderBySingleElimLineNumAsc(eventId, drawType);
        }
    }

    public List<DrawItem> listAllDrawsForTournament(List<Long> eventIds) {
        return this.drawRepository.findAllByEventFkInOrderByEventFkAscGroupNumAscPlaceInGroupAsc(eventIds);
    }

    public void updateDraws(List<DrawItem> drawItems) {
        this.drawRepository.saveAll(drawItems);
    }

    public void deleteDraws(long eventId, DrawType drawType) {
        this.drawRepository.deleteAllByEventFkAndDrawType(eventId, drawType);
    }
}
