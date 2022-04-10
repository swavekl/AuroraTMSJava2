import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TournamentDirectorDashboardComponent } from './tournament-director-dashboard/tournament-director-dashboard.component';

const routes: Routes = [{ path: '', component: TournamentDirectorDashboardComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentDirectorDashboardRoutingModule { }
