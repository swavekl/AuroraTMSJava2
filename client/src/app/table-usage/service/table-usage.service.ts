import { Injectable } from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {TableUsage} from '../model/table-usage.model';

@Injectable({
  providedIn: 'root'
})
export class TableUsageService extends EntityCollectionServiceBase<TableUsage> {

  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('TableUsage', serviceElementsFactory);
  }
}
