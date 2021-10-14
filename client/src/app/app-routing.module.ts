import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {HomeComponent} from './home/home/home.component';
import {LogoutComponent} from './user/logout/logout.component';
import {UserRoles} from './user/user-roles.enum';


const routes: Routes = [
  {path: '', redirectTo: '/login', pathMatch: 'full'},
  {path: 'logout', component: LogoutComponent},
  {
    path: 'home', component: HomeComponent
  },
  {
    path: 'tournamentsconfig',
    data: {
      roles: [UserRoles.ROLE_TOURNAMENT_DIRECTORS, UserRoles.ROLE_ADMINS]
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
      roles: [UserRoles.ROLE_TOURNAMENT_DIRECTORS, UserRoles.ROLE_ADMINS]
    }
  },
  {
    path: 'draws',
    loadChildren: () => import('./draws/draws.module').then(m => m.DrawsModule),
    data: {
      roles: [UserRoles.ROLE_TOURNAMENT_DIRECTORS, UserRoles.ROLE_ADMINS, UserRoles.ROLE_REFEREES]
    }
  },
  {
    path: 'matches',
    loadChildren: () => import('./matches/matches.module').then(m => m.MatchesModule),
    data: {
      roles: [UserRoles.ROLE_TOURNAMENT_DIRECTORS, UserRoles.ROLE_ADMINS, UserRoles.ROLE_DATA_ENTRY_CLERKS, UserRoles.ROLE_UMPIRES]
    }
  },
  {
    path: 'scheduling',
    loadChildren: () => import('./scheduling/scheduling.module').then(m => m.SchedulingModule),
    data: {
      roles: [UserRoles.ROLE_TOURNAMENT_DIRECTORS, UserRoles.ROLE_ADMINS, UserRoles.ROLE_REFEREES]
    }
  },
  { path: 'today', loadChildren: () => import('./today/today.module').then(m => m.TodayModule) }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {relativeLinkResolution: 'legacy'})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
