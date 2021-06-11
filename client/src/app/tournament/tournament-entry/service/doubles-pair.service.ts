import {Injectable} from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import { Observable } from 'rxjs/internal/Observable';
import {DoublesPair} from '../model/doubles-pair.model';

@Injectable({
  providedIn: 'root'
})
export class DoublesPairService extends EntityCollectionServiceBase<DoublesPair> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('DoublesPair', serviceElementsFactory);
  }

  public loadForEvent(doublesEventFk: number): Observable<DoublesPair[]> {
    super.clearCache();
    const queryParams = `eventId=${doublesEventFk}`;
    return super.getWithQuery(queryParams);
  }
}
