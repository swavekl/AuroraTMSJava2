package com.auroratms.justgo;

import com.auroratms.AbstractServiceTest;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class JustGoRatingsServiceTest extends AbstractServiceTest {

    @Autowired
    private JustGoRatingsService service;

    @Test
    public void getTournamentRatingByLastFirstName1() {
        Integer rating = service.getTournamentRatingByFullName("Swavek", "Lorenc");
        assertTrue(rating >= 1700, "Expected non-negative tournament rating from JustGo sandbox");
        assertEquals(Integer.class, rating.getClass());
    }

    @Test
    public void getTournamentRatingByLastFirstName3() {
        Integer rating = service.getTournamentRatingByFullName("Miroslaw", "Lepa");
        assertTrue(rating >= 1600, "Expected non-negative tournament rating from JustGo sandbox " + rating);
        assertEquals(Integer.class, rating.getClass());
    }

    @Test
    public void getTournamentRatingByLastFirstNameAsOfDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.OCTOBER, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date asOfDate = calendar.getTime();
        Integer eligibilityRating = service.getTournamentRatingByFullNameAsOfDate("Swavek", "Lorenc", asOfDate);
        assertEquals(1879, eligibilityRating, "Expected exact rating");

        calendar.set(Calendar.DATE, 28);
        asOfDate = calendar.getTime();
        Integer postTournamentRating = service.getTournamentRatingByFullNameAsOfDate("Swavek", "Lorenc", asOfDate);
        assertEquals(1750, postTournamentRating, "Expected exact rating");
    }

    @Test
    public void getPlayerRecordByFullName() {
        ApiPlayerDto playerRecordByName = service.findPlayerRecordByName("Swavek", "Lorenc");
        assertNotNull(playerRecordByName, "Expected player record");

        assertEquals("Swavek", playerRecordByName.getFirstName(), "Expected first name");
        assertEquals("Lorenc", playerRecordByName.getLastName(), "Expected last name");
        assertEquals("Aurora", playerRecordByName.getTown(), "Expected city/town");
        assertEquals("84639", playerRecordByName.getMemberNumber(), "Expected USATT membership id");
        assertEquals("718dfa8b-3604-4dcc-afe6-25d0e85cb6bd", playerRecordByName.getId(), "Expected JustGo member guid");
    }

    @Test
    public void getPlayerRecordByFullNameWithForeignCharacters() {
        ApiPlayerDto playerRecordByName = service.findPlayerRecordByName("Coralline", "Éthier");
        assertNotNull(playerRecordByName, "Expected player record");

        assertEquals("Coralline", playerRecordByName.getFirstName(), "Expected first name");
        assertEquals("Éthier", playerRecordByName.getLastName(), "Expected last name");
//        assertEquals("Aurora", playerRecordByName.getTown(), "Expected city/town");
        assertEquals("201573", playerRecordByName.getMemberNumber(), "Expected USATT membership id");
        assertEquals("c59b2f82-2069-4a75-8607-50d7b92ee2c2", playerRecordByName.getId(), "Expected JustGo member guid");
    }

    @Test
    public void getPlayerRecordByFullNameWithForeignCharacters2() {
        ApiPlayerDto playerRecordByName = service.findPlayerRecordByName("Nahiely Malaret", "Cedeño");
        assertNotNull(playerRecordByName, "Expected player record");

        assertEquals("Nahiely Malaret", playerRecordByName.getFirstName(), "Expected first name");
        assertEquals("Cedeño", playerRecordByName.getLastName(), "Expected last name");
//        assertEquals("Aurora", playerRecordByName.getTown(), "Expected city/town");
        assertEquals("1174894", playerRecordByName.getMemberNumber(), "Expected USATT membership id");
        assertEquals("76ae4181-76ba-4ca2-bf95-9c2bf1585001", playerRecordByName.getId(), "Expected JustGo member guid");
    }

    @Test
    public void getPlayerRecordByMembershipNumber() {
        ApiPlayerDto playerRecordByName = service.findPlayerRecordByMembershipId(84639L);
        assertNotNull(playerRecordByName, "Expected player record");

        assertEquals("Swavek", playerRecordByName.getFirstName(), "Expected first name");
        assertEquals("Lorenc", playerRecordByName.getLastName(), "Expected last name");
        assertEquals("Aurora", playerRecordByName.getTown(), "Expected city/town");
        assertEquals("84639", playerRecordByName.getMemberNumber(), "Expected USATT membership id");
        assertEquals("718dfa8b-3604-4dcc-afe6-25d0e85cb6bd", playerRecordByName.getId(), "Expected JustGo member guid");
    }

    @Test
    public void getPlayerRecordByMembershipNumberAsOfDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2026, Calendar.JULY, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 11);
        calendar.set(Calendar.SECOND, 22);
        calendar.set(Calendar.MILLISECOND, 123);
        Date asOfDate = calendar.getTime();
        List<ApiPlayerDto> changedPlayers = service.findChangedPlayers(asOfDate);
        assertEquals(7, changedPlayers.size(), "Expected 6 players");
        int nonZeroRating = 0;
        for (ApiPlayerDto p : changedPlayers) {
            assertNotNull(p.getFirstName(), "Expected first name");
            assertNotNull(p.getLastName(), "Expected last name");
            assertNotNull(p.getId(), "Expected JustGo uuid for member");
            assertNotNull(p.getMemberNumber(), "Expected USATT membership id for member");
        }
    }

//    @Test
//    public void getAllPlayerRecordsAndWriteCsv() {
//
//        long startAll = System.currentTimeMillis();
//
//        File reportFile = new File(
//                (StringUtils.isEmpty(System.getenv("TEMP")) ? System.getenv("TMP") : System.getenv("TEMP"))
//                        + File.separator + "players-export.csv"
//        );
//
//        try {
//            String reportFilename = reportFile.getCanonicalPath();
//            System.out.println("reportFilename = " + reportFilename);
//
//            // Instantiate strategy with your header order
//            CustomHeaderOrderStrategy<UsattPlayerCsvDto> mappingStrategy =
//                    new CustomHeaderOrderStrategy<>(UsattPlayerCsvDto.class, UsattPlayerCsvDto.getHeaderOrder());
//
//            try (
//                    FileWriter fw = new FileWriter(reportFile, StandardCharsets.UTF_8);
//                    BufferedWriter bw = new BufferedWriter(fw)
//            ) {
//                StatefulBeanToCsv<UsattPlayerCsvDto> beanToCsv = new StatefulBeanToCsvBuilder<UsattPlayerCsvDto>(bw)
//                        .withMappingStrategy(mappingStrategy)
//                        .withApplyQuotesToAll(true)
//                        .build();
//
//                int pageNum = 1;
//                int pageSize = 50;
//                Page<ApiPlayerDto> currentPage;
//
//                do {
//                    long start = System.currentTimeMillis();
//                    PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
//                    currentPage = service.listPlayers(pageRequest);
//                    long duration = (System.currentTimeMillis() - start) / 1000;
//
//                    System.out.println("Fetched page " + pageNum + " of " + currentPage.getTotalPages() + " in " + duration + " seconds");
//
//                    List<UsattPlayerCsvDto> csvRows = new ArrayList<>();
//                    for (ApiPlayerDto apiDto : currentPage.getContent()) {
//                        csvRows.add(service.mapToCsvDto(apiDto));
//                    }
//
//                    beanToCsv.write(csvRows);
//                    pageNum++;
//                } while (pageNum <= currentPage.getTotalPages());
//
//                long elapsed = (System.currentTimeMillis() - startAll) / 1000;
//                System.out.println("Finished exporting " + (pageNum - 1) + " pages in " + elapsed + " seconds.");
//            }
//
//        } catch (Exception e) {
//            System.err.println("Error writing CSV file: " + e.getMessage());
//            e.printStackTrace();
//            fail("CSV Exporter failed with exception");
//        }
//    }

//    @Test
//    public void getAllPlayerRecordsAndWriteCsv() {
//
//        long startAll = System.currentTimeMillis();
//
//        File reportFile = new File(
//                (StringUtils.isEmpty(System.getenv("TEMP")) ? System.getenv("TMP") : System.getenv("TEMP"))
//                        + File.separator + "players-export.csv"
//        );
//
//        try {
//            String reportFilename = reportFile.getCanonicalPath();
//            System.out.println("reportFilename = " + reportFilename);
//
//            // Instantiate strategy with your header order
//            CustomHeaderOrderStrategy<UsattPlayerCsvDto> mappingStrategy =
//                    new CustomHeaderOrderStrategy<>(UsattPlayerCsvDto.class, UsattPlayerCsvDto.getHeaderOrder());
//
//            try (
//                    FileWriter fw = new FileWriter(reportFile, StandardCharsets.UTF_8);
//                    BufferedWriter bw = new BufferedWriter(fw)
//            ) {
//                StatefulBeanToCsv<UsattPlayerCsvDto> beanToCsv = new StatefulBeanToCsvBuilder<UsattPlayerCsvDto>(bw)
//                        .withMappingStrategy(mappingStrategy)
//                        .withApplyQuotesToAll(true)
//                        .build();
//
//                Set<String> uniqueJustGoIds = new HashSet<>();
//
//                List<String> membershipTypes = List.of(
//                        "Lifetime",
//                        "Gold",
//                        "Silver",
//                        "Bronze",
//                        "AdultTournamentPass",
//                        "Tournament Pass",
//                        "Coach",
//                        "Foreign Athlete Pass"
//                );
//                long totalRecordsProcessed = 0;
//                for (String membership : membershipTypes) {
//                    int pageNum = 1;
//                    int pageSize = 50;
//                    Page<ApiPlayerDto> currentPage;
//                    long membershipExportStart = System.currentTimeMillis();
//
//                    do {
//                        long pageExportStart = System.currentTimeMillis();
//                        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
//                        currentPage = service.listPlayersByMembership(membership, pageRequest);
//
//                        List<UsattPlayerCsvDto> csvRows = new ArrayList<>();
//                        for (ApiPlayerDto apiDto : currentPage.getContent()) {
//                            if (!uniqueJustGoIds.contains(apiDto.getId())) {
//                                csvRows.add(service.mapToCsvDto(apiDto));
//                                uniqueJustGoIds.add(apiDto.getId());
//                            }
//                        }
//
//                        beanToCsv.write(csvRows);
//                        if (pageNum == 1) {
//                            System.out.println("There are " + currentPage.getTotalElements() + " members with " + membership);
//                            totalRecordsProcessed +=  currentPage.getTotalElements();
//                        }
//
//                        long elapsed = (System.currentTimeMillis() - pageExportStart) / 1000;
//                        System.out.println("Extracted page " + pageNum + " in " + elapsed + " seconds");
//
//                        pageNum++;
//                    } while (pageNum < currentPage.getTotalPages());
////                    } while (pageNum < 3);
//
//                    long elapsed = (System.currentTimeMillis() - membershipExportStart) / 1000;
//                    System.out.println("Finished exporting " + (pageNum - 1) + " pages in "
//                            + elapsed + " seconds for membershipType " + membership);
//                }
//                long elapsed = (System.currentTimeMillis() - startAll) / 1000;
//                System.out.println("Finished exporting all members in " + elapsed + " seconds.");
//                System.out.println("There were a total " + totalRecordsProcessed + " records processed and " + uniqueJustGoIds.size() + " were unique.");
//            }
//        } catch (Exception e) {
//            System.err.println("Error writing CSV file: " + e.getMessage());
//            e.printStackTrace();
//            fail("CSV Exporter failed with exception");
//        }
//    }
}
