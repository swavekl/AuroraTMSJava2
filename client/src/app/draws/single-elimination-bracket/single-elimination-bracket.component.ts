import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {NgttRound, NgttTournament} from 'ng-tournament-tree/lib/declarations/interfaces';
import {DrawRound} from '../model/draw-round.model';
import {DrawItem} from '../model/draw-item.model';

@Component({
  selector: 'app-single-elimination-bracket',
  templateUrl: './single-elimination-bracket.component.html',
  styleUrls: ['./single-elimination-bracket.component.scss']
})
export class SingleEliminationBracketComponent implements OnInit, OnChanges {

  @Input()
  singleEliminationRounds: DrawRound[] = [];

  myTournamentData: NgttTournament;

  constructor() { }

  ngOnInit(): void {
    const round32Matches: any [] = [];
    round32Matches.push({group: 1, left: 'Shahal, Pranav', right: 'Bye'});
    round32Matches.push({group: 9, left: 'Jia, Melynda', right: 'Bryant, Evans'});
    round32Matches.push({group: 13, left: 'Koreev, Vadim', right: 'Mohammadi Abelzi, Maziar'});
    round32Matches.push({group: 5, left: 'Bye', right: 'Eric, To'});
    round32Matches.push({group: 7, left: 'Shahal, Pranav', right: 'Bye'});
    round32Matches.push({group: 11, left: 'Jia, Melynda', right: 'Bryant, Evans'});
    round32Matches.push({group: 15, left: 'Koreev, Vadim', right: 'Mohammadi, Maziar'});
    round32Matches.push({group: 3, left: 'Bye', right: 'Eric, To'});
    round32Matches.push({group: 4, left: 'Shahal, Pranav', right: 'Bye'});
    round32Matches.push({group: 10, left: 'Jia, Melynda', right: 'Bryant, Evans'});
    round32Matches.push({group: 14, left: 'Koreev, Vadim', right: 'Mohammadi, Maziar'});
    round32Matches.push({group: 6, left: 'Bye', right: 'Eric, To'});
    round32Matches.push({group: 8, left: 'Shahal, Pranav', right: 'Bye'});
    round32Matches.push({group: 12, left: 'Jia, Melynda', right: 'Bryant, Evans'});
    round32Matches.push({group: 16, left: 'Koreev, Vadim', right: 'Mohammadi, Maziar'});
    round32Matches.push({group: 2, left: 'Bye', right: 'Eric, To'});

    const round16Matches: any [] = [];
    round16Matches.push({left: 'Shahal, Pranav', right: 'Bye'});
    round16Matches.push({left: 'Jia, Melynda', right: 'Bryant, Evans'});
    round16Matches.push({left: 'Koreev, Vadim', right: 'Mohammadi, Maziar'});
    round16Matches.push({left: 'Bye', right: 'Eric, To'});
    round16Matches.push({left: 'Shahal, Pranav', right: 'Bye'});
    round16Matches.push({left: 'Jia, Melynda', right: 'Bryant, Evans'});
    round16Matches.push({left: 'Koreev, Vadim', right: 'Mohammadi, Maziar'});
    round16Matches.push({left: 'Bye', right: 'Eric, To'});

    const quarterRoundMatches: any [] = [];
    quarterRoundMatches.push({left: 'Shahal, Pranav', right: 'Bye'});
    quarterRoundMatches.push({left: 'Jia, Melynda', right: 'Bryant, Evans'});
    quarterRoundMatches.push({left: 'Koreev, Vadim', right: 'Mohammadi, Maziar'});
    quarterRoundMatches.push({left: 'Bye', right: 'Eric, To'});

    const semiRoundMatches: any [] = [];
    semiRoundMatches.push({left: 'Shahal, Pranav', right: 'Bryant, Evans'});
    semiRoundMatches.push({left: 'Mohammadi, Maziar', right: 'Eric, To'});

    const finalRoundMatches: any [] = [];
    finalRoundMatches.push({left: 'Shahal, Pranav', right: 'Eric, To'});
    finalRoundMatches.push({left: 'Bryant, Evans', right: 'Mohammadi, Maziar'});  // 3rd and 4th

    const roundOf32: NgttRound = { type: 'Winnerbracket', matches: round32Matches};
    const roundOf16: NgttRound = { type: 'Winnerbracket', matches: round16Matches};
    const quarterRound: NgttRound = { type: 'Winnerbracket', matches: quarterRoundMatches};
    const semiRound: NgttRound = { type: 'Winnerbracket', matches: semiRoundMatches};
    const finalRound: NgttRound = { type: 'Final', matches: finalRoundMatches};

    const rounds: NgttRound [] = [];
    rounds.push(roundOf32);
    rounds.push(roundOf16);
    rounds.push(quarterRound);
    rounds.push(semiRound);
    rounds.push(finalRound);
    // this.myTournamentData = {
    //   rounds: rounds
    // };
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
            const leftPlayerName = (drawItemLeft.byeNum === 0) ? drawItemLeft.playerName : 'Bye ' + drawItemLeft.byeNum;
            const rightPlayerName = (drawItemRight.byeNum === 0) ? drawItemRight.playerName : 'Bye ' + drawItemRight.byeNum;
            const match = {
              leftGroup: drawItemLeft.groupNum, rightGroup: drawItemRight.groupNum,
              leftPlayerRating: drawItemLeft.rating, rightPlayerRating: drawItemRight.rating,
              leftPlayerName: leftPlayerName, rightPlayerName: rightPlayerName
            };
            roundMatches.push(match);
            j += 2;
          }
          const type = ((i + 1) === drawRounds.length) ? 'Final' : 'Winnerbracket';
          const round: NgttRound = { type: type, matches: roundMatches};
          rounds.push(round);
        }
        this.myTournamentData = {
          rounds: rounds
        };
      }
    }
  }
}
