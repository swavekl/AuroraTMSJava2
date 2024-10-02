import {DataSource} from '@angular/cdk/collections';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {first, map} from 'rxjs/operators';
import {Observable, of as observableOf, merge, of} from 'rxjs';
import {UmpiringService} from '../../service/umpiring.service';
import {UmpireWorkSummary} from '../../model/umpire-work-summary.model';

/**
 * Data source for the UmpireSummaryTable view. This class should
 * encapsulate all logic for fetching and manipulating the displayed data
 * (including sorting, pagination, and filtering).
 */
export class UmpireSummaryTableDataSource extends DataSource<UmpireWorkSummary> {
  data: UmpireWorkSummary[];
  data$: Observable<UmpireWorkSummary[]>;
  paginator: MatPaginator | undefined;
  sort: MatSort | undefined;

  private _tournamentId: number;

  constructor(private umpiringService: UmpiringService) {
    super();
    this.data = [];
  }


  set tournamentId(value: number) {
    this._tournamentId = value;
  }

  /**
   * Connect this data source to the table. The table will only update when
   * the returned stream emits new items.
   * @returns A stream of the items to be rendered.
   */
  connect(): Observable<UmpireWorkSummary[]> {
    if (this.paginator && this.sort) {
      if (this._tournamentId != undefined) {
        this.data$ = this.umpiringService.getSummaries(this._tournamentId);
        this.data$.pipe(
          first(),
          map((umpireWorkSummaries: UmpireWorkSummary[]) => {
            console.log(`got work summaries for tournament ${this._tournamentId}`, umpireWorkSummaries);
            this.data = umpireWorkSummaries;
            return of(this.data);
          })).subscribe();
      }

      // Combine everything that affects the rendered data into one update
      // stream for the data-table to consume.
      return merge(observableOf(this.data), this.paginator.page, this.sort.sortChange)
        .pipe(map((value: any, index: number) => {
          console.log('merging value', value);
          return this.getPagedData(this.getSortedData([...this.data]));
        }));
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
   * Paginate the data (client-side). If you're using server-side pagination,
   * this would be replaced by requesting the appropriate data from the server.
   */
  private getPagedData(data: UmpireWorkSummary[]): UmpireWorkSummary[] {
    if (this.paginator) {
      const startIndex = this.paginator.pageIndex * this.paginator.pageSize;
      return data.splice(startIndex, this.paginator.pageSize);
    } else {
      return data;
    }
  }

  /**
   * Sort the data (client-side). If you're using server-side sorting,
   * this would be replaced by requesting the appropriate data from the server.
   */
  private getSortedData(data: UmpireWorkSummary[]): UmpireWorkSummary[] {
    if (!this.sort || !this.sort.active || this.sort.direction === '') {
      return data;
    }

    return data.sort((a, b) => {
      const isAsc = this.sort?.direction === 'asc';
      switch (this.sort?.active) {
        case 'umpireName':
          return compare(a.umpireName, b.umpireName, isAsc);
        default:
          return 0;
      }
    });
  }
}

/** Simple sort comparator for example ID/Name columns (for client-side sorting). */
function compare(a: string | number, b: string | number, isAsc: boolean): number {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}
