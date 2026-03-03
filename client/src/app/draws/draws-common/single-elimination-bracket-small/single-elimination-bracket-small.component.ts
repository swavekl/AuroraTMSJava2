import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges, TemplateRef} from '@angular/core';
import {Match} from '../model/match.model';
import {SERound} from '../model/draw-round.model';

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
  seRounds: SERound[];

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

    const seRoundsChange: SimpleChange = changes.seRounds;
    if (seRoundsChange?.currentValue != null) {
      this.seRounds = seRoundsChange.currentValue;
    }

    if (this.currentRound != 0 && this.seRounds != null) {
      this.currentRoundMatches = this.filterCurrentRoundMatches(this.currentRound);
    }
  }

  private filterCurrentRoundMatches(currentRound: number): Match[] {
    let currentRoundMatches: Match [] = [];
    const rounds: SERound[] = this.seRounds || [];
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
