import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnChanges, OnInit,
  Output,
  QueryList,
  SimpleChange,
  SimpleChanges,
  ViewChild,
  ViewChildren
} from '@angular/core';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {DrawItem} from '../model/draw-item.model';
import {RoundRobinDrawsPanelComponent} from '../round-robin-draws-panel/round-robin-draws-panel.component';
import {Observable} from 'rxjs';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {map} from 'rxjs/operators';
import {PlayerStatus} from '../../../today/model/player-status.model';
import {MatchCardInfo} from '../../../matches/model/match-card-info.model';
import {DrawType} from '../model/draw-type.enum';
import {UndoablePanel} from '../undoable-panel';
import {MatTabChangeEvent, MatTabGroup} from '@angular/material/tabs';
import {MatSlideToggleChange} from '@angular/material/slide-toggle';
import {DrawAction, DrawActionType} from '../../draws-config/draws/draw-action';
import {MatDialog} from '@angular/material/dialog';
import {DrawUndoService} from '../draw-undo.service';

@Component({
    selector: 'app-tabbed-draws-panel',
    templateUrl: './tabbed-draws-panel.component.html',
    styleUrls: ['./tabbed-draws-panel.component.scss'],
    standalone: false
})
export class TabbedDrawsPanelComponent implements OnChanges, OnInit, AfterViewInit {
  @Input()
  editMode: boolean = true;

  @Input()
  allowDrawChanges: boolean = true;

  @Input()
  selectedEvent: TournamentEvent;

  @Input()
  draws: DrawItem [] = [];

  @Input()
  playerStatusList: PlayerStatus [] = [];

  @Input()
  matchCardInfos: MatchCardInfo [] = [];

  @Output()
  private drawsAction: EventEmitter<any> = new EventEmitter<any>;

  @Output()
  private updateFlagEE: EventEmitter<number> = new EventEmitter<number>();

  @ViewChild(RoundRobinDrawsPanelComponent)
  private roundRobinDrawsPanelComponent: RoundRobinDrawsPanelComponent;

  hasRRRound: boolean = false;
  hasSERound: boolean = false;

  isHandset$: Observable<boolean> = null;

  @ViewChildren('drawPanel')
  drawPanels!: QueryList<UndoablePanel>;

  // maintain current tab index so that it can be reset when we change events
  // this is to maintain the correct undo stack when switching between events
  selectedTabIndex = 0;

  // more efficient than using *ngIf to show/hide tabs
  showTabs = false;

  constructor(private breakpointObserver: BreakpointObserver,
              protected drawUndoService: DrawUndoService) {
    this.isHandset$ = this.breakpointObserver.observe(Breakpoints.Handset)
      .pipe(
        map(result => {
          return result.matches;
        })
      );
  }

  ngOnChanges(changes: SimpleChanges): void {
    const selectedEventChanges: SimpleChange = changes.selectedEvent;
    if (selectedEventChanges != null && selectedEventChanges.currentValue != null) {
      const selectedEvent: TournamentEvent = selectedEventChanges.currentValue;
      const rounds = selectedEvent.roundsConfiguration?.rounds || [];
      const len = rounds.length ?? 0;
      this.showTabs = len > 1;
      if (rounds && rounds.length > 0) {
        let hasRRRound = false;
        let hasSERound = false;
        for (const round of rounds) {
          hasRRRound = !round.singleElimination || hasRRRound;
          hasSERound = round.singleElimination || hasSERound;
        }
        this.hasRRRound = hasRRRound;
        this.hasSERound = hasSERound;
      } else {
        this.hasRRRound = !selectedEvent.singleElimination;
        this.hasSERound = (selectedEvent.singleElimination ||
          (!selectedEvent.singleElimination && selectedEvent.playersToAdvance > 0));
      }
      // console.log('this.hasRRRound', this.hasRRRound);
      // console.log('this.hasSERound', this.hasSERound);
    }
    this.onTabChange(this.selectedTabIndex);
  }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.drawPanels.changes.subscribe(() => {
      this.onTabChange(this.selectedTabIndex);
    });

    this.onTabChange(this.selectedTabIndex);
  }

  // this is to prevent tearing down the tabbed panel when switching between events
  trackByRound(_index: number, round: any): number {
    return round.ordinalNum; // stable per event (1, 2, 3...)
  }

  onDrawsAction($event: any) {
    // propagate event
    this.drawsAction.emit($event);
  }

  showTwoRounds () {
    return this.hasRRRound && this.hasSERound;
  }

  getBracketsHeight(round: string): string {
    const toolbarHeight = (this.editMode) ? 64 : 0;
    const tabsHeight = this.showTwoRounds() ? 48 : 0;
    const seHeaderHeight = round === 'se' ? 42 : 0;
    const diff = 112 + toolbarHeight + tabsHeight + seHeaderHeight;
    const strHeight = (window.innerHeight - diff) + 'px';
    // console.log('round ' + round + ' editMode ' + this.editMode + ' -> strHeight ' + strHeight);
    return strHeight;
  }

  onUpdateEEFlag($event) {
    this.updateFlagEE.emit($event);
  }

  // clearUndoStack() {
  //   if (this.roundRobinDrawsPanelComponent != null) {
  //     this.roundRobinDrawsPanelComponent.clearUndoStack();
  //   }
  // }
  //
  // hasUndoItems() {
  //   if (this.roundRobinDrawsPanelComponent != null) {
  //     return this.roundRobinDrawsPanelComponent.hasUndoItems();
  //   } else {
  //     return false;
  //   }
  // }
  //
  // undoMove() {
  //   if (this.roundRobinDrawsPanelComponent != null) {
  //     this.roundRobinDrawsPanelComponent.undoMove();
  //   }
  // }


  setExpandedView(expandedView: boolean) {
    if (this.roundRobinDrawsPanelComponent != null) {
      this.roundRobinDrawsPanelComponent.setExpandedView(expandedView);
    }
  }

  setCheckinStatus(checkinStatus: boolean) {
    if (this.roundRobinDrawsPanelComponent != null) {
      this.roundRobinDrawsPanelComponent.setCheckinStatus(checkinStatus);
    }
  }

  protected readonly DrawType = DrawType;

  onTabChange(currentTab: number) {
    // console.log('onTabChange this.selectedTabIndex', this.selectedTabIndex);
    const panels = (this.drawPanels) ? this.drawPanels.toArray() : [];
    // console.log('onTabChange currentTab ' + currentTab + ' panels ' + panels.length);
    panels.forEach((panel, index) => {
      const isActive = (index === currentTab);
      panel.setActive(isActive);
      if (isActive) {
        panel.broadcastState();
      }
    });
  }
}
