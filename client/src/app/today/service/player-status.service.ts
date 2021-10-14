import { Injectable } from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {PlayerStatus} from '../model/player-status.model';

/**
 * Service for retrieving player status for entire tournament, one tournament day or one event
 */
@Injectable({
  providedIn: 'root'
})
export class PlayerStatusService extends EntityCollectionServiceBase<PlayerStatus> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('PlayerStatus', serviceElementsFactory);
  }

}
