import {MatchCard} from '../../matches/model/match-card.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';


export enum MatchCardPlayabilityStatus {
  // a. All players needed for the match are available and should be ready to play.
  ReadyToPlay,

  // b. <Player Name> is playing on table <x> – Waiting for a player to finish a current match on another table.
  WaitingForPlayer,

  // c. Waiting for the winner of round <x> match <y> – Waiting for a player to advance from a previous round.
  WaitingForWinner,

  // d. Waiting for winners from previous rounds – Waiting for both players to advance from previous rounds.
  WaitingForBothWinners,

  // e. Waiting for players to advance – Waiting for players in a round robin to advance into the playoff rounds.
  WaitingForPlayersToAdvance

}

/**
 * Match card information for displaying in the list on the left side
 */
export class MatchInfo {
  matchCard: MatchCard;
  // tournament event info
  tournamentEvent: TournamentEvent;
  // status of entire match
  matchCardPlayability: MatchCardPlayabilityStatus = MatchCardPlayabilityStatus.ReadyToPlay;
  // additional detail about the player being watied on
  playabilityDetail: string = null;
}
