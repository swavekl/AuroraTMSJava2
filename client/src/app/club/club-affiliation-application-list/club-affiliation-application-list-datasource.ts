import {DataSource} from '@angular/cdk/collections';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {first, map, tap} from 'rxjs/operators';
import {BehaviorSubject, merge, Observable} from 'rxjs';
import {ClubAffiliationApplication} from '../model/club-affiliation-application.model';
import {ClubAffiliationApplicationService, selectClubAffiliationApplicationsTotal} from '../service/club-affiliation-application.service';

/**
 * Data source for the ClubAffiliationApplicationList view. This class should
 * encapsulate all logic for fetching and manipulating the displayed data
 * (including sorting, pagination, and filtering).
 */
export class ClubAffiliationApplicationListDataSource extends DataSource<ClubAffiliationApplication> {
  clubAffiliationApplications$: Observable<ClubAffiliationApplication[]>;
  totalApplications$: Observable<number>;
  clubAffiliationApplications: ClubAffiliationApplication [];
  filterByName$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  paginator: MatPaginator | undefined;
  sort: MatSort | undefined;

  constructor(private clubAffiliationApplicationService: ClubAffiliationApplicationService) {
    super();
    this.clubAffiliationApplications = [];
    // select ClubAffiliationApplication entities from cache
    this.clubAffiliationApplications$ = this.clubAffiliationApplicationService.store.select(
      this.clubAffiliationApplicationService.selectors.selectEntities);
    this.totalApplications$ = this.clubAffiliationApplicationService.store.select(
      selectClubAffiliationApplicationsTotal);
  }

  /**
   * Connect this data source to the table. The table will only update when
   * the returned stream emits new items.
   * @returns A stream of the items to be rendered.
   */
  connect(): Observable<ClubAffiliationApplication[]> {
    if (this.paginator && this.sort) {
      // load first page
      this.loadPage();
      // when results arrive or next page or sort order changes
      return merge(this.clubAffiliationApplications$,
        this.paginator.page, this.sort.sortChange, this.filterByName$.asObservable())
        .pipe(
          map((value: any, index: number) => {
            // console.log('value', value);
            if (Array.isArray(value)) {
              // results arrived - return them
              return value;
            } else {
              // next/previous page requested or sort change, request new page
              this.loadPage();
              // return current page for now
              return this.clubAffiliationApplications;
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
   * Paginate the data (client-side). If you're using server-side pagination,
   * this would be replaced by requesting the appropriate data from the server.
   */
  public loadPage() {
    let query = `page=${this.paginator.pageIndex}&size=${this.paginator.pageSize}`;
    if (this.sort && this.sort?.active && this.sort.direction !== '') {
      query += `&sort=${this.sort?.active},${this.sort?.direction}`;
    }
    const filterValue = this.filterByName$.value;
    if (filterValue !== '') {
      query += `&nameContains=${filterValue}`;
    }
    this.clubAffiliationApplicationService.clearCache();
    this.clubAffiliationApplicationService.getWithQuery(query)
      .pipe(tap((clubAffiliationApplications: ClubAffiliationApplication[]) => {
        this.clubAffiliationApplications = clubAffiliationApplications;
      }));
  }

  /**
   *
   * @param applicationId
   */
  public deleteApplication(applicationId: number) {
    this.clubAffiliationApplicationService.delete(applicationId)
      .pipe(first())
      .subscribe(() => {
      this.loadPage();
    });
  }
}
