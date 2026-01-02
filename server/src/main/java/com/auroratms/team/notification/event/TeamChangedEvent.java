package com.auroratms.team.notification.event;

import com.auroratms.team.Team;
import lombok.Data;

import java.util.List;

@Data
public class TeamChangedEvent {
    private TeamAction teamAction;

    private Team team;
    private List<String> previousProfileIds;
}
