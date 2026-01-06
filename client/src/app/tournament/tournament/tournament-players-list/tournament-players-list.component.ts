import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {EventEntryType} from '../../tournament-config/model/event-entry-type.enum';
import {Team} from '../../tournament-entry/model/team.model';
import {TeamEntryStatus} from '../../tournament-entry/model/team-entry-status.enum';

@Component({
    selector: 'app-tournament-players-list',
    templateUrl: './tournament-players-list.component.html',
    styleUrls: ['./tournament-players-list.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class TournamentPlayersListComponent implements OnInit, OnChanges {

  @Input()
  entryInfos: TournamentEntryInfo[];

  @Input()
  tournamentEvents: TournamentEvent[];

  @Input()
  tournamentName: string;

  @Input()
  tournamentStartDate: Date;

  @Input()
  tournamentEndDate: Date;

  @Input()
  teams!: Team[] | null;

  // tournament events with players in them
  tournamentEventsWithPlayers: TournamentEventWithPlayers[];

  sortBy: string;

  // map of event id to event object for faster lookup
  private eventIdToEventMap: any;

  // players grouped by club
  clubPlayersInfos: ClubPlayersInfo [] = null;

  // players grouped by state
  statePlayersInfos: StatePlayersInfo [] = null;

  protected hasTeamEvents: boolean = false;
  protected teamEventToTeamsMap: Map<TournamentEvent, Team[]>;
  protected teamsSortedBy: string = 'name';

  constructor() {
    this.sortBy = 'name';
  }

  ngOnInit(): void {

  }

  ngOnChanges(changes: SimpleChanges): void {
    const entryInfosChange: SimpleChange = changes.entryInfos;
    if (entryInfosChange) {
      const entryInfos: TournamentEntryInfo [] = entryInfosChange.currentValue;
      if (entryInfos) {
        const nonEmptyEntryInfos: TournamentEntryInfo [] = entryInfos.filter(
          (tournamentEntryInfo: TournamentEntryInfo) => {
            return tournamentEntryInfo.eventIds != null;
          });
        this.entryInfos = this.sortEntries(nonEmptyEntryInfos);
      }
    }

    const tournamentEventsChange: SimpleChange = changes.tournamentEvents;
    if (tournamentEventsChange) {
      this.tournamentEvents = tournamentEventsChange.currentValue;
      if (this.tournamentEvents) {
        // make a map for faster lookup
        this.eventIdToEventMap = {};
        for (let j = 0; j < this.tournamentEvents.length; j++) {
          const tournamentEvent = this.tournamentEvents[j];
          this.eventIdToEventMap[tournamentEvent.id] = tournamentEvent;
          if (tournamentEvent.eventEntryType == EventEntryType.TEAM) {
            this.hasTeamEvents = true;
          }
        }
      }
     }

    // when both are ready do it.
    if (this.tournamentEvents != null && this.tournamentEvents.length > 0
      && this.entryInfos != null && this.entryInfos.length > 0) {
      this.tournamentEventsWithPlayers = this.categorizeEntriesByEvents(this.entryInfos, this.tournamentEvents);
      this.clubPlayersInfos = null;
    }
  }

  private sortEntries(infos: TournamentEntryInfo[]): TournamentEntryInfo[] {
    let sortFunction = this.sortByNameFn;
    switch (this.sortBy) {
      case 'name':
        sortFunction = this.sortByNameFn;
        break;

      case 'rating':
        sortFunction = this.sortByRatingFn;
        break;
    }
    return (infos != null) ? infos.sort(sortFunction) : [];
  }

  /**
   * Sort by last name, first name
   * @param left
   * @param right
   * @private
   */
  private sortByNameFn(left: TournamentEntryInfo, right: TournamentEntryInfo): number {
    const leftFullName = `${left.lastName}, ${left.firstName}`;
    const rightFullName = `${right.lastName}, ${right.firstName}`;
    return leftFullName.localeCompare(rightFullName);
  }

  /**
   * Sort by rating from highest to lowest (reverse)
   * @param left
   * @param right
   * @private
   */
  private sortByRatingFn(left: TournamentEntryInfo, right: TournamentEntryInfo): number {
    return (left.seedRating === right.seedRating)
      ? 0 : (left.seedRating < right.seedRating) ? 1 : -1;
  }

  public fullName(firstName: string, lastName: string): string {
    return `${lastName}, ${firstName}`;
  }

  onSortByName() {
    this.sortBy = 'name';
    this.entryInfos = this.sortEntries(this.entryInfos);
  }

  onSortByRating() {
    this.sortBy = 'rating';
    this.entryInfos = this.sortEntries(this.entryInfos);
  }

  onSortByEvent() {
    // they are already categorized - just show them
    this.sortBy = 'event';
  }

  onSortByState() {
    this.sortByState();
    this.sortBy = 'state';
  }

  onSortByClub() {
    this.sortByClub();
    this.sortBy = 'club';
  }

  onSortByTeam() {
    this.sortByTeam();
    this.sortBy = 'team';
  }

  /**
   *
   * @param entryInfos
   * @param events
   */
  categorizeEntriesByEvents(entryInfos: TournamentEntryInfo[], events: TournamentEvent[]): TournamentEventWithPlayers[] {
    const categorizedCollection: TournamentEventWithPlayers[] = [];
    // make a map for fast lookup
    const mapEventIdToEventWithPlayers = {};
    for (let i = 0; i < events.length; i++) {
      // clone it
      const event = {...events[i]};
      const tep = new TournamentEventWithPlayers(event, []);
      mapEventIdToEventWithPlayers[event.id] = tep;
      categorizedCollection.push(tep);
    }

    // add all players into tournament event with players objects
    for (let i = 0; i < entryInfos.length; i++) {
      const entryInfo = entryInfos[i];
      const pi: PlayerInfo = new PlayerInfo();
      pi.playerName = this.fullName(entryInfo.firstName, entryInfo.lastName);
      pi.eligibilityRating = entryInfo.eligibilityRating;
      pi.seedRating = entryInfo.seedRating;
      // get events if any - they may have just entered
      const eventIds = entryInfo.eventIds;
      if (eventIds) {
        for (let j = 0; j < eventIds.length; j++) {
          const eventId = eventIds[j];
          const tep: TournamentEventWithPlayers = mapEventIdToEventWithPlayers[eventId];
          if (tep) {
            tep.addPlayer(pi);
          }
        }
      }
    }

   // sort players by rating
  function sortByRatingFn (left: PlayerInfo, right: PlayerInfo): number {
      return (left.seedRating === right.seedRating)
        ? 0 : (left.seedRating < right.seedRating) ? 1 : -1;
    }

    for (let i = 0; i < categorizedCollection.length; i++) {
      const element: TournamentEventWithPlayers = categorizedCollection[i];
      if (element?.players.length > 0) {
        element.players = element.players.sort(sortByRatingFn);
      }
    }

    // sort the same way as sorted on the blank entry form
    return categorizedCollection.sort((tep1: TournamentEventWithPlayers, tep2: TournamentEventWithPlayers) => {
      return (tep1.event.ordinalNumber === tep2.event.ordinalNumber)
        ? 0 : (tep1.event.ordinalNumber < tep2.event.ordinalNumber) ? -1 : 1;
    });
  }

  /**
   * Gets names of events for each player
   * @param eventIds
   */
  getPlayerEventList(eventIds: number[]): string {
    if (eventIds != null) {
      const eventNames: string[] = [];
      for (let i = 0; i < eventIds.length; i++) {
        const eventId = eventIds[i];
        if (this.eventIdToEventMap) {
          const tournamentEvent = this.eventIdToEventMap[eventId];
          if (tournamentEvent) {
            eventNames.push(tournamentEvent.name);
          }
        }
      }
      return eventNames.join(', ');
    } else {
      return '';
    }
  }

  sortByClub() {
    let localClubPlayerInfos: ClubPlayersInfo [] = [];
    for (const entryInfo of this.entryInfos) {
      let aClubPlayerInfo: ClubPlayersInfo = null;
      const playerClub = entryInfo.clubName ?? 'N/A';
      for (const clubPlayersInfo of localClubPlayerInfos) {
        if (clubPlayersInfo.clubName === playerClub) {
          aClubPlayerInfo = clubPlayersInfo;
          break;
        }
      }

      if (aClubPlayerInfo == null) {
        aClubPlayerInfo = new ClubPlayersInfo();
        aClubPlayerInfo.clubName = playerClub;
        localClubPlayerInfos.push(aClubPlayerInfo);
      }

      const playerInfo: PlayerInfo = new PlayerInfo();
      playerInfo.playerName = this.fullName(entryInfo.firstName, entryInfo.lastName);
      playerInfo.seedRating = entryInfo.seedRating;
      playerInfo.eligibilityRating = entryInfo.eligibilityRating;

      aClubPlayerInfo.playerInfos.push(playerInfo);
    }

    localClubPlayerInfos.sort((left: ClubPlayersInfo, right: ClubPlayersInfo) => {
      return left.clubName.localeCompare(right.clubName);
    });

    this.clubPlayersInfos = localClubPlayerInfos;
  }

  sortByState() {
    let localStatePlayerInfos: StatePlayersInfo [] = [];
    for (const entryInfo of this.entryInfos) {
      let aStatePlayerInfo: StatePlayersInfo = null;
      const playerState = (entryInfo.state === '' || entryInfo.state == null) ? 'N/A' : entryInfo.state;
      for (const statePlayersInfo of localStatePlayerInfos) {
        if (statePlayersInfo.state === playerState) {
          aStatePlayerInfo = statePlayersInfo;
          break;
        }
      }

      if (aStatePlayerInfo == null) {
        aStatePlayerInfo = new StatePlayersInfo();
        aStatePlayerInfo.state = playerState;
        localStatePlayerInfos.push(aStatePlayerInfo);
      }

      const playerInfo: PlayerInfo = new PlayerInfo();
      playerInfo.playerName = this.fullName(entryInfo.firstName, entryInfo.lastName);
      playerInfo.seedRating = entryInfo.seedRating;
      playerInfo.eligibilityRating = entryInfo.eligibilityRating;

      aStatePlayerInfo.playerInfos.push(playerInfo);
    }

    localStatePlayerInfos.sort((left: StatePlayersInfo, right: StatePlayersInfo) => {
      return left.state.localeCompare(right.state);
    });

    this.statePlayersInfos = localStatePlayerInfos;
  }

  sortByTeam() {
    if (this.hasTeamEvents && this.entryInfos != null && this.teams != null && this.teams.length > 0) {
      if (this.teamEventToTeamsMap == null) {
        // build map of profileIds to fullName and rating for fast lookup
        const profileIdToFullNameMap: Map<string, any> = new Map<string, any>();
        this.entryInfos.forEach(tei => {
          profileIdToFullNameMap.set(tei.profileId,
            { playerName: this.fullName(tei.firstName, tei.lastName), playerRating: tei.eligibilityRating}
          );
        });

        let clonedTeams: Team[] = JSON.parse(JSON.stringify(this.teams));
        for (const team of clonedTeams) {
          for (const teamMember of team.teamMembers) {
            const playerData: any = profileIdToFullNameMap.get(teamMember.profileId);
            if (playerData) {
              teamMember.playerName = playerData.playerName;
              teamMember.playerRating = playerData.playerRating;
            }
          }
        }
        // separate teams by event even though there is usually just one
        let localTeamEventToTeamsMap = new Map <TournamentEvent, Team[]>();
        const teamEvents = this.tournamentEvents.filter(
          tournamentEvent => tournamentEvent.eventEntryType == EventEntryType.TEAM);
        for (const teamEvent of teamEvents) {
          const teamEventId = teamEvent.id;
          const thisEventTeams = clonedTeams.filter(team => team.tournamentEventFk === teamEventId);
          localTeamEventToTeamsMap.set(teamEvent, thisEventTeams);
        }
        this.teams = clonedTeams;
        // separate teams by event even though there is usually just one
        this.teamEventToTeamsMap = this.sortTeamsByInternal(localTeamEventToTeamsMap, this.teamsSortedBy);
      }
    }
  }

  protected readonly TeamEntryStatus = TeamEntryStatus;

  protected sortTeamsBy($event: any) {
    const sortCriteria = $event.value;
    this.teamEventToTeamsMap = this.sortTeamsByInternal(this.teamEventToTeamsMap, sortCriteria);
  }

  private sortTeamsByInternal(teamEventToTeamsMap: Map<TournamentEvent, Team[]>, sortCriteria) : Map<TournamentEvent, Team[]>{
    const newTeamEventToTeamsMap: Map<TournamentEvent, Team[]> = new Map<TournamentEvent, Team[]>();
    teamEventToTeamsMap.forEach((teams: Team[], event: TournamentEvent) => {
      let sortedTeams = null;
      if (sortCriteria === 'name') {
        sortedTeams = teams.sort((team1: Team, team2: Team) => {
          return team1.name.localeCompare(team2.name);
        });
      } else {
        sortedTeams = teams.sort((team1: Team, team2: Team) => {
          return (team1.teamRating < team2.teamRating) ? 1 : ((team1.teamRating > team2.teamRating) ? -1 : 0);
        });
      }
      // sort team members by rating
      sortedTeams.forEach(team => team.teamMembers.sort(
        (teamMember1: any, teamMember2: any) => {
          return (teamMember1.playerRating < teamMember2.playerRating) ? 1 : -1;
        }
      ));
      newTeamEventToTeamsMap.set(event, sortedTeams);
    });
    return newTeamEventToTeamsMap;
  }
}

/**
 * Class for grouping entries by event instead of alphabetically
 */
export class TournamentEventWithPlayers {
  event: TournamentEvent;
  players: PlayerInfo [];

  constructor(event: TournamentEvent, players: PlayerInfo[]) {
    this.event = event;
    this.players = players;
  }

  public eventName(): string {
    return this.event.name;
  }

  public eventDay(): number {
    return this.event.day;
  }

  public eventStartTime(): number {
    return this.event.startTime;
  }

  public addPlayer(pi: PlayerInfo) {
    this.players.push(pi);
  }

  totalEntries(): number {
    return this.event.numEntries;
  }

  freeSpots(): number {
    return Math.max(this.event.maxEntries - this.event.numEntries, 0);
  }

  maxEntries (): number {
    return this.event.maxEntries;
  }
}

/**
 * data on individual player
 */
export class PlayerInfo {
  playerName: string;
  eligibilityRating: number;
  seedRating: number;
}

export class ClubPlayersInfo {
  clubName: string;
  playerInfos: PlayerInfo [] = [];
}

export class StatePlayersInfo {
  state: string;
  playerInfos: PlayerInfo [] = [];
}
