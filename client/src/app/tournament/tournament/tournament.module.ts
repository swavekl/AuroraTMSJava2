import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatDividerModule} from '@angular/material/divider';
import {MatIconModule} from '@angular/material/icon';
import {MatCardModule} from '@angular/material/card';
import {MatListModule} from '@angular/material/list';
import {SharedModule} from '../../shared/shared.module';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonModule} from '@angular/material/button';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatMenuModule} from '@angular/material/menu';

import {TournamentRoutingModule} from './tournament-routing.module';
import {TournamentListContainerComponent} from './tournament-list/tournament-list-container.component';
import {TournamentListComponent} from './tournament-list/tournament-list.component';
import {TournamentViewContainerComponent} from './tournament-view/tournament-view-container.component';
import {TournamentViewComponent} from './tournament-view/tournament-view.component';
import { TournamentPlayersListComponent } from './tournament-players-list/tournament-players-list.component';
import { TournamentPlayersListContainerComponent } from './tournament-players-list/tournament-players-list-container.component';


@NgModule({
  declarations: [
    TournamentListContainerComponent,
    TournamentListComponent,
    TournamentViewContainerComponent,
    TournamentViewComponent,
    TournamentPlayersListComponent,
    TournamentPlayersListContainerComponent
  ],
    imports: [
        CommonModule,
        TournamentRoutingModule,
        MatProgressBarModule,
        MatDividerModule,
        MatIconModule,
        MatCardModule,
        MatListModule,
        SharedModule,
        MatButtonToggleModule,
        MatTooltipModule,
        MatButtonModule,
        FlexLayoutModule,
        MatMenuModule
    ]
})
export class TournamentModule {
}
