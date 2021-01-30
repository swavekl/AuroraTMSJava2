import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {HomeComponent} from './home/home/home.component';

const routes: Routes = [
  {path: '', redirectTo: '/login', pathMatch: 'full'},
  {
    path: 'home', component: HomeComponent
  },
  {
    path: 'tournamentsconfig',
    loadChildren: () => import('./tournament/tournament-config/tournament-config.module').then(m => m.TournamentConfigModule)
  },
  {
    path: 'tournaments',
    loadChildren: () => import('./tournament/tournament/tournament.module').then(m => m.TournamentModule)
  },
  {
    path: 'entries',
    loadChildren: () => import('./tournament/tournament-entry/tournament-entry.module').then(m => m.TournamentEntryModule)
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
