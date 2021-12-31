import {MatchCardPlayabilityStatus, MatchInfo} from '../model/match-info.model';
import {TableUsage} from '../model/table-usage.model';
import {MatchCard} from '../../matches/model/match-card.model';
import {DrawType} from '../../draws/model/draw-type.enum';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';

/**
 * Calculates match status - if it is available for scheduling on a table
 */
export class MatchCardStatusUtil {

  private playerStatusInfos: Map<string, PlayerStatusInfo> = new Map<string, PlayerStatusInfo>();

  /**
   * Computes the status of each match card and packages into MatchInfo
   * @param tableUsages
   * @param matchCards
   * @param tournamentEvents
   */
  public generateMatchInfos(tableUsages: TableUsage[], matchCards: MatchCard[], tournamentEvents: TournamentEvent[]): MatchInfo[] {
    let matchesToPlayInfos: MatchInfo[] = [];
    if (tableUsages?.length > 0 && matchCards?.length > 0 && tournamentEvents?.length > 0) {
      matchesToPlayInfos = this.makeMatchInfos(tableUsages, matchCards, tournamentEvents);

      // compute the playability status of all matches
      this.markPlayerStatuses(tableUsages, matchCards);

      this.computeRoundRobinMatchStatus(matchesToPlayInfos, tableUsages);

      this.computeSingleEliminationMatchStatus(matchCards, matchesToPlayInfos, tableUsages);

      // sort them by starting time
      matchesToPlayInfos.sort((matchInfo1: MatchInfo, matchInfo2: MatchInfo) => {
        return (matchInfo1.matchCard.startTime === matchInfo2.matchCard.startTime)
          ? 0
          : ((matchInfo1.matchCard.startTime > matchInfo2.matchCard.startTime) ? 1 : -1);
      });
    }
    console.log('matchesToPlayInfos', matchesToPlayInfos);
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
            matchCardPlayability: MatchCardPlayabilityStatus.ReadyToPlay
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
      const matchCompleted = (matchCard.playerRankings != null);
      let isPlayedCurrently = false;
      for (let i = 0; i < tableUsageList.length; i++) {
        const tableUsage = tableUsageList[i];
        if (tableUsage.matchCardFk === matchCard.id) {
          isPlayedCurrently = true;
          // console.log('match is currently played ' + matchCard.id + ' event ' + matchCard.eventFk + ' drawType ' + matchCard.drawType);
          break;
        }
      }
      if (!matchCompleted && !isPlayedCurrently) {
        filteredMatchCards.push(matchCard);
        // console.log('match is available id ' + matchCard.id + ' event ' + matchCard.eventFk + ' drawType ' + matchCard.drawType);
      } else if (matchCompleted) {
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
    // get all currently playing players
    for (let i = 0; i < tableUsages.length; i++) {
      const tableUsage = tableUsages[i];
      // match currently played
      if (tableUsage.matchCardFk !== 0) {
        // mark all players who are currently playing as 'playing'
        this.findPlayingPlayers(matchCards, tableUsage);
      }
    }

    // find all match cards which are not assigned to tables (not played)
    // mark these players as available
    for (let i = 0; i < matchCards.length; i++) {
      const matchCard = matchCards[i];
      if (matchCard != null && matchCard.profileIdToNameMap != null) {
        const playerProfileIds = Object.keys(matchCard.profileIdToNameMap);
        if (playerProfileIds.length > 0) {
          for (const playerProfileId of playerProfileIds) {
            if (!this.isMatchPlayed(matchCard, tableUsages)) {
              this.findAvailablePlayers(matchCard);
            }
          }
        }
      }
    }
  }

  /**
   *
   * @param matchCards
   * @param tableUsage
   * @private
   */
  private findPlayingPlayers(matchCards: MatchCard[], tableUsage: TableUsage) {
    const matchCard: MatchCard = this.findMatchCard(matchCards, tableUsage.matchCardFk);
    // console.log('matchCard ' + matchCard.id + ' ' + JSON.stringify(matchCard?.profileIdToNameMap));
    if (matchCard?.profileIdToNameMap != null) {
      const playerProfileIds = Object.keys(matchCard.profileIdToNameMap);
      if (playerProfileIds.length > 0) {
        for (const playerProfileId of playerProfileIds) {
          // if match card is used on more than one table in RR round then we shouldn't add player status twice
          let playerStatusInfo = this.findPlayerStatus(playerProfileId);
          if (!playerStatusInfo) {
            const playerName = matchCard.profileIdToNameMap[playerProfileId];
            playerStatusInfo = {
              profileId: playerProfileId,
              playerName: playerName,
              status: PlayerStatus.Available,
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
  }

  private findPlayerStatus(playerProfileId: string): PlayerStatusInfo {
    return this.playerStatusInfos[playerProfileId];
  }

  private findMatchCard(matchCards: MatchCard[], matchCardFk: number): MatchCard {
    return matchCards.find((matchCard) => matchCard.id === matchCardFk);
  }

  private isMatchPlayed(matchCard: MatchCard, tableUsages: TableUsage[]): boolean {
    return (tableUsages.find(tableUsage => tableUsage.matchCardFk === matchCard.id) != null);
  }

  /**
   *
   * @param matchCard
   * @private
   */
  private findAvailablePlayers(matchCard: MatchCard) {
    if (matchCard.profileIdToNameMap != null) {
      const playerProfileIds = Object.keys(matchCard.profileIdToNameMap);
      if (playerProfileIds.length > 0) {
        for (const playerProfileId of playerProfileIds) {
          // see if this player is already playing, if not mark him as available
          let playerStatusInfo = this.findPlayerStatus(playerProfileId);
          if (!playerStatusInfo) {
            const playerName = matchCard.profileIdToNameMap[playerProfileId];
            playerStatusInfo = {
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

          const matchCard1Completed = (feedingMatchCards.length > 0) ? (feedingMatchCards[0].playerRankings != null) : false;
          const matchCard2Completed = (feedingMatchCards.length > 1) ? (feedingMatchCards[1].playerRankings != null) : false;

          let numPlayingPlayers1 = 0;
          if (feedingMatchCards.length > 0) {
            const profileIdToNameMap = feedingMatchCards[0].profileIdToNameMap;
            if (profileIdToNameMap != null) {
              const playerProfileIds = Object.keys(profileIdToNameMap);
              if (playerProfileIds.length > 0) {
                for (const playerProfileId of playerProfileIds) {
                  const playerStatusInfo = this.findPlayerStatus(playerProfileId);
                  if (playerStatusInfo != null && playerStatusInfo.status === PlayerStatus.Playing) {
                    numPlayingPlayers1++;
                    break;
                  }
                }
              }
            }
          }

          let numPlayingPlayers2 = 0;
          if (feedingMatchCards.length > 1) {
            const profileIdToNameMap = feedingMatchCards[1].profileIdToNameMap;
            if (profileIdToNameMap != null) {
              const playerProfileIds = Object.keys(profileIdToNameMap);
              if (playerProfileIds.length > 0) {
                for (const playerProfileId of playerProfileIds) {
                  const playerStatusInfo = this.findPlayerStatus(playerProfileId);
                  if (playerStatusInfo != null && playerStatusInfo.status === PlayerStatus.Playing) {
                    numPlayingPlayers2++;
                    break;
                  }
                }
              }
            }
          }

          if (matchCard1Completed && matchCard2Completed) {
            if (numPlayingPlayers1 === 0 && numPlayingPlayers2 === 0) {
              matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.ReadyToPlay;
            } else if (numPlayingPlayers1 > 0 && numPlayingPlayers2 > 0) {
              matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.WaitingForBothWinners;
            } else if (numPlayingPlayers1 > 0) {
              // determine name of player
              matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.WaitingForPlayer;
            } else {
              // determine name of player
              matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.WaitingForPlayer;
            }
          } else {
            // RR or SE
            matchInfo.matchCardPlayability = MatchCardPlayabilityStatus.WaitingForPlayersToAdvance;
          }
        }
      }
    }
  }

  /**
   *
   * @param matchCards
   * @param targetMatchCard
   * @private
   */
  private findFeedingMatchCards(matchCards: MatchCard[], targetMatchCard: MatchCard) {
    // get this event match cards
    const thisEventMatchCards = matchCards.filter((matchCard) => matchCard.eventFk === targetMatchCard.eventFk);
    // find first round number after RR
    let firstRoundNum = 0;
    thisEventMatchCards.forEach(matchCard => firstRoundNum = Math.max(firstRoundNum, matchCard.round));
    const feedingDrawType: DrawType = (targetMatchCard.round === firstRoundNum) ? DrawType.ROUND_ROBIN : DrawType.SINGLE_ELIMINATION;

    if (feedingDrawType === DrawType.SINGLE_ELIMINATION) {
      const maxSourceGroupNum = Math.pow(2, targetMatchCard.groupNum);
      const minSourceGroupNum = maxSourceGroupNum - 2;
      return thisEventMatchCards.filter(matchCard => {
        return matchCard.drawType === feedingDrawType &&
          matchCard.groupNum > minSourceGroupNum && matchCard.groupNum <= maxSourceGroupNum;
      });
    } else {
      const playerAGroupNum: number = targetMatchCard.playerAGroupNum;
      const playerBGroupNum: number = targetMatchCard.playerBGroupNum;
      return thisEventMatchCards.filter(matchCard => {
        return matchCard.drawType === feedingDrawType &&
          (matchCard.groupNum === playerAGroupNum || matchCard.groupNum === playerBGroupNum);
      });
    }
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
