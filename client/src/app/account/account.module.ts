import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCardModule} from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {ReactiveFormsModule} from '@angular/forms';
import {FlexLayoutModule} from '@angular/flex-layout';

import {AccountRoutingModule} from './account-routing.module';
import {AccountLandingComponent} from './account-landing/account-landing.component';
import {AccountOnboardStartComponent} from './account-onboard-start/account-onboard-start.component';
import {AccountOnboardFinishComponent} from './account-onboard-finish/account-onboard-finish.component';
import {SharedModule} from '../shared/shared.module';
import {AccountRefreshComponent} from './account-refresh/account-refresh.component';
import {ConnectWithStripeIconComponent} from './connect-with-stripe.icon';
import {HttpClientModule} from '@angular/common/http';
import {PaymentDialogComponent} from './payment-dialog/payment-dialog.component';
import {NgxStripeModule} from 'ngx-stripe';
import {MatDialogModule} from '@angular/material/dialog';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import { RefundDialogComponent } from './refund-dialog/refund-dialog.component';
import {PaymentDialogService} from './service/payment-dialog.service';
import {PaymentRefundService} from './service/payment-refund.service';


@NgModule({
  declarations: [
    AccountOnboardStartComponent,
    AccountOnboardFinishComponent,
    AccountLandingComponent,
    AccountRefreshComponent,
    ConnectWithStripeIconComponent,
    PaymentDialogComponent,
    RefundDialogComponent
  ],
  exports: [
    PaymentDialogComponent,
    RefundDialogComponent
  ],
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatInputModule,
    ReactiveFormsModule,
    FlexLayoutModule,
    HttpClientModule,
    NgxStripeModule.forRoot(),
    AccountRoutingModule,
    SharedModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatProgressBarModule
  ],
  providers: [
    PaymentDialogService,
    PaymentRefundService
  ]
})
export class AccountModule {
}
