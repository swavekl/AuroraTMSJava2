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
import {MatNativeDateModule} from '@angular/material/core';

import {FlexLayoutModule} from '@angular/flex-layout';

import {RouterModule} from '@angular/router';
import {TournamentConfigRoutingModule} from './tournament-config-routing.module';

import {TournamentConfigListContainerComponent} from './tournament-config-list/tournament-config-list-container.component';
import {TournamentConfigListComponent} from './tournament-config-list/tournament-config-list.component';
import {TournamentConfigEditContainerComponent} from './tournament-config-edit/tournament-config-edit-container.component';
import {TournamentConfigEditComponent} from './tournament-config-edit/tournament-config-edit.component';
import {SharedModule} from '../../shared/shared.module';

@NgModule({
  declarations: [
    TournamentConfigListContainerComponent,
    TournamentConfigListComponent,
    TournamentConfigEditContainerComponent,
    TournamentConfigEditComponent
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
    FlexLayoutModule,
    RouterModule,
    SharedModule,
    TournamentConfigRoutingModule
  ]
})
export class TournamentConfigModule {
}
