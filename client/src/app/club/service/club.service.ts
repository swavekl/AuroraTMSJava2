import {Injectable} from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {Club} from '../model/club.model';

@Injectable({
  providedIn: 'root'
})
export class ClubService extends EntityCollectionServiceBase<Club> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('Club', serviceElementsFactory);
  }
}
