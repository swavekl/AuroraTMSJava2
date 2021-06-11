import {Injectable} from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {Observable} from 'rxjs/internal/Observable';
import {DoublesPairInfo} from '../model/doubles-pair-info.model';

@Injectable({
  providedIn: 'root'
})
export class DoublesPairInfoService extends EntityCollectionServiceBase<DoublesPairInfo> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('DoublesPairInfo', serviceElementsFactory);
  }

  public loadForEvent(doublesEventFk: number): Observable<DoublesPairInfo[]> {
    console.log(`loading double event entries for event ${doublesEventFk}`);
    const queryParams = `eventId=${doublesEventFk}`;
    super.clearCache();
    return super.getWithQuery(queryParams);
  }
}
