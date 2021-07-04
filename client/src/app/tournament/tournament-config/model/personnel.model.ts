import {UserRoles} from '../../../user/user-roles.enum';

/**
 * Describes a person and his/her role at the tournament: Referee, Umpire or Data entry clerk
 */
export class Personnel {

  // name of the person
  name: string;

  // profile id of a person who is working on the tournament
  profileId: string;

  // a role filled at the tournament
  role: UserRoles;
}
