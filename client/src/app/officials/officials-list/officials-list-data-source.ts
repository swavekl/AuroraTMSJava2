import {DataSource} from '@angular/cdk/collections';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {first, map, tap} from 'rxjs/operators';
import {BehaviorSubject, merge, Observable} from 'rxjs';
import {OfficialService, selectOfficialsTotal} from '../service/official.service';
import {Official} from '../model/official.model';

/**
 * Data source for the InsuranceList view.
 */
export class OfficialsListDataSource extends DataSource<Official> {
  officials$: Observable<Official[]>;
  total$: Observable<number>;
  officials: Official [];
  filterByName$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  paginator: MatPaginator | undefined;
  sort: MatSort | undefined;

  constructor(private officialService: OfficialService) {
    super();
    this.officials = [];
    // select Official entities from cache
    this.officials$ = this.officialService.store.select(
      this.officialService.selectors.selectEntities);
    this.total$ = this.officialService.store.select(
      selectOfficialsTotal);
  }

  /**
   * Connect this data source to the table. The table will only update when
   * the returned stream emits new items.
   * @returns A stream of the items to be rendered.
   */
  connect(): Observable<Official[]> {
    if (this.paginator && this.sort) {
      // load first page
      this.loadPage(false);
      // when results arrive or next page or sort order changes
      return merge(this.officials$,
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
              return this.officials;
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
    this.officialService.clearCache();
    this.officialService.getWithQuery(query)
      .pipe(tap((Officials: Official[]) => {
        this.officials = Officials;
      }));
  }

  /**
   *
   * @param OfficialId
   */
  public delete(OfficialId: number) {
    this.officialService.delete(OfficialId)
      .pipe(first())
      .subscribe(() => {
        this.loadPage(false);
      });
  }

}
