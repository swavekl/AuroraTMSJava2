import {ChangeDetectorRef, Component, ElementRef, Inject, OnInit, QueryList, ViewChildren} from '@angular/core';
import {MatInput} from '@angular/material/input';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Match} from '../model/match.model';
import {ScoreEntryDialogData, ScoreEntryDialogResult} from './score-entry-dialog-data';
import {MatCheckboxChange} from '@angular/material/checkbox';
import {DrawType} from '../../draws/draws-common/model/draw-type.enum';
import {ScoreAuditDialogComponent} from '../score-audit-dialog/score-audit-dialog.component';

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

  // 11 or 21
  pointsPerGame: number;

  // match data
  match: Match;

  // array so we can use iteration in the template
  games: number [];

  // player names
  playerAName: string;
  playerBName: string;

  // individual or combined player rating for side A and B
  playerARating: number;
  playerBRating: number;

  // name of the match including event
  matchIdentifier: string;

  // draw type
  drawType: DrawType;

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

  // name of the input element which currently has focus
  private currentScoreInputName: string;

  // all score input elements - used to set focus
  @ViewChildren(MatInput, {read: ElementRef})
  inputFieldsElementRefs: QueryList<ElementRef>;

  constructor(public dialogRef: MatDialogRef<ScoreEntryDialogComponent>,
              private cdr: ChangeDetectorRef,
              @Inject(MAT_DIALOG_DATA) public data: ScoreEntryDialogData,
              private auditDialog: MatDialog) {
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
    this.currentScoreInputName = null;
  }

  private initialize(data: ScoreEntryDialogData) {
    this.numberOfGames = data.numberOfGames;
    this.games = Array(this.numberOfGames);
    this.match = this.deepCloneMatch(data.match);
    this.playerAName = data.playerAName;
    this.playerBName = data.playerBName;
    this.playerARating = data.playerARating;
    this.playerBRating = data.playerBRating;
    this.callbackFn = data.callbackFn;
    this.callbackFnScope = data.callbackFnScope;
    this.disablePreviousButton = (data.editedMatchIndex === 0);
    this.disableNextButton = !(data.editedMatchIndex < (data.numberOfMatchesInCard - 1));
    this.pointsPerGame = data.pointsPerGame ?? 11;
    this.currentScoreInputName = 'game1ScoreSideA';
    this.matchIdentifier = data.matchIdentifier;
    this.drawType = data.drawType;
  }

  ngOnInit(): void {
  }

  onClose() {
    const retValue = {
      match: this.match,
      action: 'cancel'
    };
    this.dialogRef.close(retValue);
  }


  onReset() {
    this.match.game1ScoreSideA = 0;
    this.match.game1ScoreSideB = 0;
    this.match.game2ScoreSideA = 0;
    this.match.game2ScoreSideB = 0;
    this.match.game3ScoreSideA = 0;
    this.match.game3ScoreSideB = 0;
    this.match.game4ScoreSideA = 0;
    this.match.game4ScoreSideB = 0;
    this.match.game5ScoreSideA = 0;
    this.match.game5ScoreSideB = 0;
    this.match.game6ScoreSideA = 0;
    this.match.game6ScoreSideB = 0;
    this.match.game7ScoreSideA = 0;
    this.match.game7ScoreSideB = 0;
    this.match.sideADefaulted = false;
    this.match.sideBDefaulted = false;
  }

  onSave(formValues: any) {
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

  displayMatch(data: ScoreEntryDialogData) {
    this.initialize(data);
    this.setFocusToScoreInputField(this.currentScoreInputName, false);
  }

  /**
   * Sets game score from the button press into currently focused score input field
   * Advances focus to next field in tab order
   * @param gameScore score to set
   */
  setGameScore(gameScore: number) {
    // get currently focused game score and set number
    if (this.currentScoreInputName != null) {
      // set the score and let the dialog update itself
      const focusedName = this.currentScoreInputName;
      const updatedMatch = this.deepCloneMatch(this.match);
      updatedMatch[focusedName] = gameScore;
      this.match = updatedMatch;

      this.setFocusToScoreInputField(focusedName, true);
    }
  }

  private setFocusToScoreInputField(focusedName: string, advanceToNext: boolean) {
    // find tab index of currently focused field (before user clicked on the score button)
    let nextInputTabIndex = -1;
    this.inputFieldsElementRefs.forEach((elementRef: ElementRef) => {
      if (focusedName === elementRef.nativeElement.name) {
        const thisTabIndex: number = Number(elementRef.nativeElement.getAttribute('tabindex'));
        // set focus to the next edit field
        if (advanceToNext) {
          nextInputTabIndex = thisTabIndex + 1;
        } else {
          // reset focus to this input field - this is used when we advance to previous or next match
          elementRef.nativeElement.focus();
        }
      }
    });
    if (nextInputTabIndex !== -1) {
      this.inputFieldsElementRefs.forEach((elementRef: ElementRef) => {
        const thisTabIndex: number = Number(elementRef.nativeElement.getAttribute('tabindex'));
        if (thisTabIndex === nextInputTabIndex) {
          elementRef.nativeElement.focus();
        }
      });
    }
  }

  /**
   * Remember the input element which gained focus
   * @param $event focus event
   */
  onFocus($event: FocusEvent) {
    this.currentScoreInputName = (<HTMLInputElement>$event.target).name;
  }

  onScoreValueChange($event: Event) {
    console.log('onValueChange', $event);
  }

  onDefaultValueChange($event: MatCheckboxChange) {
    console.log('onDefaultValueChange', $event);
  }

  isMatchWinner(profileId: string): boolean {
    return (this.match) ? Match.isMatchWinner(profileId, this.match, this.numberOfGames, this.pointsPerGame) : false;
  }

  private deepCloneMatch (match: Match): Match {
    return JSON.parse(JSON.stringify(match));
  }

  getMatchNumberIdentifier (): string {
    return (this.drawType === DrawType.ROUND_ROBIN) ? `Match ${this.match.matchNum}` : '';
  }

  onAudit() {
    let playerIdToNameMap: any = {};
    if (this.match.playerAProfileId.indexOf(';') > 0 && this.match.playerBProfileId.indexOf(';') > 0) {
      const playerIdsA = this.match.playerAProfileId.split(';')
      const playerNamesA = this.playerAName.split('/')
      playerIdToNameMap[playerIdsA[0]] = playerNamesA[0];
      playerIdToNameMap[playerIdsA[1]] = playerNamesA[1];
      const playerIdsB = this.match.playerBProfileId.split(';')
      const playerNamesB = this.playerBName.split('/')
      playerIdToNameMap[playerIdsB[0]] = playerNamesB[0];
      playerIdToNameMap[playerIdsB[1]] = playerNamesB[1];
    } else {
      playerIdToNameMap[this.match.playerAProfileId] = this.playerAName;
      playerIdToNameMap[this.match.playerBProfileId] = this.playerBName;
    }
    const config = {
      width: '600px', height: '400px', data: {
        matchId: this.match.id,
        numberOfGames: this.numberOfGames,
        pointsPerGame: this.pointsPerGame,
        profileIdToNameMap: playerIdToNameMap
      }
    };
    const dialogRef = this.auditDialog.open(ScoreAuditDialogComponent, config);
    dialogRef.afterClosed().subscribe(result => {
    });
  }
}


