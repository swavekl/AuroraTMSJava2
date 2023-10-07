import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {TournamentEvent} from '../tournament-event.model';

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

  // map of event id to event object for faster lookup
  private eventIdToEventMap: any;

  eventWithPlayersList: EventWithPlayers [] = [];

  sortBy: string;

  constructor() {
    this.sortBy = 'name';
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
    // transform each player's event entries into list of
    for (let i = 0; i < this.tournamentEntryInfos.length; i++) {
      const tei: TournamentEntryInfo = this.tournamentEntryInfos[i];
      const fullPlayerName = tei.lastName + ', ' + tei.firstName;

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
      }
    }

    // sort by event id
    this.eventWithPlayersList.sort(
      (e1: EventWithPlayers, e2: EventWithPlayers) => {
        return e1.ordinalNumber < e2.ordinalNumber ? -1 : 1; }
    );
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
}
