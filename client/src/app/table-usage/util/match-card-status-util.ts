import {MatchCardPlayabilityStatus, MatchInfo} from '../model/match-info.model';
import {TableUsage} from '../model/table-usage.model';
import {MatchCard} from '../../matches/model/match-card.model';
import {DrawType} from '../../draws/draws-common/model/draw-type.enum';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {Match} from '../../matches/model/match.model';

/**
 * Calculates match status - if it is available for scheduling on a table
 */
export class MatchCardStatusUtil {

  private playerStatusInfos: Map<string, PlayerStatusInfo> = new Map<string, PlayerStatusInfo>();

  private tournamentEvents: TournamentEvent[] = [];

  /**
   * Computes the status of each match card and packages into MatchInfo
   * @param tableUsages
   * @param matchCards
   * @param tournamentEvents
   */
  public generateMatchInfos(tableUsages: TableUsage[], matchCards: MatchCard[], tournamentEvents: TournamentEvent[]): MatchInfo[] {
    let matchesToPlayInfos: MatchInfo[] = [];
    if (tableUsages?.length > 0 && matchCards?.length > 0 && tournamentEvents?.length > 0) {
      this.tournamentEvents = tournamentEvents;
      matchesToPlayInfos = this.makeMatchInfos(tableUsages, matchCards, tournamentEvents);

      // compute the playability status of all matches
      this.markPlayerStatuses(tableUsages, matchCards);

      this.computeRoundRobinMatchStatus(matchesToPlayInfos, tableUsages);

      this.computeSingleEliminationMatchStatus(matchCards, matchesToPlayInfos, tableUsages);

      // sort them by starting time, then by ordinal number of event and finally by group number within event
      matchesToPlayInfos.sort((matchInfo1: MatchInfo, matchInfo2: MatchInfo) => {
        return (matchInfo1.matchCard.startTime === matchInfo2.matchCard.startTime)
          ? (
            (matchInfo1.tournamentEvent.ordinalNumber === matchInfo2.tournamentEvent.ordinalNumber)
              ? (matchInfo1.matchCard.groupNum > matchInfo2.matchCard.groupNum) ? 1 : -1
              : (matchInfo1.tournamentEvent.ordinalNumber > matchInfo2.tournamentEvent.ordinalNumber ? 1 : -1)
          )
          : ((matchInfo1.matchCard.startTime > matchInfo2.matchCard.startTime) ? 1 : -1);
      });
    }
    return matchesToPlayInfos;
  }

  /**
   * Makes match infos for matches that are still to be played
   *
   * @param tableUsageList
   * @param matchCards
   * @param tournamentEvents
   * @private
   */
  private makeMatchInfos(tableUsageList: TableUsage[], matchCards: MatchCard[], tournamentEvents: TournamentEvent[]) {
    const matchesToPlayInfos: MatchInfo[] = [];

    const matchCardsToBePlayed = this.getMatchCardsToBePlayed(tableUsageList, matchCards);

    // now get the tournament events combined with match cards for all of the above matches
    for (const matchCard of matchCardsToBePlayed) {
      for (const event of tournamentEvents) {
        if (matchCard.eventFk === event.id) {
          // make match info with initial status as ready to play
          const matchInfo: MatchInfo = {
            matchCard: matchCard,
            tournamentEvent: event,
            matchCardPlayability: MatchCardPlayabilityStatus.ReadyToPlay,
            playabilityDetail: null
          };
          matchesToPlayInfos.push(matchInfo);
        }
      }
    }
    return matchesToPlayInfos;
  }

  /**
   * Get match cards to be still played
   * @param tableUsageList
   * @param matchCards
   * @private
   */
  private getMatchCardsToBePlayed(tableUsageList: TableUsage[], matchCards: MatchCard[]) {
    const filteredMatchCards: MatchCard [] = [];
    for (const matchCard of matchCards) {
      const tournamentEvent = this.tournamentEvents.find(event => event.id === matchCard.eventFk);
      const isMatchCompleted = MatchCard.isMatchCardCompleted(matchCard, tournamentEvent);
      let isPlayedCurrently = false;
      for (let i = 0; i < tableUsageList.length; i++) {
        const tableUsage = tableUsageList[i];
        if (tableUsage.matchCardFk === matchCard.id) {
          isPlayedCurrently = true;
          // console.log('match is currently played ' + matchCard.id + ' event ' + matchCard.eventFk + ' drawType ' + matchCard.drawType);
          break;
        }
      }
      if (!isMatchCompleted && !isPlayedCurrently) {
        filteredMatchCards.push(matchCard);
        // console.log('match is available id ' + matchCard.id + ' event ' + matchCard.eventFk + ' drawType ' + matchCard.drawType);
      } else if (isMatchCompleted) {
        // console.log('match is completed id ' + matchCard.id + ' event ' + matchCard.eventFk + ' drawType ' + matchCard.drawType);
      }
    }
    return filteredMatchCards;
  }

  /**
   *
   * @param tableUsages
   * @param matchCards
   * @private
   */
  private markPlayerStatuses(tableUsages: TableUsage[], matchCards: MatchCard[]) {
    // mark all currently playing players
    for (let i = 0; i < tableUsages.length; i++) {
      const tableUsage = tableUsages[i];
      // match currently played
      if (tableUsage.matchCardFk !== 0) {
        // mark all players who are currently playing as 'playing'
        this.markPlayingPlayers(matchCards, tableUsage);
      }
    }
  }

  /**
   *
   * @param matchCards
   * @param tableUsage
   * @private
   */
  private markPlayingPlayers(matchCards: MatchCard[], tableUsage: TableUsage) {
    const matchCard: MatchCard = this.findMatchCard(matchCards, tableUsage.matchCardFk);
    if (matchCard?.profileIdToNameMap != null) {
      const playerProfileIds = Object.keys(matchCard.profileIdToNameMap);
      if (playerProfileIds.length > 0) {
        for (const playerProfileId of playerProfileIds) {
            const playerName = matchCard.profileIdToNameMap[playerProfileId];
            const playerStatusInfo = {
              profileId: playerProfileId,
              playerName: playerName,
              status: PlayerStatus.Playing,
              tableNumbers: matchCard.assignedTables,
              eventFk: matchCard.eventFk,
              round: matchCard.round,
              groupNum: matchCard.groupNum
            };
            this.playerStatusInfos.set(playerProfileId, playerStatusInfo);
        }
      }
    }
  }

  private findPlayerStatus(playerProfileId: string): PlayerStatusInfo {
    return this.playerStatusInfos.get(playerProfileId);
  }

  private findMatchCard(matchCards: MatchCard[], matchCardFk: number): MatchCard {
    return matchCards.find((matchCard) => matchCard.id === matchCardFk);
  }

  private isMatchPlayed(matchCard: MatchCard, tableUsages: TableUsage[]): boolean {
    return (tableUsages.find(tableUsage => tableUsage.matchCardFk === matchCard.id) != null);
  }

  /**
   *
   * @param matchInfos
   * @param tableUsages
   * @private
   */
  private computeRoundRobinMatchStatus(matchInfos: MatchInfo[], tableUsages: TableUsage[]) {
    // first mark all round robin match cards
    for (const matchInfo of matchInfos) {
      if (matchInfo.matchCard.drawType === DrawType.ROUND_ROBIN) {
        // if match card is not played
        const matchCardPlayed = this.isMatchPlayed(matchInfo.matchCard, tableUsages);
        if (!matchCardPlayed) {
          // find if all players are available to play
          const profileIdToNameMap = matchInfo.matchCard.profileIdToNameMap;
          if (profileIdToNameMap) {
            let numPlayingPlayers = 0;
            const playerProfileIds = Object.keys(matchInfo.matchCard.profileIdToNameMap);
            if (playerProfileIds.length > 0) {
              for (const playerProfileId of playerProfileIds) {
                const playerStatusInfo = this.findPlayerStatus(playerProfileId);
                if (playerStatusInfo != null && playerStatusInfo.status === PlayerStatus.Playing) {
                  numPlayingPlayers++;
                  break;
                }
              }
            }

            if (numPlayingPlayers === 0) {
              matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.ReadyToPlay;
            } else {
              // someone is playing
              // todo ???
            }
          }
        }
      }
    }
  }

  /**
   *
   * @param matchCards
   * @param matchInfos
   * @param tableUsages
   * @private
   */
  private computeSingleEliminationMatchStatus(matchCards: MatchCard[], matchInfos: MatchInfo[], tableUsages: TableUsage[]) {
    for (const matchInfo of matchInfos) {
      if (matchInfo.matchCard.drawType === DrawType.SINGLE_ELIMINATION) {
        // find if both players are available
        // if match card is not played
        const matchCardPlayed = this.isMatchPlayed(matchInfo.matchCard, tableUsages);
        if (!matchCardPlayed) {
          // get 1 or 2 match cards feeding players into this match card
          const feedingMatchCards: MatchCard [] = this.findFeedingMatchCards(matchCards, matchInfo.matchCard);
          const matchCard1CompletedOrBye = this.isMatchCompletedOrBye(feedingMatchCards, 0);
          const matchCard2CompletedOrBye = this.isMatchCompletedOrBye(feedingMatchCards, 1);
          const matchCard1PlayingPlayersProfileIds: string [] = this.findNumberOfPlayingPlayers(feedingMatchCards, 0);
          const matchCard2PlayingPlayersProfileIds: string [] = this.findNumberOfPlayingPlayers(feedingMatchCards, 1);
          const numMatchCard1PlayingPlayers = matchCard1PlayingPlayersProfileIds.length;
          const numMatchCard2PlayingPlayers = matchCard2PlayingPlayersProfileIds.length;

          if (matchCard1CompletedOrBye && matchCard2CompletedOrBye) {
            matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.ReadyToPlay;
          } else {
            const isLaterRound = this.isLaterSERound(matchInfo.matchCard);
            // if both match cards are bing played or neither is being played (not started yet)
            if ((numMatchCard1PlayingPlayers > 0 && numMatchCard2PlayingPlayers > 0) ||
                (numMatchCard1PlayingPlayers === 0 && numMatchCard2PlayingPlayers === 0) ||
                 isLaterRound) {
              const feedingMatchDrawType1 = this.getMatchDrawType (feedingMatchCards, 0);
              const feedingMatchDrawType2 = this.getMatchDrawType (feedingMatchCards, 1);
              // both feeding matches are RR then
              if (feedingMatchDrawType1 === DrawType.ROUND_ROBIN && feedingMatchDrawType2 === DrawType.ROUND_ROBIN) {
                matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.WaitingForPlayersToAdvance;
              } else {
                matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.WaitingForBothWinners;
              }
            } else {
              const thisMatchCardEvent = matchInfo.matchCard.eventFk;
              // waiting for one player
              // same event or different event
              if (numMatchCard1PlayingPlayers > 0) {
                const playerStatusInfo = this.findPlayerStatus(matchCard1PlayingPlayersProfileIds[0]);
                const eventFk = (playerStatusInfo) ? playerStatusInfo.eventFk : 0;
                if (thisMatchCardEvent === eventFk) {
                  // same event
                  matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.WaitingForPlayer;
                  matchInfo.playabilityDetail = this.makeSameEventDetail(playerStatusInfo);
                } else {
                  // different event
                  matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.WaitingForWinner;
                  matchInfo.playabilityDetail = this.makeOtherEventDetail(playerStatusInfo);
                }
              } else if (numMatchCard2PlayingPlayers > 0) {
                const playerStatusInfo = this.findPlayerStatus(matchCard2PlayingPlayersProfileIds[0]);
                const eventFk = (playerStatusInfo) ? playerStatusInfo.eventFk : 0;
                if (thisMatchCardEvent === eventFk) {
                  // same event
                  matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.WaitingForPlayer;
                  matchInfo.playabilityDetail = this.makeSameEventDetail(playerStatusInfo);
                } else {
                  // different event
                  matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.WaitingForWinner;
                  matchInfo.playabilityDetail = this.makeOtherEventDetail(playerStatusInfo);
                }
              }
            }
          }
        }
      }
    }
  }

  private makeSameEventDetail(playerStatusInfo: PlayerStatusInfo) {
    const matchName = MatchCard.getMatchName(playerStatusInfo.round, playerStatusInfo.groupNum);
    return `Waiting for winner of ${matchName}`;
  }

  private makeOtherEventDetail (playerStatusInfo: PlayerStatusInfo): string {
    const event: TournamentEvent = this.tournamentEvents.find(tournamentEvent => tournamentEvent.id === playerStatusInfo.eventFk);
    const eventName = (event != null) ? event.name : '';
    return `Waiting for player ${playerStatusInfo.playerName} playing on table ${playerStatusInfo.tableNumbers} in '${eventName}' event`;
  }

  /**
   * Finds players who are in this feeding match card who are playing somewhere
   * @param feedingMatchCards
   * @param index
   * @private
   */
  private findNumberOfPlayingPlayers(feedingMatchCards: MatchCard[], index: number): string [] {
    const playingPlayersProfileIds: string [] = [];
    if (feedingMatchCards.length > index) {
      const profileIdToNameMap = feedingMatchCards[index].profileIdToNameMap;
      if (profileIdToNameMap != null) {
        const playerProfileIds = Object.keys(profileIdToNameMap);
        if (playerProfileIds.length > 0) {
          for (const playerProfileId of playerProfileIds) {
            const playerStatusInfo = this.findPlayerStatus(playerProfileId);
            if (playerStatusInfo != null && playerStatusInfo.status === PlayerStatus.Playing) {
              playingPlayersProfileIds.push(playerProfileId);
              break;
            }
          }
        }
      }
    }
    return playingPlayersProfileIds;
  }

  /**
   *
   * @param matchCards
   * @param targetMatchCard
   * @private
   */
  private findFeedingMatchCards(matchCards: MatchCard[], targetMatchCard: MatchCard): MatchCard[] {
    // get this event match cards
    const thisEventMatchCards = matchCards.filter((matchCard) => matchCard.eventFk === targetMatchCard.eventFk);
    // find first round number after RR
    let firstRoundNum = 0;
    thisEventMatchCards.forEach(matchCard => firstRoundNum = Math.max(firstRoundNum, matchCard.round));
    const feedingDrawType: DrawType = (targetMatchCard.round === firstRoundNum) ? DrawType.ROUND_ROBIN : DrawType.SINGLE_ELIMINATION;

    let feedingMatchCards: MatchCard[] = [];
    if (feedingDrawType === DrawType.SINGLE_ELIMINATION) {
      // find match cards in the prior round to this round
      const roundToFind = targetMatchCard.round * 2;
      // the match for 3rd and 4th place (groupNum = 2)
      // if played uses players from groups 1 & 2 as well just like the final match
      const groupNumToUse = (targetMatchCard.round === 2 && targetMatchCard.groupNum === 2) ? 1 : targetMatchCard.groupNum;
      // feeding matches group numbers are like this
      const maxSourceGroupNum = Math.pow(2, groupNumToUse);
      const minSourceGroupNum = maxSourceGroupNum - 1;

      // console.log('roundToFind', roundToFind);
      // console.log(`groupNum range ${minSourceGroupNum} - ${maxSourceGroupNum} for targetGroupNum ${targetMatchCard.groupNum}`);
      feedingMatchCards = thisEventMatchCards.filter(matchCard => {
        return matchCard.drawType === feedingDrawType && matchCard.round === roundToFind &&
          (matchCard.groupNum === minSourceGroupNum || matchCard.groupNum === maxSourceGroupNum);
      });
    } else {
      const playerAGroupNum: number = targetMatchCard.playerAGroupNum;
      const playerBGroupNum: number = targetMatchCard.playerBGroupNum;
      feedingMatchCards = thisEventMatchCards.filter(matchCard => {
        return matchCard.drawType === feedingDrawType &&
          (matchCard.groupNum === playerAGroupNum || matchCard.groupNum === playerBGroupNum);
      });
    }
    // console.log(`feedingMatchCards for ev, rd, tbl: s ${targetMatchCard.eventFk}, ${targetMatchCard.round}, ${targetMatchCard.assignedTables}`, feedingMatchCards);
    return feedingMatchCards;
  }

  /**
   *
   * @param feedingMatchCards
   * @param matchIndex
   * @private
   */
  private isMatchCompletedOrBye(feedingMatchCards: MatchCard[], matchIndex: number): boolean {
    let isCompletedMatch = false;
    let isBye = false;
    if (feedingMatchCards.length === 1) {
      const matchCard = feedingMatchCards[0];
      if (matchIndex === 0) {
        // group num must be odd if this is a match feeding player A, without a bye
        isBye = !(matchCard.groupNum % 2 > 0);
      } else {
        // group num must be even if this is match feeding player B, without a bye
        isBye = !(matchCard.groupNum % 2 === 0);
      }
    }

    if (isBye) {
      isCompletedMatch = true;
    } else {
      let matchCard = null;
      if (feedingMatchCards.length === 2) {
        matchCard = feedingMatchCards[matchIndex];
      } else if (feedingMatchCards.length === 1) {
        matchCard = feedingMatchCards[0];
      }
      if (matchCard) {
        const tournamentEvent = this.tournamentEvents.find(event => event.id === matchCard.eventFk);
        isCompletedMatch = MatchCard.isMatchCardCompleted(matchCard, tournamentEvent);
      }
    }
    return isCompletedMatch;
  }

  private getMatchDrawType(feedingMatchCards: MatchCard[], matchIndex: number) {
    if (feedingMatchCards.length === 2) {
      return feedingMatchCards[matchIndex].drawType;
    } else if (feedingMatchCards.length === 1) {
        return feedingMatchCards[0].drawType;
    } else {
      return DrawType.SINGLE_ELIMINATION;
    }
  }

  private isLaterSERound(matchCard: MatchCard): boolean {
    let isLaterRound = false;
    if (matchCard.drawType === DrawType.SINGLE_ELIMINATION) {
      const theMatch = (matchCard.matches.length === 1) ? matchCard.matches[0] : null;
      if (theMatch) {
        isLaterRound = (theMatch.playerAProfileId === Match.TBD_PROFILE_ID && theMatch.playerBProfileId === Match.TBD_PROFILE_ID);
      }
    }
    return isLaterRound;
  }
}

/**
 * Player status
 */
export enum PlayerStatus {
  Available,
  Playing,
  NotPresent
}

/**
 * Individual player information
 *
 */
export class PlayerStatusInfo {
  profileId: string;
  playerName: string;
  status: PlayerStatus;
  // if playing
  tableNumbers: string;
  eventFk: number;
  round: number;
  groupNum: number;
}
