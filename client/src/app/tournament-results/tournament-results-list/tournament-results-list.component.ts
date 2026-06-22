import {Component, Input, OnInit} from '@angular/core';
import {EventResultStatus} from '../model/event-result-status';

@Component({
    selector: 'app-tournament-results',
    templateUrl: './tournament-results-list.component.html',
    styleUrls: ['./tournament-results-list.component.scss'],
    standalone: false
})
export class TournamentResultsListComponent implements OnInit {

  @Input()
  eventResultStatusList: EventResultStatus[] = [];

  @Input()
  tournamentId: number;

  // Define the columns to show in the desktop table layout
  displayedColumns: string[] = ['eventName', 'firstPlace', 'secondPlace', 'thirdFourthPlace', 'actions'];

  constructor() {
  }

  ngOnInit(): void {
  }

}
