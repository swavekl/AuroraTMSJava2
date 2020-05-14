import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatCardModule} from '@angular/material/card';
import {MatListModule} from '@angular/material/list';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {MatInputModule} from '@angular/material/input';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatButtonModule} from '@angular/material/button';
import {MatTabsModule} from '@angular/material/tabs';
import {MatNativeDateModule} from '@angular/material/core';
import {MatToolbarModule} from '@angular/material/toolbar';

import {MatTableModule} from '@angular/material/table';
import {FlexLayoutModule} from '@angular/flex-layout';

import {RouterModule} from '@angular/router';
import {TournamentConfigRoutingModule} from './tournament-config-routing.module';
import {TournamentConfigListContainerComponent} from './tournament-config-list/tournament-config-list-container.component';
import {TournamentConfigListComponent} from './tournament-config-list/tournament-config-list.component';
import {TournamentConfigEditContainerComponent} from './tournament-config-edit/tournament-config-edit-container.component';

import {TournamentConfigEditComponent} from './tournament-config-edit/tournament-config-edit.component';

import {SharedModule} from '../../shared/shared.module';
import {EntityDataService, EntityServices} from '@ngrx/data';
import {TournamentConfigService} from './tournament-config.service';
import {TournamentEventConfigService} from './tournament-event-config.service';
import {TournamentEventConfigListComponent} from './tournament-event-config-list/tournament-event-config-list.component';
import {TournamentEventConfigListContainerComponent} from './tournament-event-config-list/tournament-event-config-list-container.component';
import {TournamentEventConfigDataService} from './tournament-event-config-data.service';
import {TournamentEventConfigComponent} from './tournament-event-config/tournament-event-config.component';
import {TournamentEventConfigContainerComponent} from './tournament-event-config/tournament-event-config-container.component';
import {MatCheckboxModule} from '@angular/material/checkbox';
import { SelectEventDialogComponent } from './select-event-dialog/select-event-dialog.component';
import {MatDialogModule} from '@angular/material/dialog';
import {MatTooltipModule} from '@angular/material/tooltip';

@NgModule({
  declarations: [
    TournamentConfigListContainerComponent,
    TournamentConfigListComponent,
    TournamentConfigEditContainerComponent,
    TournamentConfigEditComponent,
    TournamentEventConfigListComponent,
    TournamentEventConfigListContainerComponent,
    TournamentEventConfigComponent,
    TournamentEventConfigContainerComponent,
    SelectEventDialogComponent
  ],
    imports: [
        CommonModule,
        FormsModule,
        MatProgressBarModule,
        MatCardModule,
        MatListModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatButtonModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatTabsModule,
        MatTableModule,
        MatToolbarModule,
        FlexLayoutModule,
        RouterModule,
        SharedModule,
        TournamentConfigRoutingModule,
        MatCheckboxModule,
        MatDialogModule,
        MatTooltipModule
    ],
  providers: []
})
export class TournamentConfigModule {
  // Inject the service to ensure it registers with EntityServices
  constructor(
    entityServices: EntityServices,
    entityDataService: EntityDataService,
    // custom collection services
    tournamentConfigService: TournamentConfigService,
    tournamentEventConfigService: TournamentEventConfigService,
    tournamentEventConfigDataService: TournamentEventConfigDataService
  ) {
    // register service for contacting REST API because it doesn't follow the pattern of standard REST call
    entityDataService.registerService('TournamentEvent', tournamentEventConfigDataService);

    entityServices.registerEntityCollectionServices([
      tournamentConfigService,
      tournamentEventConfigService
    ]);
  }
}
