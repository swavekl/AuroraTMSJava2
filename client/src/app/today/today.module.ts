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
import {FormsModule} from '@angular/forms';
import {EntityDataService, EntityServices} from '@ngrx/data';
import {PlayerStatusService} from './service/player-status.service';
import {SharedModule} from '../shared/shared.module';
import {MatCardModule} from '@angular/material/card';
import { CheckinCommunicateContainerComponentComponent } from './checkincommunicate/checkin-communicate-container-component.component';


@NgModule({
  declarations: [
    TodayComponent,
    CheckinCommunicateComponent,
    CheckinCommunicateContainerComponentComponent
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
    MatCardModule
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
