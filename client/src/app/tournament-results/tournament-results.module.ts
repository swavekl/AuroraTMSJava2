import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FlexModule} from '@angular/flex-layout';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatTabsModule} from '@angular/material/tabs';

import {TournamentResultsRoutingModule} from './tournament-results-routing.module';
import {TournamentResultsListComponent} from './tournament-results-list/tournament-results-list.component';
import {TournamentResultsListContainerComponent} from './tournament-results-list/tournament-results-list-container.component';
import {TournamentResultDetailsComponent} from './tournament-result-details/tournament-result-details.component';
import {TournamentResultDetailsContainerComponent} from './tournament-result-details/tournament-result-details-container.component';
import {DrawsModule} from '../draws/draws.module';


@NgModule({
  declarations: [
    TournamentResultsListComponent,
    TournamentResultsListContainerComponent,
    TournamentResultDetailsComponent,
    TournamentResultDetailsContainerComponent
  ],
    imports: [
        CommonModule,
        FlexModule,
        MatCardModule,
        MatButtonModule,
        TournamentResultsRoutingModule,
        DrawsModule,
        MatTabsModule
    ]
})
export class TournamentResultsModule { }
