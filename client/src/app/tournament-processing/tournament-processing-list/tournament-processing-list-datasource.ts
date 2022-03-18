import {DataSource} from '@angular/cdk/collections';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {map, tap} from 'rxjs/operators';
import {BehaviorSubject, merge, Observable} from 'rxjs';
import {TournamentProcessingRequest} from '../model/tournament-processing-request';
import {selectTournamentProcessingDataTotal, TournamentProcessingService} from '../service/tournament-processing.service';

/**
 * Data source for the TournamentProcessingList view. This class should
 * encapsulate all logic for fetching and manipulating the displayed data
 * (including sorting, pagination, and filtering).
 */
export class TournamentProcessingListDataSource extends DataSource<TournamentProcessingRequest> {
  tournamentProcessingRequests$: Observable<TournamentProcessingRequest[]>;
  totalTournamentProcessingData$: Observable<number>;
  tournamentProcessingRequestList: TournamentProcessingRequest [];
  filterByName$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  paginator: MatPaginator | undefined;
  sort: MatSort | undefined;

  constructor(private tournamentProcessingService: TournamentProcessingService) {
    super();
    this.tournamentProcessingRequestList = [];
    // select tournamentProcessingRequest entities from cache
    this.tournamentProcessingRequests$ = this.tournamentProcessingService.store.select(
      this.tournamentProcessingService.selectors.selectEntities);
    this.totalTournamentProcessingData$ = this.tournamentProcessingService.store.select(
      selectTournamentProcessingDataTotal);
  }

  /**
   * Connect this data source to the table. The table will only update when
   * the returned stream emits new items.
   * @returns A stream of the items to be rendered.
   */
  connect(): Observable<TournamentProcessingRequest[]> {
    if (this.paginator && this.sort) {
      // load first page
      this.loadPage(false);
      // when results arrive or next page or sort order changes
      return merge(this.tournamentProcessingRequests$,
        this.paginator.page, this.sort.sortChange, this.filterByName$.asObservable())
        .pipe(
          map((value: any, index: number) => {
            if (Array.isArray(value)) {
              // results arrived - return them
              return value;
            } else {
              const resetToPageOne = typeof value === 'string';
              // next/previous page requested or sort change, request new page
              this.loadPage(resetToPageOne);
              // return current page for now
              return this.tournamentProcessingRequestList;
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
  disconnect(): void {
  }

  /**
   * Loads the page worth of insurance requests. Optionally resets paging if filter is requested
   */
  public loadPage(resetToPageOne: boolean) {
    if (resetToPageOne) {
      this.paginator.pageIndex = 0;
    }
    let query = `page=${this.paginator.pageIndex}&size=${this.paginator.pageSize}`;
    if (this.sort && this.sort?.active && this.sort.direction !== '') {
      query += `&sort=${this.sort?.active},${this.sort?.direction}`;
    }
    const filterValue = this.filterByName$.value;
    if (filterValue !== '') {
      query += `&nameContains=${filterValue}`;
    }
    this.tournamentProcessingService.clearCache();
    this.tournamentProcessingService.getWithQuery(query)
      .pipe(tap((tournamentProcessingRequests: TournamentProcessingRequest[]) => {
        this.tournamentProcessingRequestList = tournamentProcessingRequests;
      }));
  }
}
