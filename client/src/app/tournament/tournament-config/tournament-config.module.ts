import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCardModule} from '@angular/material/card';
import {MatListModule} from '@angular/material/list';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressBarModule} from '@angular/material/progress-bar';

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
    TournamentConfigRoutingModule,
    MatProgressBarModule,
    MatCardModule,
    MatListModule,
    MatIconModule,
    RouterModule,
    SharedModule
  ]
})
export class TournamentConfigModule {
}
