import {Component, Input, OnInit} from '@angular/core';
import {MatchCard} from '../../matches/model/match-card.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {EventResults} from '../model/event-results';

@Component({
  selector: 'app-tournament-result-details',
  templateUrl: './tournament-result-details.component.html',
  styleUrls: ['./tournament-result-details.component.scss']
})
export class TournamentResultDetailsComponent implements OnInit {

  @Input()
  eventResultsList: EventResults[];

  @Input()
  event: TournamentEvent;

  constructor() { }

  ngOnInit(): void {
  }

}
