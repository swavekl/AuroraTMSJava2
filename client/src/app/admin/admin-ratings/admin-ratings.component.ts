import {Component, EventEmitter, Input, Output} from '@angular/core';
import {RatingsProcessorStatus} from '../model/ratings-processor-status';
import {MembershipsProcessorStatus} from '../model/memberhips-processor-status';

@Component({
  selector: 'app-admin-ratings',
  templateUrl: './admin-ratings.component.html',
  styleUrls: ['./admin-ratings.component.scss']
})
export class AdminRatingsComponent {

  @Input()
  processing: boolean;

  @Input()
  ratingsProcessorStatus: RatingsProcessorStatus;

  @Input()
  membershipsProcessorStatus: MembershipsProcessorStatus;

  @Output()
  uploaded = new EventEmitter();

  @Output()
  membershipsUploaded = new EventEmitter();

  onRatingsUploadFinished(downloadUrl: string) {
    const ratingsFileRepoPath: string = downloadUrl.substring(downloadUrl.indexOf("path=") + "path=".length);
    this.uploaded.emit(ratingsFileRepoPath);
  }

  getRatingsStoragePath() {
    return "ratingfiles";
  }

  onMembershipsFileUploadFinished(downloadUrl: string) {
    const membershipsFileRepoPath: string = downloadUrl.substring(downloadUrl.indexOf("path=") + "path=".length);
    this.membershipsUploaded.emit(membershipsFileRepoPath);
  }

  getMembershipsStoragePath() {
    return "membershipfiles";
  }

  public formatElapsedTime (start: number, end: number) {
    if (start != null && end != null) {
      // start time and end time
      const differenceInSecs = Math.round((end - start) / 1000);
      const minutes: number = Math.round(differenceInSecs / 60);
      const seconds: number = differenceInSecs - (minutes * 60);
      return (minutes > 0) ? `${minutes} min ${seconds} sec` : `${seconds} sec`;
    } else {
      return '';
    }
  }
}
