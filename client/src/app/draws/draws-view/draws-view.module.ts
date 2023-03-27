import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {MatListModule} from '@angular/material/list';
import {FlexModule} from '@angular/flex-layout';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTabsModule} from '@angular/material/tabs';

import {SharedModule} from '../../shared/shared.module';
import {DrawsCommonModule} from '../draws-common/draws-common.module';
import {DrawsViewRoutingModule} from './draws-view-routing.module';
import {DrawsViewEventsComponent} from './draws-view-events/draws-view-events.component';
import {DrawsViewEventsContainerComponent} from './draws-view-events/draws-view-events-container.component';
import {DrawsViewDetailComponent} from './draws-view-detail/draws-view-detail.component';
import {DrawsViewDetailContainerComponent} from './draws-view-detail/draws-view-detail-container.component';


@NgModule({
  declarations: [
    DrawsViewEventsComponent,
    DrawsViewEventsContainerComponent,
    DrawsViewDetailComponent,
    DrawsViewDetailContainerComponent
  ],
  imports: [
    CommonModule,
    DrawsCommonModule,
    DrawsViewRoutingModule,
    MatListModule,
    MatButtonModule,
    MatIconModule,
    FlexModule,
    SharedModule,
    MatTabsModule
  ]
})
export class DrawsViewModule {
}
