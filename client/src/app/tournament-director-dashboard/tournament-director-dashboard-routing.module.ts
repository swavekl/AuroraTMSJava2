import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TournamentDirectorDashboardComponent} from './tournament-director-dashboard/tournament-director-dashboard.component';
import {
  TournamentWaitingListContainerComponent
} from '../tournament/tournament-config/tournament-waiting-list/tournament-waiting-list-container.component';
import {
  TournamentPlayersListContainerComponent
} from '../tournament/tournament/tournament-players-list/tournament-players-list-container.component';
import {PaymentsRefundsDashletContainerComponent} from './payments-refunds-dashlet/payments-refunds-dashlet-container.component';

const routes: Routes = [
  {
    path: ':tournamentId',
    component: TournamentDirectorDashboardComponent,
    children: [
      {
        path: 'paymentsrefunds',
        component: PaymentsRefundsDashletContainerComponent,
        outlet: 'proutlet'
      },
      {
        path: 'waitinglist/:tournamentId',
        component: TournamentWaitingListContainerComponent,
        outlet: 'wloutlet'
      },
      {
        path: 'playerlist/:id',
        component: TournamentPlayersListContainerComponent,
        outlet: 'ploutlet'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentDirectorDashboardRoutingModule {
}
