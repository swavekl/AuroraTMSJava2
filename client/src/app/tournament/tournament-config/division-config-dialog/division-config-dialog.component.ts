import {Component, Inject, OnInit} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {TournamentEventRound} from '../model/tournament-event-round.model';
import {TournamentEventRoundDivision} from '../model/tournament-event-round-division.model';
import {DrawMethod} from '../model/draw-method.enum';

// Interface for the data passed into the dialog
export interface DivisionDialogData {
  division: TournamentEventRoundDivision;
  round: TournamentEventRound;
  previousRoundDivisions: TournamentEventRoundDivision[];
  isFirstRoundDivision: boolean;
}

@Component({
  selector: 'app-division-config-dialog',
  templateUrl: './division-config-dialog.component.html',
  styleUrls: ['./division-config-dialog.component.scss'],
  standalone: false
})
export class DivisionConfigDialogComponent implements OnInit {
  division: TournamentEventRoundDivision;

  round: TournamentEventRound;

  previousRoundDivisions: TournamentEventRoundDivision[] = [];

  isFirstRoundDivision: boolean;

  maxRanking: number;

  // Options for MatSelects
  drawMethods: any [] = [
    {value: DrawMethod.SNAKE.valueOf(), label: 'Snake'},
    {value: DrawMethod.DIVISION.valueOf(), label: 'Division'},
    {value: DrawMethod.BY_RECORD.valueOf(), label: 'By Record'},
    {value: DrawMethod.SINGLE_ELIMINATION.valueOf(), label: 'Single Elimination'},
  ];

  // choices of number of games in a match
  numberOfGamesChoices: any [] = [
    {value: 3, label: 'Best of 3'},
    {value: 5, label: 'Best of 5'},
    {value: 7, label: 'Best of 7'}
  ];

  // Regex patterns from your existing code
  NUMERIC_REGEX = /^[1-9][0-9]*$/;
  NUMERIC_WITH_ZERO_REGEX = /^[0-9]+$/;

  constructor(
    public dialogRef: MatDialogRef<DivisionConfigDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public originalData: DivisionDialogData
  ) {
    // Clone the division to allow for "Cancel" functionality
    this.division = {...originalData.division};
    this.round = originalData.round;
    this.previousRoundDivisions = originalData.previousRoundDivisions || [];
    this.isFirstRoundDivision = originalData.isFirstRoundDivision;
    this.maxRanking = 1;
    for (const previousRoundDivision of this.previousRoundDivisions) {
      this.maxRanking = Math.max(previousRoundDivision.playersToAdvance, this.maxRanking);
    }
  }

  ngOnInit(): void {
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    // Return the modified division object
    this.dialogRef.close(this.division);
  }
}
