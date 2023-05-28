import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {UntypedFormControl} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {Router} from '@angular/router';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';
import {Official} from '../model/official.model';
import {OfficialsListDataSource} from './officials-list-data-source';
import {OfficialService} from '../service/official.service';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {UserRoles} from '../../user/user-roles.enum';
import {AuthenticationService} from '../../user/authentication.service';

@Component({
  selector: 'app-officials-list',
  templateUrl: './officials-list.component.html',
  styleUrls: ['./officials-list.component.scss']
})
export class OfficialsListComponent implements AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatTable) table!: MatTable<Official>;
  @ViewChild('filterNameCtrl') filterNameCtrl: UntypedFormControl;
  dataSource: OfficialsListDataSource;
  filterName: string;

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  displayColumns = ['firstName', 'lastName', 'state', 'umpireRank', 'refereeRank', 'actions'];

  editUrl: string = '/ui/officials/edit';
  createUrl: string = '/ui/userprofile/addbytd/0';

  constructor(private OfficialService: OfficialService,
              private authenticationService: AuthenticationService,
              private router: Router,
              private dialog: MatDialog) {
    this.dataSource = new OfficialsListDataSource(OfficialService);
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
  }

  newOfficial() {
    this.router.navigateByUrl(`${this.editUrl}/0`);
  }

  editOfficial(id: number) {
    this.router.navigateByUrl(`${this.editUrl}/${id}`);
  }

  canAddOfficial(): boolean {
    return this.authenticationService.hasCurrentUserRole([UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_MATCH_OFFICIALS_MANAGERS]);
  }

  onDeleteOfficial(official: Official) {
    const config = {
      width: '450px', height: '230px', data: {
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
}
