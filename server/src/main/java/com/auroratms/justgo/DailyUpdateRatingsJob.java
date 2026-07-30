package com.auroratms.justgo;

import com.auroratms.notification.SystemPrincipalExecutor;
import com.auroratms.ratingsprocessing.RatingsProcessorStatus;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class DailyUpdateRatingsJob implements Job {

    @Autowired
    private JustGoRatingsService justGoService;

    @Autowired
    private UsattDataService usattDataService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
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
        // fetch all players data and ratings and write them to a .csv file
        String reportFilename = fetchAllPlayersFromJustGo();

        // process the csv file.
        RatingsProcessorStatus ratingsProcessorStatus = new RatingsProcessorStatus();
        List<UsattPlayerRecord> usattPlayerRecords = this.usattDataService.readAllPlayersFromFile(reportFilename, ratingsProcessorStatus);
        if (!usattPlayerRecords.isEmpty()) {
            this.usattDataService.insertPlayerData(usattPlayerRecords, ratingsProcessorStatus);
        }

        log.info("" + ratingsProcessorStatus);

        // delete the CSV file.
//        new File(reportFilename).delete();
    }

    private String fetchAllPlayersFromJustGo() {
        // create report file path
        long startAll = System.currentTimeMillis();

        String reportFilename = null;
        try  {

            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            File reportFile = new File(tempDir + File.separator + "players-export-" + date + ".csv");
            reportFilename = reportFile.getCanonicalPath();
            log.info("reportFilename = " + reportFilename);
            FileWriter fw = new FileWriter(reportFilename, StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(fw);

            CustomHeaderOrderStrategy<UsattPlayerCsvDto> mappingStrategy =
                    new CustomHeaderOrderStrategy<>(UsattPlayerCsvDto.class, UsattPlayerCsvDto.getHeaderOrder());

            StatefulBeanToCsv<UsattPlayerCsvDto> beanToCsv = new StatefulBeanToCsvBuilder<UsattPlayerCsvDto>(bw)
                    .withMappingStrategy(mappingStrategy)
                    .withApplyQuotesToAll(true)
                    .build();

            Set<String> uniqueJustGoIds = new HashSet<>();

            List<String> membershipTypes = List.of(
                    "Lifetime",
                    "Gold",
                    "Silver",
                    "Bronze",
                    "AdultTournamentPass",
                    "Tournament Pass",
                    "Coach",
                    "Foreign Athlete Pass"
            );
            long totalRecordsProcessed = 0;
            for (String membership : membershipTypes) {
                int pageNum = 1;
                int pageSize = 50;
                Page<ApiPlayerDto> currentPage;
                long membershipExportStart = System.currentTimeMillis();

                do {
                    long pageExportStart = System.currentTimeMillis();
                    PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
                    currentPage = justGoService.listPlayersByMembership(membership, pageRequest);

                    List<UsattPlayerCsvDto> csvRows = new ArrayList<>();
                    for (ApiPlayerDto apiDto : currentPage.getContent()) {
                        if (!uniqueJustGoIds.contains(apiDto.getId())) {
                            csvRows.add(justGoService.mapToCsvDto(apiDto));
                            uniqueJustGoIds.add(apiDto.getId());
                        }
                    }

                    beanToCsv.write(csvRows);
                    if (pageNum == 1) {
                        System.out.println("There are " + currentPage.getTotalElements() + " members with " + membership);
                        totalRecordsProcessed +=  currentPage.getTotalElements();
                    }

                    long elapsed = (System.currentTimeMillis() - pageExportStart) / 1000;
                    System.out.println("Extracted page " + pageNum + " of " +  currentPage.getTotalPages() + " in " + elapsed + " seconds");

                    pageNum++;
                } while (pageNum < currentPage.getTotalPages());

                long elapsed = (System.currentTimeMillis() - membershipExportStart) / (60 * 1000);
                System.out.println("Finished exporting " + currentPage.getTotalPages() + " pages in "
                        + elapsed + " minutes for membershipType " + membership);
            }
            long elapsed = (System.currentTimeMillis() - startAll) / (60 * 1000);
            System.out.println("Finished exporting all members in " + elapsed + " minutes.");
            System.out.println("There were a total " + totalRecordsProcessed + " records processed and " + uniqueJustGoIds.size() + " were unique.");

        } catch (Exception e) {
            log.error("Error writing CSV file: " + e.getMessage());
            e.printStackTrace();
        }
        return reportFilename;
    }

}
