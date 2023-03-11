import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-reset-password-result',
  template: `
    <app-centered-panel class="mat-body-1">
      <div *ngIf="succeeded">
        <p>Reset password was successful.</p>
        <button type="button" mat-raised-button color="primary" routerLink="/ui/login">
          Go to Login
        </button>
      </div>
      <div *ngIf="!succeeded">
        <p>Reset password failed.  Please try again</p>
        <button type="button" mat-raised-button color="primary" routerLink="/ui/resetpasswordstart">
          Go to Reset Password
        </button>
      </div>
    </app-centered-panel>
  `,
  styles: [
  ]

})
export class ResetPasswordResultComponent implements OnInit {

  public succeeded: boolean;

  constructor(private activatedRoute: ActivatedRoute) { }

  ngOnInit(): void {
    this.succeeded = this.activatedRoute.snapshot.params['succeeded'] === 'true' || false;
  }
}
