import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../guards/auth.guard';
import {AccountOnboardStartComponent} from './account-onboard-start/account-onboard-start.component';
import {AccountLandingComponent} from './account-landing/account-landing.component';
import {AccountOnboardFinishComponent} from './account-onboard-finish/account-onboard-finish.component';
import {AccountRefreshComponent} from './account-refresh/account-refresh.component';

// root is 'account'
const routes: Routes = [
  {
    path: '',
    component: AccountLandingComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'onboardstart',
    component: AccountOnboardStartComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'onboardreturn/:userProfileId',
    component: AccountOnboardFinishComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'onboardrefresh/:userProfileId',
    component: AccountRefreshComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccountRoutingModule { }
