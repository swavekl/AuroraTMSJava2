import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

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
}
