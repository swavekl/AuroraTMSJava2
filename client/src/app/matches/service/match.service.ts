import {Injectable} from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {Match} from '../model/match.model';

@Injectable({
  providedIn: 'root'
})
export class MatchService extends EntityCollectionServiceBase<Match> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('Match', serviceElementsFactory);
  }
}
