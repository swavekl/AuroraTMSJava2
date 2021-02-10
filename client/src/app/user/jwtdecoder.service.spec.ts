import { TestBed } from '@angular/core/testing';

import { JWTDecoderService } from './jwtdecoder.service';

describe('JWTDecoderService', () => {
  let service: JWTDecoderService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(JWTDecoderService);
  });

  it('bad token', () => {
    expect(service).toBeTruthy();
    expect(service.decode('abc')).toEqual({});
  });

  it('null token', () => {
    expect(service).toBeTruthy();
    expect(service.decode(null)).toEqual({});
  });

  it('good token', () => {
    const jwtToken = 'eyJraWQiOiI4aUxOSjNFWWtCUVB2OHNvbUU0Vi0wUmU0LXhQcERZak40c3FGdEJGOEx3IiwiYWxnIjoiUlMyNTYifQ.eyJ2ZXIiOjEsImp0aSI6IkFULlhydDBYdWd6ZkNheUhGeGs3RUxGbTdEbF96aG4tZ1k2TnM1WURXN3Z3NTQub2Fyd2hpcGQzeGdVOW93QlQwaDYiLCJpc3MiOiJodHRwczovL2Rldi03NTgxMjAub2t0YXByZXZpZXcuY29tL29hdXRoMi9kZWZhdWx0IiwiYXVkIjoiYXBpOi8vZGVmYXVsdCIsImlhdCI6MTYxMjkwMTA5MCwiZXhwIjoxNjEyOTA0NjkwLCJjaWQiOiIwb2F3OXdpanFuREd1TWladzBoNyIsInVpZCI6IjAwdXdiZHFtM2RlZDJDcnRoMGg3Iiwic2NwIjpbIm9mZmxpbmVfYWNjZXNzIiwib3BlbmlkIl0sInN1YiI6InN3YXZla2xvcmVuYytqdWxpYUBnbWFpbC5jb20iLCJncm91cHMiOlsiRXZlcnlvbmUiXX0.S1qyTwItvb-zUh4K3J5vtLsgLQzzNQKPDzqa8hoPQ5qsCYrhsKrfVsq9SZKtsPNrsY_YCvduNRWLbAoZsHOrwV--UC7B-7uWe-u1sgNleLHMcM4421FlxyyPrQSR2nqL4gRQcCXy4ULBy4gx9h4l_SqFZ3SC7Ec7-RmxCDlwU2f3rHqg3PmY13Hl85UH8xHuU2hVyPMiHnGxNkl_kzxoiVhgAF_IGqQDHNnc-2PqpVhM47Qz7QRtmL7Ho1fTYXPTa7rZet-LfMd2QLeVdQ5RH45IN2tSS_03G3eLoMNpBeNghQ6bX4o3IdJ_LSN-hQZqrwBS4VveReeHTXJ7trFpLg';
    expect(service).toBeTruthy();
    const decodedToken = service.decode(jwtToken);
    const expectedToken = {
      'ver': 1,
      'jti': 'AT.Xrt0XugzfCayHFxk7ELFm7Dl_zhn-gY6Ns5YDW7vw54.oarwhipd3xgU9owBT0h6',
      'iss': 'https://dev-758120.oktapreview.com/oauth2/default',
      'aud': 'api://default',
      'iat': 1612901090,
      'exp': 1612904690,
      'cid': '0oaw9wijqnDGuMiZw0h7',
      'uid': '00uwbdqm3ded2Crth0h7',
      'scp': [
        'offline_access',
        'openid'
      ],
      'sub': 'swaveklorenc+julia@gmail.com',
      'groups': [
        'Everyone'
      ]
    };
    expect(decodedToken).toEqual(expectedToken);
  });
});
