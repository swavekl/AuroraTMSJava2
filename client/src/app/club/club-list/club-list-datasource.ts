import {DataSource} from '@angular/cdk/collections';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {map, tap} from 'rxjs/operators';
import {BehaviorSubject, merge, Observable, of} from 'rxjs';
import {Club} from '../model/club.model';
import {ClubService} from '../service/club.service';

/**
 * Data source for the ClubList view. This class should
 * encapsulate all logic for fetching and manipulating the displayed data
 * (including sorting, pagination, and filtering).
 */
export class ClubListDataSource extends DataSource<Club> {
  clubs$: Observable<Club[]>;
  clubs: Club [];
  paginator: MatPaginator | undefined;
  sort: MatSort | undefined;
  filterByName$: BehaviorSubject<string> = new BehaviorSubject<string>('');

  constructor(private clubService: ClubService) {
    super();
    this.clubs = [];
  }

  /**
   * Connect this data source to the table. The table will only update when
   * the returned stream emits new items.
   * @returns A stream of the items to be rendered.
   */
  connect(): Observable<Club[]> {
    if (this.paginator && this.sort) {
      // select club entities from cache
      this.clubs$ = this.clubService.store.select(this.clubService.selectors.selectEntities);
      // load first page
      this.loadPage();
      // when results arrive or next page or sort order changes
      return merge(this.clubs$, this.paginator.page, this.sort.sortChange, this.filterByName$.asObservable())
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
              return this.clubs;
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
  private loadPage() {
      let query = `page=${this.paginator.pageIndex}&size=${this.paginator.pageSize}`;
      if (this.sort && this.sort?.active && this.sort.direction !== '') {
        query += `&sort=${this.sort?.active},${this.sort?.direction}`;
      }
      const filterValue = this.filterByName$.value;
      if (filterValue !== '') {
        query += `&nameContains=${filterValue}`;
      }
      this.clubService.clearCache();
      this.clubService.getWithQuery(query)
        .pipe(tap((clubs: Club[]) => {
          this.clubs = clubs;
        }));
  }

  getCount() {
    return 70;
  }
}
