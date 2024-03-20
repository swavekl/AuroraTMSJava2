import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatListModule} from '@angular/material/list';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatSelectModule} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {FlexLayoutModule} from 'ng-flex-layout';

import {EntityDataService, EntityServices} from '@ngrx/data';
import {TableUsageRoutingModule} from './table-usage-routing.module';
import {TableUsageComponent} from './table-usage/table-usage.component';
import {TableUsageContainerComponent} from './table-usage/table-usage-container.component';
import {TableUsageDataService} from './service/table-usage-data.service';
import {TableUsageService} from './service/table-usage.service';
import {MatIconModule} from '@angular/material/icon';
import {SharedModule} from '../shared/shared.module';
import {MatchCardStatusPipe} from './pipes/match-card-status.pipe';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {MatchAssignmentDialogComponent} from './util/match-assignment-dialog.component';
import {MatDialogModule} from '@angular/material/dialog';


@NgModule({
  declarations: [
    TableUsageComponent,
    TableUsageContainerComponent,
    MatchCardStatusPipe,
    MatchAssignmentDialogComponent
  ],
  imports: [
    CommonModule,
    TableUsageRoutingModule,
    FlexLayoutModule,
    MatListModule,
    MatTooltipModule,
    MatToolbarModule,
    MatSelectModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    SharedModule,
    MatSlideToggleModule,
    DragDropModule,
    MatDialogModule
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
