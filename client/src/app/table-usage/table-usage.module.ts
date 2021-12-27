import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatListModule} from '@angular/material/list';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatSelectModule} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {FlexModule} from '@angular/flex-layout';

import {EntityDataService, EntityServices} from '@ngrx/data';
import {TableUsageRoutingModule} from './table-usage-routing.module';
import {TableUsageComponent} from './table-usage/table-usage.component';
import {TableUsageContainerComponent} from './table-usage/table-usage-container.component';
import {TableUsageDataService} from './service/table-usage-data.service';
import {TableUsageService} from './service/table-usage.service';


@NgModule({
  declarations: [
    TableUsageComponent,
    TableUsageContainerComponent
  ],
  imports: [
    CommonModule,
    TableUsageRoutingModule,
    FlexModule,
    MatListModule,
    MatTooltipModule,
    MatToolbarModule,
    MatSelectModule,
    FormsModule,
    MatButtonModule
  ]
})
export class TableUsageModule {

  constructor(entityServices: EntityServices,
              entityDataService: EntityDataService,
              tableUsageService: TableUsageService,
              tableUsageDataService: TableUsageDataService) {
    // register service for contacting REST API because it doesn't follow the pattern of standard REST call
    entityDataService.registerService('TableUsage', tableUsageDataService);
    entityServices.registerEntityCollectionServices([tableUsageService]);
  }
}
