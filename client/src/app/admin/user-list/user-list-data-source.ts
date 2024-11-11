import {DataSource} from '@angular/cdk/collections';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {first, map} from 'rxjs/operators';
import {BehaviorSubject, merge, Observable, of, Subject} from 'rxjs';
import {ProfileListResponse, ProfileService} from '../../profile/profile.service';
import {Profile} from '../../profile/profile';

/**
 * Data source for the users list view.
 */
export class UsersListDataSource extends DataSource<Profile> {
  profiles$: Subject<Profile[]> = new Subject<Profile[]> ();
  total$: Subject<number> = new Subject<number>();
  profiles: Profile [];
  filterByName$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  paginator: MatPaginator | undefined;
  sort: MatSort | undefined;
  after: string;
  previousFilterValue: string;

  constructor(private profileService: ProfileService) {
    super();
    this.profiles = []
    this.total$.next(0);
    this.after = null;
    this.previousFilterValue = null;
  }

  /**
   * Connect this data source to the table. The table will only update when
   * the returned stream emits new items.
   * @returns A stream of the items to be rendered.
   */
  connect(): Observable<Profile[]> {
    if (this.paginator && this.sort) {
      // when results arrive or next page or sort order changes
      return merge(this.profiles$.asObservable(),
        this.paginator.page, this.sort.sortChange,
        this.filterByName$.asObservable())
        .pipe(
          map((value: any) => {
            if (Array.isArray(value)) {
              // results arrived - return them
              return value;
            } else {
              // next/previous page requested or sort change, or filter by last name request new page
              this.loadPage(value);
              // return current page for now
              return this.profiles;
            }
          })
        );
    } else {
      throw Error('Please set the paginator and sort on the data source before connecting.');
    }
  }

  /**
   *  Called when the table is being destroyed. Use this function, to clean up
   * any open connections or free any held resources that were set up during connect.
   */
  disconnect(): void {}

  /**
   * Loads the page worth of insurance requests. Optionally resets paging if filter is requested
   */
  public loadPage(value: any) {
    const filterValueChanged = typeof value === 'string' && value !== this.previousFilterValue;
    const paginatorEvent = (value?.pageIndex !== undefined);
    if (filterValueChanged) {
      this.paginator.pageIndex = 0;
      // this.after = null;
    }

    if (!paginatorEvent) {
      this.after = null;
    }

    const searchCriteria = [];
    // searchCriteria.push({name:'page', value: `${this.paginator.pageIndex}`});
    searchCriteria.push({name: 'limit', value: `${this.paginator.pageSize}`});
    // if (this.sort && this.sort?.active && this.sort.direction !== '') {
    //   searchCriteria.push({name:'sort', value: `${this.sort?.active},${this.sort?.direction}`});
    // }

    const filterValue = this.filterByName$.value;
    if (filterValue != null && filterValue !== '') {
      searchCriteria.push({name: 'lastName', value: filterValue});
      this.previousFilterValue = filterValue;
    }

    if (this.after != null) {
      searchCriteria.push({name: 'after', value: this.after})
    }
    // console.log('listing profiles search criteria', searchCriteria);
    this.profileService.listProfiles(searchCriteria)
      .pipe(first())
      .subscribe({
        next: (response: ProfileListResponse) => {
          this.profiles = response.profiles;
          this.after = response.after;
          this.profiles$.next(this.profiles);
          if (response.usersCount != null) {
            this.total$.next(response.usersCount)
          }
        },
        error: (error: any) => {
          console.error(error);
        }
      });
  }

  /**
   *
   * @param profileId
   */
  public delete(profileId: string) {
    // this.profileService.delete(profileId)
    //   .pipe(first())
    //   .subscribe(() => {
    //     this.loadPage(false);
    //   });
  }

}
