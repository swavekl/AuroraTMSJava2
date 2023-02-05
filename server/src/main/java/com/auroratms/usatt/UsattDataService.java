package com.auroratms.usatt;

import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.ratingsprocessing.RatingsProcessorStatus;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


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
            System.out.println("oldUserProfileExt = " + oldUserProfileExt);
            userProfileExt.setClubFk(oldUserProfileExt.getClubFk());
            userProfileExtService.delete(oldUserProfileExt.getProfileId());
        }
        System.out.println("userProfileExt = " + userProfileExt);
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

    /**
     * Loads data from the ratings file into a list
     *
     * @param filename
     * @param ratingsProcessorStatus
     * @return
     */
    public List<UsattPlayerRecord> readAllPlayersFromFile(String filename, RatingsProcessorStatus ratingsProcessorStatus) {
        List<UsattPlayerRecord> playerInfos = new ArrayList<>(63000);

        try {
            logger.info("Processing ratings file " + filename);
            try (CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));) {
                String[] values = null;
                int rowNumber = 0;
                int badRecordsNum = 0;
                int maxLenFirstName = 0;
                int maxLenLastName = 0;
                int maxLenZip = 0;
                int maxLenState = 0;
                while ((values = csvReader.readNext()) != null) {
                    rowNumber++;
                    if (rowNumber == 1) {
                        continue;
                    }

                    int columnNum = 0;

                    UsattPlayerRecord usattPlayerInfo = new UsattPlayerRecord();
                    if (values.length != 10) {
                        String csvValues = StringUtils.joinWith(",", Arrays.stream(values).toArray());
                        logger.warn("Insufficient values in record (" + values.length + ") " + csvValues);
                        logger.info("Attempting parsing again");
                        values = csvValues.split(",");
                        if (values.length != 10) {
                            badRecordsNum++;
                            continue;
                        } else {
                            logger.info("Parsing succeeded");
                        }
                    }
                    // Member ID	Last Name	First Name	Rating	State	Zip	Gender	Date of Birth	Expiration Date	Last Played Date
                    for (String text : values) {
                        switch (columnNum) {
                            case 0:
                                usattPlayerInfo.setMembershipId(Long.parseLong(text));
                                break;
                            case 1:
                                maxLenLastName = Math.max(maxLenLastName, text.length());
                                text = cleanupName(text);
                                usattPlayerInfo.setLastName(text);
                                break;
                            case 2:
                                maxLenFirstName = Math.max(maxLenFirstName, text.length());
                                text = cleanupName(text);
                                usattPlayerInfo.setFirstName(text);
                                break;
                            case 3:
                                usattPlayerInfo.setTournamentRating(Integer.parseInt(text));
                                break;
                            case 4:
                                maxLenState = Math.max(maxLenState, text.length());
                                usattPlayerInfo.setState(text);
                                break;
                            case 5:
                                maxLenZip = Math.max(maxLenZip, text.length());
                                usattPlayerInfo.setZip(text);
                                break;
                            case 6:
                                usattPlayerInfo.setGender(text);
                                break;
                            case 7:
                                usattPlayerInfo.setDateOfBirth(parseDate(text));
                                break;
                            case 8:
                                Date membershipExpiration = parseDate(text);
                                if (membershipExpiration != null) {
                                    usattPlayerInfo.setMembershipExpirationDate(membershipExpiration);
                                }
                                break;
                            case 9:
                                usattPlayerInfo.setLastTournamentPlayedDate(parseDate(text));
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
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return playerInfos;
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
     * @return
     */
    private Date parseDate(String strDate) {
        Date date = null;
        try {
            if (!StringUtil.isBlank(strDate)) {
                date = DATE_FORMAT.parse(strDate);
            }
        } catch (ParseException e) {
            try {
                date = ALT_DATE_FORMAT.parse(strDate);
            } catch (ParseException ex) {
                logger.error("unable to parse date '" + strDate + "' ", e);
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

                        if (updatedRecord.getFirstName().equals("Matthew") && updatedRecord.getLastName().equals("Chamblee")) {
                            System.out.println("existingRecord = " + existingRecord);
                        }

                        // rating or last played date changed
                        if (existingRecord.getTournamentRating() != updatedRecord.getTournamentRating()) {

                            if (!existingRecord.getLastTournamentPlayedDate().equals(updatedRecord.getLastTournamentPlayedDate())) {
                                // new history record
                                RatingHistoryRecord ratingHistoryRecord = new RatingHistoryRecord();
                                ratingHistoryRecordList.add(ratingHistoryRecord);

                                ratingHistoryRecord.setMembershipId(existingRecord.getMembershipId());
                                ratingHistoryRecord.setInitialRating(existingRecord.getTournamentRating());
                                ratingHistoryRecord.setInitialRatingDate(existingRecord.getLastTournamentPlayedDate());
                                ratingHistoryRecord.setFinalRating(updatedRecord.getTournamentRating());
                                ratingHistoryRecord.setFinalRatingDate(updatedRecord.getLastTournamentPlayedDate());
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
                    long initialRatingDate = updatedRecord.getLastTournamentPlayedDate().getTime();
                    initialRatingDate -= (1000L * 60 * 60 * 24);  // 1 day before
                    Timestamp tsInitialRatingDate = new Timestamp(initialRatingDate);
                    ratingHistoryRecord.setInitialRatingDate(tsInitialRatingDate);
                    ratingHistoryRecord.setFinalRating(updatedRecord.getTournamentRating());
                    ratingHistoryRecord.setFinalRatingDate(updatedRecord.getLastTournamentPlayedDate());
                }
            }

            if (batchOfExistingRecords.size() > 0) {
                this.playerRecordRepository.saveAllAndFlush(batchOfExistingRecords);
            }

            if (newRecords.size() > 0) {
                this.playerRecordRepository.saveAllAndFlush(newRecords);
                ratingsProcessorStatus.newRecords += newRecords.size();
            }

            ratingsProcessorStatus.processedRecords = endingIndex;

            if (ratingHistoryRecordList.size() > 0) {
                logger.info("Inserting " + ratingHistoryRecordList.size() + " rating history records");
                this.ratingHistoryRecordRepository.saveAllAndFlush(ratingHistoryRecordList);
            }

            if (updatedRatingHistoryRecordList.size() > 0) {
                logger.info("Updating " + updatedRatingHistoryRecordList.size() + " rating history records");
                this.ratingHistoryRecordRepository.saveAllAndFlush(updatedRatingHistoryRecordList);
            }
            ratingsProcessorStatus.newHistoryRecords += ratingHistoryRecordList.size();
            ratingsProcessorStatus.updatedHistoryRecords += updatedRatingHistoryRecordList.size();

        } while (startingIndex < (recordsToImport.size() - 1));

        long duration = (System.currentTimeMillis() - start) / 1000;
        logger.info("Finished processing " + endingIndex + " records in " + duration + " seconds.");
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
}





