import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {TournamentEvent} from '../tournament-event.model';
import {DateUtils} from '../../../shared/date-utils';

@Component({
  selector: 'app-tournament-waiting-list',
  templateUrl: './tournament-waiting-list.component.html',
  styleUrls: ['./tournament-waiting-list.component.scss']
})
export class TournamentWaitingListComponent implements OnInit, OnChanges {

  // contains both the waiting list and confirmed entries
  @Input()
  tournamentEntryInfos: TournamentEntryInfo[] = [];

  @Input()
  tournamentEvents: TournamentEvent[];

  @Input()
  standaloneScreen: boolean = true;

  @Input()
  tournamentId: number;

  // map of event id to event object for faster lookup
  private eventIdToEventMap: any;

  eventWithPlayersList: EventWithPlayers [] = [];

  sortBy: string;

  returnUrl: string;

  totalWaitedSpots: number;

  constructor() {
    this.sortBy = 'name';
    this.totalWaitedSpots = 0;
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
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

    if (this.tournamentEvents != null && this.tournamentEntryInfos != null) {
      this.prepareByEventList();
    }
    this.returnUrl = window.location.pathname;
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
          eventNames.push(tournamentEvent.name);
        }
      }
      return eventNames.join(', ');
    } else {
      return '';
    }
  }

  onSortByName() {
    this.sortBy = 'name';
  }

  onSortByEvent() {
    this.sortBy = 'event';
  }

  private prepareByEventList() {
    this.eventWithPlayersList = [];
    // sort by entry id which is increasing as new event entries are added
    this.tournamentEntryInfos.sort((tee1: TournamentEntryInfo, tee2: TournamentEntryInfo) => {
      const fullName1 = tee1.lastName + ', ' + tee1.firstName;
      const fullName2 = tee2.lastName + ', ' + tee2.firstName;
      return fullName1.localeCompare(fullName2);
        // return tee1.entryId < tee2.entryId ? -1 : 1;
    });

    // transform each player's event entries into list of
    for (let i = 0; i < this.tournamentEntryInfos.length; i++) {
      const tei: TournamentEntryInfo = this.tournamentEntryInfos[i];
      const fullPlayerName = tei.lastName + ', ' + tei.firstName;
      let waitingListEventIndex = 0;
      for (const waitingListEventId of tei.waitingListEventIds) {
        let foundEventWithPlayers: EventWithPlayers = null;
        for (const eventWithPlayers of this.eventWithPlayersList) {
          if (eventWithPlayers.eventId === waitingListEventId) {
            foundEventWithPlayers = eventWithPlayers;
            break;
          }
        }
        if (foundEventWithPlayers == null) {
          foundEventWithPlayers = new EventWithPlayers();
          foundEventWithPlayers.eventId = waitingListEventId;
          const tournamentEvent: TournamentEvent = this.eventIdToEventMap[waitingListEventId];
          foundEventWithPlayers.eventName = tournamentEvent.name;
          foundEventWithPlayers.ordinalNumber = tournamentEvent.ordinalNumber;
          this.eventWithPlayersList.push(foundEventWithPlayers);
        }
        foundEventWithPlayers.playersOnWaitingList.push(fullPlayerName);

        if (waitingListEventIndex < tei?.waitingListEnteredDates.length) {
          let wlEnteredDate: Date = tei.waitingListEnteredDates[waitingListEventIndex];
          wlEnteredDate = wlEnteredDate ?? new Date();
          foundEventWithPlayers.playersDatesEnteredWL.push(wlEnteredDate);
        }
        waitingListEventIndex++;
      }
    }

    // sort by event id
    this.eventWithPlayersList.sort(
      (e1: EventWithPlayers, e2: EventWithPlayers) => {
        return e1.ordinalNumber < e2.ordinalNumber ? -1 : 1; }
    );
    let totalWaitedSpots = 0;
    for (const eventWithPlayers of this.eventWithPlayersList) {
      totalWaitedSpots += eventWithPlayers.playersOnWaitingList.length;
    }

    const dateUtils = new DateUtils();
    for (const eventWithPlayers of this.eventWithPlayersList) {
      let playersToSort: PlayerAndDate [] = [];
      for (let i = 0; i < eventWithPlayers.playersOnWaitingList.length; i++) {
        const playerName: string = eventWithPlayers.playersOnWaitingList[i];
        const enteredDate: Date = eventWithPlayers.playersDatesEnteredWL[i];
        const pad: PlayerAndDate = {playerName: playerName, enteredDate: enteredDate};
        playersToSort.push(pad);
      }
      playersToSort.sort((playerAndDate1: PlayerAndDate, playerAndDate2: PlayerAndDate) => {
        const enteredDate1: Date = playerAndDate1.enteredDate;
        const enteredDate2: Date = playerAndDate2.enteredDate;
        return dateUtils.isDateBefore(enteredDate1, enteredDate2) ? -1 : 1;
      });
      const sortedByDateNames: string [] = playersToSort.map(
        (playerAndDate: PlayerAndDate) => {
          return playerAndDate.playerName;
        }
      );

      eventWithPlayers.playersOnWaitingList = sortedByDateNames;
    }
    this.totalWaitedSpots = totalWaitedSpots;
  }
}

/**
 * Class for displaying list of events with players waiting on them
 */
export class EventWithPlayers {
  eventId: number;
  ordinalNumber: number;
  eventName: string;
  playersOnWaitingList: string [] = [];
  playersDatesEnteredWL: Date [] = [];
}

export class PlayerAndDate {
  playerName: string;
  enteredDate: Date;
}
