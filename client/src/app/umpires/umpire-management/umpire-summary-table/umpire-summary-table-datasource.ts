import {DataSource} from '@angular/cdk/collections';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {first, map} from 'rxjs/operators';
import {Observable, of as observableOf, merge, of, Subject, BehaviorSubject} from 'rxjs';
import {UmpiringService} from '../../service/umpiring.service';
import {UmpireWorkSummary} from '../../model/umpire-work-summary.model';

/**
 * Data source for the UmpireSummaryTable view. This class should
 * encapsulate all logic for fetching and manipulating the displayed data
 * (including sorting, pagination, and filtering).
 */
export class UmpireSummaryTableDataSource extends DataSource<UmpireWorkSummary> {
  data: UmpireWorkSummary[];

  private tournamentIdSubject: BehaviorSubject<number> = new BehaviorSubject(0);

  paginator: MatPaginator | undefined;

  sort: MatSort | undefined;

  constructor(private umpiringService: UmpiringService) {
    super();
    this.data = [];
  }


  set tournamentId(value: number) {
    this.tournamentIdSubject.next(value);
  }

  /**
   * Connect this data source to the table. The table will only update when
   * the returned stream emits new items.
   * @returns A stream of the items to be rendered.
   */
  connect(): Observable<UmpireWorkSummary[]> {
    if (this.paginator && this.sort) {
      // Combine everything that affects the rendered data into one update
      // stream for the data-table to consume.
      return merge(observableOf(this.data), this.tournamentIdSubject.asObservable(),
        this.paginator.page, this.sort.sortChange)
        .pipe(map((value: any) => {
          // console.log('merging data', value);
          if (typeof value === 'number') {
            const tournamentId: number = value as number;
            this.loadSummaries(tournamentId);
            return this.data;
          } else {
            return [];
          }
        }));
    } else {
      throw Error('Please set the paginator and sort on the data source before connecting.');
    }
  }

  /**
   *
   */
  public loadSummaries(tournamentId: number) {
    if (tournamentId != undefined && tournamentId > 0) {
      this.umpiringService.getSummaries(tournamentId)
        .pipe(
          first(),
          map((umpireWorkSummaries: UmpireWorkSummary[]) => {
            // console.log('got umpire work summaries', umpireWorkSummaries);
            this.data = this.getPagedData(this.getSortedData([...umpireWorkSummaries]));
            return this.data;
          }))
        .subscribe({
          complete: () => {
            // tickle so merge fires
            this.tournamentIdSubject.next(0);
          }
        });
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
