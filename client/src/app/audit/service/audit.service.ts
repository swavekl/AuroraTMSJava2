import {Injectable} from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {Audit} from '../model/audit.model';

@Injectable({
  providedIn: 'root'
})
export class AuditService extends EntityCollectionServiceBase<Audit> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('Audit', serviceElementsFactory);
  }
}
