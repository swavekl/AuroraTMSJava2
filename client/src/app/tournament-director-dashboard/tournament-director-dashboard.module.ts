import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TournamentDirectorDashboardRoutingModule } from './tournament-director-dashboard-routing.module';
import { TournamentDirectorDashboardComponent } from './tournament-director-dashboard/tournament-director-dashboard.component';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { LayoutModule } from '@angular/cdk/layout';
import { TournamentDirectorDashboardContainerComponent } from './tournament-director-dashboard/tournament-director-dashboard-container.component';


@NgModule({
  declarations: [
    TournamentDirectorDashboardComponent,
    TournamentDirectorDashboardContainerComponent
  ],
  imports: [
    CommonModule,
    TournamentDirectorDashboardRoutingModule,
    MatGridListModule,
    MatCardModule,
    MatMenuModule,
    MatIconModule,
    MatButtonModule,
    LayoutModule
  ]
})
export class TournamentDirectorDashboardModule { }
