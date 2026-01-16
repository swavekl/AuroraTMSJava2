import { Component, ElementRef, EventEmitter, Input, Output, QueryList, ViewChildren, AfterViewChecked } from '@angular/core';
import { TournamentEventRound } from '../model/tournament-event-round.model';
import { DrawMethod } from '../model/draw-method.enum';
import { TournamentEvent } from '../tournament-event.model';
import { MatDialog } from '@angular/material/dialog';
import { DateUtils } from '../../../shared/date-utils';
import { ConfirmationPopupComponent } from '../../../shared/confirmation-popup/confirmation-popup.component';
import { RoundConfigDialogComponent } from '../round-config-dialog/round-config-dialog.component';
import {DivisionConfigDialogComponent, DivisionDialogData} from '../division-config-dialog/division-config-dialog.component';
import {TournamentEventRoundDivision} from '../model/tournament-event-round-division.model';

@Component({
  selector: 'app-event-rounds-divisions-config',
  standalone: false,
  templateUrl: './event-rounds-divisions-config.component.html',
  styleUrl: './event-rounds-divisions-config.component.scss'
})
export class EventRoundsDivisionsConfigComponent implements AfterViewChecked {
  @Input() rounds!: TournamentEventRound[];
  @Input() days!: any[];
  @Input() startDate: Date;
  startTimes!: any[];

  @Output() roundsChanged: EventEmitter<TournamentEventRound[]> = new EventEmitter();

  // Tracks connector paths for the SVG layer
  connectors: string[] = [];

  constructor(private dialog: MatDialog) {
    this.startTimes = new DateUtils().getEventStartingTimes();
  }

  ngAfterViewChecked() {
    // Re-calculate lines whenever the view updates (cards added/moved/deleted)
    // We wrap in setTimeout to avoid ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(() => this.generateConnectors(), 0);
  }

  generateConnectors() {
    const newConnectors: string[] = [];
    if (!this.rounds || this.rounds.length < 2) {
      this.connectors = [];
      return;
    }

    // Connect divisions in Round N to divisions in Round N+1
    for (let r = this.rounds.length - 1; r > 0;  r--) {
      const thisRound = this.rounds[r];
      const priorRound = this.rounds[r - 1];
      const thisRoundDivisions = thisRound.divisions;
      const priorRoundDivisions = priorRound.divisions;
      thisRoundDivisions.forEach((thisRoundDivision, dIdx) => {
        if (thisRoundDivision.previousDivisionIdx < priorRoundDivisions.length) {
          const path = this.calculateCurve(r - 1, thisRoundDivision.previousDivisionIdx, r, dIdx);
          if (path) newConnectors.push(path);
        }
      });
    }
    this.connectors = newConnectors;
  }

  calculateCurve(sR: number, sD: number, tR: number, tD: number): string | null {
    const startEl = document.getElementById(`card-${sR}-${sD}`);
    const endEl = document.getElementById(`card-${tR}-${tD}`);
    const container = document.querySelector('.pipeline-container');

    if (!startEl || !endEl || !container) return null;

    const sRect = startEl.getBoundingClientRect();
    const tRect = endEl.getBoundingClientRect();
    const cRect = container.getBoundingClientRect();

    // Point 1: Middle-right of source card
    const x1 = sRect.right - cRect.left;
    const y1 = sRect.top - cRect.top + (sRect.height / 2);

    // Point 2: Middle-left of target card
    const x2 = tRect.left - cRect.left;
    const y2 = tRect.top - cRect.top + (tRect.height / 2);

    // Control points for the S-curve
    const cp1x = x1 + (x2 - x1) / 2;
    const cp2x = x1 + (x2 - x1) / 2;

    return `M ${x1} ${y1} C ${cp1x} ${y1}, ${cp2x} ${y2}, ${x2} ${y2}`;
  }

  protected addRound(atIndex: number) {
    const newRound = TournamentEvent.makeRound('Round RR', null, DrawMethod.SNAKE,);

    // this.rounds = [...this.rounds, newRound];
    let roundsClone = [...this.rounds];
    roundsClone.splice(atIndex, 0, newRound);
    roundsClone.forEach((round: TournamentEventRound, index: number, array: any[]) => {
      round.ordinalNum = (index + 1);
    });
    this.rounds = roundsClone;
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
        roundsToPrune.forEach((round: TournamentEventRound, index: number, array: any[]) => {
          round.ordinalNum = (index + 1);
        });
        this.rounds = [...roundsToPrune];
        this.roundsChanged.emit(this.rounds);
      }
    });
  }

  protected addDivision(inRound: number) {
    const roundIdx = (inRound < this.rounds.length) ? inRound : this.rounds.length - 1;
    const round = this.rounds[roundIdx];
    const divisionName = (round?.divisions?.length > 0)
      ? 'Division ' + (round.divisions.length + 1) : 'Division 1';
    const division = this.makeDefaultOrCloneDivision(round.divisions, divisionName);
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

  private makeDefaultOrCloneDivision(divisions: TournamentEventRoundDivision[], divisionName: string) {
    let division = null;
    if (divisions.length === 0) {
      division = new TournamentEventRoundDivision();
      division.divisionName = divisionName;
    } else {
      const lastDivision = divisions[divisions.length - 1];
      const divisionName = lastDivision.divisionName + ' copy';
      division = {...lastDivision, divisionName: divisionName};
    }
    return division;
  }

  protected getPreviousDivisions(thisRoundIdx: number) {
    console.log('thisRoundIdx', thisRoundIdx);
    console.log('this.rounds.length', this.rounds.length);
    return (thisRoundIdx > 0 && thisRoundIdx < this.rounds?.length) ?
      this.rounds[thisRoundIdx - 1].divisions : [];
  }

// Inside your main component
  editDivision(rIdx: number, dIdx: number) {
    const dialogData: DivisionDialogData = {
      division: this.rounds[rIdx].divisions[dIdx],
      round: this.rounds[rIdx],
      previousRoundDivisions: this.getPreviousDivisions(rIdx),
      isFirstRoundDivision: rIdx === 0
    };
    console.log('dialogData', dialogData);
    const dialogRef = this.dialog.open(DivisionConfigDialogComponent, {
      width: '600px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(updatedDivision => {
      console.log('updatedDivision', updatedDivision);
      if (updatedDivision) {
        this.rounds[rIdx].divisions[dIdx] = updatedDivision;
        // this saves it to
        this.roundsChanged.emit(this.rounds);

        // IMPORTANT: Trigger the SVG redraw after the DOM updates
        setTimeout(() => this.generateConnectors(), 100);

        console.log('finished updating division');
      }
    });
  }

  protected buildStandardRRtoSE() {
    const qualifyingRRRound = TournamentEvent.makeRound('Round Robin', null, DrawMethod.SNAKE);
    qualifyingRRRound.ordinalNum = 1;
    qualifyingRRRound.singleElimination = false;
    const division = this.makeDefaultOrCloneDivision([], 'Qualifying');
    qualifyingRRRound.divisions = [division];

    const singleElimRound = TournamentEvent.makeRound('Single Elimination', null, DrawMethod.SINGLE_ELIMINATION);
    singleElimRound.ordinalNum = 2;
    singleElimRound.singleElimination = true;
    const divisionSE = this.makeDefaultOrCloneDivision([], 'Championship');
    singleElimRound.divisions = [divisionSE];
    this.rounds = [qualifyingRRRound, singleElimRound];
    this.roundsChanged.emit(this.rounds);
  }

  protected addSingleElimRound() {
    const singleElimRound = TournamentEvent.makeRound('Single Elimination', null, DrawMethod.SINGLE_ELIMINATION);
    singleElimRound.singleElimination = true;
    const divisionSE = this.makeDefaultOrCloneDivision([], 'Championship');
    singleElimRound.divisions = [divisionSE];
    this.rounds = [singleElimRound];
    this.roundsChanged.emit(this.rounds);
  }

  protected clearAll() {
    this.rounds = [];
    this.roundsChanged.emit(this.rounds);
  }

  editRound(round: TournamentEventRound) {
    const dialogRef = this.dialog.open(RoundConfigDialogComponent, {
      width: '450px',
      data: {round: round, days: this.days, startTimes: this.startTimes},
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Update the round object in the array with new values
        Object.assign(round, result);
      }
    });
  }
}
