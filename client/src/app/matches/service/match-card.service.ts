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

  public loadForEvent(eventId: number, includePlayerNames?: boolean): Observable<MatchCard[]> {
    let queryParams = `eventId=${eventId}`;
    queryParams += (includePlayerNames) ? '&includePlayerNames=true' : '';
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
    super.clearCache();
    super.addAllToCache(matchCards);
  }
}
