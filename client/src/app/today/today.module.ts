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
import {FlexLayoutModule} from 'ng-flex-layout';
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
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import { PlayerStatusListComponent } from './player-status-list/player-status-list.component';
import { PlayerStatusListContainerComponent } from './player-status-list/player-status-list-container.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import { PlayerCheckinDialogComponent } from './player-checkin-dialog/player-checkin-dialog.component';
import {MatDialogModule} from '@angular/material/dialog';
import { PlayerStatusIndicatorComponent } from './player-status-indicator/player-status-indicator.component';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatProgressSpinner} from '@angular/material/progress-spinner';

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
        PlayerMatchesContainerComponent,
        PlayerStatusListComponent,
        PlayerStatusListContainerComponent,
        PlayerCheckinDialogComponent,
        PlayerStatusIndicatorComponent
    ],
    exports: [
        PlayerStatusIndicatorComponent
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
        MatCheckboxModule,
        MatSelectModule,
        MatSnackBarModule,
        MatTooltipModule,
        MatDialogModule,
        MatButtonToggleModule,
        MatProgressSpinner
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
