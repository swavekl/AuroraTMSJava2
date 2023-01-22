import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationService} from '../authentication.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-reset-password-start',
  template: `
    <div class="center">
      <div fxLayout="column" fxLayoutAlign="start center">
        <mat-card appearance="outlined">
          <mat-card-content>
            <div fxLayout="column">
              <div [hidden]="done">
                <div>Specify email address for sending reset password instructions</div>
                <form name="form" #f="ngForm" novalidate>
                  <mat-form-field fxFlex="75%">
                    <input type="email" matInput name="email" [(ngModel)]="email" required>
                    <mat-error>Email is required</mat-error>
                  </mat-form-field>
                </form>
              </div>
              <div [hidden]="!done" [innerHtml]="message"></div>
            </div>
          </mat-card-content>
          <mat-card-actions>
            <div [hidden]="done">
              <button mat-raised-button color="primary" [disabled]="!f.form.valid" (click)="forgotPassword()">Submit</button>
            </div>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: []
})
export class ResetPasswordStartComponent implements OnInit, OnDestroy {

  // form fields
  public email: string;
  public message: string;
  public done: boolean;

  private subscription: Subscription;

  constructor(private authenticationService: AuthenticationService) {
  }

  ngOnInit(): void {
    this.message = '';
    this.email = '';
    this.done = false;
  }

  forgotPassword() {
    this.subscription = this.authenticationService.forgotPassword(this.email)
      .subscribe(
        data => {
          const success: boolean = (data?.status === 'SUCCESS');
          if (success) {
            this.message = `Reset password instructions were sent to ${this.email}. <p> Please follow instructions in the email.`;
          } else {
            const error = data?.errorMessage || 'Unknown error';
            this.message = `Error encountered during reset password: ${error}`;
          }
          this.done = true;
        },
        error => {
          // console.log('error ', error);
          this.message = `Error encountered during reset password: ${error}`;
        }
      );
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null;
    }
  }
}
