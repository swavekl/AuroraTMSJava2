import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FileRepositoryService {

  private apiUrl = '/api/filerepository';

  constructor(private httpClient: HttpClient) {
  }

  public upload(selectedFile: any, storagePath: string): Observable<any> {
    const uploadData = new FormData();
    uploadData.append('file', selectedFile, selectedFile.name);
    uploadData.append('storagePath', storagePath);
    const url = `${this.apiUrl}/upload`;
    return this.httpClient.post(url, uploadData, {
      reportProgress: true,
      observe: 'events'
    });
  }

  public download(url: string) {
    const downloadAs = url.substr(url.lastIndexOf('/') + 1, url.length);
    const fullUrl = `https://${environment.baseServer}/${url}`;
    this.httpClient.get(fullUrl, {
      responseType: 'blob'
    }).subscribe((blob: any) => {
        const a = document.createElement('a');
        const objectUrl = URL.createObjectURL(blob);
        a.href = objectUrl;
        a.download = downloadAs;
        a.click();
        URL.revokeObjectURL(objectUrl);
      });

  }
}
