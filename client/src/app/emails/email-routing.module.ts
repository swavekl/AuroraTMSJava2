import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {EmailCampaignListContainerComponent} from './email-campaign-list/email-campaign-list-container.component';
import {EmailCampaignEditContainerComponent} from './email/email-campaign-edit-container.component';

const routes: Routes = [
  { path: 'emailcampaign/list/:tournamentId/:tournamentName', component: EmailCampaignListContainerComponent },
  { path: 'emailcampaign/edit/:tournamentId/:emailCampaignId', component: EmailCampaignEditContainerComponent },
  { path: 'emailcampaign/create/:tournamentId/:emailCampaignId', component: EmailCampaignEditContainerComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EmailRoutingModule { }
