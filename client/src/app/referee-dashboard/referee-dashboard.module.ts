import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { RefereeDashboardRoutingModule } from './referee-dashboard-routing.module';
import { RefereeTournamentListComponent } from './referee-tournament-list/referee-tournament-list.component';
import { RefereeTournamentListContainerComponent } from './referee-tournament-list/referee-tournament-list-container.component';
import {SharedModule} from '../shared/shared.module';
import {TournamentModule} from '../tournament/tournament/tournament.module';
import {FlexLayoutModule} from 'ng-flex-layout';
import {MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatListItemIcon} from '@angular/material/list';
import {MatTooltip} from '@angular/material/tooltip';


@NgModule({
  declarations: [
    RefereeTournamentListComponent,
    RefereeTournamentListContainerComponent
  ],
  imports: [
    CommonModule,
    RefereeDashboardRoutingModule,
    SharedModule,
    TournamentModule,
    FlexLayoutModule,
    MatCard,
    MatCardContent,
    MatCardActions,
    MatCardTitle,
    MatButton,
    MatIcon,
    MatListItemIcon,
    MatTooltip,
    MatCardHeader
  ]
})
export class RefereeDashboardModule { }
