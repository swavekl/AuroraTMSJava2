import { Injectable } from '@angular/core';
import jwt_decode from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class JWTDecoderService {

  constructor() { }

  decode(jwtToken: string): any {
    let tokenObject = {};
    if (jwtToken != null) {
      try {
        tokenObject = jwt_decode(jwtToken);
      } catch (invalidTokenError) {

      }
    }
    return tokenObject;
  }
}
