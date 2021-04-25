package com.auroratms.draw;

import com.auroratms.draw.generation.DrawGeneratorFactory;
import com.auroratms.draw.generation.IDrawsGenerator;
import com.auroratms.draw.generation.PlayerDrawInfo;
import com.auroratms.event.TournamentEventEntity;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
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

    public List<Draw> generateDraws(TournamentEventEntity tournamentEvent, DrawType drawType,
                                    List<TournamentEventEntry> eventEntries, List<Draw> existingDraws,
                                    Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo) {
        IDrawsGenerator generator = DrawGeneratorFactory.makeGenerator(tournamentEvent, drawType);
        if (generator == null) {
            return new ArrayList<>();
        } else {
            return generator.generateDraws(eventEntries, entryIdToPlayerDrawInfo, existingDraws);
        }
    }

    public List<Draw> list(long eventId, DrawType drawType) {
        return this.drawRepository.findAllByEventFkAndDrawTypeOrderByGroupNumAscPlaceInGroupAsc(eventId, drawType);
    }

    public List<Draw> listAllDrawsForTournament(List<Long> eventIds) {
        return this.drawRepository.findAllByEventFkInOrderByEventFkAscGroupNumAscPlaceInGroupAsc(eventIds);
    }

    public void updateDraws(List<Draw> draws) {
        this.drawRepository.saveAll(draws);
    }

    public void deleteDraws(long eventId, DrawType drawType) {
        this.drawRepository.deleteAllByEventFkAndDrawType(eventId, drawType);
    }
}
