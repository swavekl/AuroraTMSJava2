package com.auroratms.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Additional information which will not be queryable.  This will allow us to add new configuration data in the future easily
 */
@Data
@NoArgsConstructor
public class TournamentEventConfiguration {

    // prize money information
    private List<PrizeInfo> prizeInfoList;

    // final player rankings in the event 1st place x, 2nd place y etc.
    private Map<Integer, String> finalPlayerRankings;

    /**
     * Copy constructor used for cloning events
     * @param fromConfiguration
     */
    public TournamentEventConfiguration(TournamentEventConfiguration fromConfiguration) {
        if (fromConfiguration.prizeInfoList != null) {
            this.prizeInfoList = new ArrayList<>(fromConfiguration.prizeInfoList.size());
            for (PrizeInfo prizeInfo : fromConfiguration.prizeInfoList) {
                PrizeInfo prizeInfoCopy = new PrizeInfo(prizeInfo);
                prizeInfoList.add(prizeInfoCopy);
            }
        }

        if (fromConfiguration.finalPlayerRankings != null) {
            this.finalPlayerRankings = new HashMap<>();
            for (Integer ranking : fromConfiguration.finalPlayerRankings.keySet()) {
                String playerName = fromConfiguration.finalPlayerRankings.get(ranking);
                this.finalPlayerRankings.put(ranking, playerName);
            }
        }
    }
}
