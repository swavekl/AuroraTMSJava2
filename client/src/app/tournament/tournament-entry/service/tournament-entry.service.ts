import {Injectable} from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {TournamentEntry} from '../model/tournament-entry.model';

@Injectable({
  providedIn: 'root'
})
export class TournamentEntryService extends EntityCollectionServiceBase<TournamentEntry> {

  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('TournamentEntry', serviceElementsFactory);
  }
}
