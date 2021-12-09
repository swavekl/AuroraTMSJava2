import {Router} from '@angular/router';
import {FormControl} from '@angular/forms';
import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {SanctionRequestListDataSource} from './sanction-request-list-data-source';
import {SanctionRequestService} from '../service/sanction-request.service';
import {SanctionRequest} from '../model/sanction-request.model';

@Component({
  selector: 'app-sanction-list',
  templateUrl: './sanction-request-list.component.html',
  styleUrls: ['./sanction-request-list.component.scss']
})
export class SanctionRequestListComponent implements AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatTable) table!: MatTable<SanctionRequest>;
  @ViewChild('filterClubNameCtrl') filterClubNameCtrl: FormControl;
  dataSource: SanctionRequestListDataSource;
  filterClubName: string;

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  displayColumns = ['tournamentName', 'startDate', 'status', 'actions'];

  constructor(private sanctionRequestService: SanctionRequestService,
              private router: Router,
              private dialog: MatDialog) {
    this.dataSource = new SanctionRequestListDataSource(sanctionRequestService);
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.table.dataSource = this.dataSource;
    this.filterClubNameCtrl.valueChanges
      .pipe(
        skip(1),
        distinctUntilChanged(),
        debounceTime(500)
      )
      .subscribe((value) => {
        this.dataSource.filterByName$.next(value);
      });
  }

  canAddSanctionRequest(): boolean {
    return true;
  }

  onDeleteSanctionRequest(sanctionRequestId: number, eventName: string) {
    const config = {
      width: '450px', height: '230px', data: {
        message: `Are you sure you want to delete sanction request for '${eventName}'?`,
      }
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        this.dataSource.delete(sanctionRequestId);
      }
    });
  }

}
