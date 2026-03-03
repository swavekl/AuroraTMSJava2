import {DrawItem} from './draw-item.model';
import {Match} from './match.model';

/**
 * Memento for undoing player move
 */
export interface UndoMemento {
  // draw items from which transfer was done
  fromGroupItems: DrawItem [];

  // draw items to which transfere was done
  toGroupItems: DrawItem [];

  // group numbers between which exchange was made
  changedGroupNum1: number;
  changedGroupNum2: number;

  // row index (0 based) of exchanged draw items
  rowIndex: number;

  // division index (0 based) of exchanged draw items
  divIdx: number;
}

/**
 * Undo mememnto for preserving single elimination moves information
 */
export interface SeUndoMemento {
  // draw items from which transfer was done
  fromMatch: Match;

  // draw items to which transfere was done
  toMatch: Match;

  // ids of items which where exchanged
  drawItemId1: number;
  drawItemId2: number;

  // line numbers which were changed during transfer
  singleElimLineNum1: number;
  singleElimLineNum2: number;

  // division index (0 based) of exchanged draw items
  divIdx: number;
}
