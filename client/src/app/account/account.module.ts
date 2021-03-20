import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';

import {AccountRoutingModule} from './account-routing.module';
import {AccountLandingComponent} from './account-landing/account-landing.component';
import {AccountOnboardStartComponent} from './account-onboard-start/account-onboard-start.component';
import {AccountOnboardFinishComponent} from './account-onboard-finish/account-onboard-finish.component';
import {SharedModule} from '../shared/shared.module';
import {MatCardModule} from '@angular/material/card';
import { AccountRefreshComponent } from './account-refresh/account-refresh.component';


@NgModule({
  declarations: [
    AccountOnboardStartComponent,
    AccountOnboardFinishComponent,
    AccountLandingComponent,
    AccountRefreshComponent
  ],
  imports: [
    CommonModule,
    AccountRoutingModule,
    MatButtonModule,
    SharedModule,
    MatCardModule
  ]
})
export class AccountModule {
}
