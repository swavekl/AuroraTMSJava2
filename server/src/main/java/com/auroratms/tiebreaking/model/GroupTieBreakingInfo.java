package com.auroratms.tiebreaking.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class GroupTieBreakingInfo {
    // main matrix holding final results for all players
    List<PlayerTieBreakingInfo> playerTieBreakingInfoList;

    // subsets of tie breaking infos when n-way tie
    // maps a string indicating which players are involved and which level
    Map<String, List<PlayerTieBreakingInfo>> nWayTieBreakingInfoList;
}
