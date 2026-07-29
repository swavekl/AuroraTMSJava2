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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    // Run daily at 10 PM Mountain Time
//    @Scheduled(cron = "0 0 22 * * *", zone = "America/Denver")
//    public void runJob() {
//        SystemPrincipalExecutor task = new SystemPrincipalExecutor() {
//            @Override
//            protected void taskBody() {
//                log.info("DailyUpdateRatingsJob - BEGIN");
//                syncAllRatings();
//                log.info("DailyUpdateRatingsJob - END");
//            }
//        };
//        task.execute();
//    }

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
        new File(reportFilename).delete();
    }

    private String fetchAllPlayersFromJustGo() {
        // create report file path
        long startAll = System.currentTimeMillis();

        int pageNum = 0;
        int pageSize = 50;
        Page<ApiPlayerDto> currentPage;
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

            StatefulBeanToCsv<UsattPlayerCsvDto> beanToCsv = new StatefulBeanToCsvBuilder<UsattPlayerCsvDto>(bw)
                    .withApplyQuotesToAll(true)
                    .build();

            do {
                long start = System.currentTimeMillis();
                PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
                currentPage = justGoService.listPlayers(pageRequest);
                long duration = (System.currentTimeMillis() - start) / 1000;

                log.info("Fetched page " + (pageNum + 1) + " of " + currentPage.getTotalPages() + " in " + duration + " seconds");

                List<UsattPlayerCsvDto> csvRows = new ArrayList<>();
                for (ApiPlayerDto apiDto : currentPage.getContent()) {
                    csvRows.add(justGoService.mapToCsvDto(apiDto));
                }

                beanToCsv.write(csvRows);
                pageNum++;
            } while (pageNum < currentPage.getTotalPages());

        } catch (Exception e) {
            log.error("Error writing CSV file: " + e.getMessage());
            e.printStackTrace();
        }

        long elapsed = (System.currentTimeMillis() - startAll) / 1000;
        log.info("Finished exporting " + pageNum + " pages in " + elapsed + " seconds.");
        return reportFilename;
    }

}
