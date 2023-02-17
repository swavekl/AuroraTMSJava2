import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LogoutComponent} from './user/logout/logout.component';
import {UserRoles} from './user/user-roles.enum';


const routes: Routes = [
  {path: '', redirectTo: '/login/signin', pathMatch: 'full'},
  {path: 'logout', component: LogoutComponent},
  {
    path: 'home',
    loadChildren: () => import('./home/home.module').then(m => m.HomeModule)
  },
  {
    path: 'userprofile',
    loadChildren: () => import('./profile/profile.module').then(m => m.ProfileModule)
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
  {
    path: 'today',
    loadChildren: () => import('./today/today.module').then(m => m.TodayModule)
  },
  {
    path: 'tddashboard',
    loadChildren: () => import('./tournament-director-dashboard/tournament-director-dashboard.module').then(m => m.TournamentDirectorDashboardModule),
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  {
    path: 'club',
    loadChildren: () => import('./club/club.module').then(m => m.ClubModule),
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_CLUB_MANAGERS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  {
    path: 'insurance',
    loadChildren: () => import('./insurance/insurance.module').then(m => m.InsuranceModule),
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_INSURANCE_MANAGERS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  { path: 'sanction', loadChildren: () => import('./sanction/sanction.module').then(m => m.SanctionModule),
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_SANCTION_COORDINATORS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  { path: 'monitor', loadChildren: () => import('./monitor/monitor.module').then(m => m.MonitorModule),
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_MONITORS]
    }
  },
  { path: 'scoreboard', loadChildren: () => import('./score-board/score-board.module').then(m => m.ScoreBoardModule),
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_DIGITAL_SCORE_BOARDS]
    }
  },
  { path: 'tableusage', loadChildren: () => import('./table-usage/table-usage.module').then(m => m.TableUsageModule),
    data: {
      roles: [UserRoles.ROLE_TOURNAMENT_DIRECTORS, UserRoles.ROLE_ADMINS, UserRoles.ROLE_REFEREES]
    }
  },
  { path: 'prizes', loadChildren: () => import('./prizes/prizes.module').then(m => m.PrizesModule),
    data: {
      roles: [UserRoles.ROLE_TOURNAMENT_DIRECTORS, UserRoles.ROLE_ADMINS]
    }
  },
  {
    path: 'processing',
    loadChildren: () => import('./tournament-processing/tournament-processing.module').then(m => m.TournamentProcessingModule),
    data: {
      roles: [UserRoles.ROLE_USATT_TOURNAMENT_MANAGERS, UserRoles.ROLE_ADMINS]
    }
  },
  { path: 'results', loadChildren: () => import('./tournament-results/tournament-results.module').then(m => m.TournamentResultsModule) },
  { path: 'ui/admin', loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule),
    data: {
      roles: [UserRoles.ROLE_ADMINS]
    }
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { enableTracing: false })],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
