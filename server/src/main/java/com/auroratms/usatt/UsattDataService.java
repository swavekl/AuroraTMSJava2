package com.auroratms.usatt;

import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Service
@CacheConfig(cacheNames = {"player-data"})
@Transactional
public class UsattDataService {

    @Autowired
    private UsattPlayerRecordRepository playerRecordRepository;

    @Autowired
    private RatingHistoryRecordRepository ratingHistoryRecordRepository;

    @Autowired
    private UserProfileExtService userProfileExtService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    public List<UsattPlayerRecord> findAllPlayersByNames(String firstName, String lastName, Pageable pageable) {
        return this.playerRecordRepository.findAllByFirstNameOrLastName(firstName, lastName, pageable);
    }

    public UsattPlayerRecord getPlayerByMembershipId(Long membershipId) {
        return this.playerRecordRepository.getFirstByMembershipId(membershipId);
    }

    public UsattPlayerRecord getPlayerByNames (String firstName, String lastName) {
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
            Calendar calendar = Calendar.getInstance();
            calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date expired = calendar.getTime();
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
        userProfileExtService.save(userProfileExt);

        return recordToReturn;
    }

    /**
     * Loads data from the ratings file into a list
     * @param filename
     * @return
     */
    public List<UsattPlayerRecord> readAllPlayersFromFile (String filename) {
        List<UsattPlayerRecord> playerInfos = new ArrayList<>(63000);

        try {
            try (CSVReader csvReader = new CSVReader(new FileReader(filename));) {
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
                        System.out.println("skip header");
                        continue;
                    }

                    int columnNum = 0;

                    UsattPlayerRecord usattPlayerInfo = new UsattPlayerRecord();
                    if (values.length != 10) {
                        System.out.print("Insufficient values in record ");
                        for (String value : values) {
                            System.out.print(value + ", ");
                        }
                        System.out.println();
                        badRecordsNum++;
                        continue;
                    }
                    // Member ID	Last Name	First Name	Rating	State	Zip	Gender	Date of Birth	Expiration Date	Last Played Date
                    for (String text : values) {
                        switch (columnNum) {
                            case 0:
                                usattPlayerInfo.setMembershipId(Long.parseLong(text));
                                break;
                            case 1:
                                maxLenLastName = Math.max(maxLenLastName, text.length());
                                usattPlayerInfo.setLastName(text);
                                break;
                            case 2:
                                maxLenFirstName = Math.max(maxLenFirstName, text.length());
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
                            StringUtils.isEmpty(usattPlayerInfo.getLastName()) ||
                            (usattPlayerInfo.getMembershipExpirationDate() == null) ||
                            StringUtils.isEmpty(usattPlayerInfo.getGender())) {
                        System.out.print("Insufficient critical values in record ");
                        for (String value : values) {
                            System.out.print(value + ", ");
                        }
                        System.out.println();
                        badRecordsNum++;
                       continue;
                    }
                    playerInfos.add(usattPlayerInfo);
                }

                System.out.println("total records = " + rowNumber);
                System.out.println("bad   records = " + badRecordsNum);
                System.out.println("maxLenFirstName = " + maxLenFirstName);
                System.out.println("maxLenLastName  = " + maxLenLastName);
                System.out.println("maxLenState     = " + maxLenState);
                System.out.println("maxLenZip       = " + maxLenZip);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
        return playerInfos;
    }

    /**
     *
     * @param strDate
     * @return
     */
    private Date parseDate (String strDate) {
        Date date = null;
        try {
            if (!StringUtil.isBlank(strDate)) {
                date = DATE_FORMAT.parse(strDate);
            }
        } catch (ParseException e) {
            System.out.println("unable to parse date '" + strDate + "' " + e);
        }
        return date;
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
     * @param recordsToImport
     */
    public void insertPlayerData (List<UsattPlayerRecord> recordsToImport) {

        int startingIndex = 0;
        int BATCH_SIZE = 100;
        int endingIndex = startingIndex;
        List<Long> batchOfIds = new ArrayList<>(BATCH_SIZE);
        List<UsattPlayerRecord> newRecords = new ArrayList<>();
        System.out.println("recordsToImport = " + recordsToImport.size());
        do {
            batchOfIds.clear();
            newRecords.clear();

            endingIndex = ((startingIndex + BATCH_SIZE) < recordsToImport.size())
                    ? (startingIndex + BATCH_SIZE)
                    : recordsToImport.size();
//            endingIndex = endingIndex - 1;  // sublist ending index is exclusive

            // get a sublist of updated records
            System.out.println("startingIndex = " + startingIndex + " endingIndex = " + endingIndex);
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
            for (UsattPlayerRecord updatedRecord : subList) {
                boolean found = false;
                for (UsattPlayerRecord existingRecord : batchOfExistingRecords) {
                    // find existing record in the batch
                    if (updatedRecord.getMembershipId().equals(existingRecord.getMembershipId())) {
                        found = true;
                        // merge updated & existing record
                        existingRecord.setMembershipExpirationDate(updatedRecord.getMembershipExpirationDate());
                        break;
                    }
                }
                if (!found) {
                    newRecords.add(updatedRecord);
                }
            }

            if (batchOfExistingRecords.size() > 0) {
                this.playerRecordRepository.saveAll(batchOfExistingRecords);
            }

            if (newRecords.size() > 0) {
                this.playerRecordRepository.saveAll(newRecords);
            }
            this.playerRecordRepository.flush();

        } while (startingIndex < (recordsToImport.size() - 1));
    }

    /**
     * Gets current total count
     * @return
     */
    public long getTotalCount() {
        return playerRecordRepository.count();
    }

    /**
     * Gets the rating of a player that he had on the particular date
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
     * @param membershipIds
     * @return
     */
    public List<UsattPlayerRecord> findAllByMembershipIdIn(List<Long> membershipIds) {
        return this.playerRecordRepository.findAllByMembershipIdIn(membershipIds);
    }
}

//    private String baseURL = "https://usatt.simplycompete.com/userAccount/s2?citizenship=usOnly&gamesEligibility=&gender=&minAge=&maxAge=&minTrnRating=&maxTrnRating=&minLeagueRating=&maxLeagueRating=&state=&region=Any Region&favorites=&q=${query}&displayColumns=First Name,Last Name,USATT#,Location,Home Club,Tournament Rating,Last Played Tournament,League Rating,Last Played League,Membership Expiration&pageSize=25";


//    public UsattPlayerInfo getPlayerInfo(String firstName, String lastName) {
//        UsattPlayerInfo usattPlayerInfo = new UsattPlayerInfo();
//        try {
//            String url = getUrl(firstName, lastName);
//
////            File input = new File("C:\\Users\\Swavek\\AppData\\Roaming\\JetBrains\\IntelliJIdea2020.3\\scratches\\scratch_8.html");
////            Document doc = Jsoup.parse(input, "UTF-8", "https://usatt.simplycompete.com/");
//            Document doc = makeRequest(url);
//            // <table class="table table-striped table-condensed list-area">
//            Elements listTables = doc.select("table.list-area");
//            for (Element table : listTables) {
//                Elements tableRows = table.select("tbody tr.list-item");
//                for (Element tableRow : tableRows) {
//                    Elements rowColumns = tableRow.select("td.list-column");
//                    // First Name,Last Name,USATT#,Location,Home Club,Tournament Rating,Last Played Tournament,
//                    // League Rating,Last Played League,Membership Expiration
//                    //<td class="list-item-bar"></td>
//                    //<td class="list-column">1</td>
//                    //<td class="list-column"><a href="/userAccount/up/1866">Samson</a></td>
//                    //<td class="list-column"><a href="/userAccount/up/1866">Dubina</a></td>
//                    //<td class="list-column">9051</td>
//                    //<td class="list-column">Akron, OH</td>
//                    //<td class="list-column"><a href="/c/p/183">Samson Dubina Table Tennis Academy</a></td>
//                    //<td class="list-column text-center">2443</td>
//                    //<td class="list-column">03/08/2020</td>
//                    //<td class="list-column text-center">2435</td>
//                    //<td class="list-column">01/23/2016</td>
//                    //<td class="list-column">02/28/2100</td>
//                    int columnNum = 0;
//                    for (Element rowColumn : rowColumns) {
//                        String text = rowColumn.text();
////                        System.out.println("text = " + text);
//                        switch (columnNum) {
//                            case 0: // skip row number
//                                break;
//                            case 1:
//                                usattPlayerInfo.setFirstName(text);
//                                break;
//                            case 2:
//                                usattPlayerInfo.setLastName(text);
//                                break;
//                            case 3:
//                                usattPlayerInfo.setMembershipId(Long.parseLong(text));
//                                break;
//                            case 4:
//                                usattPlayerInfo.setLocation(text);
//                                break;
//                            case 5:
//                                usattPlayerInfo.setHomeClub(text);
//                                break;
//                            case 6:
//                                usattPlayerInfo.setTournamentRating(Integer.parseInt(text));
//                                break;
//                            case 7:
//                                usattPlayerInfo.setLastTournamentPlayedDate(parseDate(text));
//                                break;
//                            case 8:
//                                usattPlayerInfo.setLeagueRating(Integer.parseInt(text));
//                                break;
//                            case 9:
//                                usattPlayerInfo.setLastLeaguePlayedDate(parseDate(text));
//                                break;
//                            case 10:
//                                usattPlayerInfo.setMembershipExpiration(parseDate(text));
//                                break;
//                            default:
//                                break;
//                        }
//                        columnNum++;
//                    }
//                }
//            }
//        } catch (IOException e) {
//
//        }
//
//        return usattPlayerInfo;
//    }
//
//    String getUrl(String firstName, String lastName) {
//        String url = baseURL;
//        url = url.replace("${query}", lastName);
//        url = url.replaceAll("\\s", "+");
//        return url;
//    }
//
//
//    private Document makeRequest(String url) throws IOException {
//
//        // Firefox
//        return Jsoup.connect(url)
//                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:85.0) Gecko/20100101 Firefox/85.0")
//                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
//                .header("Accept-Encoding", "gzip, deflate, br")
//                .header("Accept-Language", "en-US,en;q=0.5")
//                .header("Host", "usatt.simplycompete.com")
//                .header("Connection", "keep-alive")
//                .header("Referrer", "https://usatt.simplycompete.com/userAccount/s2?citizenship=usOnly&gamesEligibility=&gender=&minAge=&maxAge=&minTrnRating=&maxTrnRating=&minLeagueRating=&maxLeagueRating=&state=&region=Any+Region&favorites=&q=Samson&displayColumns=First+Name&displayColumns=Last+Name&displayColumns=USATT%23&displayColumns=Location&displayColumns=Home+Club&displayColumns=Tournament+Rating&displayColumns=Last+Played+Tournament&displayColumns=League+Rating&displayColumns=Last+Played+League&displayColumns=Membership+Expiration&pageSize=25")
//                .cookie("AWSALB", "FyKFWnAdy1BXJZv21nq0HNWEe6qefGwYB3FaohObpTAHRTLhIBOD4ehpJMSWldO9Z7++v6crPRbjRiJMALJeEoFNp4+swgTneUru0WItdNKdkNrD7m0DsFl23wqG;")
//                .cookie("AWSALBCORS", "FyKFWnAdy1BXJZv21nq0HNWEe6qefGwYB3FaohObpTAHRTLhIBOD4ehpJMSWldO9Z7++v6crPRbjRiJMALJeEoFNp4+swgTneUru0WItdNKdkNrD7m0DsFl23wqG")
//                .cookie("JSESSIONID", "C6A73587F16F4D3D7D950954C87A219B")
//                .timeout(3000)
//                .get();
//
//
//        // MS Edge
//        return Jsoup.connect(url)
//                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36 Edg/88.0.705.63")
//                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
//                .header("Accept-Encoding", "gzip, deflate, br")
//                .header("Accept-Language", "en-US,en;q=0.9")
//                .header("Referrer", "https://www.teamusa.org/")
//                .header("sec-fetch-dest", "document")
//                .header("sec-fetch-mode", "navigate")
//                .header("sec-fetch-site", "cross-site")
//                .header("sec-fetch-user", "?1")
//                .header("upgrade-insecure-requests", "1")
//                .cookie("AWSALB", "k4l9d0aABlbfDGdZy1sPxOxGMfk5/AhjUbdTLgS/Dm4sqjNwttCXCNCI79fG6f7trglkQvO2sa+K4Qb6f5+oTIyBN/j61Zvqy9yi0cUERXyjXQab91B5M9SvzFbD;")
//                .cookie("AWSALBCORS", "k4l9d0aABlbfDGdZy1sPxOxGMfk5/AhjUbdTLgS/Dm4sqjNwttCXCNCI79fG6f7trglkQvO2sa+K4Qb6f5+oTIyBN/j61Zvqy9yi0cUERXyjXQab91B5M9SvzFbD")
////                .cookie("JSESSIONID", "")
//                .timeout(3000)
//                .get();
//        }
//    }





