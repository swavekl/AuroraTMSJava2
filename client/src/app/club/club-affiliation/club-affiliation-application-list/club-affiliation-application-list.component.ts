import {AfterViewInit, Component, OnDestroy, ViewChild} from '@angular/core';
import {Router} from '@angular/router';
import {UntypedFormControl} from '@angular/forms';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {MatDialog} from '@angular/material/dialog';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';
import {Subscription} from 'rxjs';
import {ClubAffiliationApplicationListDataSource} from './club-affiliation-application-list-datasource';
import {ClubAffiliationApplication} from '../model/club-affiliation-application.model';
import {ClubAffiliationApplicationService} from '../service/club-affiliation-application.service';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-club-affiliation-application-list',
  templateUrl: './club-affiliation-application-list.component.html',
  styleUrls: ['./club-affiliation-application-list.component.scss']
})
export class ClubAffiliationApplicationListComponent implements AfterViewInit, OnDestroy {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatTable) table!: MatTable<ClubAffiliationApplication>;
  @ViewChild('filterClubNameCtrl') filterClubNameCtrl: UntypedFormControl;
  dataSource: ClubAffiliationApplicationListDataSource;
  filterClubName: string;
  editApplicationLink: string = '../edit';

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  displayedColumns = ['name', 'cityState', 'affiliation_expiration_date', 'status', 'actions'];

  private subscriptions: Subscription = new Subscription();

  constructor(private clubAffiliationApplicationService: ClubAffiliationApplicationService,
              private linearProgressBarService: LinearProgressBarService,
              private router: Router,
              private dialog: MatDialog) {
    this.dataSource = new ClubAffiliationApplicationListDataSource(clubAffiliationApplicationService);
    this.setupProgressIndicator();
  }

  private setupProgressIndicator() {
    const loadingSubscription = this.clubAffiliationApplicationService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.filterClubNameCtrl.valueChanges
      .pipe(
        skip(1),
        distinctUntilChanged(),
        debounceTime(500)
      )
      .subscribe((value) => {
        this.dataSource.filterByName$.next(value);
      });
    this.table.dataSource = this.dataSource;
  }

  newApplication() {
    this.router.navigateByUrl('/ui/clubaffiliation/edit/0');
  }

  canAddApplication(): boolean {
    return true;
  }

  onDeleteApplication(applicationId: number, clubName: string) {
    const config = {
      width: '450px', height: '230px', data: {
        message: `Are you sure you want to delete application for '${clubName}'?`,
      }
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        this.dataSource.deleteApplication(applicationId);
      }
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
