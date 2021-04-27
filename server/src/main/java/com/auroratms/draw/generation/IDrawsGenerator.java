package com.auroratms.draw.generation;

import com.auroratms.draw.DrawItem;
import com.auroratms.tournamentevententry.TournamentEventEntry;

import java.util.List;
import java.util.Map;

/**
 * Interface for various draws generator
 */
public interface IDrawsGenerator {

    /**
     * Generates draws based on existing draws and player information
     *
     * @param eventEntries              entries into the event for which we are generating a draw
     * @param entryIdToPlayerDrawInfo   information about players who entered the event (state, club, rating etc)
     * @param existingDrawItems             draws to other events for conflict resolution
     * @return draws for this event
     */
    List<DrawItem> generateDraws(List<TournamentEventEntry> eventEntries,
                                 Map<Long, PlayerDrawInfo> entryIdToPlayerDrawInfo,
                                 List<DrawItem> existingDrawItems);
}
