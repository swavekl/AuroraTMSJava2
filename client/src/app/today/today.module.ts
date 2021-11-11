import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {TodayRoutingModule} from './today-routing.module';
import {TodayComponent} from './today/today.component';
import {CheckinCommunicateComponent} from './checkincommunicate/checkin-communicate.component';
import {MatRadioModule} from '@angular/material/radio';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatListModule} from '@angular/material/list';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatInputModule} from '@angular/material/input';
import {MatCardModule} from '@angular/material/card';
import {FormsModule} from '@angular/forms';
import {EntityDataService, EntityServices} from '@ngrx/data';
import {PlayerStatusService} from './service/player-status.service';
import {SharedModule} from '../shared/shared.module';
import {CheckinCommunicateContainerComponent} from './checkincommunicate/checkin-communicate-container.component';
import {PlayerScheduleContainerComponent} from './playerschedule/player-schedule-container.component';
import {PlayerScheduleComponent} from './playerschedule/player-schedule.component';
import {PlayerStatusPipe} from './pipe/player-status.pipe';
import {PlayerScheduleDetailComponent} from './player-schedule-detail/player-schedule-detail.component';
import {PlayerScheduleDetailContainerComponent} from './player-schedule-detail/player-schedule-detail-container.component';
import {PlayerMatchesComponent} from '../matches/player-matches/player-matches.component';
import {PlayerMatchesContainerComponent} from '../matches/player-matches/player-matches-container.component';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatCheckboxModule} from '@angular/material/checkbox';


@NgModule({
  declarations: [
    TodayComponent,
    CheckinCommunicateComponent,
    CheckinCommunicateContainerComponent,
    PlayerScheduleContainerComponent,
    PlayerScheduleComponent,
    PlayerStatusPipe,
    PlayerScheduleDetailComponent,
    PlayerScheduleDetailContainerComponent,
    PlayerMatchesComponent,
    PlayerMatchesContainerComponent
  ],
    imports: [
        CommonModule,
        TodayRoutingModule,
        MatRadioModule,
        MatFormFieldModule,
        MatIconModule,
        MatButtonModule,
        MatListModule,
        FlexLayoutModule,
        MatInputModule,
        FormsModule,
        SharedModule,
        MatCardModule,
        MatExpansionModule,
        MatToolbarModule,
        MatCheckboxModule
    ]
})
export class TodayModule {  // Inject the service to ensure it registers with EntityServices
  constructor(
    entityServices: EntityServices,
    entityDataService: EntityDataService,
    playerStatusService: PlayerStatusService
  ) {

    entityServices.registerEntityCollectionServices([
      playerStatusService
    ]);
  }
}
