export enum ScheduleItemStatus {
  // not ready because one or more players have not been determined
  NotReady = 'NotReady',
  // all players are determined but the match was not started on the table yet
  NotStarted = 'NotStarted',
  // match started on the table
  Started = 'Started',
  // some matches have been played already
  InProgress = 'InProgress',
  // all matches are completed
  Completed = 'Completed'
}
