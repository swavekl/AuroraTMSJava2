import {Injectable} from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {Observable} from 'rxjs';
import {TableUsage} from '../model/table-usage.model';
import {TableUsageDataService} from './table-usage-data.service';

@Injectable({
  providedIn: 'root'
})
export class TableUsageService extends EntityCollectionServiceBase<TableUsage> {

  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory,
              private tableUsageDataService: TableUsageDataService) {
    super('TableUsage', serviceElementsFactory);
  }

  public updateMany(tableUsages: TableUsage[]): Observable<TableUsage[]> {
    return this.tableUsageDataService.updateMany(tableUsages);
  }
}
