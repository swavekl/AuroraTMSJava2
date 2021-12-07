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
    const startUrl = url.substr(0, url.indexOf('/download/') + '/download'.length);
    // path must be passed in as parameter or else the controller can't extract it from the url
    const repositoryPath = url.substring(startUrl.length + 1, url.length);
    // save this file locally under it's own name
    const downloadAs = repositoryPath.substr(repositoryPath.lastIndexOf('/') + 1, repositoryPath.length);
    // request without repository path
    const fullUrl = `https://${environment.baseServer}/${startUrl}`;
    this.httpClient.get(fullUrl, {
      params: {
        path: repositoryPath
      },
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
