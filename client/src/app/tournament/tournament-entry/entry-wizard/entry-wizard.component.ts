import {ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {MembershipType, TournamentEntry} from '../model/tournament-entry.model';
import {PlayerFindPopupComponent} from '../../../profile/player-find-popup/player-find-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {BehaviorSubject} from 'rxjs';
import {EventEntryStatus, TournamentEventEntry} from '../model/tournament-event-entry.model';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {FormGroup} from '@angular/forms';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';

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
  allEventEntryInfos: TournamentEventEntryInfo[];

  enteredEvents: TournamentEventEntryInfo[] = [];
  availableEvents: TournamentEventEntryInfo[] = [];
  unavailableEvents: TournamentEventEntryInfo[] = [];

  @Output()
  tournamentEntryChanged: EventEmitter<TournamentEntry> = new EventEmitter<TournamentEntry>();

  @Output()
  eventEntryChanged: EventEmitter<TournamentEventEntry> = new EventEmitter<TournamentEventEntry>();

  columnsToDisplay: string[] = ['name', 'action'];

  public membershipOptions: any [] = [
    {value: MembershipType.NO_MEMBERSHIP_REQUIRED.valueOf(), label: 'My Membership is up to date', cost: 0},
    {value: MembershipType.TOURNAMENT_PASS.valueOf(), label: 'Tournament Pass', cost: 20},
    {value: MembershipType.JUNIOR_ONE_YEAR.valueOf(), label: 'Junior 1-year (17 and under)', cost: 45},
    {value: MembershipType.JUNIOR_THREE_YEARS.valueOf(), label: 'Junior 3-year (14 and under)', cost: 125},
    {value: MembershipType.COLLEGIATE_ONE_YEAR.valueOf(), label: 'Collegiate 1-year', cost: 45},
    {value: MembershipType.ADULT_ONE_YEAR.valueOf(), label: 'Adult 1 year', cost: 75},
    {value: MembershipType.ADULT_THREE_YEARS.valueOf(), label: 'Adult 3 years', cost: 210},
    {value: MembershipType.ADULT_FIVE_YEARS.valueOf(), label: 'Adult 5 years', cost: 325},
    {value: MembershipType.HOUSEHOLD_ONE_YEAR.valueOf(), label: 'Household 1-year', cost: 150},
    {value: MembershipType.LIFETIME.valueOf(), label: 'Lifetime', cost: 1300}
  ];

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

  enteredEventsFilter(eventEntryInfo: TournamentEventEntryInfo, index: number, array: TournamentEventEntryInfo[]): boolean {
    return this.filterEventEntries(eventEntryInfo.eventEntry.status,
      [EventEntryStatus.CONFIRMED,
        EventEntryStatus.PENDING_CONFIRMATION,
        EventEntryStatus.ENTERED_WAITING_LIST
      ]);
  }

  availableEventsFilter(eventEntryInfo: TournamentEventEntryInfo, index: number, array: TournamentEventEntryInfo[]): boolean {
    return this.filterEventEntries(eventEntryInfo.eventEntry.status,
      [
        EventEntryStatus.NOT_ENTERED,
        EventEntryStatus.PENDING_DELETION,
        EventEntryStatus.WAITING_LIST
      ]);
  }

  unavailableEventsFilter(eventEntryInfo: TournamentEventEntryInfo, index: number, array: TournamentEventEntryInfo[]): boolean {
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

  onChange(form: FormGroup) {
    console.log ('in onChange');
    if (this.entry != null) {
      const updatedEntry = {
        ...this.entry,
        ...form.value };
      this.tournamentEntryChanged.emit(updatedEntry);
    }
    this._change.markForCheck();
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
        this.eventEntryChanged.emit(withdrawEntry);
      }
    }
  }

  onEventEnter(eventId: number): void {
    // console.log ('onEventEnter ', eventId);
    for (let i = 0; i < this.availableEvents.length; i++) {
      const availableEvent = this.availableEvents[i];
      if (availableEvent.event.id === eventId) {
        const eventEntry: TournamentEventEntry = {...availableEvent.eventEntry};
        // const eventEntry: TournamentEventEntry = new TournamentEventEntry();
        // eventEntry.tournamentEntryFk = this.entry.id;
        // eventEntry.tournamentEventFk = eventId;
        // eventEntry.tournamentFk = this.entry.tournamentFk;
        eventEntry.dateEntered = new Date();
        if (availableEvent.eventEntry.status === EventEntryStatus.WAITING_LIST) {
          eventEntry.status = EventEntryStatus.ENTERED_WAITING_LIST;
        } else {
          eventEntry.status = EventEntryStatus.PENDING_CONFIRMATION;
        }
        this.eventEntryChanged.emit(eventEntry);
      }
    }
  }

  getPlayerTotal(tournamentEntryId: number): string {
    let total = 0;
    const membershipOption = this.entry?.membershipOption;
    for (let i = 0; i < this.membershipOptions.length; i++) {
      const option = this.membershipOptions[i];
      if (option.value === membershipOption) {
        total += option.cost;
        break;
      }
    }

    for (let i = 0; i < this.enteredEvents.length; i++) {
      const enteredEvent = this.enteredEvents[i];
      total += enteredEvent.event.feeAdult;
    }
    return '$' + total;
  }

  getMembershipAndPrice(entryId: number): string {
    let membershipAndCost = '';
    if (this.entry?.id === entryId) {
      const membershipOption = this.entry.membershipOption;
      for (let i = 0; i < this.membershipOptions.length; i++) {
        const option = this.membershipOptions[i];
        if (option.value === membershipOption) {
          const cost = option.cost > 0 ? '$' + option.cost : '';
          membershipAndCost = `${option.label}: ${cost}`;
          break;
        }
      }
    }
    return membershipAndCost;
  }

}
