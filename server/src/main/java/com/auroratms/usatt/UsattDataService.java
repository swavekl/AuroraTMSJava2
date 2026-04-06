package com.auroratms.usatt;

import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.ratingsprocessing.MembershipsProcessorStatus;
import com.auroratms.ratingsprocessing.RatingsProcessorStatus;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Service
@CacheConfig(cacheNames = {"player-data"})
@Transactional
public class UsattDataService {

    private static final Logger logger = LoggerFactory.getLogger(UsattDataService.class);

    @Autowired
    private UsattPlayerRecordRepository playerRecordRepository;

    @Autowired
    private RatingHistoryRecordRepository ratingHistoryRecordRepository;

    @Autowired
    private UserProfileExtService userProfileExtService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat ALT_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    public List<UsattPlayerRecord> findAllPlayersByNames(String firstName, String lastName, Pageable pageable) {
        return this.playerRecordRepository.findAllByFirstNameOrLastName(firstName, lastName, pageable);
    }

    public UsattPlayerRecord getPlayerByMembershipId(Long membershipId) {
        return this.playerRecordRepository.getFirstByMembershipId(membershipId);
    }

    public UsattPlayerRecord getPlayerByNames(String firstName, String lastName) {
        return this.playerRecordRepository.getFirstByFirstNameAndLastName(firstName, lastName);
    }

    /**
     * Links Okta profile id to the USATT membership id.  In case of new USATT member assigns
     * new membership id.
     *
     * @param usattPlayerRecord
     * @param profileId
     * @return
     */
    public UsattPlayerRecord linkPlayerToProfile(UsattPlayerRecord usattPlayerRecord, String profileId) {
        logger.info("linkPlayerToProfile " + profileId);
        UsattPlayerRecord recordToReturn = null;
        Long membershipId = usattPlayerRecord.getMembershipId();
        if (membershipId == null) {
            // create new player
            // set membership expiration date to a known value indicating newly created user by this application
            Date expired = getNewMembershipExpirationDate();
            // find the next membership id
            membershipId = this.playerRecordRepository.assignNext();
            usattPlayerRecord.setMembershipId(membershipId);
            usattPlayerRecord.setMembershipExpirationDate(expired);
            usattPlayerRecord.setTournamentRating(0);
            logger.info("Created new membership ID: " + membershipId);

            // save a record with this new membership id
            recordToReturn = this.playerRecordRepository.save(usattPlayerRecord);
        } else {
            // read existing
            recordToReturn = this.playerRecordRepository.getFirstByMembershipId(membershipId);
        }

        // save mapping between the user profile and membership id
        UserProfileExt userProfileExt = new UserProfileExt();
        userProfileExt.setProfileId(profileId);
        userProfileExt.setMembershipId(membershipId);
        if (userProfileExtService.existsByMembershipId(membershipId)) {
            UserProfileExt oldUserProfileExt = userProfileExtService.getByMembershipId(membershipId);
            logger.info("Deleting oldUserProfileExt = " + oldUserProfileExt);
            if (oldUserProfileExt.getClubFk() != null) {
                userProfileExt.setClubFk(oldUserProfileExt.getClubFk());
            }
            userProfileExtService.delete(oldUserProfileExt.getProfileId());
        }
        logger.info("Saving userProfileExt = " + userProfileExt);
        userProfileExtService.save(userProfileExt);

        return recordToReturn;
    }

    /**
     * Establish this date as the expiration date for new membership so we can tell it easily
     *
     * @return
     */
    private static Date getNewMembershipExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * @param membershipExpirationDate
     * @return
     */
    public static boolean isNewMembership(Date membershipExpirationDate) {
        Date newMembershipExpirationDate = getNewMembershipExpirationDate();
        return newMembershipExpirationDate.equals(membershipExpirationDate);
    }

//    /**
//     * Loads data from the ratings file into a list
//     *
//     * @param filename
//     * @param ratingsProcessorStatus
//     * @return
//     */
//    public List<UsattPlayerRecord> readAllPlayersFromFileOld(String filename, RatingsProcessorStatus ratingsProcessorStatus) {
//        List<UsattPlayerRecord> playerInfos = new ArrayList<>(75000);
//
//        try {
//            logger.info("Processing ratings file " + filename);
//            try (CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));) {
//                String[] values = null;
//                int rowNumber = 0;
//                int badRecordsNum = 0;
//                int maxLenFirstName = 0;
//                int maxLenLastName = 0;
//                int maxLenZip = 0;
//                int maxLenState = 0;
//                while ((values = csvReader.readNext()) != null) {
//                    rowNumber++;
//                    if (rowNumber == 1) {
//                        String actualHeaders = Arrays.stream(values)
//                                .map(value -> (value == null) ? null : value.replace("\uFEFF", ""))
//                                .collect(Collectors.joining(","));
//                        // Normalize to NFC form (canonical composed)
//                        actualHeaders = Normalizer.normalize(actualHeaders, Normalizer.Form.NFC);
//                        // check headers and stop if they are not what is expected
//                        String expectedHeaders = Normalizer.normalize(
//                                "Member ID,Last Name,First Name,Rating,State,Zip,Gender,Date of Birth,Expiration Date,Last Played Date",
//                                Normalizer.Form.NFC);
//                        if (!expectedHeaders.equals(actualHeaders)) {
//                            ratingsProcessorStatus.error = "Unexpected file format\n" +
//                                    "Expected headers: " + expectedHeaders + "\n" +
//                                    "Actual headers  : " + actualHeaders;
//                            throw new RuntimeException(ratingsProcessorStatus.error);
//                        }
//                        continue;
//                    }
//
//                    int columnNum = 0;
//
//                    UsattPlayerRecord usattPlayerInfo = new UsattPlayerRecord();
//                    if (values.length != 10) {
//                        String csvValues = StringUtils.joinWith(",", Arrays.stream(values).toArray());
//                        logger.warn("Insufficient values in record (" + values.length + ") " + csvValues);
//                        logger.info("Attempting parsing again");
//                        values = csvValues.split(",");
//                        if (values.length != 10) {
//                            badRecordsNum++;
//                            continue;
//                        } else {
//                            logger.info("Parsing succeeded");
//                        }
//                    }
//                    // Member ID	Last Name	First Name	Rating	State	Zip	Gender	Date of Birth	Expiration Date	Last Played Date
//                    for (String text : values) {
//                        switch (columnNum) {
//                            case 0:
//                                usattPlayerInfo.setMembershipId(Long.parseLong(text));
//                                break;
//                            case 1:
//                                maxLenLastName = Math.max(maxLenLastName, text.length());
//                                text = cleanupName(text);
//                                usattPlayerInfo.setLastName(text);
//                                break;
//                            case 2:
//                                maxLenFirstName = Math.max(maxLenFirstName, text.length());
//                                text = cleanupName(text);
//                                usattPlayerInfo.setFirstName(text);
//                                break;
//                            case 3:
//                                usattPlayerInfo.setTournamentRating(Integer.parseInt(text));
//                                break;
//                            case 4:
//                                maxLenState = Math.max(maxLenState, text.length());
//                                usattPlayerInfo.setState(text);
//                                break;
//                            case 5:
//                                maxLenZip = Math.max(maxLenZip, text.length());
//                                text = cleanupZipCode(text);
//                                usattPlayerInfo.setZip(text);
//                                break;
//                            case 6:
//                                usattPlayerInfo.setGender(text);
//                                break;
//                            case 7:
//                                usattPlayerInfo.setDateOfBirth(parseDate(text, usattPlayerInfo));
//                                break;
//                            case 8:
//                                Date membershipExpiration = parseDate(text, usattPlayerInfo);
//                                if (membershipExpiration != null) {
//                                    usattPlayerInfo.setMembershipExpirationDate(membershipExpiration);
//                                }
//                                break;
//                            case 9:
//                                usattPlayerInfo.setLastTournamentPlayedDate(parseDate(text, usattPlayerInfo));
//                                break;
//                            default:
//                                break;
//                        }
//                        columnNum++;
//                    }
//                    if (StringUtils.isEmpty(usattPlayerInfo.getFirstName()) ||
//                            StringUtils.isEmpty(usattPlayerInfo.getLastName())) {
//                        String csvValues = StringUtils.joinWith(",", Arrays.stream(values).toArray());
//                        logger.warn("Missing critical values in record " + csvValues);
//
//                        badRecordsNum++;
//                        ratingsProcessorStatus.badRecords = badRecordsNum;
//                        continue;
//                    }
//                    playerInfos.add(usattPlayerInfo);
//                    ratingsProcessorStatus.totalRecords = rowNumber - 1;
//                }
//
//                logger.info("total records = " + rowNumber);
//                logger.info("bad   records = " + badRecordsNum);
//                logger.info("maxLenFirstName = " + maxLenFirstName);
//                logger.info("maxLenLastName  = " + maxLenLastName);
//                logger.info("maxLenState     = " + maxLenState);
//                logger.info("maxLenZip       = " + maxLenZip);
//            }
//        } catch (IOException | CsvValidationException e) {
//            e.printStackTrace();
//        }
//        return playerInfos;
//    }

    /**
     * Reads all members information from a file in a new format downloaded from reports section of JustGo  website.
     *
     * @param filename
     * @param ratingsProcessorStatus
     * @return
     */
    public List<UsattPlayerRecord> readAllPlayersFromFile(String filename, RatingsProcessorStatus ratingsProcessorStatus) {
        List<UsattPlayerRecord> playerInfos = new ArrayList<>(150000);

        String[] values = null;
        int rowNumber = 0;

        // Initialize status tracking
        if (ratingsProcessorStatus != null) {
            ratingsProcessorStatus.phase = "READING FILE";
            ratingsProcessorStatus.startTime = System.currentTimeMillis();
            ratingsProcessorStatus.totalRecords = 0;
            ratingsProcessorStatus.processedRecords = 0;
            ratingsProcessorStatus.badRecords = 0;
        }

        Writer writer = null;
        try {
            logger.info("Processing memberships file " + filename);

            String badRecordsFilename = filename.replace(".csv", ".bad.csv");
            logger.info("Writing bad records to filename = " + badRecordsFilename);
            File badRecordsFile = new File(badRecordsFilename);
            if (badRecordsFile.exists()) {
                badRecordsFile.delete();
            }
            writer = new OutputStreamWriter(new FileOutputStream(badRecordsFile), StandardCharsets.UTF_8);

            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(',')
                    .withEscapeChar('\0') // Disables the backslash escape behavior
                    .build();

            CSVReaderBuilder builder = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))
                    .withCSVParser(parser);

            try (CSVReader csvReader = builder.build()) {

                int badRecordsNum = 0;
                int maxLenFirstName = 0;
                int maxLenLastName = 0;
                int maxLenZip = 0;
                int maxLenState = 0;

                while ((values = csvReader.readNext()) != null) {
                    rowNumber++;

                    // 1. Validate Header Row
                    if (rowNumber == 1) {
                        String actualHeaders = Arrays.stream(values)
                                .map(value -> (value == null) ? null : value.replace("\uFEFF", ""))
                                .collect(Collectors.joining(","));

                        actualHeaders = Normalizer.normalize(actualHeaders, Normalizer.Form.NFC);

                        String expectedHeaders = Normalizer.normalize(
                                "USATT Id,First Name,Middle Name,Nick Name,Last Name,Date Of Birth,ZipCode,Gender,CityTown,State,Primary Club,FinalRating,League Rating,Rating As Of Date,Latest tournament play date,Latest Membership expiry date,Latest Membership,Background Check Completed Expiry date,Background Check Completed Status,Safesport Trained Expiry Date,Safesport Trained Status",
                                Normalizer.Form.NFC);
                        writer.append(expectedHeaders + "\n");

                        if (!expectedHeaders.equals(actualHeaders)) {
                            String errorMsg = "Unexpected file format\n" +
                                    "Expected headers: " + expectedHeaders + "\n" +
                                    "Actual headers  : " + actualHeaders;

                            if (ratingsProcessorStatus != null) {
                                ratingsProcessorStatus.error = errorMsg;
                            }
                            throw new RuntimeException(errorMsg);
                        }
                        continue; // Skip processing the header row
                    }

                    // 2. Parse Data Row
                    try {
                        UsattPlayerRecord usattPlayerInfo = new UsattPlayerRecord();
                        int columnNum = 0;

                        String csvValues = Arrays.stream(values)
                                .map(value -> (value == null) ? "" : value.replace("\uFEFF", ""))
                                .map(value -> "\"" + value + "\"")
                                .collect(Collectors.joining(","));

                        if (values.length > 21) {
                            logger.warn("Unexpected number of values in row : " + rowNumber + " # values " + values.length);
                        }
                        if (csvValues.length() > 300) {
                            logger.warn("Unexpected values in row " + rowNumber + " length " + csvValues.length());
                        }

                        for (String text : values) {
                            if (text != null) {
                                text = text.trim();
                            }

                            switch (columnNum) {
                                case 0: // USATT Id
                                    if (StringUtils.isNotEmpty(text)) {
                                        usattPlayerInfo.setMembershipId(Long.parseLong(text));
                                    }
                                    break;
                                case 1: // First Name
                                    maxLenFirstName = Math.max(maxLenFirstName, text.length());
                                    text = cleanupName(text);
                                    usattPlayerInfo.setFirstName(text);
                                    break;
                                case 2: // Middle Name
                                case 3: // Nick Name
                                    // Skipped since they aren't in your UsattPlayerRecord entity
                                    break;
                                case 4: // Last Name
                                    maxLenLastName = Math.max(maxLenLastName, text.length());
                                    text = cleanupName(text);
                                    usattPlayerInfo.setLastName(text);
                                    break;
                                case 5: // Date Of Birth
                                    usattPlayerInfo.setDateOfBirth(parseDate(text, usattPlayerInfo));
                                    break;
                                case 6: // ZipCode
                                    maxLenZip = Math.max(maxLenZip, text.length());
                                    text = cleanupZipCode(text);
                                    usattPlayerInfo.setZip(text);
                                    break;
                                case 7: // Gender
                                    String gender = ("female".equalsIgnoreCase(text) || "F".equalsIgnoreCase(text)) ? "F" : "M";
                                    usattPlayerInfo.setGender(gender);
                                    break;
                                case 8: // CityTown
                                    usattPlayerInfo.setCity(text);
                                    break;
                                case 9: // State
                                    maxLenState = Math.max(maxLenState, text.length());
                                    if (text.length() <= 10) {
                                        usattPlayerInfo.setState(text);
                                    } else {
                                        logger.warn("State is too long: '" + text + "'");
                                        usattPlayerInfo.setCountry(text);
                                    }
                                    break;
                                case 10: // Primary Club
                                    usattPlayerInfo.setHomeClub(text);
                                    break;
                                case 11: // FinalRating
                                    if (StringUtils.isNotEmpty(text) && text.matches("\\d*")) {
                                        usattPlayerInfo.setTournamentRating(Integer.parseInt(text));
                                    } else {
                                        usattPlayerInfo.setTournamentRating(0);
                                    }
                                    break;
                                case 12: // League Rating
                                    if (StringUtils.isNotEmpty(text) && text.matches("\\d*")) {
                                        usattPlayerInfo.setLeagueRating(Integer.parseInt(text));
                                    } else {
                                        usattPlayerInfo.setLeagueRating(0);
                                    }
                                    break;
                                case 13: // Rating As Of Date (INTERCEPT BUG)
                                    if (StringUtils.isNotEmpty(text)) {
                                        // If it's all digits like "2010" or "1788", it's a rating, not a date!
                                        if (text.matches("\\d+")) {
                                            logger.debug("Skipping Rating As Of Date on row " + rowNumber + " because it contains a numeric rating: " + text + " player " + usattPlayerInfo.getLastName() + ", " + usattPlayerInfo.getFirstName());
                                        } else {
                                            usattPlayerInfo.setLastLeaguePlayedDate(parseDate(text, usattPlayerInfo));
                                        }
                                    }
                                    break;
                                case 14: // Latest tournament play date
                                    usattPlayerInfo.setLastTournamentPlayedDate(parseDate(text, usattPlayerInfo));
                                    break;
                                case 15: // Latest Membership expiry date
                                    usattPlayerInfo.setMembershipExpirationDate(parseDate(text, usattPlayerInfo));
                                    break;
                                case 16: // Latest Membership
                                    usattPlayerInfo.setMembershipType(text);
                                    break;
                                default:
                                    // Columns 17 to 20 are Background checks and SafeSport.
                                    // Skipped since they aren't in your UsattPlayerRecord entity.
                                    break;
                            }
                            columnNum++;
                        }

                        // Apply your cleanup edge case for Zips that hold country strings
                        moveZipToCountry(usattPlayerInfo);

                        // Validate critical values
                        if (StringUtils.isEmpty(usattPlayerInfo.getFirstName()) ||
                                StringUtils.isEmpty(usattPlayerInfo.getLastName()) ||
                                usattPlayerInfo.getMembershipId() == null) {

                            logger.warn("Missing critical values in record " + csvValues);
                            writer.write(csvValues + "\n");

                            badRecordsNum++;
                            if (ratingsProcessorStatus != null) {
                                ratingsProcessorStatus.badRecords = badRecordsNum;
                            }
                            continue;
                        }

                        if ("Historical Membership".equals(usattPlayerInfo.getMembershipType())) {
                            continue;
                        }

                        // Success: Add record and update processed counts in real-time
                        playerInfos.add(usattPlayerInfo);

                        if (ratingsProcessorStatus != null) {
                            ratingsProcessorStatus.processedRecords++;
                            ratingsProcessorStatus.totalRecords = rowNumber - 1;
                            if (ratingsProcessorStatus.processedRecords % 1000 == 0) {
                                logger.info("Processed " + ratingsProcessorStatus.processedRecords + " records");
                            }
                        }

                    } catch (Exception e) {
                        logger.error("Failed to parse row " + rowNumber + ": " + Arrays.toString(values), e);
                        badRecordsNum++;
                        if (ratingsProcessorStatus != null) {
                            ratingsProcessorStatus.badRecords = badRecordsNum;
                            ratingsProcessorStatus.totalRecords = rowNumber - 1;
                        }
                    }
                }

                logger.info("Total records processed = " + (rowNumber - 1));
                logger.info("Bad records = " + badRecordsNum);
                logger.info("Max lengths -> First Name: " + maxLenFirstName + ", Last Name: " + maxLenLastName + ", State: " + maxLenState + ", Zip: " + maxLenZip);
            }
        } catch (Exception e) {
            logger.error("Terminal error processing memberships file. Last processed line # " + rowNumber, e);
            if (ratingsProcessorStatus != null) {
                ratingsProcessorStatus.error = "File error at line " + rowNumber + ": " + e.getMessage();
            }
        } finally {
            if (ratingsProcessorStatus != null) {
                ratingsProcessorStatus.endTime = System.currentTimeMillis();
            }
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return playerInfos;
    }

     /**
     * Loads data from the membershps file into a list
     *
     * @param filename
     * @param ratingsProcessorStatus
     * @return
     */
    public List<UsattPlayerRecord> readMembershipFile(String filename, MembershipsProcessorStatus ratingsProcessorStatus) {
        List<UsattPlayerRecord> playerInfos = new ArrayList<>(15000);

        String[] values = null;
        int rowNumber = 0;
        try {
            logger.info("Processing memberships file " + filename);
            try (CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));) {
                int badRecordsNum = 0;
                int maxLenFirstName = 0;
                int maxLenLastName = 0;
                int maxLenZip = 0;
                int maxLenState = 0;
                while ((values = csvReader.readNext()) != null) {
                    rowNumber++;
                    if (rowNumber == 1) {
                        String actualHeaders = Arrays.stream(values)
                                .map(value -> (value == null) ? null : value.replace("\uFEFF", ""))
                                .collect(Collectors.joining(","));
                        // Normalize to NFC form (canonical composed)
                        actualHeaders = Normalizer.normalize(actualHeaders, Normalizer.Form.NFC);
                        // check headers and stop if they are not what is expected
                        // as of 10/25/2025 this includes membership type
                        String expectedHeaders = Normalizer.normalize(
                                "USATT #,Membership Expiration Date,Membership Type,First Name,Last Name,Gender,DOB,State,Zip Code,Rating,Last Played Date",
                                Normalizer.Form.NFC);
                        if (!expectedHeaders.equals(actualHeaders)) {
                            ratingsProcessorStatus.error = "Unexpected file format\n" +
                                    "Expected headers: " + expectedHeaders + "\n" +
                                    "Actual headers  : " + actualHeaders;
                            throw new RuntimeException(ratingsProcessorStatus.error);
                        }
                        continue;
                    }

                    int columnNum = 0;

                    UsattPlayerRecord usattPlayerInfo = new UsattPlayerRecord();
                    for (String text : values) {
                        switch (columnNum) {
                            case 0:
                                usattPlayerInfo.setMembershipId(Long.parseLong(text));
                                break;
                            case 1:
                                Date membershipExpiration = parseDate(text, usattPlayerInfo);
                                if (membershipExpiration != null) {
                                    usattPlayerInfo.setMembershipExpirationDate(membershipExpiration);
                                }
                                break;
                            case 2:
                                usattPlayerInfo.setMembershipType(text);
                                break;
                            case 3:
                                maxLenFirstName = Math.max(maxLenFirstName, text.length());
                                text = cleanupName(text);
                                usattPlayerInfo.setFirstName(text);
                                break;
                            case 4:
                                maxLenLastName = Math.max(maxLenLastName, text.length());
                                text = cleanupName(text);
                                usattPlayerInfo.setLastName(text);
                                break;
                            case 5:
                                String gender = ("female".equals(text)) ? "F" : "M";
                                usattPlayerInfo.setGender(gender);
                                break;
                            case 6:
                                usattPlayerInfo.setDateOfBirth(parseDate(text, usattPlayerInfo));
                                break;
                            case 7:
                                maxLenState = Math.max(maxLenState, text.length());
                                usattPlayerInfo.setState(text);
                                if (text.length() > 2) {
                                    usattPlayerInfo.setCountry(text);
                                }
                                break;
                            case 8:
                                maxLenZip = Math.max(maxLenZip, text.length());
                                text = cleanupZipCode(text);
                                usattPlayerInfo.setZip(text);
                                break;
                            case 9:
                                if (!text.isEmpty() && text.matches("\\d*")) {
                                    usattPlayerInfo.setTournamentRating(Integer.parseInt(text));
                                } else {
                                    usattPlayerInfo.setTournamentRating(0);
                                }
                                break;
                            case 10:
                                usattPlayerInfo.setLastTournamentPlayedDate(parseDate(text, usattPlayerInfo));
                                break;
                            default:
                                break;
                        }
                        columnNum++;
                    }
                    if (StringUtils.isEmpty(usattPlayerInfo.getFirstName()) ||
                            StringUtils.isEmpty(usattPlayerInfo.getLastName())) {
                        String csvValues = StringUtils.joinWith(",", Arrays.stream(values).toArray());
                        logger.warn("Missing critical values in record " + csvValues);

                        badRecordsNum++;
                        ratingsProcessorStatus.badRecords = badRecordsNum;
                        continue;
                    }
                    playerInfos.add(usattPlayerInfo);
                    ratingsProcessorStatus.totalRecords = rowNumber - 1;
                }

                logger.info("total records = " + rowNumber);
                logger.info("bad   records = " + badRecordsNum);
                logger.info("maxLenFirstName = " + maxLenFirstName);
                logger.info("maxLenLastName  = " + maxLenLastName);
                logger.info("maxLenState     = " + maxLenState);
                logger.info("maxLenZip       = " + maxLenZip);
            }
        } catch (Exception e) {
            logger.error("Error processing line # " + rowNumber + " with values: " + Arrays.toString(values), e);
        }
        return playerInfos;
    }


    private String cleanupZipCode(String text) {
        // remove .0 from 89456.0
        if (StringUtils.isNotEmpty(text)) {
            text = text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
            // zip code was treated as a number - preceed it with 0s e.g. 926 is really 00926
            if (text.length() < 5 && text.matches("\\d*")) {
                text = "%05d".formatted(Integer.parseInt(text));
            }
        }
        return text;
    }

    /**
     * Removes non-breaking space from strings and trims it
     * @param text
     * @return
     */
    private String cleanupName(String text) {
        text = text.replace("\u00A0", "");
        return text.trim();
    }

    /**
     * @param strDate
     * @param usattPlayerInfo
     * @return
     */
    private Date parseDate(String strDate, UsattPlayerRecord usattPlayerInfo) {
        Date date = null;
        try {
            if (!StringUtil.isBlank(strDate)) {
                date = ALT_DATE_FORMAT.parse(strDate);
            }
        } catch (ParseException e) {
            try {
                date = DATE_FORMAT.parse(strDate);
            } catch (ParseException ex) {
                logger.error("unable to parse date '" + strDate + "' for player " + usattPlayerInfo.getFirstName() + " " + usattPlayerInfo.getLastName(), e);
            }
        }

        return (date != null) ? new Timestamp(date.getTime()) : null;
    }

    void moveZipToCountry(UsattPlayerRecord usattPlayerRecord) {
        String zip = usattPlayerRecord.getZip();
        if (isZipACountry(zip)) {
            usattPlayerRecord.setCountry(zip);
            usattPlayerRecord.setZip("");
        }
    }

    boolean isZipACountry(String zip) {
        return zip.length() > 10;
//        return (zip.matches("(\\d*(-\\d*)?)|([a-zA-z0-9\\s]*)"));
    }

    /**
     * Imports changes to the records
     *
     * @param recordsToImport
     * @param ratingsProcessorStatus
     */
    public void insertPlayerData(List<UsattPlayerRecord> recordsToImport, RatingsProcessorStatus ratingsProcessorStatus) {

        int startingIndex = 0;
        int BATCH_SIZE = 100;
        int endingIndex = startingIndex;
        List<Long> batchOfIds = new ArrayList<>(BATCH_SIZE);
        List<UsattPlayerRecord> newRecords = new ArrayList<>();
        long start = System.currentTimeMillis();
        Date today = new Date();
        logger.info("recordsToImport = " + recordsToImport.size());
        ratingsProcessorStatus.newRecords = 0;
        ratingsProcessorStatus.processedRecords = 0;
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        do {
            batchOfIds.clear();
            newRecords.clear();

            endingIndex = Math.min((startingIndex + BATCH_SIZE), recordsToImport.size());

            // get a sublist of updated records
            logger.info("Processing records sublist (" + startingIndex + ", " + endingIndex + ")");
            List<UsattPlayerRecord> subList = recordsToImport.subList(startingIndex, endingIndex);
            startingIndex = endingIndex;

            for (UsattPlayerRecord usattPlayerRecord : subList) {
                Long membershipId = usattPlayerRecord.getMembershipId();
                if (membershipId != null) {
                    batchOfIds.add(membershipId);
                }
            }

            // fetch records with same membership id from database
            List<UsattPlayerRecord> batchOfExistingRecords = this.playerRecordRepository.findAllByMembershipIdIn(batchOfIds);
            List<RatingHistoryRecord> ratingHistoryRecordList = new ArrayList<>(subList.size());
            List<RatingHistoryRecord> updatedRatingHistoryRecordList = new ArrayList<>(subList.size());
            for (UsattPlayerRecord updatedRecord : subList) {
                boolean found = false;
                for (UsattPlayerRecord existingRecord : batchOfExistingRecords) {
                    // find existing record in the batch
                    if (updatedRecord.getMembershipId().equals(existingRecord.getMembershipId())) {
                        found = true;

                        // rating or last played date changed
                        if (existingRecord.getTournamentRating() != updatedRecord.getTournamentRating() && updatedRecord.getLastTournamentPlayedDate() != null) {
                            Date initialRatingDate = (existingRecord.getLastTournamentPlayedDate() != null) ? existingRecord.getLastTournamentPlayedDate() : today;
                            Date lastPlayedDate = (updatedRecord.getLastTournamentPlayedDate() != null) ? updatedRecord.getLastTournamentPlayedDate() : today;
                            if (DateUtils.truncatedCompareTo(initialRatingDate, lastPlayedDate, Calendar.DAY_OF_MONTH) != 0) {
                                // new history record
                                RatingHistoryRecord ratingHistoryRecord = new RatingHistoryRecord();
                                ratingHistoryRecord.setMembershipId(existingRecord.getMembershipId());
                                ratingHistoryRecord.setInitialRating(existingRecord.getTournamentRating());
                                ratingHistoryRecord.setInitialRatingDate(initialRatingDate);
                                ratingHistoryRecord.setFinalRating(updatedRecord.getTournamentRating());
                                ratingHistoryRecord.setFinalRatingDate(updatedRecord.getLastTournamentPlayedDate());
                                Optional<RatingHistoryRecord> optExistingRatingHistoryRecord = this.ratingHistoryRecordRepository.findByMembershipIdAndFinalRatingDate(
                                        ratingHistoryRecord.getMembershipId(), ratingHistoryRecord.getFinalRatingDate());
                                if (optExistingRatingHistoryRecord.isEmpty()) {
                                    ratingHistoryRecordList.add(ratingHistoryRecord);
                                } else {
                                    logger.warn("Found duplicate new history record" + ratingHistoryRecord);
                                }
                            } else {
                                // same last played date means it is a ratings update so we need to update existing record
                                // there should be few of these
                                Optional<RatingHistoryRecord> optExistingRatingHistoryRecord = this.ratingHistoryRecordRepository.findByMembershipIdAndFinalRatingDate(
                                        existingRecord.getMembershipId(), existingRecord.getLastTournamentPlayedDate());
                                if (optExistingRatingHistoryRecord.isPresent()) {
                                    RatingHistoryRecord existingRatingHistoryRecord = optExistingRatingHistoryRecord.get();
                                    logger.info("Fixing rating history rating for " + existingRecord.getLastName() +
                                            ", " + existingRecord.getFirstName() +
                                            " Id: " + existingRecord.getMembershipId() +
                                            " as of: " + dateFormat.format(existingRecord.getLastTournamentPlayedDate()) +
                                            " from " + existingRatingHistoryRecord.getFinalRating() +
                                            " to " + updatedRecord.getTournamentRating());
                                    existingRatingHistoryRecord.setFinalRating(updatedRecord.getTournamentRating());
                                    updatedRatingHistoryRecordList.add(existingRatingHistoryRecord);
                                }
                            }
                        }
                        // merge updated & existing record
                        existingRecord.setMembershipExpirationDate(updatedRecord.getMembershipExpirationDate());
                        existingRecord.setTournamentRating(updatedRecord.getTournamentRating());
                        // player might have moved to another state/zip or finally provided it
                        if (StringUtils.isNotEmpty(updatedRecord.getState())) {
                            existingRecord.setState(updatedRecord.getState());
                        }
                        if (StringUtils.isNotEmpty(updatedRecord.getZip())) {
                            existingRecord.setZip(updatedRecord.getZip());
                        }

                        // changed/corrected spelling of name
                        existingRecord.setFirstName(updatedRecord.getFirstName());
                        existingRecord.setLastName(updatedRecord.getLastName());

                        if (updatedRecord.getDateOfBirth() != null) {
                            existingRecord.setDateOfBirth(updatedRecord.getDateOfBirth());
                        }

                        if (updatedRecord.getLastTournamentPlayedDate() != null) {
                            existingRecord.setLastTournamentPlayedDate(updatedRecord.getLastTournamentPlayedDate());
                        }
                        if (updatedRecord.getLastLeaguePlayedDate() != null) {
                            existingRecord.setLastLeaguePlayedDate(updatedRecord.getLastLeaguePlayedDate());
                        }
                        if (existingRecord.getGender() == null && updatedRecord.getGender() != null) {
                            existingRecord.setGender(updatedRecord.getGender());
                        }
                        break;
                    }
                }
                if (!found) {
                    newRecords.add(updatedRecord);

                    RatingHistoryRecord ratingHistoryRecord = new RatingHistoryRecord();
                    ratingHistoryRecordList.add(ratingHistoryRecord);

                    ratingHistoryRecord.setMembershipId(updatedRecord.getMembershipId());
                    ratingHistoryRecord.setInitialRating(0);
                    if (updatedRecord.getLastTournamentPlayedDate() != null) {
                        long initialRatingDate = updatedRecord.getLastTournamentPlayedDate().getTime();
                        initialRatingDate -= (1000L * 60 * 60 * 24);  // 1 day before
                        Timestamp tsInitialRatingDate = new Timestamp(initialRatingDate);
                        ratingHistoryRecord.setInitialRatingDate(tsInitialRatingDate);
                        ratingHistoryRecord.setFinalRatingDate(updatedRecord.getLastTournamentPlayedDate());
                    } else {
                        long initialRatingDate = today.getTime();
                        initialRatingDate -= (1000L * 60 * 60 * 24);  // 1 day before
                        Timestamp tsInitialRatingDate = new Timestamp(initialRatingDate);
                        ratingHistoryRecord.setInitialRatingDate(tsInitialRatingDate);
                        ratingHistoryRecord.setFinalRatingDate(today);
                    }
                    ratingHistoryRecord.setFinalRating(updatedRecord.getTournamentRating());
                }
            }

            if (batchOfExistingRecords.size() > 0) {
                try {
                    this.playerRecordRepository.saveAllAndFlush(batchOfExistingRecords);
                } catch (Exception e) {
                    System.out.println("batchOfExistingRecords = " + batchOfExistingRecords);

                    throw new RuntimeException(e);
                }
            }

            if (!newRecords.isEmpty()) {
                ratingsProcessorStatus.newRecords += newRecords.size();
//                replaceTemporaryMembershipId(newRecords);
            }

            ratingsProcessorStatus.processedRecords = endingIndex;

            if (ratingHistoryRecordList.size() > 0) {
                logger.info("Inserting " + ratingHistoryRecordList.size() + " rating history records");
//                logger.info("new rating history records: " + ratingHistoryRecordList);
                this.ratingHistoryRecordRepository.saveAllAndFlush(ratingHistoryRecordList);
            }

            if (updatedRatingHistoryRecordList.size() > 0) {
                logger.info("Updating " + updatedRatingHistoryRecordList.size() + " rating history records");
//                logger.info("updated rating history records: " + updatedRatingHistoryRecordList);
                this.ratingHistoryRecordRepository.saveAllAndFlush(updatedRatingHistoryRecordList);
            }
            ratingsProcessorStatus.newHistoryRecords += ratingHistoryRecordList.size();
            ratingsProcessorStatus.updatedHistoryRecords += updatedRatingHistoryRecordList.size();

        } while (startingIndex < (recordsToImport.size() - 1));

        long duration = (System.currentTimeMillis() - start) / 1000;
        logger.info("Finished processing " + endingIndex + " records in " + duration + " seconds.");
    }

    /**
     * Inserts new records of members who paid to USATT directly and are not in the regular ratings file because they didn't play in a tournament yet.
     *
     * @param recordsToImport list of new member records to import
     * @param membershipsProcessorStatus status object
     */
    public void insertNewMembers(List<UsattPlayerRecord> recordsToImport, MembershipsProcessorStatus membershipsProcessorStatus) {
        int startingIndex = 0;
        int BATCH_SIZE = 100;
        int endingIndex = startingIndex;
        List<Long> batchOfIds = new ArrayList<>(BATCH_SIZE);
        List<UsattPlayerRecord> newRecords = new ArrayList<>();
        long start = System.currentTimeMillis();
        Date today = new Date();
        logger.info("recordsToImport = " + recordsToImport.size());
        membershipsProcessorStatus.newRecords = 0;
        membershipsProcessorStatus.processedRecords = 0;
        do {
            batchOfIds.clear();
            newRecords.clear();

            endingIndex = Math.min((startingIndex + BATCH_SIZE), recordsToImport.size());

            // get a sublist of updated records
            logger.info("Processing records sublist (" + startingIndex + ", " + endingIndex + ")");
            List<UsattPlayerRecord> subList = recordsToImport.subList(startingIndex, endingIndex);
            startingIndex = endingIndex;

            for (UsattPlayerRecord usattPlayerRecord : subList) {
                Long membershipId = usattPlayerRecord.getMembershipId();
                if (membershipId != null) {
                    batchOfIds.add(membershipId);
                }
            }

            // fetch records with same membership id from database
            List<UsattPlayerRecord> batchOfExistingRecords = this.playerRecordRepository.findAllByMembershipIdIn(batchOfIds);
            List<RatingHistoryRecord> ratingHistoryRecordList = new ArrayList<>(subList.size());
            List<RatingHistoryRecord> updatedRatingHistoryRecordList = new ArrayList<>(subList.size());
            for (UsattPlayerRecord updatedRecord : subList) {
                boolean found = false;
                for (UsattPlayerRecord existingRecord : batchOfExistingRecords) {
                    // find existing record in the batch
                    if (updatedRecord.getMembershipId().equals(existingRecord.getMembershipId())) {
                        found = true;
                    }
                }
                if (!found) {
                    newRecords.add(updatedRecord);
                }
            }

            if (!newRecords.isEmpty()) {
                membershipsProcessorStatus.newRecords += newRecords.size();
//                replaceTemporaryMembershipId(newRecords);
            }
            membershipsProcessorStatus.processedRecords = endingIndex;

        } while (startingIndex < (recordsToImport.size() - 1));

        long duration = (System.currentTimeMillis() - start) / 1000;
        logger.info("Finished processing " + endingIndex + " records in " + duration + " seconds.");
    }

    /**
     * Finds a temporary USATT record and replace it temporary membership id with a permanent one
     *
     * @param newRecords
     * @return
     */
    private void replaceTemporaryMembershipId (List<UsattPlayerRecord> newRecords) {
        logger.info("Looking for new players who have been assigned a permanent membership id");
        Map<Long, Long> oldToNewMembershipIdMap = new HashMap<>();
        for (UsattPlayerRecord newRecord : newRecords) {
            try {
                logger.info("Trying to find temporary membership record for " + newRecord.getLastName() + ", " + newRecord.getFirstName());
                UsattPlayerRecord playerRecord = playerRecordRepository.getFirstByFirstNameIgnoreCaseAndLastNameIgnoreCase(newRecord.getFirstName(), newRecord.getLastName());
                if (playerRecord != null) {
                    Long membershipId = playerRecord.getMembershipId();
                    if (membershipId >= 400000 && membershipId <= 500000) {
                        logger.info("Found temporary player record with membership id " + membershipId + " to be updated to " + newRecord.getMembershipId() + ". Deleting...");
                        playerRecordRepository.deleteById(playerRecord.getId());
                        oldToNewMembershipIdMap.put(membershipId, newRecord.getMembershipId());
                    } else {
                        logger.info("Temporary membership not found");
                    }
                } else {
                    logger.warn("Didn't find player named " + newRecord.getLastName() + ", " + newRecord.getFirstName() + ". Fix manually " + newRecord.getMembershipId());
                }
            } catch (Exception e) {
                logger.error("Unable to delete temporary membership id and update profile ext");
            }

            try {
                this.playerRecordRepository.saveAllAndFlush(newRecords);
            } catch (Exception e) {
                logger.error("Unable to save " + newRecords.size() + " new player records");
                for (UsattPlayerRecord record : newRecords) {
                    logger.error("Record: " + record);
                }
                throw new RuntimeException("Unable to save " + newRecords.size() + " new player records",e);
            }

            // now update profile ext linking usatt record to profile
            for (Long oldMembershipId : oldToNewMembershipIdMap.keySet()) {
                if (userProfileExtService.existsByMembershipId(oldMembershipId)) {
                    Long newMembershipId = oldToNewMembershipIdMap.get(oldMembershipId);
                    logger.info("Changing ext user profile from temp membership id " + oldMembershipId + " to " + newMembershipId);
                    UserProfileExt userProfileExt = userProfileExtService.getByMembershipId(oldMembershipId);
                    userProfileExt.setMembershipId(newMembershipId);
                    userProfileExtService.save(userProfileExt);
                }
            }
        }
    }

    /**
     * Gets current total count
     *
     * @return
     */
    public long getTotalCount() {
        return playerRecordRepository.count();
    }

    /**
     * Gets the rating of a player that he had on the particular date
     *
     * @param membershipId
     * @param dateOfRating
     * @return
     */
    public int getPlayerRatingAsOfDate(long membershipId, Date dateOfRating) {
        int rating = 0;
        List<RatingHistoryRecord> historyRecords = this.ratingHistoryRecordRepository.getPlayerRatingAsOfDate(membershipId, dateOfRating);
        if (historyRecords != null && historyRecords.size() > 0) {
            RatingHistoryRecord ratingHistoryRecord = historyRecords.get(0);
            rating = ratingHistoryRecord.getFinalRating();
        } else {
            UsattPlayerRecord currentPlayerRecord = this.playerRecordRepository.getFirstByMembershipId(membershipId);
            if (currentPlayerRecord != null) {
                rating = currentPlayerRecord.getTournamentRating();
            }
        }
        return rating;
    }

    /**
     * Gets all player records matching the membership id
     *
     * @param membershipIds
     * @return
     */
    public List<UsattPlayerRecord> findAllByMembershipIdIn(List<Long> membershipIds) {
        return this.playerRecordRepository.findAllByMembershipIdIn(membershipIds);
    }

    public List<UsattPlayerRecord> findMembershipStatus(List<String> playerFullNames) {
        List<Object[]> results = this.playerRecordRepository.findMembershipStatus(playerFullNames);
        List<UsattPlayerRecord> membersWithStatus = new ArrayList<>(results.size());
        for (Object[] row : results) {
            String fullName = (String) row[0];
            String [] nameParts = fullName.split(", ");
            String lastName = nameParts[0];
            String firstName = nameParts[1];
            Long membershipId = (row[1] != null) ? ((Number) row[1]).longValue() : 0L;
            String gender = "" + (Character) row[2];
            String city = (String) row[3];
            String state = (String) row[4];
            String zip = (String) row[5];
            int tournamentRating = (row[6] != null) ? ((Number) row[6]).intValue() : 0;
            UsattPlayerRecord usattPlayerRecord = new UsattPlayerRecord();
            usattPlayerRecord.setLastName(lastName);
            usattPlayerRecord.setFirstName(firstName);
            usattPlayerRecord.setMembershipId(membershipId);
            usattPlayerRecord.setGender(gender);
            usattPlayerRecord.setState(city);
            usattPlayerRecord.setState(state);
            usattPlayerRecord.setZip(zip);
            usattPlayerRecord.setTournamentRating(tournamentRating);

            membersWithStatus.add(usattPlayerRecord);
        }
        return membersWithStatus;
    }
}





