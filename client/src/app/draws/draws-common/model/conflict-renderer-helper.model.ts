import {ConflictType} from './conflict-type.enum';

/**
 * Shared code for determining how to render conflict type and tooltip
 */
export class ConflictRendererHelper {
  static getConflictClass(conflictType: ConflictType) {
    let conflictClass = 'no-conflict';
    if (conflictType != null) {
      switch (conflictType) {
        case ConflictType.NO_CONFLICT:
          conflictClass = 'no-conflict';
          break;

        case ConflictType.LIVES_NEARBY:
          conflictClass = 'lives-nearby';
          break;

        case ConflictType.SAME_CLUB_FIRST_ROUND:
          conflictClass = 'same-club-first-round';
          break;

        case ConflictType.SAME_CLUB_SECOND_ROUND:
          conflictClass = 'same-club-second-round';
          break;

        case ConflictType.PLAYS_IN_OTHER_EVENT_FIRST_ROUND:
          conflictClass = 'plays-in-other-event';
          break;

        case ConflictType.SCORES_ENTERED:
          conflictClass = 'scores-entered';
          break;
      }
    }
    return conflictClass;
  }

  static getConflictTooltipText(conflictType: ConflictType) {
    let tooltipText = '';
    if (conflictType != null) {
      switch (conflictType) {
        case ConflictType.NO_CONFLICT:
          tooltipText = '';
          break;

        case ConflictType.LIVES_NEARBY:
          tooltipText = 'This player lives near the other player in this group';  // green
          break;

        case ConflictType.SAME_CLUB_FIRST_ROUND:
          tooltipText = 'This player and another player in this group are from the same club';  // red
          break;

        case ConflictType.SAME_CLUB_SECOND_ROUND:
          tooltipText = 'This player and another player from the same club may meet in second round';  // yellow
          break;

        case ConflictType.PLAYS_IN_OTHER_EVENT_FIRST_ROUND:
          tooltipText = 'The players are in a first round match with each other in some other event';  // purpple
          break;

        case ConflictType.SCORES_ENTERED:
          tooltipText = 'Winner of the group'; // blue
          break;
      }
    }
    return tooltipText;
  }
}
