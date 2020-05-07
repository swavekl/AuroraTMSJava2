import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {TournamentConfigListContainerComponent} from './tournament-config-list/tournament-config-list-container.component';
import {TournamentConfigEditContainerComponent} from './tournament-config-edit/tournament-config-edit-container.component';
import {TournamentEventConfigContainerComponent} from './tournament-event-config/tournament-event-config-container.component';


const routes: Routes = [
  {
    path: '',
    component: TournamentConfigListContainerComponent
  },
  {
    path: 'tournament/edit/:id',
    component: TournamentConfigEditContainerComponent
  },
  {
    path: 'tournament/:tournamentId/tournamentevent/create',
    component: TournamentEventConfigContainerComponent
  },
  {
    path: 'tournament/:tournamentId/tournamentevent/edit/:id',
    component: TournamentEventConfigContainerComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentConfigRoutingModule {
}
