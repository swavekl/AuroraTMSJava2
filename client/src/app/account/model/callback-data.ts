/**
 * Additional data used for calling back client
 */
export class CallbackData {
  // success and failure callbacks
  successCallbackFn: (scope: any) => void;
  cancelCallbackFn: (scope: any) => void;

  // object who has the callback functions
  callbackScope: any;
}

