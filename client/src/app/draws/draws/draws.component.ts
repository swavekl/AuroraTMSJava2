import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {DrawType} from '../model/draw-type.enum';
import {DrawItem} from '../model/draw-item.model';
import {DrawAction, DrawActionType} from './draw-action';
import {DrawGroup} from '../model/draw-group.model';

@Component({
  selector: 'app-draws',
  templateUrl: './draws.component.html',
  styleUrls: ['./draws.component.scss']
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

  constructor() {
    this.groups = [];
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
      console.log('DrawsComponent got tournament events of length ' + te.length);
    }
    const drawsChanges: SimpleChange = changes.draws;
    if (drawsChanges) {
      console.log('DrawsComponent got draws of length ' + drawsChanges.currentValue.length);
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
    }
  }

  /**
   * Loads draw for selected event
   * @param tournamentEvent selected event
   */
  onSelectEvent(tournamentEvent: TournamentEvent) {
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
}
