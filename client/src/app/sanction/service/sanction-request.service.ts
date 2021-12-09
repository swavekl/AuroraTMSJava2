import {Injectable} from '@angular/core';
import {EntityCache, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {SanctionRequest} from '../model/sanction-request.model';

@Injectable({
  providedIn: 'root'
})
export class SanctionRequestService extends EntityCollectionServiceBase<SanctionRequest>{
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('SanctionRequest', serviceElementsFactory);
  }
}

// crude selector for extracting the total entities satisfying query
export const selectSanctionRequestsTotal = (entityCache: EntityCache) => {
  const entityCacheElement = entityCache?.entityCache['SanctionRequest'];
  return (entityCacheElement) ? entityCacheElement['total'] as number : 0;
};
