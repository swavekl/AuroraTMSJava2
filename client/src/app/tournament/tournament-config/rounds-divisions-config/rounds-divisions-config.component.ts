import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TournamentEventRound} from '../model/tournament-event-round.model';
import {DrawMethod} from '../model/draw-method.enum';
import {CommonRegexPatterns} from '../../../shared/common-regex-patterns';
import {TournamentEvent} from '../tournament-event.model';
import {TournamentEventRoundDivision} from '../model/tournament-event-round-division.model';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';

@Component({
  selector: 'app-rounds-divisions-config',
  standalone: false,
  templateUrl: './rounds-divisions-config.component.html',
  styleUrl: './rounds-divisions-config.component.scss'
})
export class RoundsDivisionsConfigComponent {
  @Input() rounds!: TournamentEventRound[];

  @Input()
  days: any [] = [];

  @Input()
  startTimes: any [] = [];

  @Output()
  roundsChanged: EventEmitter<TournamentEventRound[]> = new EventEmitter();

  readonly NUMERIC_WITH_ZERO_REGEX = CommonRegexPatterns.NUMERIC_WITH_ZERO_REGEX;

  constructor(private dialog: MatDialog) {

  }

  protected addRound() {
    const newRound = TournamentEvent.makeRound('Round X', null, DrawMethod.SNAKE,);
    this.rounds = [...this.rounds, newRound];
    this.roundsChanged.emit(this.rounds);
  }

  protected removeRound(roundIdx: number) {
    const round = this.rounds[roundIdx];
    const roundName = round.roundName;
    const config = {
      width: '450px', height: '230px', data: {
        message: `Are you sure you want to delete round '${roundName}' and all of its divisions?`,
      }
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        const roundsToPrune = [...this.rounds];
        roundsToPrune.splice(roundIdx, 1 );
        this.rounds = [...roundsToPrune];
        this.roundsChanged.emit(this.rounds);
      }
    });

  }

  protected addDivision(inRound: number) {
    const roundIdx = (inRound < this.rounds.length) ? inRound : this.rounds.length - 1;
    const round = this.rounds[roundIdx];
    const division = this.makeDefaultOrCloneDivision(round.divisions);
    const updatedDivisions = [...round.divisions, division];
    this.updatedRoundAndEmit(roundIdx, updatedDivisions);
  }

  protected removeDivision(inRound: number, divIdx: number) {
    const roundIdx = (inRound < this.rounds.length) ? inRound : this.rounds.length - 1;
    const round = this.rounds[roundIdx];
    const division = round.divisions[divIdx];
    const divisionName = division.divisionName;
    const config = {
      width: '450px', height: '230px', data: {
        message: `Are you sure you want to delete division '${divisionName}'?`,
      }
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        const cloneDivisions = [...round.divisions];
        cloneDivisions.splice(divIdx, 1);
        const updatedDivisions = [...cloneDivisions];
        this.updatedRoundAndEmit(roundIdx, updatedDivisions);
      }
    });
  }

  private updatedRoundAndEmit(roundIdx: number, updatedDivisions: TournamentEventRoundDivision[]) {
    let updatedRounds: TournamentEventRound[] = [];
    for (let i = 0; i < this.rounds.length; i++) {
      if (i === roundIdx) {
        const updatedRound = {...this.rounds[i], divisions: updatedDivisions};
        updatedRounds.push(updatedRound);
      } else {
        updatedRounds.push(this.rounds[i]);
      }
    }
    this.rounds = updatedRounds;
    this.roundsChanged.emit(this.rounds);
  }

  private makeDefaultOrCloneDivision(divisions: TournamentEventRoundDivision[]) {
    let division = null;
    if (divisions.length === 0) {
      division = new TournamentEventRoundDivision();
      division.divisionName = 'Championship';
    } else {
      const lastDivision = divisions[divisions.length - 1];
      const divisionName = lastDivision.divisionName + ' copy';
      division = {...lastDivision, divisionName: divisionName};
    }
    return division;
  }

  protected getPreviousDivisions(thisRoundIdx: number) {
    // console.log('thisRoundIdx', thisRoundIdx);
    // console.log('this.rounds.length', this.rounds.length);
    return (thisRoundIdx > 0 && thisRoundIdx < this.rounds?.length) ?
      this.rounds[thisRoundIdx - 1].divisions : [];
  }
}
