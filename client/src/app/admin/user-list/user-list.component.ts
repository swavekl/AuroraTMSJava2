import {Component, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {UntypedFormControl} from '@angular/forms';
import {of, Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {Router} from '@angular/router';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {debounceTime, distinctUntilChanged, first, skip} from 'rxjs/operators';
import {ProfileService} from '../../profile/profile.service';
import {UsersListDataSource} from './user-list-data-source';
import {Profile} from '../../profile/profile';
import {RolesDialogComponent} from '../groups-dialog/roles-dialog.component';
import {OktaUserStatusPipe} from '../okta-user-status.pipe';

@Component({
    selector: 'app-user-list',
    templateUrl: './user-list.component.html',
    styleUrl: './user-list.component.scss',
    standalone: false
})
export class UserListComponent {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatTable) table!: MatTable<Profile>;
  @ViewChild('filterNameCtrl') filterNameCtrl: UntypedFormControl;
  @ViewChild('filterByStatusCtrl') filterByStatusCtrl: UntypedFormControl;
  dataSource: UsersListDataSource;
  filterName: string;
  filterByStatus: any;

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  displayColumns = ['firstName', 'lastName', 'mobilePhone', 'state', 'status', 'actions'];

  public readonly editUrl = "/ui/userprofile/edit";
  public readonly returnUrl = "/ui/admin/userlist";

  private subscriptions: Subscription = new Subscription();

  constructor(private profileService: ProfileService,
              private linearProgressBarService: LinearProgressBarService,
              private router: Router,
              private dialog: MatDialog) {
    this.dataSource = new UsersListDataSource(profileService);
    this.setupProgressIndicator();
  }

  private setupProgressIndicator() {
    const loadingSubscription = this.profileService.loading$.subscribe((loading: boolean) => {
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
        console.log('filter by name changes', value);
        this.dataSource.filterByName$.next(value);
      });
    this.filterByStatusCtrl.valueChanges
      .pipe(
        skip(1),
        distinctUntilChanged(),
        debounceTime(500)
      )
      .subscribe((value) => {
        console.log("changing status filter to " + value);
        this.dataSource.filterByStatus$.next(value);
      });
  }

  back() {
    this.router.navigateByUrl("/ui/admin")
  }

  onViewGroups(userProfile: Profile) {
    this.profileService.getUserRoles(userProfile.userId).subscribe({
      next:
        (roles: string[]) => {
          const config: MatDialogConfig = {
            width: '700px', height: '460px', data: {roles: roles, profile: userProfile}
          };
          const dialogRef = this.dialog.open(RolesDialogComponent, config);
          dialogRef.afterClosed().subscribe(result => {
            if (result.action === 'ok') {
              this.profileService.updateUserRoles(userProfile.userId, result.roles).subscribe({
              });
            }
          });
        }
    });
  }

  clearFilter() {
    this.filterNameCtrl.reset();
  }

  isUserLockedOut(profile: Profile) : boolean {
    return profile?.userStatus === 'LOCKED_OUT';
  }

  onUnlockUser(profile: Profile) {
    this.profileService.unlockProfile(profile)
      .pipe(first())
      .subscribe({
          next: (updatedProfile: Profile) => {
            // refresh the page
            this.dataSource.loadPage(null);
          },
          error: (error: any) => {
            try {
              const msg = JSON.parse(error.error.text);
              console.error(msg);
            } catch (e) {
            }
          }
        }
      );
  }

  isUserSuspended (profile: Profile) {
    return profile?.userStatus === 'SUSPENDED';
  }

  onActivateUser(profile: Profile) {
    this.profileService.activateProfile(profile)
      .pipe(first())
      .subscribe({
          next: (updatedProfile: Profile) => {
            // refresh the page
            this.dataSource.loadPage(null);
          },
          error: (error: any) => {
            try {
              const msg = JSON.parse(error.error.text);
              console.error(msg);
            } catch (e) {
            }
          }
        }
      );
  }

  getStatusList(): string [] {
    return OktaUserStatusPipe.getAllStatus();
  }
}
