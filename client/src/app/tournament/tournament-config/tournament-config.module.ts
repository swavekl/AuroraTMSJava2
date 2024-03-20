import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
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
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatDialogModule} from '@angular/material/dialog';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {FlexLayoutModule} from 'ng-flex-layout';

import {SharedModule} from '../../shared/shared.module';
import {TournamentConfigRoutingModule} from './tournament-config-routing.module';

import {TournamentConfigListContainerComponent} from './tournament-config-list/tournament-config-list-container.component';
import {TournamentConfigListComponent} from './tournament-config-list/tournament-config-list.component';
import {TournamentConfigEditContainerComponent} from './tournament-config-edit/tournament-config-edit-container.component';
import {TournamentConfigEditComponent} from './tournament-config-edit/tournament-config-edit.component';
import {TournamentEventConfigListComponent} from './tournament-event-config-list/tournament-event-config-list.component';
import {TournamentEventConfigListContainerComponent} from './tournament-event-config-list/tournament-event-config-list-container.component';
import {TournamentEventConfigComponent} from './tournament-event-config/tournament-event-config.component';
import {TournamentEventConfigContainerComponent} from './tournament-event-config/tournament-event-config-container.component';
import {SelectEventDialogComponent} from './select-event-dialog/select-event-dialog.component';
import {PrizeInfoDialogComponent} from './tournament-event-config/prize-info-dialog/prize-info-dialog.component';
import {PrizeInfoValidatorDirective} from './tournament-event-config/prize-info-dialog/prize-info-validator.directive';
import {TournamentWaitingListComponent} from './tournament-waiting-list/tournament-waiting-list.component';
import {TournamentWaitingListContainerComponent} from './tournament-waiting-list/tournament-waiting-list-container.component';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';

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
    SelectEventDialogComponent,
    PrizeInfoDialogComponent,
    PrizeInfoValidatorDirective,
    TournamentWaitingListComponent,
    TournamentWaitingListContainerComponent
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
        MatTooltipModule,
        MatButtonToggleModule,
        MatSlideToggleModule
    ],
  providers: []
})
export class TournamentConfigModule {
}
