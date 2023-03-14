import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FlexLayoutModule, FlexModule} from '@angular/flex-layout';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatTabsModule} from '@angular/material/tabs';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';

import {TournamentResultsRoutingModule} from './tournament-results-routing.module';
import {TournamentResultsListComponent} from './tournament-results-list/tournament-results-list.component';
import {TournamentResultsListContainerComponent} from './tournament-results-list/tournament-results-list-container.component';
import {TournamentResultDetailsComponent} from './tournament-result-details/tournament-result-details.component';
import {TournamentResultDetailsContainerComponent} from './tournament-result-details/tournament-result-details-container.component';
import {DrawsModule} from '../draws/draws.module';
import {PlayerResultsComponent} from './player-results/player-results.component';
import {PlayerResultsContainerComponent} from './player-results/player-results-container.component';
import {SharedModule} from '../shared/shared.module';


@NgModule({
  declarations: [
    TournamentResultsListComponent,
    TournamentResultsListContainerComponent,
    TournamentResultDetailsComponent,
    TournamentResultDetailsContainerComponent,
    PlayerResultsComponent,
    PlayerResultsContainerComponent
  ],
  imports: [
    CommonModule,
    FlexModule,
    MatCardModule,
    MatButtonModule,
    TournamentResultsRoutingModule,
    DrawsModule,
    MatTabsModule,
    SharedModule,
    FlexLayoutModule,
    MatToolbarModule,
    MatIconModule
  ]
})
export class TournamentResultsModule {
}
