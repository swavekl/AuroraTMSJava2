import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {HomeComponent} from './home/home/home.component';
import {LogoutComponent} from './user/logout/logout.component';

const ROLE_ADMINS = 'Admins';
const ROLE_TOURNAMENT_DIRECTORS = 'TournamentDirectors';

const routes: Routes = [
  {path: '', redirectTo: '/login', pathMatch: 'full'},
  {path: 'logout', component: LogoutComponent},
  {
    path: 'home', component: HomeComponent
  },
  {
    path: 'tournamentsconfig',
    data: {
      roles: [ROLE_TOURNAMENT_DIRECTORS, ROLE_ADMINS]
    },
    loadChildren: () => import('./tournament/tournament-config/tournament-config.module').then(m => m.TournamentConfigModule)
  },
  {
    path: 'tournaments',
    loadChildren: () => import('./tournament/tournament/tournament.module').then(m => m.TournamentModule)
  },
  {
    path: 'entries',
    loadChildren: () => import('./tournament/tournament-entry/tournament-entry.module').then(m => m.TournamentEntryModule)
  },
  {
    path: 'account',
    loadChildren: () => import('./account/account.module').then(m => m.AccountModule),
    data: {
      roles: [ROLE_TOURNAMENT_DIRECTORS, ROLE_ADMINS]
    },
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
