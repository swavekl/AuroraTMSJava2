import {ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {MembershipType, TournamentEntry} from '../model/tournament-entry.model';
import {PlayerFindPopupComponent} from '../../../profile/player-find-popup/player-find-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {BehaviorSubject} from 'rxjs';
import {FormGroup} from '@angular/forms';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {EventEntryStatus} from '../model/event-entry-status.enum';
import {AvailabilityStatus} from '../model/availability-status.enum';
import {EventEntryCommand} from '../model/event-entry-command.enum';
import {Profile} from '../../../profile/profile';
import {DateUtils} from '../../../shared/date-utils';

@Component({
  selector: 'app-entry-wizard',
  templateUrl: './entry-wizard.component.html',
  styleUrls: ['./entry-wizard.component.scss']
})
export class EntryWizardComponent implements OnInit, OnChanges {

  @Input()
  entry: TournamentEntry;

  @Input()
  tournamentStartDate: Date;

  @Input()
  teamsTournament: boolean;

  @Input()
  playerProfile: Profile;

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
  eventEntryChanged: EventEmitter<TournamentEventEntryInfo> = new EventEmitter<TournamentEventEntryInfo>();

  @Output()
  confirmEntries: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  finish: EventEmitter<any> = new EventEmitter<any>();

  columnsToDisplay: string[] = ['name', 'action'];

  public membershipOptions: any [] = [
    {value: MembershipType.NO_MEMBERSHIP_REQUIRED.valueOf(), label: 'My Membership is up to date', cost: 0, available: true},
    {value: MembershipType.TOURNAMENT_PASS_JUNIOR.valueOf(), label: 'Tournament Pass Junior (17 and under)', cost: 20, available: true},
    {value: MembershipType.TOURNAMENT_PASS_ADULT.valueOf(), label: 'Tournament Pass Adult', cost: 50, available: true},
    {value: MembershipType.BASIC_PLAN.valueOf(), label: 'Basic Plan 1 year (0 – 4 star)', cost: 25, available: true},
    {value: MembershipType.PRO_PLAN.valueOf(), label: 'Pro Plan 1 year', cost: 75, available: true},
    {value: MembershipType.LIFETIME.valueOf(), label: 'Lifetime', cost: 1300, available: true}
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

    const playerProfileChange: SimpleChange = changes.playerProfile;
    if (playerProfileChange != null) {
      this.playerProfile = playerProfileChange.currentValue;
      if (this.playerProfile != null) {
        const dateOfBirth = this.playerProfile.dateOfBirth;
        this.hideMembershipOptions(dateOfBirth, this.tournamentStartDate);
      }
    }
  }

  enteredEventsFilter(eventEntryInfo: TournamentEventEntryInfo, index: number, array: TournamentEventEntryInfo[]): boolean {
    return this.filterEventEntries(eventEntryInfo.status,
      [EventEntryStatus.ENTERED,
        EventEntryStatus.ENTERED_WAITING_LIST,
        EventEntryStatus.PENDING_CONFIRMATION,
        EventEntryStatus.PENDING_WAITING_LIST
      ]);
  }

  availableEventsFilter(eventEntryInfo: TournamentEventEntryInfo, index: number, array: TournamentEventEntryInfo[]): boolean {
    return this.filterEventEntries(eventEntryInfo.status,
      [
        EventEntryStatus.NOT_ENTERED,
        EventEntryStatus.PENDING_DELETION,
        EventEntryStatus.RESERVED_WAITING_LIST
      ]) && (eventEntryInfo.availabilityStatus === AvailabilityStatus.AVAILABLE_FOR_ENTRY);
  }

  unavailableEventsFilter(eventEntryInfo: TournamentEventEntryInfo, index: number, array: TournamentEventEntryInfo[]): boolean {
    return this.filterEventEntries(eventEntryInfo.status,
      [
        EventEntryStatus.NOT_ENTERED,
        EventEntryStatus.PENDING_DELETION
      ]) && (eventEntryInfo.availabilityStatus !== AvailabilityStatus.AVAILABLE_FOR_ENTRY);
  }

  filterEventEntries(eventEntryStatus: EventEntryStatus, statusList: EventEntryStatus []): boolean {
    const foundIndex = statusList.indexOf(eventEntryStatus);
    // console.log('eventEntryStatus', eventEntryStatus);
    // console.log ('foundIndex', foundIndex);
    return (statusList.indexOf(eventEntryStatus) !== -1);
  }

  onSave(formValues: any) {
    console.log('formValues', formValues);
    this.finish.emit(null);
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
    console.log('in onChange');
    if (this.entry != null) {
      const updatedEntry = {
        ...this.entry,
        ...form.value
      };
      this.tournamentEntryChanged.emit(updatedEntry);
    }
    this._change.markForCheck();
  }

  onEventWithdraw(eventEntryId: number) {
    // console.log ('onEventWithdraw ', eventEntryId);
    for (let i = 0; i < this.enteredEvents.length; i++) {
      const enteredEvent = this.enteredEvents[i];
      if (enteredEvent.eventEntryFk === eventEntryId) {
        const withdrawEntry: TournamentEventEntryInfo = {
          ...enteredEvent,
          event: undefined
        };
        this.eventEntryChanged.emit(withdrawEntry);
      }
    }
  }

  onEventEnter(eventId: number, eventEntryCommand: EventEntryCommand): void {
    console.log ('onEventEnter eventId ', eventId);
    console.log ('onEventEnter command ', eventEntryCommand);
    for (let i = 0; i < this.availableEvents.length; i++) {
      const availableEvent = this.availableEvents[i];
      if (availableEvent.eventFk === eventId) {
        const eventEntryInfo: TournamentEventEntryInfo = {
          ...availableEvent,
          event: undefined
        };
        this.eventEntryChanged.emit(eventEntryInfo);
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

  getPayTotal(): string {
    return this.getPlayerTotal(this.entry?.id);
  }

  isRefund(): boolean {
    return false;
  }

  getMembershipLabel(entryId: number): string {
    const membershipOption = this.getMembershipOption(entryId);
    return membershipOption.label;
  }

  getMembershipPrice(entryId: number): string {
    const membershipOption = this.getMembershipOption(entryId);
    return '$' + membershipOption.cost;
  }

  getMembershipOption(entryId: number): any {
    if (this.entry?.id === entryId) {
      const membershipOption = this.entry.membershipOption;
      for (let i = 0; i < this.membershipOptions.length; i++) {
        const option = this.membershipOptions[i];
        if (option.value === membershipOption) {
          return option;
        }
      }
    }
    return {value: 0, label: '', cost: 0};
  }

  getStatusClass(status: EventEntryStatus) {
    switch (status) {
      case EventEntryStatus.ENTERED:
        return 'status-confirmed';
      case EventEntryStatus.PENDING_CONFIRMATION:
      case EventEntryStatus.PENDING_DELETION:
        return 'status-pending-confirmation';
      case EventEntryStatus.ENTERED_WAITING_LIST:
        return 'status-waiting-list';
      case EventEntryStatus.NOT_ENTERED:
      case EventEntryStatus.PENDING_WAITING_LIST:
        return 'status-not-entered';
      default:
        return 'status-disqualified';
    }
  }

  onPayPlayerTotal() {
    console.log('Paying by credit card');
    const confirmEntry = {
      ...this.entry,
      confirm: true
    };
    this.confirmEntries.emit(confirmEntry);
  }

  /**
   *
   * @param dateOfBirth
   * @param tournamentStartDate
   * @private
   */
  private hideMembershipOptions(dateOfBirth: Date, tournamentStartDate: Date) {
    if (dateOfBirth != null && tournamentStartDate != null) {
      const ageOnTournamentStartDate = new DateUtils().getAgeOnDate(dateOfBirth, tournamentStartDate);
      this.membershipOptions.forEach((membershipOption: any) => {
        switch (membershipOption.value) {
          case MembershipType.TOURNAMENT_PASS_JUNIOR:
            membershipOption.available = (ageOnTournamentStartDate < 18);
            break;
          case MembershipType.TOURNAMENT_PASS_ADULT:
            membershipOption.available = (ageOnTournamentStartDate >= 18);
            break;
          case MembershipType.PRO_PLAN:
          case MembershipType.BASIC_PLAN:
            break;
        }
      });
    }
  }
}
