import {Component, EventEmitter, Input, Output} from '@angular/core';
import {RatingsProcessorStatus} from '../model/ratings-processor-status';

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

  @Output()
  uploaded = new EventEmitter();

  onRatingsUploadFinished(downloadUrl: string) {
    const ratingsFileRepoPath: string = downloadUrl.substring(downloadUrl.indexOf("path=") + "path=".length);
    this.uploaded.emit(ratingsFileRepoPath);
  }

  getRatingsStoragePath() {
    return "ratingfiles";
  }
}
