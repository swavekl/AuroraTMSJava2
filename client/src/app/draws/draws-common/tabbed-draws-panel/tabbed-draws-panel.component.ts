import {Component, EventEmitter, Input, Output, SimpleChange, SimpleChanges, ViewChild} from '@angular/core';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {DrawItem} from '../model/draw-item.model';
import {RoundRobinDrawsPanelComponent} from '../round-robin-draws-panel/round-robin-draws-panel.component';
import {Observable} from 'rxjs';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {map} from 'rxjs/operators';

@Component({
  selector: 'app-tabbed-draws-panel',
  templateUrl: './tabbed-draws-panel.component.html',
  styleUrls: ['./tabbed-draws-panel.component.scss']
})
export class TabbedDrawsPanelComponent {
  @Input()
  editMode: boolean = true;

  @Input()
  allowDrawChanges: boolean = true;

  @Input()
  selectedEvent: TournamentEvent;

  @Input()
  draws: DrawItem [] = [];

  @Output()
  private drawsAction: EventEmitter<any> = new EventEmitter<any>;

  @Output()
  private updateFlagEE: EventEmitter<number> = new EventEmitter<number>();

  @ViewChild(RoundRobinDrawsPanelComponent)
  private roundRobinDrawsPanelComponent: RoundRobinDrawsPanelComponent;

  hasRRRound: boolean = false;
  hasSERound: boolean = false;

  isHandset$: Observable<boolean> = null;

  constructor(private breakpointObserver: BreakpointObserver) {
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
      this.hasRRRound = false;
      this.hasSERound = false;
      const selectedEvent: TournamentEvent = selectedEventChanges.currentValue;
      this.hasRRRound = !selectedEvent.singleElimination;
      this.hasSERound = (selectedEvent.singleElimination ||
        (!selectedEvent.singleElimination && selectedEvent.playersToAdvance > 0));
    }
  }

  ngOnInit(): void {
  }

  onRRDrawsAction($event: any) {
    // propagate event
    this.drawsAction.emit($event);
  }

  showTwoRounds () {
    return this.hasRRRound && this.hasSERound;
  }

  getBracketsHeight(): string {
    const toolbarHeight = (this.editMode) ? 64 : 0;
    const tabsHeight = this.showTwoRounds() ? 48 : 0;
    // const diff = (this.editMode) ? 224 : 160;
    const diff = 112 + toolbarHeight + tabsHeight;
    const strHeight = (window.innerHeight - diff) + 'px';
    // console.log('editMode ' + this.editMode + ' -> strHeight' + strHeight);
    return strHeight;
  }

  onUpdateEEFlag($event) {
    this.updateFlagEE.emit($event);
  }

  clearUndoStack() {
    if (this.roundRobinDrawsPanelComponent != null) {
      this.roundRobinDrawsPanelComponent.clearUndoStack();
    }
  }

  hasUndoItems() {
    if (this.roundRobinDrawsPanelComponent != null) {
      return this.roundRobinDrawsPanelComponent.hasUndoItems();
    } else {
      return false;
    }
  }

  undoMove() {
    if (this.roundRobinDrawsPanelComponent != null) {
      this.roundRobinDrawsPanelComponent.undoMove();
    }
  }

  setExpandedView(expandedView: boolean) {
    if (this.roundRobinDrawsPanelComponent != null) {
      this.roundRobinDrawsPanelComponent.setExpandedView(expandedView);
    }
  }
}
