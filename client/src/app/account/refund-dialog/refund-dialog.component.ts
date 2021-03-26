import {Component, Inject, Input, OnDestroy, OnInit} from '@angular/core';
import {BehaviorSubject, Observable, Subscription} from 'rxjs';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {PaymentData} from '../payment-dialog/payment-data';
import {distinctUntilChanged} from 'rxjs/operators';
import {PaymentRefundService} from '../service/payment-refund.service';

@Component({
  selector: 'app-refund-dialog',
  templateUrl: './refund-dialog.component.html',
  styleUrls: ['./refund-dialog.component.scss']
})
export class RefundDialogComponent implements OnInit, OnDestroy {
  @Input()
  amount: number;

  @Input()
  currencyCode: string;

  private refundInProgressSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  refundInProgress$: Observable<boolean>;

  errorMessage: string;
  isError: boolean;

  refundComplete: boolean;

  public OK = 'ok';
  public CANCEL = 'cancel';

  private subscriptions: Subscription = new Subscription();

  constructor(public dialogRef: MatDialogRef<RefundDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: PaymentData,
              private paymentRefundService: PaymentRefundService) {
    this.refundInProgress$ = this.refundInProgressSubject.asObservable().pipe(distinctUntilChanged());
    this.refundComplete = false;
    this.isError = true;
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setRefundInProgress(inProgress: boolean) {
    this.refundInProgressSubject.next(inProgress);
  }

  onCancel() {
    this.dialogRef.close(this.CANCEL);
  }

  onSubmit() {
    console.log('refunding...');
    this.setRefundInProgress(true);
    this.paymentRefundService.issueRefund(this.data)
      .subscribe(
        () => {
          this.isError = false;
          this.errorMessage = 'Success';
        },
        (error: any) => {
          console.log('got error from payment refund' + JSON.stringify(error));
          this.errorMessage = error?.error;
        }
      );

    this.setRefundInProgress(false);
    this.refundComplete = true;
  }

  onClose() {
    this.dialogRef.close(this.OK);
  }
}
