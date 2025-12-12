export class FeeScheduleItem {
  // description of price offer - e.g. early bird special
  offerName: string;
  // deadline when this offer expires
  deadline: Date;
  // entry fee before this deadline
  entryFee: number;
  // cancellation fee
  cancellationFee: number;
}
