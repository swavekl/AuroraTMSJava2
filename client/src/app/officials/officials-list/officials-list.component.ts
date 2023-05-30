import {AfterViewInit, Component, OnDestroy, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {UntypedFormControl} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {Router} from '@angular/router';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';
import {Subscription} from 'rxjs';
import {Official} from '../model/official.model';
import {OfficialsListDataSource} from './officials-list-data-source';
import {OfficialService} from '../service/official.service';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {UserRoles} from '../../user/user-roles.enum';
import {AuthenticationService} from '../../user/authentication.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {StatesList} from '../../shared/states/states-list';

@Component({
  selector: 'app-officials-list',
  templateUrl: './officials-list.component.html',
  styleUrls: ['./officials-list.component.scss']
})
export class OfficialsListComponent implements AfterViewInit, OnDestroy {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatTable) table!: MatTable<Official>;
  @ViewChild('filterNameCtrl') filterNameCtrl: UntypedFormControl;
  @ViewChild('filterStateCtrl') filterStateCtrl: UntypedFormControl;
  dataSource: OfficialsListDataSource;
  filterName: string;
  filterState: string;

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  displayColumns = ['firstName', 'lastName', 'state', 'umpireRank', 'refereeRank', 'actions'];

  editUrl: string = '/ui/officials/edit';

  statesList: any[] = [];

  private subscriptions: Subscription = new Subscription();

  constructor(private officialService: OfficialService,
              private authenticationService: AuthenticationService,
              private linearProgressBarService: LinearProgressBarService,
              private router: Router,
              private dialog: MatDialog) {
    this.dataSource = new OfficialsListDataSource(officialService);
    this.statesList = StatesList.getCountryStatesList('US');
    this.setupProgressIndicator();
  }

  private setupProgressIndicator() {
    const loadingSubscription = this.officialService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.table.dataSource = this.dataSource;
    this.filterNameCtrl.valueChanges
      .pipe(
        skip(1),
        distinctUntilChanged(),
        debounceTime(500)
      )
      .subscribe((value) => {
        this.dataSource.filterByName$.next(value);
      });
    this.filterStateCtrl.valueChanges
      .pipe(distinctUntilChanged())
      .subscribe((state) => {
        if (state != undefined) {
          this.dataSource.filterByState$.next(state);
        }
      });
  }

  addOfficial() {
    const extras = {
      state: {
        returnUrl: '/ui/officials'
      }
    };
    this.router.navigateByUrl('/ui/userprofile/addbytd/0', extras);
  }

  editOfficial(id: number) {
    this.router.navigateByUrl(`${this.editUrl}/${id}`);
  }

  canAddOfficial(): boolean {
    return this.authenticationService.hasCurrentUserRole([UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_MATCH_OFFICIALS_MANAGERS]);
  }

  onDeleteOfficial(official: Official) {
    const config = {
      width: '450px', height: '200px', data: {
        message: `Are you sure you want to delete official '${official.firstName} ${official.lastName}'?`,
      }
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        this.dataSource.delete(official.id);
      }
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
