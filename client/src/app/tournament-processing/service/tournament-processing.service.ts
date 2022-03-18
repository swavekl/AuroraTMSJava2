import { Injectable } from '@angular/core';
import {EntityCache, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {TournamentProcessingRequest} from '../model/tournament-processing-request';

@Injectable({
  providedIn: 'root'
})
export class TournamentProcessingService extends EntityCollectionServiceBase<TournamentProcessingRequest> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('TournamentProcessingRequest', serviceElementsFactory);
  }
}

// crude selector for extracting the total entities satisfying query
export const selectTournamentProcessingDataTotal = (entityCache: EntityCache) => {
  const entityCacheElement = entityCache?.entityCache['TournamentProcessingRequest'];
  return (entityCacheElement) ? entityCacheElement['total'] as number : 0;
};

