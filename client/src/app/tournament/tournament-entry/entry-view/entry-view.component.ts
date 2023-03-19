import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEntry} from '../model/tournament-entry.model';
import {Tournament} from '../../tournament-config/tournament.model';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {EventEntryStatus} from '../model/event-entry-status.enum';
import {Subscription} from 'rxjs';
import {DateUtils} from '../../../shared/date-utils';
import {TodayService} from '../../../shared/today.service';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';

@Component({
  selector: 'app-entry-view',
  templateUrl: './entry-view.component.html',
  styleUrls: ['./entry-view.component.scss']
})
export class EntryViewComponent implements OnInit, OnChanges, OnDestroy {
  @Input()
  entry: TournamentEntry;

  @Input()
  tournament: Tournament;

  @Input()
  allEventEntryInfos: TournamentEventEntryInfo[];

  @Output()
  action: EventEmitter<string> = new EventEmitter<string>();

  enteredEvents: TournamentEventEntryInfo[] = [];

  tournamentStartDate: Date;

  private subscriptions = new Subscription ();

  constructor(private todayService: TodayService,
              private messageDialog: MatDialog) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.allEventEntryInfos != null) {
      this.allEventEntryInfos = changes.allEventEntryInfos.currentValue || [];
      this.enteredEvents = this.allEventEntryInfos.filter(this.enteredEventsFilter, this);
    }
    const tournamentChange: SimpleChange = changes.tournament;
    if (tournamentChange != null) {
      this.tournament = tournamentChange.currentValue;
      if (this.tournament != null) {
        this.tournamentStartDate = new DateUtils().convertFromString(this.tournament.startDate);
      }
    }

    const entryChanges: SimpleChange = changes.entry;
    if (entryChanges != null) {
      this.entry = entryChanges.currentValue;
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  ngOnInit(): void {
  }

  enteredEventsFilter(eventEntryInfo: TournamentEventEntryInfo, index: number, array: TournamentEventEntryInfo[]): boolean {
    return this.filterEventEntries(eventEntryInfo.status,
      [EventEntryStatus.ENTERED,
        EventEntryStatus.ENTERED_WAITING_LIST,
        EventEntryStatus.PENDING_CONFIRMATION,
        EventEntryStatus.PENDING_WAITING_LIST
      ]);
  }

  filterEventEntries(eventEntryStatus: EventEntryStatus, statusList: EventEntryStatus []): boolean {
    return (statusList.indexOf(eventEntryStatus) !== -1);
  }

  private isBeforeEntryCutoffDate() {
    const entryCutoffDate = this.tournament?.configuration.entryCutoffDate;
    if (entryCutoffDate != null) {
      const today = this.todayService.todaysDate;
      return (new DateUtils().isDateBefore(today, entryCutoffDate));
    } else {
      return false;
    }
  }

  canModify() {
    const isBeforeEntryCutoffDate = this.isBeforeEntryCutoffDate();
    const hasEntry = this.entry?.id !== 0;
    return hasEntry && isBeforeEntryCutoffDate;
  }

  private isBeforeRefundDate() {
    const refundDate = this.tournament?.configuration.refundDate;
    if (refundDate) {
      const today = this.todayService.todaysDate;
      return (new DateUtils().isDateBefore(today, refundDate));
    } else {
      return false;
    }
  }

  canWithdraw() {
    const isBeforeEntryCutoffDate = this.isBeforeRefundDate();
    const hasEntry = this.entry?.id !== 0;
    const hasEvents = this.enteredEvents?.length > 0;
    return hasEntry && hasEvents && isBeforeEntryCutoffDate;
  }

  onModify() {
    this.action.emit('modify');
  }

  onWithdraw() {
    const message = "To withdraw from the tournament, please remove yourself from each event one by one. " +
      "Then continue through all the steps of the dialog and initiate a refund.  ";
      // "You will get 2 emails confirming your refund and withdrawal." +
      // "Thank you for your interest in our tournament.";
    const dialogRef = this.messageDialog.open(ConfirmationPopupComponent, {
      width: '300px', height: '320px',
      data: { message: message, title: 'Withdrawal Confirmation',
        showCancelButton: true, contentAreaHeight: '170px' }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed with result ', result);
      if (result === 'ok') {
        // tell them what to expect in dialog
        this.action.emit('withdraw');
      } else {

      }
    });

  }
}
