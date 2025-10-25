export class ImportProgressInfo {
  jobId: string;
  // target tournament id
  tournamentId: number;
  phaseName: string;
  status: string;

  // percent completion
  overallCompleted: number;
  phaseCompleted: number;
  profilesCreated: number;

  entriesAdded: number;
  entriesUpdated: number;
  entriesDeleted: number;
  totalEntries: number;

  evenEntriesAdded: number;
  evenEntriesDeleted: number;
  totalEventEntries: number;
}
