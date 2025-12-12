import {Component, EventEmitter, Input, OnChanges, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEvent} from '../tournament-event.model';
import {CommonRegexPatterns} from '../../../shared/common-regex-patterns';
import {FeeScheduleItem} from '../model/fee-schedule-item';
import {FeeStructure} from '../model/fee-structure.enum';
import {EventEntryType} from '../model/event-entry-type.enum';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';

@Component({
  selector: 'app-event-fees-config',
  standalone: false,
  templateUrl: './event-fees-config.component.html',
  styleUrl: './event-fees-config.component.scss'
})
export class EventFeesConfigComponent implements OnChanges {
  @Input()
  tournamentEvent!: TournamentEvent;

  @Output()
  feesChanged = new EventEmitter<any>();

  readonly PRICE_REGEX = CommonRegexPatterns.PRICE_REGEX;

  readonly feeStructures: any [] = [
    {value: FeeStructure.FIXED, name: 'Fixed'},
    {value: FeeStructure.PER_SCHEDULE, name: 'Schedule of Fees'},
    {value: FeeStructure.PACKAGE_DISCOUNT, name: 'Package Discount'}
  ];

  protected readonly FeeStructure = FeeStructure;
  protected readonly EventEntryType = EventEntryType;

  constructor(private dialog: MatDialog) {

  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentEventChange: SimpleChange = changes.tournamentEvent;
    if (tournamentEventChange?.currentValue != null) {
      const tournamentEvent: TournamentEvent = tournamentEventChange.currentValue;
      if (tournamentEvent.feeStructure == null) {
        this.tournamentEvent.feeStructure = FeeStructure.FIXED;
      }
    }
  }

  protected onAddScheduleItem() {
    const feeSchedule = this.tournamentEvent.feeScheduleItems || [];
    const newItem: FeeScheduleItem = new FeeScheduleItem();
    this.tournamentEvent.feeScheduleItems = [...feeSchedule, newItem];
  }

  protected onRemoveScheduleItem(deleteAtIndex: number) {
    const config = {
      width: '450px', height: '230px', data: {
        message: `Are you sure you want to delete this schedule item?`
      }
    };

    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        const feeSchedule = this.tournamentEvent.feeScheduleItems || [];
        feeSchedule.splice(deleteAtIndex, 1);
        this.tournamentEvent.feeScheduleItems = [...feeSchedule];
      }
    });
  }

}

export default EventFeesConfigComponent;
