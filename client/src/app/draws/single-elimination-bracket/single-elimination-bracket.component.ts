import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {NgttRound, NgttTournament} from 'ng-tournament-tree/lib/declarations/interfaces';
import {DrawRound} from '../model/draw-round.model';
import {DrawItem} from '../model/draw-item.model';
import {Match} from '../model/match.model';

@Component({
  selector: 'app-single-elimination-bracket',
  templateUrl: './single-elimination-bracket.component.html',
  styleUrls: ['./single-elimination-bracket.component.scss']
})
export class SingleEliminationBracketComponent implements OnInit, OnChanges {

  @Input()
  singleEliminationRounds: DrawRound[] = [];

  tournament: NgttTournament;

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const singleEliminationRoundsChange: SimpleChange = changes.singleEliminationRounds;
    if (singleEliminationRoundsChange != null) {
      const drawRounds: DrawRound [] = singleEliminationRoundsChange.currentValue;
      if (drawRounds) {
        const rounds: NgttRound [] = [];
        for (let i = 0; i < drawRounds.length; i++) {
          const drawRound = drawRounds[i];
          const drawItems: DrawItem [] = drawRound.drawItems;
          const roundMatches = [];
          for (let j = 0; j < drawItems.length; ) {
            const drawItemLeft: DrawItem = drawItems[j];
            const drawItemRight: DrawItem = drawItems[j + 1];
            const match: Match = new Match();
            match.opponentA = drawItemLeft;
            match.opponentB = drawItemRight;
            match.time = 10.5;
            match.tableNum = 6 + j;  // for now
            match.result = [] ; //[6, -10, 8, 7];
            match.showSeedNumber = (i === 0); // show seed number for first round only

            roundMatches.push(match);
            j += 2;
          }
          const type = ((i + 1) === drawRounds.length) ? 'Final' : 'Winnerbracket';
          const round: NgttRound = { type: type, matches: roundMatches};
          rounds.push(round);
        }
        if (rounds.length > 0) {
          this.tournament = {
            rounds: rounds
          };
        }
      }
    }
  }
}
