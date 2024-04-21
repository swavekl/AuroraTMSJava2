import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationService} from '../authentication.service';
import {Subscription} from 'rxjs';
import {Router} from '@angular/router';

@Component({
  selector: 'app-reset-password-start',
  template: `
      <app-centered-panel>
        <div fxLayout="column" fxLayoutAlign="start center" style="text-align: left;">
          <mat-card>
            <mat-card-content>
              <div fxLayout="column" class="mat-body-1" >
                <div [hidden]="done">
                  <div style="padding-bottom: 10px;">Enter email address for sending reset password instructions</div>
                  <form name="form" #f="ngForm">
                    <mat-form-field fxFlex="100%">
                      <input type="email" matInput name="email" [(ngModel)]="email" email required>
                      <mat-error *ngIf="f.form.controls['email']?.errors?.required">Field is required</mat-error>
                      <mat-error *ngIf="f.form.controls['email']?.errors?.email">Email is invalid</mat-error>
                    </mat-form-field>
                  </form>
                </div>
                <div [hidden]="!done" [innerHtml]="message"></div>
              </div>
            </mat-card-content>
            <mat-card-actions>
              <ng-container *ngIf="!notRegistered; else other">
                <div fxLayout="row" fxLayoutAlign="end start" style="width: 100%">
                  <div [hidden]="done">
                    <button mat-raised-button color="primary" [disabled]="!f.form.valid" (click)="forgotPassword()">Submit</button>
                  </div>
                </div>
              </ng-container>
              <ng-template #other>
                <div fxLayout="row" fxLayoutAlign="space-between start" style="width: 100%">
                  <button mat-raised-button color="" [disabled]="!f.form.valid" (click)="goToCreateAccount()">Create Account</button>
                  <button mat-raised-button color="primary" [disabled]="!f.form.valid" (click)="tryAgain()">Try Again</button>
                </div>
              </ng-template>
            </mat-card-actions>
          </mat-card>
        </div>
      </app-centered-panel>
  `,
  styles: []
})
export class ResetPasswordStartComponent implements OnInit, OnDestroy {

  // form fields
  public email: string;
  public message: string;
  public done: boolean;
  public notRegistered: boolean;

  private subscription: Subscription;

  constructor(private authenticationService: AuthenticationService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.message = '';
    this.email = '';
    this.done = false;
    this.notRegistered = false;
  }

  forgotPassword() {
    this.notRegistered = false;
    this.subscription = this.authenticationService.forgotPassword(this.email)
      .subscribe(
        data => {
          const success: boolean = (data?.status === 'SUCCESS');
          if (success) {
            this.message = `<p>Reset password instructions were sent to</p><p>${this.email}.</p><p> Please follow instructions in the email.`;
          } else {
            const error = data?.errorMessage || 'Unknown error';
            this.notRegistered = error.indexOf('Resource not found') > 0;
            if (this.notRegistered) {
              this.message = `<p>We didn't find a user registered using email address:</p><p>${this.email}.</p><p>Are you sure you registered on this website before? If not, please create an account.</p><p>If you misspelled the email please try again.</p>`;
            } else {
              this.message = `<p>Error encountered during reset password.</p><p>This may be caused by using a different email address then the one used for registration/login</p><p>Error details: ${error}</p>`;
            }
          }
          this.done = true;
        },
        error => {
          // console.log('error ', error);
          this.message = `<p>Error encountered during reset password.</p><p>This may be caused by using a different email address then the one used for registration/login</p><p>Error details: ${error}</p>`;
        }
      );
  }

  goToCreateAccount(): void {
    this.router.navigateByUrl('/ui/login/register');
  }

  tryAgain() {
    this.message = '';
    this.email = '';
    this.done = false;
    this.notRegistered = false;
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null;
    }
  }
}
