package com.auroratms.justgo;

import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.auroratms.usatt.UsattPlayerRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DailyUpdateRatingsJob {

    @Autowired
    private JustGoRatingsService justGoService;

    @Autowired
    private UsattDataService usattDataService;

    // Run daily at 6 PM Mountain Time
//    @Scheduled(cron = "0 0 18 * * *", zone = "America/Denver")
    public void runJob() {
        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
            @Override
            protected void taskBody() {
                log.info("DailyUpdateRatingsJob - BEGIN");
                syncAllRatings();
                log.info("DailyUpdateRatingsJob - END");
            }
        };
        task.execute();
    }

    /**
     * Syncs all ratings from JustGo to the database.
     */
    public void syncAllRatings() {
        // Use your service to pull the ~18k records that have GUIDs
        List<UsattPlayerRecordRepository.RatingProjection> recordsToUpdate = usattDataService.findAllWithMembershipGuid();
        log.info("Found {} records to update", recordsToUpdate.size());

        final int batchSize = 20;
        int totalUpdated = 0;
        List<UsattPlayerRecord> batchOfExistingRecords = new ArrayList<>(batchSize);
        for (UsattPlayerRecordRepository.RatingProjection record : recordsToUpdate) {
            try {
                // Fetch the latest rating using the GUID stored in UsattPlayerRecord
                Integer newRating = justGoService.getTournamentRatingByFullName(record.getFirstName(), record.getLastName());

                if (newRating != null && newRating != record.getTournamentRating()) {
                    UsattPlayerRecord fullUsattPlayerRecord = usattDataService.getPlayerByMembershipId(record.getMembershipId());
                    fullUsattPlayerRecord.setTournamentRating(newRating);
                    batchOfExistingRecords.add(fullUsattPlayerRecord);
                    totalUpdated++;

                    if (batchOfExistingRecords.size() % batchSize == 0) {
                        usattDataService.saveAllAndFlush(batchOfExistingRecords);
                        log.info("Saved {} records", batchOfExistingRecords.size());
                        batchOfExistingRecords.clear();
                    }
                }
            } catch (Exception e) {
                log.error("Sync failed for USATT ID: {}", record.getMembershipId());
            }
        }
        log.info("Saved {} records", totalUpdated);
    }
}
