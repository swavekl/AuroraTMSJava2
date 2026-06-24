import {Component, Input} from '@angular/core';
import {EventResults} from '../model/event-results';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-round-robin-results-summary',
  imports: [
    CommonModule
  ],
  templateUrl: './round-robin-results-summary.component.html',
  styleUrl: './round-robin-results-summary.component.scss'
})
export class RoundRobinResultsSummaryComponent {

  @Input()
  eventResults: EventResults;

  getDoublesPlayerName(playerNames: string, index: number): string {
    const playerNamesArray: string [] = playerNames.split('/');
    if (playerNamesArray.length === 2) {
      const playerName = playerNamesArray[index].trim();
      return (index === 0) ? playerName + ' /' : playerName;
    } else {
      return playerNames;
    }
  }

}
