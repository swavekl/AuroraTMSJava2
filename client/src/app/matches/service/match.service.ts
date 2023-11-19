import {Injectable} from '@angular/core';
import {EntityCollectionServiceBase, EntityCollectionServiceElementsFactory} from '@ngrx/data';
import {Match} from '../model/match.model';
import {HttpClient} from '@angular/common/http';
import {map} from 'rxjs/operators';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MatchService extends EntityCollectionServiceBase<Match> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory,
              private http: HttpClient) {
    super('Match', serviceElementsFactory);
  }

  lockMatch(matchId: number, profileId: string): Observable<boolean> {
    const url: string = `/api/match/lock/${matchId}/${profileId}`;
    return this.http.put<boolean>(url, "")
      .pipe(
        map((result: boolean) => {
            // console.log('lockedMatch', result);
            return result;
      }, (error: any) => {
          console.error('Unable to lock match error', error);
        }) );
  }
}
