/**
 * local action for telling container what to do
 */

export enum DrawActionType {
  DRAW_ACTION_LOAD = 'DrawActionLoad',
  DRAW_ACTION_GENERATE = 'DrawActionGenerate',
  DRAW_ACTION_CLEAR = 'DrawActionClear',
  DRAW_ACTION_UPDATE = 'DrawActionUpdate',
  DRAW_ACTION_PRINT = 'DrawActionPrint',
  DRAW_ACTION_LOAD_STATUS = 'DrawActionLoadStatuses',
  DRAW_ACTION_REPLACE_PLAYER = 'DrawActionReplacePlayer'
}

export interface DrawAction {
  actionType: DrawActionType;
  eventId: number;
  payload: any;
}
