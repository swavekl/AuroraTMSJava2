import { Injectable } from '@angular/core';

import {
  EntityCollectionServiceBase,
  EntityCollectionServiceElementsFactory
} from 'ngrx-data';
import { Tournament } from './tournament.model';

@Injectable({ providedIn: 'root' })
export class TournamentConfigService extends EntityCollectionServiceBase<Tournament> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('Tournament', serviceElementsFactory);
  }
}
