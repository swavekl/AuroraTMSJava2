package com.auroratms.event;

import lombok.Data;
import lombok.NoArgsConstructor;

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

}
