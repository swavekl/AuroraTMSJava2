import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {TournamentConfigListContainerComponent} from './tournament-config-list/tournament-config-list-container.component';
import {TournamentConfigEditContainerComponent} from './tournament-config-edit/tournament-config-edit-container.component';
import {TournamentEventConfigContainerComponent} from './tournament-event-config/tournament-event-config-container.component';
import {AuthGuard} from '../../guards/auth.guard';
import {TournamentWaitingListContainerComponent} from './tournament-waiting-list/tournament-waiting-list-container.component';


const routes: Routes = [
  {
    path: '',
    component: TournamentConfigListContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'tournament/edit/:id',
    component: TournamentConfigEditContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'tournament/create',
    component: TournamentConfigEditContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'tournament/:tournamentId/tournamentevent/create',
    component: TournamentEventConfigContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'tournament/:tournamentId/tournamentevent/edit/:id',
    component: TournamentEventConfigContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'tournamentwaitinglist/:tournamentId',
    component: TournamentWaitingListContainerComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentConfigRoutingModule {
}
