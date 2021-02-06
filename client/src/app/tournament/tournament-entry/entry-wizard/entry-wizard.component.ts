import {ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {TournamentEntry} from '../model/tournament-entry.model';
import {PlayerFindPopupComponent} from '../../../profile/player-find-popup/player-find-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {BehaviorSubject, Observable} from 'rxjs';
import {EventEntryStatus, TournamentEventEntry} from '../model/tournament-event-entry.model';
import {EventEntryInfo} from '../model/event-entry-info-model';

@Component({
  selector: 'app-entry-wizard',
  templateUrl: './entry-wizard.component.html',
  styleUrls: ['./entry-wizard.component.css']
})
export class EntryWizardComponent implements OnInit, OnChanges {

  @Input()
  entry: TournamentEntry;

  @Input()
  tournamentStartDate: Date;

  @Input()
  teamsTournament: boolean;

  @Input()
  otherPlayers: any[];

  otherPlayersBS$: BehaviorSubject<any[]>;

  @Input()
  allEventEntryInfos: EventEntryInfo[];

  enteredEvents: EventEntryInfo[] = [];
  availableEvents: EventEntryInfo[] = [];
  unavailableEvents: EventEntryInfo[] = [];

  @Output()
  emitter: EventEmitter<TournamentEntry>;

  @Output()
  eventEntry: EventEmitter<TournamentEventEntry> = new EventEmitter<TournamentEventEntry>();

  columnsToDisplay: string[] = ['name', 'action'];

  constructor(private dialog: MatDialog,
              private _change: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.otherPlayersBS$ = new BehaviorSubject(this.otherPlayers);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.allEventEntryInfos != null) {
      this.allEventEntryInfos = changes.allEventEntryInfos.currentValue || [];
      this.enteredEvents = this.allEventEntryInfos.filter(this.enteredEventsFilter, this);
      this.availableEvents = this.allEventEntryInfos.filter(this.availableEventsFilter, this);
      this.unavailableEvents = this.allEventEntryInfos.filter(this.unavailableEventsFilter, this);
    }
  }

  enteredEventsFilter(eventEntryInfo: EventEntryInfo, index: number, array: EventEntryInfo[]): boolean {
    return this.filterEventEntries(eventEntryInfo.eventEntry.status,
      [EventEntryStatus.CONFIRMED,
        EventEntryStatus.PENDING_CONFIRMATION,
        EventEntryStatus.ENTERED_WAITING_LIST
      ]);
  }

  availableEventsFilter(eventEntryInfo: EventEntryInfo, index: number, array: EventEntryInfo[]): boolean {
    return this.filterEventEntries(eventEntryInfo.eventEntry.status,
      [
        EventEntryStatus.NOT_ENTERED,
        EventEntryStatus.PENDING_DELETION,
        EventEntryStatus.WAITING_LIST
      ]);
  }

  unavailableEventsFilter(eventEntryInfo: EventEntryInfo, index: number, array: EventEntryInfo[]): boolean {
    return this.filterEventEntries(eventEntryInfo.eventEntry.status,
      [EventEntryStatus.DISQUALIFIED_RATING,
        EventEntryStatus.DISQUALIFIED_AGE,
        EventEntryStatus.DISQUALIFIED_GENDER,
        EventEntryStatus.DISQUALIFIED_TIME_CONFLICT
      ]);
  }

  filterEventEntries(eventEntryStatus: EventEntryStatus, statusList: EventEntryStatus []): boolean {
    const foundIndex = statusList.indexOf(eventEntryStatus);
    // console.log('eventEntryStatus', eventEntryStatus);
    // console.log ('foundIndex', foundIndex);
    return (statusList.indexOf(eventEntryStatus) !== -1);
  }

  onSave(formValues: any) {
    console.log('formValues', formValues);
  }

  editEntry(profileId: number) {

  }

  addMember() {
    const config = {
      width: '250px', height: '550px', data: {}
    };
    const me = this;
    const dialogRef = this.dialog.open(PlayerFindPopupComponent, config);
    dialogRef.afterClosed().subscribe(next => {
      if (next !== null && next !== 'cancel') {
        console.log('got ok player', next);
        const currentValue = me.otherPlayersBS$.getValue();
        currentValue.push(next);
        this.otherPlayersBS$.next(currentValue);
      }
    });
  }

  typeChanged(event) {
    console.log('event', event);
    // console.log ('this.entry.type', this.entry.type);
    const type: string = <string>event.value;
    // this.entry.type = EntryType[type];
    // this._change.markForCheck();
  }

  onEventWithdraw(eventEntryId: number) {
    // console.log ('onEventWithdraw ', eventEntryId);
    for (let i = 0; i < this.enteredEvents.length; i++) {
      const enteredEvent = this.enteredEvents[i];
      if (enteredEvent.eventEntry.id === eventEntryId) {
        const withdrawEntry: TournamentEventEntry = {
          ...enteredEvent.eventEntry,
          status: EventEntryStatus.PENDING_DELETION
        };
        this.eventEntry.emit(withdrawEntry);
      }
    }
  }

  onEventEnter(eventId: number): void {
    // console.log ('onEventEnter ', eventId);
    for (let i = 0; i < this.availableEvents.length; i++) {
      const availableEvent = this.availableEvents[i];
      if (availableEvent.event.id === eventId) {
        const eventEntry: TournamentEventEntry = new TournamentEventEntry();
        eventEntry.tournamentEntryFk = this.entry.id;
        eventEntry.tournamentEventFk = eventId;
        eventEntry.tournamentFk = this.entry.tournamentFk;
        eventEntry.dateEntered = new Date();
        if (availableEvent.eventEntry.status === EventEntryStatus.NOT_ENTERED) {
          eventEntry.status = EventEntryStatus.PENDING_CONFIRMATION;
        } else if (availableEvent.eventEntry.status === EventEntryStatus.WAITING_LIST) {
          eventEntry.status = EventEntryStatus.ENTERED_WAITING_LIST;
        }
        this.eventEntry.emit(eventEntry);
      }
    }
  }
}
