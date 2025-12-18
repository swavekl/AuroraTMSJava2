import { Injectable } from '@angular/core';
import {EntityCache, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {Team} from '../model/team.model';

@Injectable({
  providedIn: 'root'
})
export class TeamService extends EntityCollectionServiceBase<Team>{

  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('Team', serviceElementsFactory);
  }
}

// crude selector for extracting the total entities satisfying query
export const selectTeamsTotal = (entityCache: EntityCache) => {
  const teamsEntityCollection = entityCache?.entityCache['Team'];
  return (teamsEntityCollection) ? teamsEntityCollection['total'] as number : 0;
};
