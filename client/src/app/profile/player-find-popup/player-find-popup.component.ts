import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {MatDialogRef} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {ProfileService} from '../profile.service';

@Component({
  selector: 'app-player-find-popup',
  templateUrl: './player-find-popup.component.html',
  styleUrls: ['./player-find-popup.component.css']
})
export class PlayerFindPopupComponent implements OnInit {
  // search criteria
  firstName: string;
  lastName: string;

  foundPlayers$: Observable<any>;

  constructor(public dialogRef: MatDialogRef<PlayerFindPopupComponent>,
              private profileService: ProfileService) {
  }

  ngOnInit(): void {
  }

  onSearch() {
    const searchCriteria = [];
    if (this.firstName != null) {
      searchCriteria.push({name: 'firstName', value: this.firstName});
    }
    if (this.lastName != null) {
      searchCriteria.push({name: 'lastName', value: this.lastName});
    }
    this.foundPlayers$ = this.profileService.findProfiles(searchCriteria);
  }

  onSelection(userId: number, firstName: string, lastName: string) {
    const selectedPlayerData = {
      firstName: firstName, lastName: lastName, id: userId, rating: 1239
    };
    this.dialogRef.close(selectedPlayerData);
  }

  onCancel(): void {
    this.dialogRef.close('cancel');
  }

}
