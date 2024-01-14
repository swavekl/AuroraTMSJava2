import { Injectable } from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {EmailServerConfiguration} from '../model/email-server-configuration.model';

@Injectable({
  providedIn: 'root'
})
export class EmailServerConfigurationService extends EntityCollectionServiceBase<EmailServerConfiguration> {

  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('EmailServerConfiguration', serviceElementsFactory);
  }
}
