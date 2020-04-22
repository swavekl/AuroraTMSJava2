import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {TournamentRoutingModule} from './tournament-routing.module';
import {TournamentListContainerComponent} from './tournament-list/tournament-list-container.component';
import {TournamentListComponent} from './tournament-list/tournament-list.component';
import {TournamentViewContainerComponent} from './tournament-view/tournament-view-container.component';
import {TournamentViewComponent} from './tournament-view/tournament-view.component';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatDividerModule} from '@angular/material/divider';
import {MatIconModule} from '@angular/material/icon';
import {MatCardModule} from '@angular/material/card';
import {MatListModule} from '@angular/material/list';


@NgModule({
  declarations: [
    TournamentListContainerComponent,
    TournamentListComponent,
    TournamentViewContainerComponent,
    TournamentViewComponent
  ],
  imports: [
    CommonModule,
    TournamentRoutingModule,
    MatProgressBarModule,
    MatDividerModule,
    MatIconModule,
    MatCardModule,
    MatListModule
  ]
})
export class TournamentModule {
}
