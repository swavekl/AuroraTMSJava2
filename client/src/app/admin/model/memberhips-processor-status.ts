export class MembershipsProcessorStatus {

  // phase of processing - reading file, updating player records, writing records history etc.
  phase: string;
  error: string;
  // start & end time
  startTime: number;
  endTime: number;
  totalRecords: number;
  processedRecords: number;

  badRecords: number;
  newRecords: number;

}
