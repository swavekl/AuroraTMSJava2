import {Injectable} from '@angular/core';
import {EntityCache, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {Club} from '../model/club.model';

@Injectable({
  providedIn: 'root'
})
export class ClubService extends EntityCollectionServiceBase<Club> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('Club', serviceElementsFactory);
  }
}

// crude selector for extracting the total entities satisfying query
export const selectClubsTotal = (entityCache: EntityCache) => {
  const clubsEntityCollection = entityCache?.entityCache['Club'];
  return (clubsEntityCollection) ? clubsEntityCollection['total'] as number : 0;
};
