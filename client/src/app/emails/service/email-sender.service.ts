import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {distinctUntilChanged, tap} from 'rxjs/operators';
import {BehaviorSubject, Observable} from 'rxjs';
import {EmailServerConfiguration} from '../model/email-server-configuration.model';
import {EmailCampaign, Recipient} from '../model/email-campaign.model';

/**
 * Email sending service for various testing and sending mass mailings
 */
@Injectable({
  providedIn: 'root'
})
export class EmailSenderService {
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  private readonly API_BASE_URL: string = '/api/emailsender';

  constructor(private httpClient: HttpClient) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  /**
   * Gets email recipients for the whole tournament or selected events without removed recipients
   * @param tournamentId
   * @param recipientFilters
   * @param removedRecipients
   */
  public getRecipientEmails(tournamentId: number, recipientFilters: number[], removedRecipients: Recipient[], allRecipients: boolean, excludeRegistered: boolean): Observable<Recipient[]> {
    this.setLoading(true);
    const body = {recipientFilters: recipientFilters, removedRecipients: removedRecipients, allRecipients: allRecipients, excludeRegistered: excludeRegistered};
    return this.httpClient.post(`${this.API_BASE_URL}/${tournamentId}`, body)
      .pipe(
        tap(
          (recipients: Recipient []) => {
            this.setLoading(false);
          },
          (error: any) => {
            console.error('error', error);
            this.setLoading(false);
          },
          () => {
          })
      );
  }

  /**
   * Sends test email
   * @param config
   */
  public sendTestEmail(config: EmailServerConfiguration): Observable<string> {
    return this.httpClient.post(`${this.API_BASE_URL}/testemail`, config)
      .pipe(
        tap({
            next: (value: string) => {
              // this.setLoading(false);
            },
            error: (error: any) => {
              // this.setLoading(false);
            },
            complete: () => {
            }
          }
        ));
  }

  public sendCampaign(tournamentId: number, emailCampaign: EmailCampaign, sendTestEmail: boolean): Observable<string> {
    return this.httpClient.post(`${this.API_BASE_URL}/sendcampaign/${tournamentId}/${sendTestEmail}`, emailCampaign)
      .pipe(
        tap({
            next: (value: string) => {
              // this.setLoading(false);
            },
            error: (error: any) => {
              // this.setLoading(false);
            },
            complete: () => {
            }
          }
        ));
  }
}
