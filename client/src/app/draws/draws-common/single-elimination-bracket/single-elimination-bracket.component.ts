import {
  Component,
  EventEmitter,
  Input,
  OnChanges, OnDestroy,
  OnInit, Optional,
  Output,
  SimpleChange,
  SimpleChanges,
  TemplateRef,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {DrawRound, SEDrawDivision, SERound} from '../model/draw-round.model';
import {DrawItem} from '../model/draw-item.model';
import {Match} from '../model/match.model';
import {DrawType} from '../model/draw-type.enum';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {ConflictType} from '../model/conflict-type.enum';
import {CdkDragDrop} from '@angular/cdk/drag-drop';
import {PlayerStatus} from '../../../today/model/player-status.model';
import {MatchCardInfo} from '../../../matches/model/match-card-info.model';
import {DrawAction, DrawActionType} from '../../draws-config/draws/draw-action';
import {SeUndoMemento} from '../model/undo-memento';
import {UndoablePanel} from '../undoable-panel';
import {DrawUndoService} from '../draw-undo.service';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';

@Component({
    selector: 'app-single-elimination-bracket',
    templateUrl: './single-elimination-bracket.component.html',
    styleUrls: ['./single-elimination-bracket.component.scss'],
    standalone: false,
    encapsulation: ViewEncapsulation.None // This allows styles to hit the global <body>
})
export class SingleEliminationBracketComponent implements OnInit, OnDestroy, OnChanges, UndoablePanel {

  @Input()
  selectedEvent: TournamentEvent;

  @Input()
  draws: DrawItem [] = [];

  @Input()
  roundOrdinalNumber: number = 1;

  // player check in status
  @Input()
  playerStatusList: PlayerStatus [] = [];

  @Input()
  matchCardInfos: MatchCardInfo [] = [];

  @Input()
  bracketsHeight: string;

  @Input()
  doublesEvent: boolean;

  // single elimination divisions
  divisions: SEDrawDivision [] = [];

  // round numbers
  @Input()
  roundNumbers: number [] = [];

  // checks if there are any scores entered for the event to prevent any changes to the draw after results are entered
  @Input()
  allowDrawChanges: boolean;

  // if true allow editing draws and drag and drop
  @Input()
  editMode: boolean = true;

  @Output()
  private drawsAction: EventEmitter<any> = new EventEmitter<any>();

  // to be determined player profile id same as matches/match.model.ts
  public readonly TBD_PROFILE_ID = 'TBD';

  // dimensions of the bracket
  roundSpacing: number = 300; // Distance between the start of each round
  matchWidth: number = 240;   // Width of the match card itself
  padding: number = 40;       // Extra room for the final winner's line if needed
  matchHeight: number = 80;
  matchGap: number = 40;

  // Capture the template defined in HTML
  @ViewChild('dragPreview', { static: true })
  dragPreview: TemplateRef<any>;

  activeMatchLayout: any = null;

  // information about moved items
  undoStack: SeUndoMemento [] = [];

  private destroy$ = new Subject<void>();
  private _isActive: boolean;

  constructor(@Optional() private drawUndoService: DrawUndoService) {
    this.undoStack = [];
    this._isActive = false;
  }

  ngOnInit(): void {
    // Listen for the Undo click
    this.drawUndoService.undoAction$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (this.isActive()) {
          this.undoMove();
          this.broadcastState();
        }
      });
  }

  ngOnDestroy() {
    // 3. Emit a value to complete all subscriptions
    this.destroy$.next();
    // 4. Clean up the subject itself
    this.destroy$.complete();

    // 5. Optional: Clear the undo button if this was the last active panel
    this.drawUndoService.updateCanUndo(false);
  }


  // Helper to push the local state up to the service
  broadcastState() {
    // Promise.resolve().then() ensures this happens in the next tick,
    // preventing the ExpressionChanged error.
    Promise.resolve().then(() => {
      // console.log('SE broadcasting undo state:');
      this.drawUndoService.updateCanUndo(this.undoStack.length > 0);
    });
  }

  // When the user switches to this tab
  setActive(val: boolean) {
    // console.log('SE setActive:', val);
    this._isActive = val;
  }

  isActive(): boolean {
    // console.log('SE is active:', this._isActive);
    return this._isActive;
  }

  ngOnChanges(changes: SimpleChanges): void {
    // make tournament and rounds from draws (as in draw edit or view)
    // get them already prepared in tournament and roundNumbers (in results page)
    const drawsChanges: SimpleChange = changes.draws;
    if (drawsChanges && drawsChanges.currentValue != null) {
      // console.log('DrawsComponent got draws of length ' + drawsChanges.currentValue.length);
      const drawItems: DrawItem[] = drawsChanges.currentValue;
      if (drawItems.length > 0) {
        this.divisions = this.extractSingleEliminationDrawItems(drawItems);
      } else {
        this.divisions = [];
      }

      this.finishRemainingSERounds();

      this.transformToBracketMatchData();
    }

    const matchCardsInfosSC: SimpleChange = changes.matchCardInfos;
    if (this.divisions.length > 0 && matchCardsInfosSC && matchCardsInfosSC.currentValue != null) {
      this.transformToBracketMatchData();
    }
  }

  /**
   * Extracts and organizes draw items belonging to a single elimination format based on their division and rounds.
   *
   * This method filters the provided draw items to include only those that are relevant to the current round and configured
   * for single elimination. It organizes these items into divisions and further groups them by rounds within each division.
   *
   * @param {DrawItem[]} drawItems - An array of draw items to be processed, where each item contains information about its division, round, and type.
   * @return {SEDrawDivision[]} An array of single elimination draw divisions, where each division contains its respective rounds and associated draw items.
   */
  private extractSingleEliminationDrawItems(drawItems: DrawItem[]) : SEDrawDivision[] {
    const divisions: SEDrawDivision[] = [];

    const relevantItems = drawItems.filter((drawItem: DrawItem) =>
      drawItem.drawType === DrawType.SINGLE_ELIMINATION &&
      this.roundOrdinalNumber === drawItem.roundOrdinalNumber);

    // 2. Group by Division Index
    const divisionMap = new Map<number, DrawItem[]>();
    relevantItems.forEach(item => {
      const list = divisionMap.get(item.divisionIdx) || [];
      list.push(item);
      divisionMap.set(item.divisionIdx, list);
    });

    // 3. Process each Division
    divisionMap.forEach((itemsInDivision, divIdx) => {
      const foundDivision = this.selectedEvent?.roundsConfiguration?.rounds
        ?.find(r => r.ordinalNum === this.roundOrdinalNumber)
        ?.divisions?.find(d => d.divisionIdx === divIdx);

      const currentDivision = new SEDrawDivision();
      currentDivision.divisionIdx = divIdx;
      currentDivision.divisionName = foundDivision?.divisionName ?? `Division ${divIdx}`;

      // 4. Group items WITHIN this division by round
      const roundMap = new Map<number, DrawItem[]>();
      itemsInDivision.forEach(item => {
        const list = roundMap.get(item.round) || [];
        list.push(item);
        roundMap.set(item.round, list);
      });

      // Convert group map to DrawRound objects
      currentDivision.singleEliminationRounds = Array.from(roundMap.entries())
        .map(([round, items]) => {
          const drawRound = new DrawRound();
          drawRound.round = round;
          drawRound.drawItems = items;
          return drawRound;
        });
        // .sort((a, b) => a.groupNum - b.groupNum); // Ensure groups stay in order

      divisions.push(currentDivision);
    });
    return divisions;
  }

  /**
   *
   * @private
   */
  private finishRemainingSERounds() {
    if (this.selectedEvent && this.divisions.length > 0) {
      const eventFK = this.selectedEvent.id;
      this.divisions.forEach((division: SEDrawDivision) => {
        if (division.singleEliminationRounds.length > 0) {
          // find the first round of single elimination e.g. round of 32 or 16 etc.
          let firstRound: DrawRound = null;
          let firstRoundOf = 0;
          for (let i = 0; i < division.singleEliminationRounds.length; i++) {
            const singleEliminationRound = division.singleEliminationRounds[i];
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
            const drawRound: DrawRound = this.findDrawRound(thisRoundOf, division.divisionIdx);
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
                  byeNum: 0, round: drawRound.round, seSeedNumber: 0, singleElimLineNum: 0, entryId: 0, doublesPairId: 0, teamFk: 0, teamName: ' ',
                  roundOrdinalNumber: this.roundOrdinalNumber, divisionIdx: 0
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
                    byeNum: 0, round: 2, seSeedNumber: 0, singleElimLineNum: 0, entryId: 0, doublesPairId: 0, teamFk: 0, teamName: ' ',
                    roundOrdinalNumber: this.roundOrdinalNumber, divisionIdx: 0
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
          division.singleEliminationRounds.sort((round1: DrawRound, round2: DrawRound) => {
            return (round1.round === round2.round) ? 0 : ((round1.round < round2.round) ? 1 : -1);
          });
        }
      });
    }
  }

  /**
   *
   * @param round
   * @param divisionIdx
   * @private
   */
  private findDrawRound(round: number, divisionIdx: number): DrawRound {
    let drawRound: DrawRound = null;
    this.divisions.forEach((division: SEDrawDivision, divIdx: number) => {
      if (divisionIdx === divIdx) {
        const singleEliminationRounds = division.singleEliminationRounds;
        for (let i = 0; i < singleEliminationRounds.length; i++) {
          const singleEliminationRound = singleEliminationRounds[i];
          if (singleEliminationRound.round === round) {
            drawRound = singleEliminationRound;
            break;
          }
        }
        // not found - create one
        if (drawRound == null) {
          drawRound = new DrawRound();
          drawRound.round = round;
          singleEliminationRounds.push(drawRound);
        }
      }
    });
    return drawRound;
  }

  /**
   *
   * @private
   */
  private transformToBracketMatchData() {
    this.divisions.forEach((division: SEDrawDivision) => {
      const rounds: SERound [] = [];
      const roundNumbers: number [] = [];
      if (division.singleEliminationRounds) {
        const drawRounds = division.singleEliminationRounds;
        for (let i = 0; i < drawRounds.length; i++) {
          const drawRound = drawRounds[i];
          roundNumbers.push(drawRound.round);
          const drawItems: DrawItem [] = drawRound.drawItems;
          const roundMatches = [];
          for (let j = 0; j < drawItems.length;) {
            const drawItemLeft: DrawItem = drawItems[j];
            const drawItemRight: DrawItem = drawItems[j + 1];
            const groupNumber = (j / 2) + 1;
            const matchCardInfo: MatchCardInfo = this.getMatchCardInfo(drawRound.round, groupNumber);
            const match: Match = new Match();
            match.opponentA = drawItemLeft;
            match.opponentB = drawItemRight;
            match.time = matchCardInfo ? matchCardInfo.startTime : 0;
            match.tableNum = (matchCardInfo?.assignedTables != null) ? Number(matchCardInfo.assignedTables) : (6 + j);  // for now
            match.result = null;
            match.opponentAWon = false;
            match.showSeedNumber = (i === 0); // show seed number for first round only
            // only first round matches can be rearanged
            match.dragDisabled = (drawRound.round != roundNumbers[0])
              || (drawItemLeft.seSeedNumber === 1)
              || (drawItemRight.seSeedNumber === 2)
              || (this.editMode === false);
            // assign id so we can identify over which player (A or B) the mouse is hovering.
            match.matchElementId = `match-${division.divisionIdx}-${drawItemLeft.id}-${drawItemRight.id}`;

            roundMatches.push(match);
            j += 2;
          }
          const type = ((i + 1) === drawRounds.length) ? 'Final' : 'Winnerbracket';
          const round: SERound = {type: type, matches: roundMatches};
          rounds.push(round);
        }
      }

      let dropListDrawItems: DrawItem[] = [];
      if (rounds.length > 0) {
        division.seRounds = rounds;
        dropListDrawItems = this.makeDropList(rounds[0]);
      } else {
        division.seRounds = null;
      }
      division.roundNumbers = roundNumbers;
      division.dropListData = {
        firstRound: roundNumbers[0],
        dropListDrawItems: dropListDrawItems
      };
    });

    for (let i = 0; i < this.divisions.length; i++) {
      const seDrawDivision = this.divisions[i];
      seDrawDivision.svgData = this.calculateLayout(seDrawDivision);
    }
  }

  /**
   * Calculates the total width required for the given division based on its rounds, spacing, match width, and padding.
   *
   * @param {SEDrawDivision} division - The division object containing the information about rounds.
   * @return {number} The computed width for the division. Returns 0 if the division has no rounds.
   */
  calculateWidth(division: SEDrawDivision): number {
    if (!division.seRounds || division.seRounds.length === 0) return 0;

    // Total Width = (Number of Rounds - 1) * spacing + last match width
    return ((division.seRounds.length - 1) * this.roundSpacing) + this.matchWidth + this.padding;
  }

  /**
   * Calculates the total height required for rendering all matches in a single elimination division,
   * including additional space for a 3rd/4th place match if applicable.
   *
   * @param {SEDrawDivision} division - The division object containing rounds and match details.
   * @return {number} The total calculated height in pixels.
   */
  calculateHeight(division: SEDrawDivision): number {
    if (!division.seRounds || division.seRounds.length === 0) return 0;

    // Get the count of matches in the very first round
    const firstRoundMatchCount = division.seRounds[0].matches.length;

    // Total Height = Matches * height + (Matches - 1) * gaps
    // We add a little extra padding (e.g., 20px) to ensure borders aren't clipped
    let totalHeight = (firstRoundMatchCount * this.matchHeight) + ((firstRoundMatchCount - 1) * this.matchGap);

    // If there is a 3rd/4th place match, add space for 1 more match + gap + label
    if (this.has3rd4thPlaceMatch(division)) {
      totalHeight += (this.matchHeight + this.matchGap);
    }

    return totalHeight;
  }

  /**
   * Determines whether a division includes a 3rd/4th place match round in the single elimination rounds.
   *
   * @param division The single elimination division to evaluate.
   * @return A boolean indicating if a 3rd/4th place match round exists (true) or not (false).
   */
  private has3rd4thPlaceMatch(division: SEDrawDivision): boolean {
    const drawRound = division.singleEliminationRounds.find(round => round.round === 2 && round.drawItems.length === 4);
    return drawRound != null;
  }

  protected getSERounds(divIdx: number) {
    return (this.divisions?.length > 0 && this.divisions[divIdx].seRounds.length > 0)
      ? this.divisions[divIdx].seRounds : null;
  }

  /**
   * Calculates the layout of a tournament bracket, including match positions, bracket connectors, and overall dimensions.
   *
   * @param {SEDrawDivision} seDrawDivision - The tournament division data, including rounds and matches.
   * @return {Object} An object containing the layout details:
   *                  - `layoutMatches`: An array of match layout metadata, including coordinates and other properties.
   *                  - `connectors`: An array of connector paths defining bracket links between matches.
   *                  - `totalWidth`: The total width of the layout for container sizing.
   *                  - `totalHeight`: The total height of the layout for container sizing.
   */
  private calculateLayout(seDrawDivision: SEDrawDivision): any {
    const rounds: SERound[] = seDrawDivision.seRounds;
    const layoutMatches: any[] = [];
    const connectors: string[] = [];
    const coordsMap = new Map<string, { x: number, y: number }>();

    rounds.forEach((round, rIdx) => {
      round.matches.forEach((match: Match, mIdx: number) => {
        let x = rIdx * this.roundSpacing;
        let y: number;

        if (rIdx === 0) {
          // First Round: Simple vertical stack
          y = mIdx * (this.matchHeight + this.matchGap);
        } else {
          // Subsequent Rounds: Position at the midpoint of the two children
          const child1 = coordsMap.get(`${rIdx - 1}-${mIdx * 2}`);
          const child2 = coordsMap.get(`${rIdx - 1}-${mIdx * 2 + 1}`);

          if (child1 && child2) {
            y = (child1.y + child2.y) / 2;

            // Generate the "Bracket" path connecting the two children to this match
            connectors.push(this.drawBracketConnector(child1, child2, x, y));
          } else {
            // Fallback positioning
            y = mIdx * (this.matchHeight + this.matchGap) * Math.pow(2, rIdx);
          }
        }

        coordsMap.set(`${rIdx}-${mIdx}`, { x, y });

        const isConsolation = (round.type === 'Final' && mIdx === 1);
        layoutMatches.push({
          data: match,
          x: x,
          y: y,
          isConsolation: isConsolation,
          roundIndex: rIdx // Helpful for drag/drop logic later
        });
      });
    });

    // Calculate total dimensions for the container scroll
    const totalWidth = this.calculateWidth(seDrawDivision);
    const totalHeight = this.calculateHeight(seDrawDivision);

    return { layoutMatches, connectors, totalWidth, totalHeight };
  }

  /**
   * Draws a bracket-shaped connector path between two child nodes and their parent node.
   *
   * @param {Object} c1 - The first child node, containing x and y coordinates.
   * @param {Object} c2 - The second child node, containing x and y coordinates.
   * @param {number} targetX - The x-coordinate of the parent node to connect to.
   * @param {number} targetY - The y-coordinate of the parent node to connect to.
   * @return {string} A string representing the SVG path data for the bracket connector.
   */
  private drawBracketConnector(c1: any, c2: any, targetX: number, targetY: number): string {
    const startX = c1.x + this.matchWidth;
    const midX = startX + (this.roundSpacing - this.matchWidth) / 2;
    const child1Y = c1.y + (this.matchHeight / 2);
    const child2Y = c2.y + (this.matchHeight / 2);
    const parentY = targetY + (this.matchHeight / 2);

    // Path: Move to C1 -> Horizontal to Mid -> Vertical to C2 -> Horizontal to C2
    // AND a horizontal line from Mid to Parent
    return `M ${startX} ${child1Y} L ${midX} ${child1Y}
          M ${startX} ${child2Y} L ${midX} ${child2Y}
          M ${midX} ${child1Y} L ${midX} ${child2Y}
          M ${midX} ${parentY} L ${targetX} ${parentY}`;
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

  makeDropList(firstRound: SERound) {
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

  /**
   * Handles the event triggered when a drag action enters a valid drop target.
   *
   * @param {any} event - The drag event that contains contextual information about the drag action.
   * @param {any} match - The matching target or element related to the drag action.
   * @return {void} No return value.
   */
  onDragEntered(event: any, match: any) {
    this.activeMatchLayout = match;
  }

  /**
   * Handles the event triggered when a drag operation exits a specific drop area.
   * This method resets any active match layout and its corresponding highlighted slot.
   *
   * @param {any} event - The drag event object that contains information about the drag operation.
   * @param {any} match - The match object associated with the current drag context.
   * @return {void} This method does not return a value.
   */
  onDragExited(event: any, match: any) {
    if (this.activeMatchLayout) {
      this.activeMatchLayout.highlightedSlot = null;
      this.activeMatchLayout = null;
    }
  }

  /**
   * Handles the mouse or touch move event to detect the hovered slot (A or B) in a match layout.
   *
   * @param {MouseEvent | TouchEvent} event - The event object containing either mouse or touch data.
   * @return {void} Does not return a value, but updates the active match layout's highlighted slot based on the user's interaction.
   */
  onMouseMove(event: MouseEvent | TouchEvent) {
    if (!this.activeMatchLayout) return;

    // Get coordinates regardless of mouse or touch
    const clientY = (event instanceof MouseEvent) ? event.clientY : event.touches[0].clientY;

    // Use the native element of the anchor we know is active
    // We'll need a way to reference the element, or just use the layout X/Y
    // Best way: find the element by ID or use a Template Reference
    const id = this.activeMatchLayout.data?.matchElementId;
    const matchElement = document.getElementById(id);
    if (!matchElement) return;

    const rect = matchElement.getBoundingClientRect();
    const relativeY = clientY - rect.top;
    const midline = this.matchHeight / 2;
    const targetSlot = relativeY < midline ? 'A' : 'B';

    // Seed & bye Protection
    const targetPlayer = targetSlot === 'A' ? this.activeMatchLayout.data.opponentA : this.activeMatchLayout.data.opponentB;
    if (targetPlayer?.seSeedNumber === 1 || targetPlayer?.seSeedNumber === 2 || targetPlayer.byeNum > 0) {
      this.activeMatchLayout.highlightedSlot = null;
    } else {
      this.activeMatchLayout.highlightedSlot = targetSlot;
    }
  }

  /**
   * Handles the drop event for an item being dragged and dropped between container elements.
   * This method performs logic to swap players between matches while recalculating their positions
   * and updating the server with the new state.
   *
   * @param {CdkDragDrop<any>} event - The drag-and-drop event containing information about
   * the source and target containers, the dragged item, and the drop point.
   * @return {void} Does not return any value.
   */
  onDrawItemDrop(event: CdkDragDrop<any>) {
    if (this.activeMatchLayout == null || this.activeMatchLayout?.highlightedSlot == null) {
      return;
    }

    if (event.previousContainer === event.container) {
      if (this.activeMatchLayout) {
        this.activeMatchLayout.highlightedSlot = null;
        this.activeMatchLayout = null;
      }
      return;
    }

    const dragInfo = event.item.data; // { player: DrawItem, originMatch: Match, slot: 'A'|'B' }
    const targetMatch: Match = event.container.data;
    let draggedPlayer: DrawItem = dragInfo.player;

    // 1. Calculate where the drop happened relative to the target match box
    const dropY = event.dropPoint.y;
    const targetElement = event.container.element.nativeElement;
    const targetRect = targetElement.getBoundingClientRect();
    const relativeY = dropY - targetRect.top;

    // 2. Determine if we dropped on the top half (A) or bottom half (B)
    // Match height is 80px, so 40px is our midline
    const midline = this.matchHeight / 2;
    const targetSlot = relativeY < midline ? 'A' : 'B';

    // 3. Identify the player being replaced
    let targetPlayer: DrawItem = targetSlot === 'A' ? targetMatch.opponentA : targetMatch.opponentB;
    // console.log('in drawItemDrop ', targetSlot);

    this.saveUndo(event, draggedPlayer, targetPlayer);

    const savedTargetSingleElimLineNum = targetPlayer.singleElimLineNum;
    targetPlayer = {...targetPlayer, singleElimLineNum: draggedPlayer.singleElimLineNum};
    draggedPlayer = {...draggedPlayer, singleElimLineNum: savedTargetSingleElimLineNum};

    // 4. Perform the 2-way Swap
    // Put the target player into the source match's original slot
    if (dragInfo.slot === 'A') {
      dragInfo.originMatch.opponentA = targetPlayer;
    } else {
      dragInfo.originMatch.opponentB = targetPlayer;
    }

    // Put the dragged player into the target match's detected slot
    if (targetSlot === 'A') {
      targetMatch.opponentA = draggedPlayer;
    } else {
      targetMatch.opponentB = draggedPlayer;
    }

    if (this.activeMatchLayout) {
      this.activeMatchLayout.highlightedSlot = null;
      this.activeMatchLayout = null;
    }

    // update on the server too
    const movedDrawItems: DrawItem[] = [];
    movedDrawItems.push(draggedPlayer);
    movedDrawItems.push(targetPlayer);
    this.updateDrawItems(movedDrawItems);
  }

  /**
   * Updates the draw items by emitting a draw action with the specified moved draw items.
   *
   * @param {DrawItem[]} movedDrawItems - An array of draw items that have been moved or updated.
   * @return {void} This method does not return a value.
   */
  private updateDrawItems (movedDrawItems: DrawItem[]) {
    const action: DrawAction = {
      actionType: DrawActionType.DRAW_ACTION_UPDATE,
      eventId: this.selectedEvent.id,
      payload: {movedDrawItems: movedDrawItems, drawType: DrawType.SINGLE_ELIMINATION}
    };
    this.drawsAction.emit(action);
  }

  private getMatchCardInfo(round: number, groupNumber: number): MatchCardInfo {
    let foundMatchCard: MatchCardInfo = null;
    if (this.matchCardInfos) {
      const filtered : MatchCardInfo [] = this.matchCardInfos.filter((matchCardInfo: MatchCardInfo) => {
        return matchCardInfo.round == round && matchCardInfo.groupNum == groupNumber && matchCardInfo.drawType === DrawType.SINGLE_ELIMINATION;
      });
      if (filtered?.length > 0) {
        foundMatchCard = filtered[0];
      }
    }
    return foundMatchCard;
  }

  /**
   * Saves the current drag-and-drop operation state to the undo stack for potential reversal.
   *
   * @param {CdkDragDrop<any>} event - The drag-and-drop event containing information about source and destination containers.
   * @param {DrawItem} draggedPlayer - The player item being dragged during the operation.
   * @param {DrawItem} targetPlayer - The player item at the target drop location.
   * @return {void} This method does not return a value.
   */
  private saveUndo(event: CdkDragDrop<any>, draggedPlayer: DrawItem, targetPlayer: DrawItem) {
    // console.log('draggedPlayer', draggedPlayer);
    // console.log('targetPlayer', targetPlayer)
    const undoMemento: SeUndoMemento = {
      fromMatch: event.previousContainer.data,
      toMatch: event.container.data,
      drawItemId1: draggedPlayer.id,
      drawItemId2: targetPlayer.id,
      singleElimLineNum1: draggedPlayer.singleElimLineNum,
      singleElimLineNum2: targetPlayer.singleElimLineNum,
      divIdx: this.activeMatchLayout.data.divisionIdx
    };
    // console.log('in saveUndo', undoMemento);
    this.undoStack.push(undoMemento);

    this.broadcastState(); // Enable the button!
  }

  undoMove() {
    if (this.undoStack?.length > 0) {
      const undoMemento: SeUndoMemento = this.undoStack[this.undoStack.length - 1];
      this.undoStack.splice(this.undoStack.length - 1, 1);
      // console.log('undoMemento', undoMemento);

      let targetSlot = undoMemento.fromMatch.opponentA.singleElimLineNum === undoMemento.singleElimLineNum1 ? 'A' : 'B';
      let sourceSlot = undoMemento.toMatch.opponentA.singleElimLineNum === undoMemento.singleElimLineNum2 ? 'A' : 'B';
      let drawItem1 = (targetSlot === 'A') ?
        undoMemento.fromMatch.opponentA : undoMemento.fromMatch.opponentB;
      let drawItem2 = (sourceSlot === 'A') ?
        undoMemento.toMatch.opponentA : undoMemento.toMatch.opponentB;
      if (drawItem1 && drawItem2) {
        drawItem1 = {...drawItem1, singleElimLineNum: undoMemento.singleElimLineNum2};
        drawItem2 = {...drawItem2, singleElimLineNum: undoMemento.singleElimLineNum1};

        if (targetSlot === 'A') {
          undoMemento.fromMatch.opponentA = drawItem2;
        } else {
          undoMemento.fromMatch.opponentB = drawItem2;
        }

        if (sourceSlot === 'A') {
           undoMemento.toMatch.opponentA = drawItem1;
        } else {
          undoMemento.toMatch.opponentB = drawItem1;
        }

        const movedDrawItems: DrawItem[] = [];
        movedDrawItems.push(drawItem1);
        movedDrawItems.push(drawItem2);
        this.updateDrawItems(movedDrawItems);
      }
    }
    this.broadcastState();
  }

  hasUndoItems(): boolean {
    // console.log('this.undoStack?.length', this.undoStack?.length);
    return this.undoStack?.length > 0;
  }

  clearUndoItems() {
    this.undoStack = [];
  }
}
