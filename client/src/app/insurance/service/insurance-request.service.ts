import { Injectable } from '@angular/core';
import {EntityCache, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {InsuranceRequest} from '../model/insurance-request.model';

@Injectable({
  providedIn: 'root'
})
export class InsuranceRequestService extends EntityCollectionServiceBase<InsuranceRequest> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('InsuranceRequest', serviceElementsFactory);
  }
}

// crude selector for extracting the total entities satisfying query
export const selectInsuranceRequestsTotal = (entityCache: EntityCache) => {
  const entityCacheElement = entityCache?.entityCache['InsuranceRequest'];
  return (entityCacheElement) ? entityCacheElement['total'] as number : 0;
};

