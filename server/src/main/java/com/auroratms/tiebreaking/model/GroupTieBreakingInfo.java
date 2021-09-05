package com.auroratms.tiebreaking.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GroupTieBreakingInfo {
    List<PlayerTieBreakingInfo> playerTieBreakingInfoList;
}
