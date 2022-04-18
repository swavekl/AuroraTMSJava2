import {Component, Input, OnInit} from '@angular/core';
import {EventResultStatus} from '../model/event-result-status';

@Component({
  selector: 'app-tournament-results',
  templateUrl: './tournament-results-list.component.html',
  styleUrls: ['./tournament-results-list.component.scss']
})
export class TournamentResultsListComponent implements OnInit {

  @Input()
  eventResultStatusList: EventResultStatus[] = [];

  @Input()
  tournamentId: number;

  constructor() {
  }

  ngOnInit(): void {
  }

}
