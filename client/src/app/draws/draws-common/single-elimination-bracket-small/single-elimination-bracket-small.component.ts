import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges, TemplateRef} from '@angular/core';
import {NgttRound, NgttTournament} from 'ng-tournament-tree/lib/declarations/interfaces';
import {Match} from '../model/match.model';

@Component({
    selector: 'app-single-elimination-bracket-small',
    templateUrl: './single-elimination-bracket-small.component.html',
    styleUrls: ['./single-elimination-bracket-small.component.scss'],
    standalone: false
})
export class SingleEliminationBracketSmallComponent implements OnInit, OnChanges {
  @Input()
  rounds: number [] = [];

  @Input()
  tournament: NgttTournament;

  @Input()
  doublesEvent: boolean;

  @Input()
  matchTemplate: TemplateRef<any>;

  currentRound: number = 0;

  firstRound: number = 0;

  currentRoundMatches: Match[] = [];

  ctx: any;

  constructor() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const roundsChange: SimpleChange = changes.rounds;
    if (roundsChange?.currentValue != null && roundsChange?.currentValue.length > 0) {
      this.currentRound = roundsChange.currentValue[0];
      this.firstRound = this.currentRound;
    }

    const tournamentChange: SimpleChange = changes.tournament;
    if (tournamentChange?.currentValue != null) {
      this.tournament = tournamentChange.currentValue;
    }

    if (this.currentRound != 0 && this.tournament != null) {
      this.currentRoundMatches = this.filterCurrentRoundMatches(this.currentRound);
    }
  }

  private filterCurrentRoundMatches(currentRound: number): Match[] {
    let currentRoundMatches: Match [] = [];
    const rounds: NgttRound[] = this.tournament.rounds;
    for (let i = 0; i < rounds.length; i++) {
      const round = rounds[i];
      const matches: Match[] = round.matches;
      for (let j = 0; j < matches.length; j++) {
        const match: Match = matches[j];
        if (match.opponentA?.round === currentRound ||
          match.opponentB?.round === currentRound) {
          currentRoundMatches.push(match);
        }
      }
    }
    return currentRoundMatches;
  }

  ngOnInit(): void {

  }

  onNextRound(): void {
    this.currentRound = (this.currentRound > 2) ? this.currentRound / 2 : 2;
    // console.log('onNextRound this.currentRound', this.currentRound);
    this.currentRoundMatches = this.filterCurrentRoundMatches(this.currentRound);
  }

  onPreviousRound(): void {
    this.currentRound = this.currentRound * 2;
    // console.log('onPreviousRound this.currentRound', this.currentRound);
    this.currentRoundMatches = this.filterCurrentRoundMatches(this.currentRound);
  }

  hasPreviousRound(): boolean {
    return this.currentRound < this.firstRound;
  }

  hasNextRound(): boolean {
    return this.currentRound > 2;
  }
}
