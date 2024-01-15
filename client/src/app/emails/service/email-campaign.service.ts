import {Injectable} from '@angular/core';
import {EntityCache, EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {EmailCampaign} from '../model/email-campaign.model';

@Injectable({
  providedIn: 'root'
})
export class EmailCampaignService extends EntityCollectionServiceBase<EmailCampaign> {

  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('EmailCampaign', serviceElementsFactory);
  }
}

// crude selector for extracting the total entities satisfying query
export const selectEmailCampaignsTotal = (entityCache: EntityCache) => {
  const entityCacheElement = entityCache?.entityCache['EmailCampaign'];
  return (entityCacheElement) ? entityCacheElement['total'] as number : 0;
};
