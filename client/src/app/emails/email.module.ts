import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {FlexLayoutModule} from 'ng-flex-layout';

import {SharedModule} from '../shared/shared.module';
import {EmailRoutingModule} from './email-routing.module';
import {EmailCampaignEditComponent} from './email/email-campaign-edit.component';
import {EmailCampaignEditContainerComponent} from './email/email-campaign-edit-container.component';
import {EmailServerConfigDialogComponent} from './email-server-config-dialog/email-server-config-dialog.component';
import {EmailCampaignListComponent} from './email-campaign-list/email-campaign-list.component';
import {MatTableModule} from '@angular/material/table';
import {MatSortModule} from '@angular/material/sort';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatIconModule} from '@angular/material/icon';
import {MatListModule} from '@angular/material/list';
import {EmailCampaignListContainerComponent} from './email-campaign-list/email-campaign-list-container.component';
import {MatTabsModule} from '@angular/material/tabs';
import {MatCheckbox} from '@angular/material/checkbox';


@NgModule({
  declarations: [
    EmailCampaignEditComponent,
    EmailCampaignEditContainerComponent,
    EmailServerConfigDialogComponent,
    EmailCampaignListComponent,
    EmailCampaignListContainerComponent
  ],
    imports: [
        CommonModule,
        EmailRoutingModule,
        MatToolbarModule,
        MatButtonModule,
        FlexLayoutModule,
        FormsModule,
        MatTooltipModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        SharedModule,
        MatProgressBarModule,
        MatTableModule,
        MatSortModule,
        MatPaginatorModule,
        MatIconModule,
        MatListModule,
        MatTabsModule,
        MatCheckbox
    ]
})
export class EmailModule {
}
