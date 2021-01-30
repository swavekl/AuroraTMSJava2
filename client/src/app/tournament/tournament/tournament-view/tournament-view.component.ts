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

  constructor(private router: Router) {
  }

  ngOnInit(): void {
  }

  onEnter(tournamentId: number) {
    const url = `entries/entrywizard/${tournamentId}/create`;
    this.router.navigateByUrl(url);
  }
}
