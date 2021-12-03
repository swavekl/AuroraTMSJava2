import {DataSource} from '@angular/cdk/collections';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {first, map, tap} from 'rxjs/operators';
import {BehaviorSubject, merge, Observable} from 'rxjs';
import {InsuranceRequestService, selectInsuranceRequestsTotal} from '../service/insurance-request.service';
import {InsuranceRequest} from '../model/insurance-request.model';

/**
 * Data source for the InsuranceList view.
 */
export class InsuranceListDataSource extends DataSource<InsuranceRequest> {
  insuranceRequests$: Observable<InsuranceRequest[]>;
  totalInsuranceRequests$: Observable<number>;
  insuranceRequests: InsuranceRequest [];
  filterByName$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  paginator: MatPaginator | undefined;
  sort: MatSort | undefined;

  constructor(private insuranceRequestService: InsuranceRequestService) {
    super();
    this.insuranceRequests = [];
    // select insuranceRequest entities from cache
    this.insuranceRequests$ = this.insuranceRequestService.store.select(
      this.insuranceRequestService.selectors.selectEntities);
    this.totalInsuranceRequests$ = this.insuranceRequestService.store.select(
      selectInsuranceRequestsTotal);
  }

  /**
   * Connect this data source to the table. The table will only update when
   * the returned stream emits new items.
   * @returns A stream of the items to be rendered.
   */
  connect(): Observable<InsuranceRequest[]> {
    if (this.paginator && this.sort) {
      // load first page
      this.loadPage(false);
      // when results arrive or next page or sort order changes
      return merge(this.insuranceRequests$,
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
              return this.insuranceRequests;
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
    this.insuranceRequestService.clearCache();
    this.insuranceRequestService.getWithQuery(query)
      .pipe(tap((insuranceRequests: InsuranceRequest[]) => {
        this.insuranceRequests = insuranceRequests;
      }));
  }

  /**
   *
   * @param insuranceRequestId
   */
  public delete(insuranceRequestId: number) {
    this.insuranceRequestService.delete(insuranceRequestId)
      .pipe(first())
      .subscribe(() => {
        this.loadPage(false);
      });
  }

}
