import {Injectable} from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {MatchCard} from '../model/match-card.model';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MatchCardService extends EntityCollectionServiceBase<MatchCard> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    super('MatchCard', serviceElementsFactory);
  }

  public loadForEvent(eventId: number): Observable<MatchCard[]> {
    const queryParams = `eventId=${eventId}`;
    super.clearCache();
    return super.getWithQuery(queryParams);
  }

  public loadAllForTheTournamentDay(tournamentId: number, day: number, includePlayerNames?: boolean) {
    let queryParams = `tournamentId=${tournamentId}&day=${day}`;
    queryParams += (includePlayerNames) ? '&includePlayerNames=true' : '';
    super.clearCache();
    return super.getWithQuery(queryParams);
  }

  public putIntoCache(matchCards: MatchCard[]) {
    super.addAllToCache(matchCards);
  }
}
