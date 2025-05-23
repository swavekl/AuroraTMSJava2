import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {CdkDrag, CdkDragDrop, CdkDropList, transferArrayItem} from '@angular/cdk/drag-drop';
import {MatDialog} from '@angular/material/dialog';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {DrawGroup} from '../model/draw-group.model';
import {DrawItem} from '../model/draw-item.model';
import {DrawType} from '../model/draw-type.enum';
import {UndoMemento} from '../model/undo-memento';
import {DrawAction, DrawActionType} from '../../draws-config/draws/draw-action';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {ConflictType} from '../model/conflict-type.enum';
import {ConflictRendererHelper} from '../model/conflict-renderer-helper.model';
import {EventStatusCode} from '../../../today/model/event-status-code.enum';
import {PlayerStatus} from '../../../today/model/player-status.model';
import {PlayerStatusPipe} from '../../../today/pipe/player-status.pipe';
import {MatchCardInfo} from '../../../matches/model/match-card-info.model';

@Component({
  selector: 'app-round-robin-draws-panel',
  templateUrl: './round-robin-draws-panel.component.html',
  styleUrls: ['./round-robin-draws-panel.component.scss']
})
export class RoundRobinDrawsPanelComponent implements OnChanges {

  // items for this event
  @Input()
  draws: DrawItem [] = [];

  // player check in status
  @Input()
  playerStatusList: PlayerStatus [] = [];

  @Input()
  matchCardInfos: MatchCardInfo [] = [];

  // checks if there are any scores entered for the event to prevent any changes to the draw after results are entered
  @Input()
  allowDrawChanges: boolean;

  // if true allow editing draws and drag and drop
  @Input()
  editMode: boolean = true;

  // currently selected event for viewing draws
  @Input()
  selectedEvent: TournamentEvent;

  @Output()
  private drawsAction: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  private updateFlagEE: EventEmitter<number> = new EventEmitter<number>();

  // height of the panel
  @Input()
  bracketsHeight!: string;

  // array of group objects with group
  groups: DrawGroup [] = [];

  // if true expanded information i.e. state, club of player
  expandedView: boolean;

  // if true show checkin status
  checkinStatus: boolean;

  // information about moved items
  undoStack: UndoMemento [] = [];

  constructor(private dialog: MatDialog) {
    this.groups = [];
    this.expandedView = false;
    this.checkinStatus = false;
    this.allowDrawChanges = true;
    this.undoStack = [];
  }

  ngOnInit(): void {
  }


  ngOnChanges(changes: SimpleChanges): void {
    // const selectedEventChange: SimpleChange = changes.selectedEvent;
    // if (selectedEventChange != null && selectedEventChange.currentValue != null) {
    //   console.log('clearing undo stack');
    //   this.undoStack = [];
    // }

    if (this.draws != null && this.selectedEvent != null) {
      this.initializeGroups(this.draws);
      this.setupGroupsForDragAndDrop();
    }
  }

  private initializeGroups(drawItems: DrawItem[]) {
    let groupNum = 0;
    let currentGroup: DrawGroup = null;
    let groups: DrawGroup [] = [];
    drawItems.forEach((drawItem: DrawItem) => {
      if (drawItem.drawType === DrawType.ROUND_ROBIN) {
        if (drawItem.groupNum !== groupNum) {
          groupNum = drawItem.groupNum;
          currentGroup = new DrawGroup();
          currentGroup.groupNum = drawItem.groupNum;
          currentGroup.drawItems = [];
          groups.push(currentGroup);
        }
        currentGroup.drawItems.push(drawItem);
      }
    });
    this.groups = groups;
  }

  /**
   * Sets up connectedTo array of tables
   */
  setupGroupsForDragAndDrop() {
    // we need to fill the empty rows in group tables with
    // some fake draw items so we can move horizontally and drop on them
    // what is the max number of rows
    if (this.selectedEvent) {
      const playersPerGroup = this.selectedEvent.playersPerGroup;
      this.groups.forEach((drawGroup: DrawGroup) => {
        // if group is not showing one seeded player and it has fewer than players per group
        // then add fake draw items
        if (drawGroup.drawItems.length < playersPerGroup && drawGroup.drawItems.length !== 1) {
          const startItemIndex = drawGroup.drawItems.length;
          for (let i = startItemIndex; i < playersPerGroup; i++) {
            const fakeDrawItem: DrawItem = {
              id: -1,
              eventFk: this.selectedEvent.id,
              groupNum: drawGroup.groupNum,
              placeInGroup: i,
              drawType: DrawType.ROUND_ROBIN,
              playerId: 'N/A',
              conflictType: ConflictType.NO_CONFLICT,
              rating: -1,
              playerName: ' ',
              state: ' ',
              clubName: ' ',
              byeNum: 0,
              round: 0,
              seSeedNumber: 0,
              singleElimLineNum: 0,
              entryId: 0
            };
            drawGroup.drawItems.push(fakeDrawItem);
          }
        }
      });
    }
  }

  /**
   * Drag and Drop functionality
   * @param event drop event
   */
  onDrawItemDrop(event: CdkDragDrop<DrawItem[]>) {
    // only allow movement between different groups and in the same row
    if (event.previousContainer !== event.container &&
      event.previousIndex === event.currentIndex) {
      // make sure they know if scores are entered that they will be wiped out
      this.confirmDrawChanges(() => {
        this.onDrawItemDropInternal(event);
        this.updateFlag();
      });
    }
  }

  private confirmDrawChanges(callbackMethod: () => void) {
    const config = {
      width: '450px', height: '230px', data: {
        message: `This event already has entered scores. Are you sure you want to modify this event draws and lose all entered scores?`,
      }
    };
    if (!this.allowDrawChanges) {
      const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
      dialogRef.afterClosed().subscribe(result => {
        if (result === 'ok') {
          callbackMethod();
        }
      });
    } else {
      callbackMethod();
    }
  }

  /**
   * Internal method
   * @param event
   */
  onDrawItemDropInternal(event: CdkDragDrop<DrawItem[]>) {
    const changedGroupNum1 = event.previousContainer.data[0].groupNum;
    const changedGroupNum2 = event.container.data[0].groupNum;
    // save this move on the undo stack so
    const undoMemento: UndoMemento = {
      toGroupItems: event.previousContainer.data,
      fromGroupItems: event.container.data,
      rowIndex: event.previousIndex,
      changedGroupNum1: changedGroupNum1,
      changedGroupNum2: changedGroupNum2
    };
    this.undoStack.push(undoMemento);

    // exchange items
    // move into new group
    transferArrayItem(event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex);
    // move the other item out of new group - since item previously was pushed down + 1
    transferArrayItem(event.container.data,
      event.previousContainer.data,
      event.currentIndex + 1,
      event.previousIndex);

    this.updateDrawItems(event.currentIndex, changedGroupNum1, changedGroupNum2);
  }

  /**
   * Fixes group numbers after move
   * @param rowIndex row position that changed
   * @param groupNum1 group that changed
   * @param groupNum2 another group that changed
   * @private
   */
  private updateDrawItems(rowIndex: number, groupNum1: number, groupNum2: number) {
    // collect changed draw items
    const movedDrawItems: DrawItem [] = [];
    this.groups.forEach((drawGroup: DrawGroup) => {
      const groupNum = drawGroup.groupNum;
      // if this is one of the changed groups
      if (groupNum === groupNum1 || groupNum === groupNum2) {
        drawGroup.drawItems.forEach((drawItem: DrawItem, index: number) => {
          // find the moved item that is not a fake (empty) that has wrong group after move
          if (index === rowIndex && drawItem.id !== -1) {
              const newGroupNum = (drawItem.groupNum === groupNum1) ? groupNum2 : groupNum1;
              // console.log('groupNum: ' + groupNum +' newGroupNum: ' + newGroupNum);
            const movedItem = {...drawItem, groupNum: newGroupNum};
            movedDrawItems.push(movedItem);
          }
        });
      }
    });

    // emit update event to update the server
    const action: DrawAction = {
      actionType: DrawActionType.DRAW_ACTION_UPDATE,
      eventId: this.selectedEvent.id,
      payload: {movedDrawItems: movedDrawItems, drawType: DrawType.ROUND_ROBIN}
    };
    this.drawsAction.emit(action);
  }

  /**
   * Called to find out if an item can be moved
   * @param item item to be moved
   */
  canMoveDrawItem(item: CdkDrag<DrawItem>) {
    // only non-top seeds can be moved
    return item.data?.placeInGroup > 1;
  }

  /**
   * Prevents users from dropping in a different row - players can only be moved within a row.
   * We are taking advantage of sorting capability
   * @param index index where the item is dropped (0 based)
   * @param item item being dropped
   * @param drop drop list onto which the item is being dropped
   */
  canDropPredicate(index: number, item: CdkDrag<DrawItem>, drop: CdkDropList) {
    return (index + 1) === item?.data.placeInGroup;
  }

  /**
   * Clears the scores entered flag to allow regenerating or clearing draws again
   * @private
   */
  private updateFlag() {
    if (this.selectedEvent && !this.allowDrawChanges) {
      this.updateFlagEE.emit(this.selectedEvent.id);
    }
  }

  /**
   * Returns draw item tooltip
   * @param drawItem draw item
   */
  getTooltipText(drawItem: DrawItem): string {
    if (this.editMode) {
      return (this.expandedView) ? ''
        : (drawItem.state ? drawItem.state : 'N/A') + ', ' + (drawItem.clubName ? drawItem.clubName : 'N/A');
    } else {
      return '';
    }
  }

  hasUndoItems(): boolean {
    // console.log('this.undoStack?.length', this.undoStack?.length);
    return this.undoStack?.length > 0;
  }

  /**
   * Undo the player move
   */
  undoMove() {
    if (this.undoStack?.length > 0) {
      const undoMemento: UndoMemento = this.undoStack[this.undoStack.length - 1];
      this.undoStack.splice(this.undoStack.length - 1, 1);

      // exchange items
      // move into new group
      transferArrayItem(undoMemento.fromGroupItems,
        undoMemento.toGroupItems,
        undoMemento.rowIndex,
        undoMemento.rowIndex);
      // move the other item out of new group - since item previously was pushed down + 1
      transferArrayItem(undoMemento.toGroupItems,
        undoMemento.fromGroupItems,
        undoMemento.rowIndex + 1,
        undoMemento.rowIndex);

      this.updateDrawItems(undoMemento.rowIndex, undoMemento.changedGroupNum1, undoMemento.changedGroupNum2);
    }
  }

  setExpandedView(expandedView: boolean) {
    this.expandedView = expandedView;
  }

  setCheckinStatus(checkinStatus: boolean) {
    this.checkinStatus = checkinStatus;
  }

  clearUndoStack() {
    this.undoStack = [];
  }

  getConflictClass(drawItem: DrawItem) {
    return (this.editMode) ? ConflictRendererHelper.getConflictClass(drawItem.conflictType) : "";
  }

  getConflictTooltipText(drawItem: DrawItem) {
    return (this.editMode) ? ConflictRendererHelper.getConflictTooltipText(drawItem.conflictType) : "";
  }

  getPlayerStatusCode(playerId: string, playerIndex: number): EventStatusCode {
    const playerStatusObject: PlayerStatus = this.getPlayerStatus(playerId, playerIndex);
    return (playerStatusObject != null) ? playerStatusObject.eventStatusCode : null;
  }

  getPlayerStatusReason(playerId: string, playerIndex: number): string {
    let reason = '';
    const playerStatusObject: PlayerStatus = this.getPlayerStatus(playerId, playerIndex);
    if (playerStatusObject != null) {
      switch (playerStatusObject.eventStatusCode) {
        case EventStatusCode.WILL_NOT_PLAY:
          reason = new PlayerStatusPipe().transform(playerStatusObject.eventStatusCode, playerStatusObject.estimatedArrivalTime);
          reason += '. ' + playerStatusObject.reason;
          break;
        case EventStatusCode.WILL_PLAY_BUT_IS_LATE:
          reason = new PlayerStatusPipe().transform(playerStatusObject.eventStatusCode, playerStatusObject.estimatedArrivalTime);
          break;
      }
    }
    return reason;
  }

  private getPlayerStatus(playerId: string, playerIndex: number): PlayerStatus {
    if (this.playerStatusList?.length > 0) {
      const foundPlayerStatus: PlayerStatus[] = this.playerStatusList.filter((playerStatus: PlayerStatus) => {
        if (this.selectedEvent?.doubles) {
          let playerIds = playerId.split(';');
          const doublesPlayerId = playerIds[playerIndex];
          return playerStatus.playerProfileId === doublesPlayerId;
        } else {
          return playerStatus.playerProfileId === playerId;
        }
      });
      return (foundPlayerStatus.length > 0) ? foundPlayerStatus[0] : null;
    } else {
      return null;
    }
  }

  getStartTime(groupNum: number): number {
    let startTime = this.selectedEvent?.startTime;
    if (this.matchCardInfos) {
      const filteredMC : MatchCardInfo [] = this.matchCardInfos.filter((matchCardInfo: MatchCardInfo) => {
        return (matchCardInfo.groupNum == groupNum && matchCardInfo.drawType === DrawType.ROUND_ROBIN);
      });
      if (filteredMC?.length > 0 && filteredMC[0].startTime != null) {
        startTime = filteredMC[0].startTime;
      }
    }
    return startTime;
  }

  getAssignedTables(groupNum: number): string {
    let assignedTables = '';
    let multipleTables = true;
    if (this.matchCardInfos) {
      const filteredMC : MatchCardInfo [] = this.matchCardInfos.filter((matchCardInfo: MatchCardInfo) => {
        return (matchCardInfo.groupNum == groupNum && matchCardInfo.drawType === DrawType.ROUND_ROBIN);
      });
      if (filteredMC?.length > 0) {
        assignedTables = filteredMC[0].assignedTables;
        assignedTables = (assignedTables != null) ? assignedTables : '';
        multipleTables = assignedTables.indexOf(',') > 0;
      }
    }
    const tablesStr = (multipleTables) ? 'Tables' : 'Table'
    return assignedTables != '' ? `${tablesStr}: ${assignedTables}` : '';
  }
}

