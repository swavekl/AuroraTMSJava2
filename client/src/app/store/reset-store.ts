import {Action} from '@ngrx/store';

export class ActionTypes {
  static RESET_STORE = '[App] resetstore';
}
export class ResetStore implements Action {
   readonly type = ActionTypes.RESET_STORE;
}
