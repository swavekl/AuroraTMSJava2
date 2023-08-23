import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCardModule} from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
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
import {NgxStripeModule, StripeFactoryService} from 'ngx-stripe';
import {MatDialogModule} from '@angular/material/dialog';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {RefundDialogComponent} from './refund-dialog/refund-dialog.component';
import {PaymentDialogService} from './service/payment-dialog.service';
import {PaymentRefundService} from './service/payment-refund.service';
import { CheckCashPaymentDialogComponent } from './check-cash-payment-dialog/check-cash-payment-dialog.component';
import {CheckCashPaymentDialogService} from './service/check-cash-payment-dialog.service';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';


@NgModule({
  declarations: [
    AccountOnboardStartComponent,
    AccountOnboardFinishComponent,
    AccountLandingComponent,
    AccountRefreshComponent,
    ConnectWithStripeIconComponent,
    PaymentDialogComponent,
    RefundDialogComponent,
    CheckCashPaymentDialogComponent
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
    MatProgressBarModule,
    FormsModule,
    MatSlideToggleModule
  ],
  providers: [
    PaymentDialogService,
    PaymentRefundService,
    CheckCashPaymentDialogService,
    StripeFactoryService
  ]
})
export class AccountModule {
}
