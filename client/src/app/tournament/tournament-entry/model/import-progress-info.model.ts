export class ImportProgressInfo {
  jobId: string;
  // target tournament id
  tournamentId: number;
  phaseName: string;
  status: string;

  // percent completion
  overallCompleted: number;
  phaseCompleted: number;

  // profiles information
  profilesCreated: number;
  profilesExisting: number;
  profilesMissing: number;
  // file which can be downloaded  containing player names who are missing profiles
  missingProfileFileRepoUrl: string;

  entriesAdded: number;
  entriesUpdated: number;
  entriesDeleted: number;
  totalEntries: number;

  evenEntriesAdded: number;
  evenEntriesDeleted: number;
  totalEventEntries: number;
}
