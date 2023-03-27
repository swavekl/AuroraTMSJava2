import {DrawItem} from './draw-item.model';

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
}
