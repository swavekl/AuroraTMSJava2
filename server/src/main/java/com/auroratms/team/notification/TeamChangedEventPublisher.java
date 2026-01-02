package com.auroratms.team.notification;

import com.auroratms.team.Team;
import com.auroratms.team.notification.event.TeamAction;
import com.auroratms.team.notification.event.TeamChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TeamChangedEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishTeamSavedEvent(Team team, List<String> previousProfileIds) {
        TeamChangedEvent teamChangedEvent = makeTeamChangedEvent(team, previousProfileIds);
        this.applicationEventPublisher.publishEvent(teamChangedEvent);
    }

    private TeamChangedEvent makeTeamChangedEvent(Team team, List<String> previousProfileIds) {
        TeamAction teamAction = (previousProfileIds.isEmpty()) ? TeamAction.CREATED : TeamAction.UPDATED;
        TeamChangedEvent teamChangedEvent = new TeamChangedEvent();
        teamChangedEvent.setTeamAction(teamAction);
        teamChangedEvent.setTeam(team);
        teamChangedEvent.setPreviousProfileIds(previousProfileIds);
        return teamChangedEvent;
    }
}
