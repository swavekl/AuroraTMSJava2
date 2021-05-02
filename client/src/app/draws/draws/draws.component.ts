import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges, ViewEncapsulation} from '@angular/core';
import {CdkDrag, CdkDragDrop, CdkDropList, transferArrayItem} from '@angular/cdk/drag-drop';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {DrawType} from '../model/draw-type.enum';
import {DrawItem} from '../model/draw-item.model';
import {DrawGroup} from '../model/draw-group.model';
import {DrawAction, DrawActionType} from './draw-action';
import {UndoMemento} from '../model/undo-memento';

@Component({
  selector: 'app-draws',
  templateUrl: './draws.component.html',
  styleUrls: ['./draws.component.scss'],
  // // Need to remove view encapsulation so that the custom tooltip style defined in
  // // `tooltip-custom-class-example.css` will not be scoped to this component's view.
  // encapsulation: ViewEncapsulation.None,
})
export class DrawsComponent implements OnInit, OnChanges {

  @Input()
  tournamentEvents: TournamentEvent [] = [];

  @Input()
  draws: DrawItem [] = [];

  @Output()
  private drawsAction: EventEmitter<any> = new EventEmitter<any>();

  // currently selected event for viewing draws
  selectedEvent: TournamentEvent;

  // array of group objects with group
  groups: DrawGroup [] = [];

  // if true expanded information i.e. state, club of player
  expandedView: boolean;

  // information about moved items
  undoStack: UndoMemento [] = [];

  constructor() {
    this.groups = [];
    this.expandedView = false;
  }

  ngOnInit(): void {
  }

  /**
   * Called when observables get their values
   * @param changes changes
   */
  ngOnChanges(changes: SimpleChanges): void {
    const tournamentEventsChanges: SimpleChange = changes.tournamentEvents;
    if (tournamentEventsChanges) {
      const te = tournamentEventsChanges.currentValue;
      // console.log('DrawsComponent got tournament events of length ' + te.length);
    }
    const drawsChanges: SimpleChange = changes.draws;
    if (drawsChanges) {
      // console.log('DrawsComponent got draws of length ' + drawsChanges.currentValue.length);
      const drawItems: DrawItem[] = drawsChanges.currentValue;
      let groupNum = 0;
      let currentGroup: DrawGroup = null;
      this.groups = [];
      drawItems.forEach((drawItem: DrawItem) => {
        if (drawItem.groupNum !== groupNum) {
          groupNum = drawItem.groupNum;
          currentGroup = new DrawGroup();
          currentGroup.groupNum = drawItem.groupNum;
          currentGroup.drawItems = [];
          this.groups.push(currentGroup);
        }
        currentGroup.drawItems.push(drawItem);
      });

      this.setupGroupsForDragAndDrop();
    }
  }

  /**
   * Loads draw for selected event
   * @param tournamentEvent selected event
   */
  onSelectEvent(tournamentEvent: TournamentEvent) {
    this.undoStack = [];
    this.selectedEvent = tournamentEvent;
    const action: DrawAction = {
      actionType: DrawActionType.DRAW_ACTION_LOAD,
      eventId: this.selectedEvent.id,
      payload: {}
    };
    this.drawsAction.emit(action);
  }

  /**
   * Generates draw for currently selected event
   */
  generateDraw() {
    this.undoStack = [];
    const action: DrawAction = {
      actionType: DrawActionType.DRAW_ACTION_GENERATE,
      eventId: this.selectedEvent.id,
      payload: {drawType: DrawType.ROUND_ROBIN}
    };
    this.drawsAction.emit(action);
  }

  /**
   * Clears draw for selected event
   */
  clearDraw() {
    this.undoStack = [];
    const action: DrawAction = {
      actionType: DrawActionType.DRAW_ACTION_CLEAR,
      eventId: this.selectedEvent.id,
      payload: {}
    };
    this.drawsAction.emit(action);
  }

  isSelected(tournamentEvent: TournamentEvent) {
    return tournamentEvent.id === this.selectedEvent?.id;
  }

  showPlayoffDraw() {
    console.log('show playoff draw for event ' + this.selectedEvent.id);
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
              conflicts: null,
              rating: -1,
              playerName: ' ',
              state: ' ',
              clubName: ' '
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
      // save this move on the undo stack so
      const undoMemento: UndoMemento = {
        toGroupItems: event.previousContainer.data,
        fromGroupItems: event.container.data,
        rowIndex: event.previousIndex
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
    }
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
    }
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
   * Returns draw item tooltip
   * @param drawItem draw item
   */
  getTooltipText(drawItem: DrawItem): string {
    return (this.expandedView) ? ''
      : (drawItem.state ? drawItem.state : 'N/A') + ', ' + (drawItem.clubName ? drawItem.clubName : 'N/A');
  }

}
