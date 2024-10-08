import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChange,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {MatTable} from '@angular/material/table';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {UmpireSummaryTableDataSource} from './umpire-summary-table-datasource';
import {UmpiringService} from '../../service/umpiring.service';
import {UmpireWorkSummary} from '../../model/umpire-work-summary.model';

@Component({
  selector: 'app-umpire-summary-table',
  templateUrl: './umpire-summary-table.component.html',
  styleUrl: './umpire-summary-table.component.scss'
})
export class UmpireSummaryTableComponent implements AfterViewInit, OnChanges {
  @Input()
  private tournamentId: number;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatTable) table!: MatTable<UmpireWorkSummary>;
  dataSource: UmpireSummaryTableDataSource;

  @Output()
  private viewDetailsEvent: EventEmitter<string> = new EventEmitter<string>();

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  displayedColumns = ['umpireName', 'numUmpiredMatches', 'numAssistantUmpiredMatches'];

  constructor(private umpiringService: UmpiringService) {
    this.tournamentId = 0;
    this.dataSource = new UmpireSummaryTableDataSource(this.umpiringService);
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentIdSimpleChange: SimpleChange = changes.tournamentId;
    if (tournamentIdSimpleChange != null) {
      const tournamentId = tournamentIdSimpleChange.currentValue;
      if (!tournamentIdSimpleChange.isFirstChange() && tournamentId != undefined) {
        this.tournamentId = tournamentId;
        this.dataSource.tournamentId = this.tournamentId;
      }
    }
  }


  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.table.dataSource = this.dataSource;
  }

  onViewUmpireDetails(umpireProfileId: string) {
    this.viewDetailsEvent.emit(umpireProfileId);
  }

  refresh() {
    this.dataSource.loadSummaries(this.tournamentId);
    this.viewDetailsEvent.emit(null);
  }
}
