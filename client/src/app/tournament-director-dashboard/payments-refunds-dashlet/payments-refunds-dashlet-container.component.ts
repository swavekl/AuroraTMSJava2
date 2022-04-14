import {Component, OnDestroy, OnInit} from '@angular/core';
import {PaymentRefundInfo, PaymentRefundService} from '../../account/service/payment-refund.service';
import {Observable, of, Subscription} from 'rxjs';
import {ActivatedRoute} from '@angular/router';
import {PaymentRefundFor} from '../../account/model/payment-refund-for.enum';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-payments-refunds-dashlet-container',
  template: `
    <app-payments-refunds-dashlet [paymentRefundInfos]="paymentRefundInfos$ | async">
    </app-payments-refunds-dashlet>
  `,
  styles: [
  ]
})
export class PaymentsRefundsDashletContainerComponent implements OnInit, OnDestroy {

  paymentRefundInfos$: Observable<PaymentRefundInfo[]>;

  private tournamentId: number;

  private subscriptions: Subscription = new Subscription();

  constructor(private paymentRefundService: PaymentRefundService,
              private activatedRoute: ActivatedRoute) {
    const strTournamentId = this.activatedRoute.snapshot.parent.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
  }

  ngOnInit(): void {
    const subscription = this.paymentRefundService.listPaymentsRefundsForEvent(
      PaymentRefundFor.TOURNAMENT_ENTRY, this.tournamentId)
      .pipe(first())
      .subscribe((infos: PaymentRefundInfo[]) => {
        this.paymentRefundInfos$ = of(infos);
      }, (error => {
        console.log(error);
      }));
    this.subscriptions.add(subscription);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
