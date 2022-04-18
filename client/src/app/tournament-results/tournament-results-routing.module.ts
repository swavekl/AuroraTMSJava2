import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TournamentResultsListContainerComponent} from './tournament-results-list/tournament-results-list-container.component';
import {TournamentResultDetailsContainerComponent} from './tournament-result-details/tournament-result-details-container.component';

const routes: Routes = [
  {
    path: ':tournamentId',
    component: TournamentResultsListContainerComponent
  },
  {
    path: 'details/:tournamentId/:eventId',
    component: TournamentResultDetailsContainerComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentResultsRoutingModule { }
