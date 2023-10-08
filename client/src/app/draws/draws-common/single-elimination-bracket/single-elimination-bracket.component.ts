import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {NgttRound, NgttTournament} from 'ng-tournament-tree/lib/declarations/interfaces';
import {DrawRound} from '../model/draw-round.model';
import {DrawItem} from '../model/draw-item.model';
import {Match} from '../model/match.model';
import {DrawType} from '../model/draw-type.enum';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {ConflictType} from '../model/conflict-type.enum';
import {CdkDrag, CdkDragDrop, CdkDropList, transferArrayItem} from '@angular/cdk/drag-drop';

@Component({
  selector: 'app-single-elimination-bracket',
  templateUrl: './single-elimination-bracket.component.html',
  styleUrls: ['./single-elimination-bracket.component.scss']
})
export class SingleEliminationBracketComponent implements OnInit, OnChanges {

  @Input()
  selectedEvent: TournamentEvent;

  @Input()
  draws: DrawItem [] = [];

  @Input()
  bracketsHeight: string;

  @Input()
  doublesEvent: boolean;

  // single elimination draw rounds
  singleEliminationRounds: DrawRound [] = [];

  // round numbers
  @Input()
  roundNumbers: number [] = [];

  @Input()
  tournament: NgttTournament;

  // to be determined player profile id same as matches/match.model.ts
  public readonly TBD_PROFILE_ID = 'TBD';

  // data needed by callbacks
  dropListData: any;

  constructor() {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    // make tournament and rounds from draws (as in draw edit or view)
    // get them already prepared in tournament and roundNumbers (in results page)
    const drawsChanges: SimpleChange = changes.draws;
    if (drawsChanges && drawsChanges.currentValue != null) {
      // console.log('DrawsComponent got draws of length ' + drawsChanges.currentValue.length);
      const drawItems: DrawItem[] = drawsChanges.currentValue;
      this.singleEliminationRounds = [];
      drawItems.forEach((drawItem: DrawItem) => {
        if (drawItem.drawType === DrawType.SINGLE_ELIMINATION) {
          const drawRound: DrawRound = this.findDrawRound(drawItem.round);
          drawRound.drawItems.push(drawItem);
        }
      });

      this.finishRemainingSERounds();

      this.transformToNgttTournament();
    }
  }

  /**
   *
   * @private
   */
  private finishRemainingSERounds() {
    if (this.selectedEvent && this.singleEliminationRounds.length > 0) {
      const eventFK = this.selectedEvent.id;
      // find the first round of single elimination e.g. round of 32 or 16 etc.
      let firstRound: DrawRound = null;
      let firstRoundOf = 0;
      for (let i = 0; i < this.singleEliminationRounds.length; i++) {
        const singleEliminationRound = this.singleEliminationRounds[i];
        if (singleEliminationRound.round > firstRoundOf) {
          firstRoundOf = singleEliminationRound.round;
          firstRound = singleEliminationRound;
        }
      }

      const firstRoundParticipants = firstRound.drawItems.length;
      firstRound.round = firstRound.drawItems.length;
      const rounds = Math.ceil(Math.log(firstRoundParticipants) / Math.log(2));
      for (let round = 1; round < rounds; round++) {
        const divider = Math.pow(2, round);
        const thisRoundOf = firstRoundParticipants / divider;
        // find round - it may exist from rank and advance
        const drawRound: DrawRound = this.findDrawRound(thisRoundOf);
        for (let i = 0; i < drawRound.round; i++) {
          // find draw item - it may exist from rank and advance
          const groupNum: number = Math.floor(i / 2) + 1;
          const placeInGroup: number = (i % 2) + 1;
          let drawItem: DrawItem = this.findDrawItem(drawRound, groupNum, placeInGroup);
          // not found create one and add
          if (drawItem == null) {
            drawItem = {
              id: 0, eventFk: eventFK, drawType: DrawType.SINGLE_ELIMINATION, groupNum: groupNum, placeInGroup: placeInGroup,
              state: null, rating: 0, clubName: null, playerName: null, playerId: this.TBD_PROFILE_ID, conflictType: ConflictType.NO_CONFLICT,
              byeNum: 0, round: drawRound.round, seSeedNumber: 0
            };
            drawRound.drawItems.push(drawItem);
          }
        }
        // last round and play for 3 & 4th place ?
        if ((round + 1) === rounds && this.selectedEvent?.play3rd4thPlace) {
          for (let i = 0; i < drawRound.round; i++) {
            const groupNum = 2;
            const placeInGroup: number = (i % 2) + 1;
            let drawItem: DrawItem = this.findDrawItem(drawRound, groupNum, placeInGroup);
            if (drawItem == null) {
              drawItem = {
                id: 0, eventFk: eventFK, drawType: DrawType.SINGLE_ELIMINATION, groupNum: groupNum, placeInGroup: placeInGroup,
                state: null, rating: 0, clubName: null, playerName: null, playerId: this.TBD_PROFILE_ID, conflictType: ConflictType.NO_CONFLICT,
                byeNum: 0, round: 2, seSeedNumber: 0
              };
              drawRound.drawItems.push(drawItem);
            }
          }
        }

        // sort items by group number and placeInGroup
        drawRound.drawItems.sort((drawItem1: DrawItem, drawItem2: DrawItem) => {
          return (drawItem1.groupNum === drawItem2.groupNum)
            ? ((drawItem1.placeInGroup < drawItem2.placeInGroup) ? -1 : 1)
            : ((drawItem1.groupNum < drawItem2.groupNum) ? -1 : 1);
        });
      }

      // sort array by round from highest to lowest
      this.singleEliminationRounds.sort((round1: DrawRound, round2: DrawRound) => {
        return (round1.round === round2.round) ? 0 : ((round1.round < round2.round) ? 1 : -1);
      });
    }
  }

  /**
   *
   * @param round
   * @private
   */
  private findDrawRound(round: number): DrawRound {
    let drawRound: DrawRound = null;
    for (let i = 0; i < this.singleEliminationRounds.length; i++) {
      const singleEliminationRound = this.singleEliminationRounds[i];
      if (singleEliminationRound.round === round) {
        drawRound = singleEliminationRound;
        break;
      }
    }
    // not found - create one
    if (drawRound == null) {
      drawRound = new DrawRound();
      drawRound.round = round;
      this.singleEliminationRounds.push(drawRound);
    }
    return drawRound;
  }

  private transformToNgttTournament() {
    const roundNumbers: number [] = [];
    const rounds: NgttRound [] = [];
    if (this.singleEliminationRounds) {
      const drawRounds = this.singleEliminationRounds;
      for (let i = 0; i < drawRounds.length; i++) {
        const drawRound = drawRounds[i];
        roundNumbers.push(drawRound.round);
        const drawItems: DrawItem [] = drawRound.drawItems;
        const roundMatches = [];
        for (let j = 0; j < drawItems.length;) {
          const drawItemLeft: DrawItem = drawItems[j];
          const drawItemRight: DrawItem = drawItems[j + 1];
          const match: Match = new Match();
          match.opponentA = drawItemLeft;
          match.opponentB = drawItemRight;
          match.time = 10.5;
          match.tableNum = 6 + j;  // for now
          match.result = null;
          match.opponentAWon = false;
          match.showSeedNumber = (i === 0); // show seed number for first round only
          // only first round matches can be rearanged
          match.dragDisabled = (drawRound.round != roundNumbers[0])
            || (drawItemLeft.seSeedNumber === 1)
            || (drawItemRight.seSeedNumber === 2);

          roundMatches.push(match);
          j += 2;
        }
        const type = ((i + 1) === drawRounds.length) ? 'Final' : 'Winnerbracket';
        const round: NgttRound = {type: type, matches: roundMatches};
        rounds.push(round);
      }
    }

    let dropListDrawItems: DrawItem[] = [];
    if (rounds.length > 0) {
      this.tournament = {
        rounds: rounds
      };
      dropListDrawItems = this.makeDropList(rounds[0]);
    } else {
      this.tournament = null;
    }
    this.roundNumbers = roundNumbers;
    this.dropListData = {
      firstRound: roundNumbers[0],
      dropListDrawItems: dropListDrawItems
    };
  }

  /**
   *
   * @param drawRound
   * @param groupNum
   * @param placeInGroup
   * @private
   */
  private findDrawItem(drawRound: DrawRound, groupNum: number, placeInGroup: number): DrawItem {
    const drawItems: DrawItem[] = drawRound.drawItems;
    for (const drawItem of drawItems) {
      if (drawItem.groupNum === groupNum &&
        drawItem.placeInGroup === placeInGroup) {
        return drawItem;
      }
    }
    return null;
  }

  makeDropList(firstRound: NgttRound) {
    const matches: Match [] = firstRound.matches;
    let dropList: DrawItem[] = [];
    for (let i = 0; i < matches.length; i++) {
      const match = matches[i];
      if (match.opponentA.byeNum === 0) {
        dropList.push(match.opponentA);
      }
      if (match.opponentB.byeNum === 0) {
        dropList.push(match.opponentB);
      }
    }
    return dropList;
  }

  onDrawItemDrop(event: CdkDragDrop<DrawItem[]>) {
    // console.log('in onDrawItemDrop', event);
  }

  /**
   * Called to find out if an item can be moved
   * @param item item to be moved
   */
  canMoveDrawItem(item: CdkDrag<DrawItem>) {
    console.log('in canMoveDrawItem', item?.data);
    return item?.data.byeNum === 0;  // only non-bye items can be moved
  }

  /**
   * Prevents users from dropping in a different row - players can only be moved within a row.
   * We are taking advantage of sorting capability
   * @param index index where the item is dropped (0 based)
   * @param item item being dropped
   * @param cdkDropList data passed to drag and drop functionality
   */
  canDropPredicate(index: number, item: CdkDrag<DrawItem>, cdkDropList: CdkDropList) {
    const dropListData = cdkDropList.data;
    const topSeeds = item?.data.seSeedNumber <= 2;
    return item?.data.byeNum === 0 && dropListData.firstRound === item?.data.round;
  }
}
