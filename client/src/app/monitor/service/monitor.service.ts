import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {environment} from '../../../environments/environment';
import {AuthenticationService} from '../../user/authentication.service';
import {MonitorMessage} from '../model/monitor-message.model';
// stomp implementation
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

/**
 * Data service providing match scores for monitor via websocket i.e. pushed from server
 */
@Injectable({
  providedIn: 'root'
})
export class MonitorService {

  public messagesSubject$ = new Subject<MonitorMessage>();
  // public messages$ = this.messagesSubject$.pipe(
  //   switchAll(),
  //   catchError(e => {
  //   throw e;
  // }));

  // --------------------------------------------
  // Stomp implementation based on SockJS
  // "sockjs-client": "^1.5.2",
  // "stompjs": "^2.3.3",
  // --------------------------------------------
  private stompClient: any;
  private isConnected$ = new BehaviorSubject<boolean>(false);

  constructor(private authenticationService: AuthenticationService) {
  }

  public connect(topicName: string) {
    const accessToken = this.authenticationService.getAccessToken();
    const me = this;
    const url = `https://${environment.baseServer}/gs-guide-websocket?access_token=${accessToken}`;
    const socket = new SockJS(url);
    this.stompClient = Stomp.over(socket);
    this.stompClient.connect({'Authorization': accessToken}, function (frame) {
      me.setConnected(true);
      me.stompClient.subscribe(topicName, function (message) {
        // console.log ('monitorMessage', monitorMessage);
        const monitorMessage: MonitorMessage = JSON.parse(message.body);
        me.messagesSubject$.next(monitorMessage);
      });
    });
  }

  public disconnect() {
    if (this.stompClient !== null) {
      this.stompClient.disconnect();
    }
    this.setConnected(false);
    // console.log('Disconnected');
  }

  private setConnected(connected: boolean) {
    // console.log('setConnected to ', connected);
    this.isConnected$.next(connected);
  }

  isConnected(): Observable<boolean> {
    return this.isConnected$.asObservable();
  }
}
