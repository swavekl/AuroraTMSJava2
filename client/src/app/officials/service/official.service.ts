import {Injectable} from '@angular/core';
import {EntityCache, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {Official} from '../model/official.model';

@Injectable({
  providedIn: 'root'
})
export class OfficialService extends EntityCollectionServiceBase<Official> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('Official', serviceElementsFactory);
  }
}

// crude selector for extracting the total entities satisfying query
export const selectOfficialsTotal = (entityCache: EntityCache) => {
  const entityCacheElement = entityCache?.entityCache['Official'];
  return (entityCacheElement) ? entityCacheElement['total'] as number : 0;
};
