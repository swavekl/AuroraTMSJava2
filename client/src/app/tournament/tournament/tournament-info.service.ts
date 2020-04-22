import { Injectable } from '@angular/core';

import {
  EntityCollectionServiceBase,
  EntityCollectionServiceElementsFactory
} from 'ngrx-data';
import { TournamentInfo } from './tournament-info.model';

@Injectable({ providedIn: 'root' })
export class TournamentInfoService extends EntityCollectionServiceBase<TournamentInfo> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('TournamentInfo', serviceElementsFactory);
  }
}

