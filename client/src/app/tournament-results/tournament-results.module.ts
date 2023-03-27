import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FlexLayoutModule, FlexModule} from '@angular/flex-layout';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatTabsModule} from '@angular/material/tabs';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';

import {SharedModule} from '../shared/shared.module';
import {DrawsCommonModule} from '../draws/draws-common/draws-common.module';
import {TournamentResultsRoutingModule} from './tournament-results-routing.module';
import {TournamentResultsListComponent} from './tournament-results-list/tournament-results-list.component';
import {TournamentResultsListContainerComponent} from './tournament-results-list/tournament-results-list-container.component';
import {TournamentResultDetailsComponent} from './tournament-result-details/tournament-result-details.component';
import {TournamentResultDetailsContainerComponent} from './tournament-result-details/tournament-result-details-container.component';
import {PlayerResultsComponent} from './player-results/player-results.component';
import {PlayerResultsContainerComponent} from './player-results/player-results-container.component';


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
    DrawsCommonModule,
    MatTabsModule,
    SharedModule,
    FlexLayoutModule,
    MatToolbarModule,
    MatIconModule
  ]
})
export class TournamentResultsModule {
}
