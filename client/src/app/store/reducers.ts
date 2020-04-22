import {ActionTypes} from './reset-store';

export function clearState(reducer) {
  return function (state, action) {

    if (action.type === ActionTypes.RESET_STORE) {
      state = undefined;
    }

    return reducer(state, action);
  };
}
