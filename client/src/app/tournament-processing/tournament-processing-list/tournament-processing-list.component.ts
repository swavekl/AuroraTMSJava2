import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {FormControl} from '@angular/forms';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';
import {TournamentProcessingListDataSource} from './tournament-processing-list-datasource';
import {TournamentProcessingService} from '../service/tournament-processing.service';
import {TournamentProcessingRequest} from '../model/tournament-processing-request';

@Component({
  selector: 'app-tournament-processing-list',
  templateUrl: './tournament-processing-list.component.html',
  styleUrls: ['./tournament-processing-list.component.css']
})
export class TournamentProcessingListComponent implements AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatTable) table!: MatTable<TournamentProcessingRequest>;
  @ViewChild('filterTournamentNameCtrl') filterTournamentNameCtrl: FormControl;
  dataSource: TournamentProcessingListDataSource;

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  displayedColumns = ['eventName', 'eventStartDate', 'status'];

  filterTournamentName: string;

  constructor(private tournamentProcessingService: TournamentProcessingService) {
    this.dataSource = new TournamentProcessingListDataSource(tournamentProcessingService);
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.table.dataSource = this.dataSource;
    this.filterTournamentNameCtrl.valueChanges
      .pipe(
        skip(1),
        distinctUntilChanged(),
        debounceTime(500)
      )
      .subscribe((value) => {
        this.dataSource.filterByName$.next(value);
      });

  }
}
