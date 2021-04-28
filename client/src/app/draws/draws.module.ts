import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {DrawsRoutingModule} from './draws-routing.module';
import {DrawsComponent} from './draws/draws.component';
import {DrawsContainerComponent} from './draws/draws-container.component';
import {MatListModule} from '@angular/material/list';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {EntityDataService, EntityServices} from '@ngrx/data';
import {DrawService} from './service/draw.service';
import {DrawDataService} from './service/draw-data.service';
import {SharedModule} from '../shared/shared.module';


@NgModule({
  declarations: [
    DrawsComponent,
    DrawsContainerComponent
  ],
  imports: [
    CommonModule,
    DrawsRoutingModule,
    MatListModule,
    FlexLayoutModule,
    MatGridListModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    SharedModule
  ]
})
export class DrawsModule {
  // Inject the service to ensure it registers with EntityServices
  constructor(
    entityServices: EntityServices,
    entityDataService: EntityDataService,
    drawService: DrawService,
    drawDataService: DrawDataService
  ) {

    // register service for contacting REST API because it doesn't follow the pattern of standard REST call
    entityDataService.registerService('DrawItem', drawDataService);

    entityServices.registerEntityCollectionServices([
      drawService
    ]);
  }
}
