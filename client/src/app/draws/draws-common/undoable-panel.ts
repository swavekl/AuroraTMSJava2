/**
 * Interface for undoable panels
 */
export interface UndoablePanel {
  hasUndoItems(): boolean;
  undoMove(): void;
  clearUndoItems(): void;
  broadcastState(): void;
  setActive(active: boolean): void;
  isActive(): boolean;
}
