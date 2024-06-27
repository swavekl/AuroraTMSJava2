import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';

@Component({
  selector: 'app-tournament-players-list',
  templateUrl: './tournament-players-list.component.html',
  styleUrls: ['./tournament-players-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentPlayersListComponent implements OnInit, OnChanges {

  @Input()
  entryInfos: TournamentEntryInfo[];

  @Input()
  tournamentEvents: TournamentEvent[];

  @Input()
  tournamentStartDate: Date;

  // tournament events with players in them
  tournamentEventsWithPlayers: TournamentEventWithPlayers[];

  sortBy: string;

  // map of event id to event object for faster lookup
  private eventIdToEventMap: any;

  // players grouped by club
  clubPlayersInfos: ClubPlayersInfo [] = null;

  // players grouped by state
  statePlayersInfos: StatePlayersInfo [] = null;

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
        this.entryInfos = this.sortEntries(entryInfos);
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
    return categorizedCollection;
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
      console.log(`entryInfo.state = '${entryInfo.state}'`);
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
