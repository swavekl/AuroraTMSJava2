import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
// import {OktaCallbackComponent} from '@okta/okta-angular';
import {HomeComponent} from './home/home/home.component';

const routes: Routes = [
  // {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: '', redirectTo: '/login', pathMatch: 'full'},
  // {
  //   path: 'implicit/callback',
  //   component: OktaCallbackComponent
  // },
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
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
