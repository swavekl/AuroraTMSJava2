import {AfterViewInit, Component, OnDestroy, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {UntypedFormControl} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {Router} from '@angular/router';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';
import {Subscription} from 'rxjs';
import {InsuranceRequest} from '../model/insurance-request.model';
import {InsuranceListDataSource} from './insurance-list-datasource';
import {InsuranceRequestService} from '../service/insurance-request.service';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-insurance-list',
  templateUrl: './insurance-list.component.html',
  styleUrls: ['./insurance-list.component.scss']
})
export class InsuranceListComponent implements AfterViewInit, OnDestroy {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatTable) table!: MatTable<InsuranceRequest>;
  @ViewChild('filterClubNameCtrl') filterClubNameCtrl: UntypedFormControl;
  dataSource: InsuranceListDataSource;
  filterClubName: string;

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  displayColumns = ['eventName', 'eventStartDate', 'status', 'requestDate', 'actions'];

  private subscriptions: Subscription = new Subscription();

  constructor(private insuranceRequestService: InsuranceRequestService,
              private linearProgressBarService: LinearProgressBarService,
              private router: Router,
              private dialog: MatDialog) {
    this.dataSource = new InsuranceListDataSource(insuranceRequestService);
    this.setupProgressIndicator();
  }

  private setupProgressIndicator() {
    const loadingSubscription = this.insuranceRequestService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
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

  newInsuranceRequest() {
    this.router.navigateByUrl('/ui/insurance/edit/0');
  }

  editInsuranceRequest(insuranceRequestId: number) {
    this.router.navigateByUrl('/ui/insurance/edit/' + insuranceRequestId);
  }

  canAddInsuranceRequest(): boolean {
    return true;
  }

  onDeleteInsuranceRequest(insuranceRequestId: number, eventName: string) {
    const config = {
      width: '450px', height: '230px', data: {
        message: `Are you sure you want to delete insurance request for '${eventName}'?`,
      }
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        this.dataSource.delete(insuranceRequestId);
      }
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
