import {DrawItem} from './draw-item.model';

/**
 * Memento for undoing player move
 */
export interface UndoMemento {
  fromGroupItems: DrawItem [];
  toGroupItems: DrawItem [];
  rowIndex: number;
}
