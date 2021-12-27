import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {DefaultDataService, DefaultDataServiceConfig, HttpUrlGenerator} from '@ngrx/data';
import {TableUsage} from '../model/table-usage.model';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TableUsageDataService extends DefaultDataService<TableUsage> {

  constructor(http: HttpClient,
              httpUrlGenerator: HttpUrlGenerator,
              config?: DefaultDataServiceConfig) {
    super('TableUsage', http, httpUrlGenerator, config);
  }

  public updateMany(tableUsages: TableUsage[]): Observable<TableUsage[]> {
    const url = `/api/tableusage`;
    return this.execute('PUT', url, tableUsages);
  }
}
