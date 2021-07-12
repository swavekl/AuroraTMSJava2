import {ChangeDetectorRef, Component, EventEmitter, Inject, OnInit, Output} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Match} from '../model/match.model';
import {ScoreEntryDialogData, ScoreEntryDialogResult} from './score-entry-dialog-data';

/**
 * Dialog for entering match score
 */
@Component({
  selector: 'app-score-entry-dialog',
  templateUrl: './score-entry-dialog.component.html',
  styleUrls: ['./score-entry-dialog.component.scss']
})
export class ScoreEntryDialogComponent implements OnInit {

  // number of games in this match
  numberOfGames: number;

  // match data
  match: Match;

  // array so we can use iteration in the template
  games: number [];

  // player names
  playerAName: string;
  playerBName: string;

  // callback function to save ane move to previous or next match without closing dialog
  callbackFn: (scope: any, result: ScoreEntryDialogResult) => void;
  callbackFnScope: any;

  // scoreEmitter: EventEmitter<ScoreEntryDialogResult> = new EventEmitter<ScoreEntryDialogResult>();
  // array for drawing buttons from 1 to 10
  firstRowButtons: number [] = [];
  // button 11 is a big button
  // buttons 12 through 21
  secondRowButtons: number [] = [];

  // indicates if these buttons need to be enabled
  disablePreviousButton: boolean;
  disableNextButton: boolean;

  constructor(public dialogRef: MatDialogRef<ScoreEntryDialogComponent>,
              private cdr: ChangeDetectorRef,
              @Inject(MAT_DIALOG_DATA) public data: ScoreEntryDialogData) {
    this.initialize(data);
    // buttons 0 - 10
    this.firstRowButtons = Array(11);
    for (let i = 0; i < this.firstRowButtons.length; i++) {
      this.firstRowButtons[i] = i;
    }
    // 12 - 23
    this.secondRowButtons = Array(11);
    for (let i = 0; i < this.secondRowButtons.length; i++) {
      this.secondRowButtons[i] = 12 + i;
    }
  }

  private initialize(data: ScoreEntryDialogData) {
    this.numberOfGames = data.numberOfGames;
    this.games = Array(this.numberOfGames);
    this.match = data.match;
    this.playerAName = data.playerAName;
    this.playerBName = data.playerBName;
    this.callbackFn = data.callbackFn;
    this.callbackFnScope = data.callbackFnScope;
    this.disablePreviousButton = (data.editedMatchIndex === 0);
    this.disableNextButton = !(data.editedMatchIndex < (data.numberOfMatchesInCard - 1));
  }

  ngOnInit(): void {
  }

  onCancel() {
    const retValue = {
      match: this.match,
      action: 'cancel'
    };
    this.dialogRef.close(retValue);
  }

  onOk(formValues: any) {
    const updatedMatch = {
      ...this.match,
      ...formValues
    };
    const retValue = {
      match: updatedMatch,
      action: 'ok'
    };
    this.dialogRef.close(retValue);
  }

  onNext(formValues: any) {
    const updatedMatch = {
      ...this.match,
      ...formValues
    };
    const retValue = {
      match: updatedMatch,
      action: 'next'
    };
    this.callbackFn(this.callbackFnScope, retValue);
  }

  onPrevious(formValues: any) {
    const updatedMatch = {
      ...this.match,
      ...formValues
    };
    const retValue = {
      match: updatedMatch,
      action: 'previous'
    };
    this.callbackFn(this.callbackFnScope, retValue);
  }

  setGameScore(gameScore: number) {
    // get currently focused game score and set number
    console.log('score' + gameScore);
  }

  displayMatch(data: ScoreEntryDialogData) {
    console.log('in DisplayData', data);
    this.initialize(data);
    // refresh dialog with new data
    this.cdr.markForCheck();
  }
}


