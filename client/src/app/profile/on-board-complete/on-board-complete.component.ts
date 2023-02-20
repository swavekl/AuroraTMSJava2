import {Component, OnInit} from '@angular/core';

@Component({
  template: `
    <app-centered-panel class="mat-body-1">
      <p>
        Nice job {{ firstName }}! Your on-boarding is complete.
      </p>
      <p *ngIf="newMember">Your USATT (USA Table Tennis) membership id is {{ membershipId }}.</p>
      <p>Now let's find some local tournaments to sign up for.</p>
      <button mat-raised-button color="primary" routerLink="/ui/tournaments" [state]="{selectRegion: memberRegion }">
        Go to Tournaments
      </button>
    </app-centered-panel>
  `,
  styles: []
})
export class OnBoardCompleteComponent implements OnInit {

  newMember: boolean;

  membershipId: string;

  firstName: string;

  memberRegion: string;

  constructor() {
    this.newMember = history?.state?.newMember;
    this.membershipId = history?.state?.membershipId;
    this.firstName = history?.state?.firstName;
    this.memberRegion = history?.state?.memberRegion;
  }

  ngOnInit(): void {
  }

}
