import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {ClubListDataSource} from './club-list-datasource';
import {ClubService} from '../service/club.service';
import {Club} from '../model/club.model';
import {AuthenticationService} from '../../user/authentication.service';
import {UserRoles} from '../../user/user-roles.enum';
import {FormControl} from '@angular/forms';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';
import {ClubEditCallbackData, ClubEditPopupService} from '../service/club-edit-popup.service';

@Component({
  selector: 'app-club-list',
  templateUrl: './club-list.component.html',
  styleUrls: ['./club-list.component.scss']
})
export class ClubListComponent implements AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatTable) table!: MatTable<Club>;
  @ViewChild('filterClubNameCtrl') filterClubNameCtrl: FormControl;
  dataSource: ClubListDataSource;
  filterClubName: string;

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  displayedColumns = ['clubName', 'city', 'state'];

  constructor(private clubService: ClubService,
              private authenticationService: AuthenticationService,
              private clubEditPopupService: ClubEditPopupService) {
    this.dataSource = new ClubListDataSource(clubService);
    this.filterClubName = '';
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

  canAddClub() {
    return this.authenticationService.hasCurrentUserRole([UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_OFFICIALS]);
  }

  canEditClub(clubId: number) {
    return this.authenticationService.hasCurrentUserRole([UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_OFFICIALS]);
  }

  addClub() {
    const callbackParams: ClubEditCallbackData = {
      successCallbackFn: this.onAddClubOKCallback,
      cancelCallbackFn: null,
      callbackScope: this
    };
    const newClub: Club = new Club();
    this.clubEditPopupService.showPopup(newClub, callbackParams);
  }

  onAddClubOKCallback(scope: any, club: Club) {
    const me: ClubListComponent = scope;
    me.clubService.upsert(club);
  }
}
