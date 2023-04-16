import {Component, Input} from '@angular/core';
import {SummaryReportItem} from '../summary-report.model';

@Component({
  selector: 'app-entry-summary-table',
  templateUrl: './entry-summary-table.component.html',
  styleUrls: ['./entry-summary-table.component.scss']
})
export class EntrySummaryTableComponent {
  @Input()
  summaryReportItems: SummaryReportItem [] = [];

  @Input()
  entryTotal: number;

  @Input()
  tournamentCurrency: string;
}
