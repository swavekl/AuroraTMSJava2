import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatSlideToggleChange} from '@angular/material/slide-toggle';

import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {DrawType} from '../../draws-common/model/draw-type.enum';
import {DrawItem} from '../../draws-common/model/draw-item.model';
import {DrawAction, DrawActionType} from './draw-action';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {TabbedDrawsPanelComponent} from '../../draws-common/tabbed-draws-panel/tabbed-draws-panel.component';
import {PlayerStatus} from '../../../today/model/player-status.model';
import {MatchCardInfo} from '../../../matches/model/match-card-info.model';

@Component({
  selector: 'app-draws',
  templateUrl: './draws.component.html',
  styleUrls: ['./draws.component.scss'],
})
export class DrawsComponent implements OnInit, OnChanges {

  @Input()
  tournamentEvents: TournamentEvent [] = [];

  @Input()
  draws: DrawItem [] = [];

  @Input()
  playerStatusList: PlayerStatus [] = [];

  @Input()
  matchCardInfos: MatchCardInfo [] = [];

  @Input()
  tournamentName: string;

  @Output()
  private drawsAction: EventEmitter<any> = new EventEmitter<any>();

  // currently selected event for viewing draws
  selectedEvent: TournamentEvent;

  // if true expanded information i.e. state, club of player
  expandedView: boolean;

  // if true show checkin status
  checkinStatus: boolean;

  // checks if there are any scores entered for the event to prevent any changes to the draw after results are entered
  allowDrawChanges: boolean;

  @ViewChild(TabbedDrawsPanelComponent)
  tabbedDrawsPanelComponent!: TabbedDrawsPanelComponent;

  constructor(private dialog: MatDialog) {
    this.expandedView = false;
    this.checkinStatus = false;
    this.allowDrawChanges = true;
  }

  ngOnInit(): void {
  }

  /**
   * Called when observables get their values
   * @param changes changes
   */
  ngOnChanges(changes: SimpleChanges): void {
  }

  /**
   * Loads draw for selected event
   * @param tournamentEvent selected event
   */
  onSelectEvent(tournamentEvent: TournamentEvent) {
    this.clearUndoStack();
    this.selectedEvent = tournamentEvent;
    this.allowDrawChanges = !tournamentEvent.matchScoresEntered;
    const drawType: DrawType = this.selectedEvent.singleElimination ? DrawType.SINGLE_ELIMINATION : DrawType.ROUND_ROBIN;
    const action: DrawAction = {
      actionType: DrawActionType.DRAW_ACTION_LOAD,
      eventId: this.selectedEvent.id,
      payload: {drawType: drawType, loadStatus: this.checkinStatus, tournamentDay: this.selectedEvent.day}
    };
    this.drawsAction.emit(action);
  }

  /**
   * Generates draw for currently selected event
   */
  generateDraw() {
    if (this.selectedEvent != null) {
      this.confirmDrawChanges(() => {
        this.generateDrawInternal();
        this.updateFlag();
      });
    }
  }

  private generateDrawInternal(): void {
    this.clearUndoStack();
    const drawType: DrawType = this.selectedEvent.singleElimination ? DrawType.SINGLE_ELIMINATION : DrawType.ROUND_ROBIN;
    const action: DrawAction = {
      actionType: DrawActionType.DRAW_ACTION_GENERATE,
      eventId: this.selectedEvent.id,
      payload: {drawType: drawType}
    };
    this.drawsAction.emit(action);
  }

  /**
   * Clears draw for selected event
   */
  clearDraw() {
    if (this.selectedEvent != null) {
      this.confirmDrawChanges(() => {
        this.clearDrawInternal();
        this.updateFlag();
      });
    }
  }

  clearDrawInternal() {
    if (this.selectedEvent != null) {
      this.clearUndoStack();
      const action: DrawAction = {
        actionType: DrawActionType.DRAW_ACTION_CLEAR,
        eventId: this.selectedEvent.id,
        payload: {}
      };
      this.drawsAction.emit(action);
    }
  }

  isSelected(tournamentEvent: TournamentEvent) {
    return tournamentEvent.id === this.selectedEvent?.id;
  }

  private confirmDrawChanges (callbackMethod: () => void) {
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
   * Clears the scores entered flag to allow regenerating or clearing draws again
   * @private
   */
  public updateFlag() {
    if (this.selectedEvent && !this.allowDrawChanges) {
      this.allowDrawChanges = true;
      const eventToUpdateId = this.selectedEvent.id;
      const updatedEvents = JSON.parse(JSON.stringify(this.tournamentEvents));
      updatedEvents.forEach((event: TournamentEvent) => {
        if (event.id === eventToUpdateId) {
          event.matchScoresEntered = false;
        }
      });
      this.tournamentEvents = updatedEvents;
    }
  }

  onRRDrawsAction(action: DrawAction) {
    this.drawsAction.emit(action);
  }

  clearUndoStack() {
    if (this.tabbedDrawsPanelComponent != null) {
      this.tabbedDrawsPanelComponent.clearUndoStack();
    }
  }

  hasUndoItems(): boolean {
    return (this.tabbedDrawsPanelComponent != null) ?
      this.tabbedDrawsPanelComponent.hasUndoItems() : false;
  }

  undoMove() {
    if (this.tabbedDrawsPanelComponent != null) {
      this.tabbedDrawsPanelComponent.undoMove();
    }
  }

  onExpandedViewChange($event: MatSlideToggleChange) {
    if (this.tabbedDrawsPanelComponent != null) {
      this.tabbedDrawsPanelComponent.setExpandedView(this.expandedView);
    }
  }

  onCheckinStatusChange($event: MatSlideToggleChange) {
    if (this.tabbedDrawsPanelComponent != null) {
      this.tabbedDrawsPanelComponent.setCheckinStatus(this.checkinStatus);
    }

    if (this.checkinStatus) {
      const drawType: DrawType = this.selectedEvent.singleElimination ? DrawType.SINGLE_ELIMINATION : DrawType.ROUND_ROBIN;
      const action: DrawAction = {
        actionType: DrawActionType.DRAW_ACTION_LOAD_STATUS,
        eventId: this.selectedEvent.id,
        payload: {drawType: drawType, loadStatus: this.checkinStatus, tournamentDay: this.selectedEvent.day}
      };
      this.drawsAction.emit(action);
    }
  }

  public onPrintMatchCards() {
    const drawType: DrawType = this.selectedEvent.singleElimination ? DrawType.SINGLE_ELIMINATION : DrawType.ROUND_ROBIN;
    const action: DrawAction = {
      actionType: DrawActionType.DRAW_ACTION_PRINT,
      eventId: this.selectedEvent.id,
      payload: {drawType: drawType}
    };
    this.drawsAction.emit(action);
  }
}
