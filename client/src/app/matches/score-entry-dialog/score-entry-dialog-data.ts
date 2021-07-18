import {Match} from '../model/match.model';

/**
 * Data for match score entry dialog
 */
export interface ScoreEntryDialogData {

  // match to enter score for
  match: Match;

  // number of games to display
  numberOfGames: number;

  // player or player names for side A and B
  playerAName: string;
  playerBName: string;

  // number of matches on the card to determine if there is next or previous to enter
  numberOfMatchesInCard: number;

  // zero based match index
  editedMatchIndex: number;

  // number of points per game to win 11 or 21
  pointsPerGame: number;

  callbackFn: (scope: any, result: ScoreEntryDialogResult) => void;
  callbackFnScope: any;
}

export interface ScoreEntryDialogResult {
  match: Match;

  // one of 'ok', 'cancel', 'previous', 'next'
  action: string;
}
