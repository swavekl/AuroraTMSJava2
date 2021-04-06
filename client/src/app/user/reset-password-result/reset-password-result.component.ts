import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-reset-password-result',
  template: `
    <app-centered-panel>
      <div *ngIf="succeeded">Reset password was successful.  Proceed to <a routerLink="/login">Login</a></div>
      <div *ngIf="!succeeded">Reset password failed.  Please try again</div>
    </app-centered-panel>
  `,
  styles: [
  ]

})
export class ResetPasswordResultComponent implements OnInit {

  public succeeded: boolean;

  constructor(private activatedRoute: ActivatedRoute) { }

  ngOnInit(): void {
    this.succeeded = this.activatedRoute.snapshot.params['succeeded'] || false;
  }
}
