package com.auroratms.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Additional information which will not be queryable.  This will allow us to add new configuration data in the future easily
 */
@Data
@NoArgsConstructor
public class TournamentEventConfiguration {

    // prize money information
    private List<PrizeInfo> prizeInfoList;

}
