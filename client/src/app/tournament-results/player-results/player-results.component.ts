import {Component, Input, OnInit} from '@angular/core';
import {PlayerMatchSummary} from '../model/player-match-summary';

@Component({
  selector: 'app-player-results',
  templateUrl: './player-results.component.html',
  styleUrls: ['./player-results.component.scss']
})
export class PlayerResultsComponent implements OnInit {

  @Input()
  playerMatchSummaryList: PlayerMatchSummary[] = [];

  constructor() { }

  ngOnInit(): void {
  }

  getDoublesPlayerName (playerNames: string, index: number): string {
    const playerNamesArray: string [] = playerNames.split('/');
    if (playerNamesArray.length === 2) {
      const playerName = playerNamesArray[index].trim();
      return (index === 0) ? playerName + ' /' : playerName;
    } else {
      return playerNames;
    }
  }
}
