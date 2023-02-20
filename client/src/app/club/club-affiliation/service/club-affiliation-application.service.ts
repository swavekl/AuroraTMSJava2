import {Injectable} from '@angular/core';
import {EntityCache, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {ClubAffiliationApplication} from '../model/club-affiliation-application.model';

@Injectable({
  providedIn: 'root'
})
export class ClubAffiliationApplicationService extends EntityCollectionServiceBase<ClubAffiliationApplication> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('ClubAffiliationApplication', serviceElementsFactory);
  }
}

// crude selector for extracting the total entities satisfying query
export const selectClubAffiliationApplicationsTotal = (entityCache: EntityCache) => {
  const clubsEntityCollection = entityCache?.entityCache['ClubAffiliationApplication'];
  return (clubsEntityCollection) ? clubsEntityCollection['total'] as number : 0;
};

