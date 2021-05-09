package com.auroratms.draw;

import com.auroratms.draw.generation.DrawGeneratorFactory;
import com.auroratms.draw.generation.IDrawsGenerator;
import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    public DrawService(DrawRepository drawRepository) {
        this.drawRepository = drawRepository;
    }

    public List<DrawItem> generateDraws(TournamentEventEntity tournamentEvent, DrawType drawType,
                                        List<TournamentEventEntry> eventEntries, List<DrawItem> existingDrawItems,
                                        Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        List<DrawItem> drawItemList = Collections.emptyList();
        try {
            IDrawsGenerator generator = DrawGeneratorFactory.makeGenerator(tournamentEvent, drawType);
            if (generator != null) {
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
        return this.drawRepository.findAllByEventFkAndDrawTypeOrderByGroupNumAscPlaceInGroupAsc(eventId, drawType);
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
