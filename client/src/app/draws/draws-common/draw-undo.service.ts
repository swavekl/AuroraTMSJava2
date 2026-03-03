import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

@Injectable()
export class DrawUndoService {
  // 1. The State: Is the Undo button enabled?
  private canUndoSubject = new BehaviorSubject<boolean>(false);
  canUndo$ = this.canUndoSubject.asObservable();

  // 2. The Action: The actual event of clicking Undo
  private undoActionSubject = new Subject<void>();
  undoAction$ = this.undoActionSubject.asObservable();

  // 2. The Action: The actual event of clicking Undo
  private clearUndoActionSubject = new Subject<void>();
  clearAction$ = this.clearUndoActionSubject.asObservable();

  // Called by child components to update the toolbar button
  updateCanUndo(state: boolean) {
    this.canUndoSubject.next(state);
  }

  // Called by the Toolbar (DrawsPanel) when button is clicked
  triggerUndo() {
    this.undoActionSubject.next();
  }

  clearUndoItems() {
    this.clearUndoActionSubject.next();
  }
}
