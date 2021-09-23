package com.auroratms.tiebreaking.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class GroupTieBreakingInfo {
    // handy map of player ids to their names
    Map<String, String> profileIdToNameMap;

    // main matrix holding final results for all players
    List<PlayerTieBreakingInfo> playerTieBreakingInfoList;

    // maps a string indicating the order in which tie breaks are resolved
    // to a subset of matrix used to do the tie breaking
    Map<String, List<PlayerTieBreakingInfo>> nWayTieBreakingInfosMap;
}
