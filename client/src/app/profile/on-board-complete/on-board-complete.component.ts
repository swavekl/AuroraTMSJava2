import {Component, OnInit} from '@angular/core';

@Component({
  template: `
    <app-centered-panel>
      <p>
        Nice job {{ firstName }}!  You are now fully registered.
      </p>
      <p *ngIf="newMember">Your USATT (USA Table Tennis) membership id is {{ membershipId }}.</p>
      <p>Now let's find some local tournaments <a routerLink="/tournaments" [state]="{selectRegion: memberRegion }"> to sign up for</a></p>
    </app-centered-panel>
  `,
  styles: [
  ]
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
