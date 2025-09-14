import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {ClubListDataSource} from './club-list-datasource';
import {ClubService} from '../service/club.service';
import {Club} from '../model/club.model';
import {AuthenticationService} from '../../../user/authentication.service';
import {UserRoles} from '../../../user/user-roles.enum';
import {UntypedFormControl} from '@angular/forms';
import {debounceTime, distinctUntilChanged, first, skip} from 'rxjs/operators';
import {ClubEditCallbackData, ClubEditPopupService} from '../service/club-edit-popup.service';
import {StatesList} from '../../../shared/states/states-list';

@Component({
    selector: 'app-club-list',
    templateUrl: './club-list.component.html',
    styleUrls: ['./club-list.component.scss'],
    standalone: false
})
export class ClubListComponent implements AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatTable) table!: MatTable<Club>;
  @ViewChild('filterClubNameCtrl') filterClubNameCtrl: UntypedFormControl;
  @ViewChild('filterStateCtrl') filterStateCtrl: UntypedFormControl;
  dataSource: ClubListDataSource;
  filterClubName: string;
  filterState: string;

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  displayedColumns = ['clubName', 'city', 'state', 'actions'];

  statesList: any[] = [];

  constructor(private clubService: ClubService,
              private authenticationService: AuthenticationService,
              private clubEditPopupService: ClubEditPopupService) {
    this.dataSource = new ClubListDataSource(clubService);
    this.filterClubName = '';
    this.statesList = [{'': ''}];
    this.statesList = this.statesList.concat(StatesList.getCountryStatesList('US'));
    this.filterState = '';
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
    this.filterStateCtrl.valueChanges
      .pipe(
        skip(1),
        distinctUntilChanged(),
        debounceTime(500)
      )
      .subscribe((value) => {
      this.dataSource.filterByState$.next(value);
    });
    this.table.dataSource = this.dataSource;
  }

  canAddEditClub() {
    return this.authenticationService.hasCurrentUserRole(
      [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_CLUB_MANAGERS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    );
  }

  addClub() {
    const newClub: Club = new Club();
    this.onAddEditClub(newClub);
  }

  onEditClub(club: Club) {
    const clubToEdit: Club = {...club};
    this.onAddEditClub(clubToEdit);
  }

  private onAddEditClub(club: Club) {
    // show dialog
    const callbackParams: ClubEditCallbackData = {
      successCallbackFn: this.onAddClubOKCallback,
      cancelCallbackFn: null,
      callbackScope: this
    };
    this.clubEditPopupService.showPopup(club, callbackParams);
  }

  onAddClubOKCallback(scope: any, club: Club) {
    const me: ClubListComponent = scope;
    me.clubService.upsert(club)
      .pipe(first())
      .subscribe(() => {
        // refresh the page
      me.dataSource.loadPage(true);
    });
  }
}
