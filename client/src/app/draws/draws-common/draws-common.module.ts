import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {DragDropModule} from '@angular/cdk/drag-drop';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatListModule} from '@angular/material/list';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatTabsModule} from '@angular/material/tabs';

import {EntityDataService, EntityServices} from '@ngrx/data';

import {SharedModule} from '../../shared/shared.module';
import {DrawService} from './service/draw.service';
import {DrawDataService} from './service/draw-data.service';
import {NgttSingleEliminationTreeModule} from 'ng-tournament-tree';
import {SingleEliminationBracketComponent} from './single-elimination-bracket/single-elimination-bracket.component';
import {SEMatchComponent} from './single-elimination-bracket/sematch/sematch.component';
import {SingleEliminationBracketSmallComponent} from './single-elimination-bracket-small/single-elimination-bracket-small.component';
import {RoundRobinDrawsPanelComponent} from './round-robin-draws-panel/round-robin-draws-panel.component';
import {TabbedDrawsPanelComponent} from './tabbed-draws-panel/tabbed-draws-panel.component';

/**
 * Module created so we can share some components between screens implementing
 * creation vs viewing of draws
 */
@NgModule({
  declarations: [
    SingleEliminationBracketComponent,
    SEMatchComponent,
    SingleEliminationBracketSmallComponent,
    RoundRobinDrawsPanelComponent,
    TabbedDrawsPanelComponent
  ],
  exports: [
    SingleEliminationBracketComponent,
    SingleEliminationBracketSmallComponent,
    SEMatchComponent,
    RoundRobinDrawsPanelComponent,
    TabbedDrawsPanelComponent
  ],
  imports: [
    CommonModule,
    MatListModule,
    FlexLayoutModule,
    MatGridListModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    SharedModule,
    DragDropModule,
    MatTooltipModule,
    FormsModule,
    MatSlideToggleModule,
    NgttSingleEliminationTreeModule,
    MatTabsModule
  ]
})
export class DrawsCommonModule {
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
