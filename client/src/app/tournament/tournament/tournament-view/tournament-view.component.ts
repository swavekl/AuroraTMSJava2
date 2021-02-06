import {Component, Input, OnInit} from '@angular/core';
import {TournamentInfo} from '../tournament-info.model';
import {Router} from '@angular/router';

@Component({
  selector: 'app-tournament-view',
  templateUrl: './tournament-view.component.html',
  styleUrls: ['./tournament-view.component.css']
})
export class TournamentViewComponent implements OnInit {
  @Input()
  tournament: TournamentInfo;

  @Input()
  entryId: number;

  constructor(private router: Router) {
  }

  ngOnInit(): void {
  }

  onEnter() {
    const url = `entries/entrywizard/${this.tournament.id}/create`;
    this.router.navigateByUrl(url);
  }

  onView() {
    const url = `entries/entrywizard/${this.tournament.id}/edit/${this.entryId}`;
    this.router.navigateByUrl(url);
  }
}
