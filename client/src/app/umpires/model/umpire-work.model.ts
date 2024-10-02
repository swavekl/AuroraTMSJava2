/**
 * Umpire work assignment - created so that it is more efficient to retrieve
 * a list of matches umpired by an individual umpire
 */

export interface UmpireWork {

  // unique id of work
  id: number;

  // id of a match which is umpired
  matchFk: number;

  // event fk so it is easy to get event name
  eventFk: number;

  tournamentFk: number;

  // profile id of the umpire plus assistant umpire if any separated by ;
  umpireProfileId: string;

  // profile id of the assistant umpire if any separated by ;
  assistantUmpireProfileId: string;

  // the date and time when the match was assigned
  // i.e. approximately when the match was actually played
  matchDate: Date;
}
