package com.auroratms.utils;

import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubService;
import com.auroratms.event.*;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.*;
import com.auroratms.tournamententry.MembershipType;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.tournamentevententry.EventEntryStatus;
import com.auroratms.tournamentevententry.TournamentEventEntry;
import com.auroratms.tournamentevententry.TournamentEventEntryService;
import com.auroratms.tournamentevententry.doubles.DoublesPair;
import com.auroratms.tournamentevententry.doubles.DoublesService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.auroratms.utils.filerepo.FileInfo;
import com.auroratms.utils.filerepo.FileRepositoryException;
import com.auroratms.utils.filerepo.FileRepositoryFactory;
import com.auroratms.utils.filerepo.IFileRepository;
import com.auroratms.utils.pdfdto.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImportTournamentService {

    public static final String BASE_OMNIPONG_URL = "https://www.omnipong.com/";

    private final static String BLANK_ENTRY_FORM_FOLDER = "tournament/blankentryform/";

    private static final Logger log = LoggerFactory.getLogger(ImportTournamentService.class);

    // 9:00A or 3:00P
    // 9:00 AM or 5:00 PM
    private final Pattern EVENT_START_TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{1,2})\\s?([AP]M?)");

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private UsattDataService usattDataService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentEventEntryService tournamentEventEntryService;

    @Autowired
    private FileRepositoryFactory fileRepositoryFactory;

    @Autowired
    private ClubService clubService;

    @Autowired
    private DoublesService doublesService;

    @Autowired
    private BlankEntryFormParserService blankEntryFormParserService;

    // Under 3800 Doubles RR - Partner: Teamed With Devaansh Boda
    // Under 3800 Doubles RR - Partner: None selected
    // Under 3800 Doubles RR - Partner: Requesting Srinivas Chiravuri
    private static final Pattern doublesPartnerPattern = Pattern.compile("(.*) - Partner: (.*)");

    // U19B    U17B
    // U17G    U15G
    // U15XD
    // U15GD
    // U15BD
    private static final Pattern juniorEventPattern = Pattern.compile("U(\\d{1,2})(B|G|XD|BD|GD)");

    @Value("${client.host.url}")
    private String clientHostUrl;

    /**
     *
     *
     * @return
     */
    @NotNull List<Map<String, String>> listTournaments() {
        String url = BASE_OMNIPONG_URL + "T-tourney.asp?t=8&Region=0&y=&k=&e=0"; // all regions
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String listBody = response.getBody();
        List<Map<String, String>> tournaments = new ArrayList<>();
        if (listBody != null) {
            tournaments = extractTournaments(listBody);
        }
        return tournaments;
    }

    /**
     *
     * @param listBody
     * @return
     */
    public List<Map<String, String>> extractTournaments(String listBody) {
        Document document = Jsoup.parse(listBody, BASE_OMNIPONG_URL);
        // list table containing tournaments only
        List<Map<String, String>> tournamentList = new ArrayList<>();
        Elements stateHeaders = document.select("h3");
        List<String> stateNames = new ArrayList<>(stateHeaders.size());
        for (Element stateHeader : stateHeaders) {
            stateNames.add(stateHeader.text());
        }
        // skip the first header indicating which USATT region is selected
        stateNames.remove(0);
        int stateNameIndex = 0;
        Elements stateTournamentTables = document.select("table.omnipong");
        for (Element stateTournamentsTable : stateTournamentTables) {
            String currentState = (stateNameIndex < stateNames.size()) ? stateNames.get(stateNameIndex) : null;
            stateNameIndex++;
            Elements tournamentRows = stateTournamentsTable.select("tbody tr");
            int tournamentRowCount = 0;
            for (Element tournamentRow : tournamentRows) {
                tournamentRowCount++;
                if (tournamentRowCount < 2) {
                    continue;
                }
//                            <tr>
//                            <td Align="Center"><input type="submit" Class="omnipong_blue" name="Action" value="Results"
//                    title="Click to see event results"
//                    onclick="open_window('T-tourney.asp?t=103&r=4881','_self')"/></td>
//                            <td Align="Center"><input type="submit" class="omnipong_blue" name="Action" value="Players"
//                    title="Click for a list of players"
//                    onclick="open_window('T-tourney.asp?t=100&r=4881','_self')"/><br></td>
//                    <td Align="Left">
//                        <a href="https://fvttc.org/2025-aurora-fall-open/" target="_blank">2025 Aurora Fall Open</a>
//                    </td>
// OR
//                     <td Align="Left">
//                        <a href="EntryForms/1092-32.pdf" target="_blank">2025 South Bend Open-GiantRound Robin</a>
//                     </td>

                // if tournament is still open to enter or closed and it is not Info i.e. it has players
                Element infoInput = tournamentRow.selectFirst("input[value=Info]");
                Element playersInput = tournamentRow.selectFirst("input[value=Players]");
                if (playersInput != null && infoInput == null) {
//                    <td Align="Left">Aurora</td>
//                    <td Align="Center">09/07/25</td>
//                    <td Align="Left"><a href="mailto:swaveklorenc@yahoo.com">Swavek Lorenc</a><br>630-251-8860</td>
//                    <td align=center>JOOLA Prime<br></td>
//                    <td align=center>2-Star</td>
//                    </tr>
// OR
//                    <td Align="Left">Burlingame, CA</td>
//                    <td Align="Center">09/17/25 - 09/20/25</td>
//                    <td Align="Left"><a href="mailto:jasna@usatt.org">Jasna Rather</a><br></td>
//                    <td align=center></td>
//                    <td align=center>Non-Sanctioned</td>
                    int cellIndex = 0;
                    String tournamentName = null;
                    String playersUrl = null;
                    String blankEntryFormPDFUrl = null;
                    String tournamentDates = null;
                    String tournamentCity = null;
                    String tournamentState = null;
                    String tournamentStarLevel = null;
                    String tournamentDirectorName = null;
                    String tournamentDirectorEmail = null;
                    String tournamentDirectorPhone = null;
                    String ballType = null;
                    Elements tableCells = tournamentRow.select("td");
                    for (Element tableCell : tableCells) {
                        cellIndex++;
                        switch (cellIndex) {
                            case 1:
                                // results or Enter button
                                break;
                            case 2:
                                // Players button
                                // Extract URL inside the JavaScript call
                                String onclick = playersInput != null ? playersInput.attr("onclick") : null;
                                if (onclick != null) {
                                    Matcher m = Pattern.compile("'([^']+)'").matcher(onclick);
                                    if (m.find()) {
                                        playersUrl = m.group(1);
                                    }
                                }
                                break;

                            case 3:
                                // --- Extract tournament link and name ---
                                // Use a broader selector — find any <a> whose href starts with "http"
                                Element tournamentLink = tableCell.selectFirst("a[href^=http]");
                                if (tournamentLink == null) {
                                    // As a fallback, try any <a> inside the 3rd <td>
                                    tournamentLink = tableCell.selectFirst("a[href]");
                                }

                                if (tournamentLink != null) {
                                    tournamentName = tournamentLink.text().trim();
                                    String url = tournamentLink.attr("href");
                                    if (url.endsWith(".pdf")) {
                                        blankEntryFormPDFUrl = url;
                                    }
                                } else {
                                    tournamentName = tableCell.text();
                                }
                                break;

                            case 4:
                                tournamentCity = tableCell.text().trim();
                                if (tournamentCity.contains(",")) {
                                    String[] split = tournamentCity.split(",");
                                    tournamentCity = split[0].trim();
                                    if (split.length == 2) {
                                        tournamentState = split[1].trim();
                                    }
                                } else {
                                    tournamentState = currentState;
                                }
                                break;

                            case 5:
                                tournamentDates = tableCell.text().trim();
                                break;
                            case 6:
                                // <td Align="Left"><a href="mailto:swaveklorenc@yahoo.com">Swavek Lorenc</a><br>630-251-8860</td>
                                Element tdMailNamePhoneLink = tableCell.selectFirst("a[href]");
                                tournamentDirectorEmail = tdMailNamePhoneLink.attr("href");
                                if (tournamentDirectorEmail != null) {
                                    tournamentDirectorEmail = tournamentDirectorEmail.substring("mailto:".length());
                                    tournamentDirectorName = tdMailNamePhoneLink.text().trim();
                                    tournamentDirectorPhone = tableCell.html();
                                    tournamentDirectorPhone = tournamentDirectorPhone.substring(tournamentDirectorPhone.indexOf("<br>") + "<br>".length());
                                }
                                break;
                            case 7:
                                // <td align=center>JOOLA Prime<br></td>
                                ballType = tableCell.text().trim();
                                break;
                            case 8:
                                // <td align=center>2-Star</td>
                                // <td align=center>Non-Sanctioned</td>
                                tournamentStarLevel = tableCell.text().trim();
                                break;
                        }
                    }

                    if (tournamentName != null) {
                        Map<String, String> extractedValues = new HashMap<>();
                        extractedValues.put("playersUrl", playersUrl);
                        extractedValues.put("blankEntryFormPDFUrl", blankEntryFormPDFUrl);
                        extractedValues.put("tournamentName", tournamentName);
                        extractedValues.put("tournamentDates", tournamentDates);
                        extractedValues.put("tournamentCity", tournamentCity);
                        extractedValues.put("tournamentState", tournamentState);
                        extractedValues.put("tournamentStarLevel", tournamentStarLevel);
                        extractedValues.put("tournamentDirectorName", tournamentDirectorName);
                        extractedValues.put("tournamentDirectorEmail", tournamentDirectorEmail);
                        extractedValues.put("tournamentDirectorPhone", tournamentDirectorPhone);
                        extractedValues.put("ballType", ballType);
                        log.info("extractedValues" + extractedValues);
                        tournamentList.add(extractedValues);
                    }
                }
            }
        }

        Collections.sort(tournamentList, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> tournament1Info, Map<String, String> tournament2Info) {
                String t1Name = tournament1Info.get("tournamentName");
                String t2Name = tournament2Info.get("tournamentName");
                return t1Name.compareTo(t2Name);
            }
        });

        return tournamentList;
    }

    // ===================================================================
    // Import player entries
    // ===================================================================

    /**
     *
     * @param tournamentId
     * @param fromUrl
     * @param emailsFileRepoPath
     * @param importProgressInfo
     */
    public void importEntries(long tournamentId, String fromUrl, String emailsFileRepoPath, ImportProgressInfo importProgressInfo) {
        String url = BASE_OMNIPONG_URL + fromUrl;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String playerListHTML = response.getBody();

        importEntriesInternal(tournamentId, playerListHTML, emailsFileRepoPath, importProgressInfo);
    }

    /**
     *
     * @param tournamentId
     * @param playerListHTML
     * @param emailsFileRepoPath
     * @param importProgressInfo
     */
    public void importEntriesInternal(long tournamentId, String playerListHTML, String emailsFileRepoPath, ImportProgressInfo importProgressInfo) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        importProgressInfo.overallCompleted = 0;
        importProgressInfo.status = "RUNNING";
        importProgressInfo.phaseName = "Parsing list of player entries";
        Document document = Jsoup.parse(playerListHTML, BASE_OMNIPONG_URL);
        Elements outerTableWithControls = document.select("table tr td.omnipong");
        if (!outerTableWithControls.isEmpty()) {
            Element firstTDElement = outerTableWithControls.first();
            String tournamentName = firstTDElement.selectFirst("h3").text();
            // 2025 Edgeball Chicago International Open - Players by Name
            tournamentName = tournamentName.substring(0, tournamentName.indexOf(" - "));
            log.info(String.format("Importing tournament '%s'", tournamentName));
            Element playerEntriesTable = firstTDElement.selectFirst("table.omnipong");
            List<Map<String, Object>> playerEntriesDetails = extractPlayerEntriesDetails(playerEntriesTable);
            importProgressInfo.totalEntries = playerEntriesDetails.size();
            Set<String> uniqueEventNames = new HashSet<>();
            importProgressInfo.totalEventEntries = 0;
            for (Map<String, Object> playerEntriesDetail : playerEntriesDetails) {
                List<String> eventNamesList = (List<String>) playerEntriesDetail.get("events");
                uniqueEventNames.addAll(eventNamesList);
                importProgressInfo.totalEventEntries += eventNamesList.size();
            }
            importProgressInfo.phaseName = "Making event list";
            importProgressInfo.overallCompleted = 5;

            Map<String, String> playerNameToProfileMap = null;
            try {
                Map<String, TournamentEvent> eventNameToTournamentEventMap = getTournamentEventsMap(tournamentId, uniqueEventNames);

                Map<String, Long> clubNamesToIdsMap = addNewClubs(playerEntriesDetails);

                importProgressInfo.phaseName = "Getting/creating user " + importProgressInfo.totalEntries + " profiles";
                // get or create user accounts
                playerNameToProfileMap = getOrCreatePlayerAccounts(playerEntriesDetails, emailsFileRepoPath, clubNamesToIdsMap, importProgressInfo);
                importProgressInfo.overallCompleted = 40;

                // create/drop tournament entries
                importProgressInfo.phaseName = "Creating/updating " + importProgressInfo.totalEntries + " tournament entries";
                Map<String, TournamentEntry> userProfileToEntryMap = createTournamentEntries(tournamentId, playerEntriesDetails, playerNameToProfileMap, importProgressInfo);
                importProgressInfo.overallCompleted = 90;

                importProgressInfo.phaseName = "Dropping obsolete tournament entries";
                dropRemovedTournamentEntries(userProfileToEntryMap, playerNameToProfileMap, importProgressInfo);
                importProgressInfo.overallCompleted = 95;

                // create/drop event entries
                importProgressInfo.phaseName = "Creating/removing tournament " + importProgressInfo.totalEventEntries + " event entries";
                createTournamentEventEntries(tournamentId, playerEntriesDetails, eventNameToTournamentEventMap, playerNameToProfileMap, userProfileToEntryMap, importProgressInfo);

                importProgressInfo.phaseName = "Updating tournament event entries counts";
                updateTournamentCounts(tournamentId, userProfileToEntryMap.values().size(), eventNameToTournamentEventMap);

                importProgressInfo.overallCompleted = 100;
                importProgressInfo.phaseName = "Import finished!";
                importProgressInfo.status = "COMPLETED";
            } catch (Exception e) {
                log.error("Error during import", e);
                importProgressInfo.status = "FAILED";
            }
        }
        stopWatch.stop();
        log.info("Import completed in " + stopWatch.toString());
    }

    /**
     * Updates all tournament events entry counts and tournament entries count
     *
     * @param tournamentId
     * @param countTournamentEntries
     * @param eventNameToTournamentEventMap
     */
    private void updateTournamentCounts(long tournamentId, int countTournamentEntries, Map<String, TournamentEvent> eventNameToTournamentEventMap) {
        Collection<TournamentEvent> tournamentEvents = eventNameToTournamentEventMap.values();
        long totalEventEntriesCount = 0;
        for (TournamentEvent tournamentEvent : tournamentEvents) {
            long count = this.tournamentEventEntryService.getCountValidEntriesInEvent(tournamentEvent.getId());
            tournamentEvent.setNumEntries((int) count);
            totalEventEntriesCount += count;
        }
        tournamentEventEntityService.saveAll(new ArrayList<>(tournamentEvents));

        log.info("updating tournament with tournament entries count of " + countTournamentEntries + " totalEventEntriesCount " + totalEventEntriesCount);
        Tournament tournament = tournamentService.getByKey(tournamentId);
        tournament.setNumEntries(countTournamentEntries);
        tournament.setNumEventEntries((int) totalEventEntriesCount);
        tournamentService.saveTournament(tournament);
    }

    /**
     *
     * @param playerEntriesDetails
     * @return
     */
    private Map<String, Long> addNewClubs(List<Map<String, Object>> playerEntriesDetails) {
        Map<String, Long> clubNamesToIds = new HashMap<>();

        List<ClubEntity> allClubs = clubService.findAll();
        for (Map<String, Object> playerEntriesDetail : playerEntriesDetails) {
            String clubName = (String) playerEntriesDetail.get("clubName");
            String state = (String) playerEntriesDetail.get("state");
//            System.out.println("clubName = " + clubName + ", state = " + state);

            Long clubFk = clubNamesToIds.get(clubName);
            if (clubFk == null && StringUtils.isNotEmpty(clubName)) {
                List<ClubEntity> clubList = allClubs.stream()
                        .filter(clubEntity ->
                                // strongest match by name and state, then by alternative name, then just by name
                                (StringUtils.equalsIgnoreCase(clubEntity.getClubName(), clubName) && StringUtils.equals(clubEntity.getState(), state))
                                        || (clubEntity.getAlternateClubNames() != null && clubEntity.getAlternateClubNames().toLowerCase().contains(clubName.toLowerCase()))
                                        || StringUtils.equalsIgnoreCase(clubEntity.getClubName(), clubName)
                        ).toList();

                if (clubList.isEmpty()) {
                    ClubEntity clubEntity = new ClubEntity();
                    clubEntity.setClubName(clubName);
                    clubEntity.setState(state);
                    clubEntity.setAffiliated(true);
                    ClubEntity savedClub = clubService.save(clubEntity);
                    clubFk = savedClub.getId();
                } else {
                    ClubEntity clubEntity = clubList.get(0);
                    clubFk = clubEntity.getId();
                    if (StringUtils.isEmpty(clubEntity.getState())) {
                        clubEntity.setState(state);
                        clubService.save(clubEntity);
                    }
                }
                clubNamesToIds.put(clubName, clubFk);
            }
        }

        return clubNamesToIds;
    }

    /**
     *
     * @param playerNameToProfileMap
     * @param playerEntriesDetails
     */
    private void removeTemporaryUserProfiles(Map<String, String> playerNameToProfileMap,
                                             List<Map<String, Object>> playerEntriesDetails) {
        if (playerNameToProfileMap != null) {
            List<String> profileIdsToDelete = new ArrayList<>();
            for (Map<String, Object> playerEntriesDetail : playerEntriesDetails) {
                if (playerEntriesDetail.get("newProfile") != null) {
                    String playerName = playerEntriesDetail.get("playerName").toString();
                    String profileId = playerNameToProfileMap.get(playerName);
                    profileIdsToDelete.add(profileId);
                }
            }

            removeProfileAndExts(profileIdsToDelete);
        }
    }

    private void removeProfileAndExts(List<String> profileIdsToDelete) {
        log.info("Found " + profileIdsToDelete.size() + "profiles to delete");
        for (String profileId : profileIdsToDelete) {
            try {
                if (userProfileService.getProfile(profileId) != null) {
                    log.info("Deleting profile " + profileId);
                    userProfileService.deleteProfile(profileId);
                }
            } catch (Exception e) {
                log.error("Unable to find profile " + profileId, e);
            }
        }

//        log.info("Deleting profile exts");
//        for (String profileId : profileIdsToDelete) {
//            userProfileExtService.delete(profileId);
//        }
    }

    /**
     *
     * @param tournamentId
     * @param playerEntriesDetails
     * @param eventNameToTournamentEventMap
     * @param playerNameToProfileMap
     * @param userProfileToEntryMap
     * @param importProgressInfo
     */
    private void createTournamentEventEntries(long tournamentId,
                                              List<Map<String, Object>> playerEntriesDetails,
                                              Map<String, TournamentEvent> eventNameToTournamentEventMap,
                                              Map<String, String> playerNameToProfileMap,
                                              Map<String, TournamentEntry> userProfileToEntryMap,
                                              ImportProgressInfo importProgressInfo) {
        // make a map of tournament event fk to event name
        Map<Long, String> eventFkToEventNameMap = new HashMap<>();
        for (TournamentEvent tournamentEvent : eventNameToTournamentEventMap.values()) {
            eventFkToEventNameMap.put(tournamentEvent.getId(), tournamentEvent.getName());
        }

        // get all event entries for all players in this tournament
        List<TournamentEventEntry> allTournamentEventEntries = this.tournamentEventEntryService.listAllForTournament(tournamentId);

        List<TournamentEventEntry> newTournamentEventEntries = new ArrayList<>();

        // go through current list of players and their event entries and synchronize them
        for (Map<String, Object> playerEntriesDetail : playerEntriesDetails) {
            String playerName = (String) playerEntriesDetail.get("playerName");
            log.info("Creating event entries for player " + playerName);
            String profileId = playerNameToProfileMap.get(playerName);
            TournamentEntry tournamentEntry = userProfileToEntryMap.get(profileId);
            // find all existing event entries for this player
            List<TournamentEventEntry> oldEventEntriesForPlayer = allTournamentEventEntries.stream()
                    .filter(tournamentEventEntry -> tournamentEventEntry.getTournamentEntryFk() == tournamentEntry.getId())
                    .toList();

            // ensure there is event entry for new events
            List<String> newEventNameList = (List<String>) playerEntriesDetail.get("events");
            for (String eventName : newEventNameList) {
                TournamentEvent tournamentEvent = eventNameToTournamentEventMap.get(eventName);
                // in the list of existing find the entry for this event
                List<TournamentEventEntry> foundEventEntries = oldEventEntriesForPlayer.stream()
                        .filter(tournamentEventEntry -> tournamentEventEntry.getTournamentEventFk() == tournamentEvent.getId())
                        .toList();
                if (foundEventEntries.isEmpty()) {
                    // player doesn't have entry into this event - create it
                    TournamentEventEntry tournamentEventEntry = new TournamentEventEntry();
                    tournamentEventEntry.setTournamentFk(tournamentId);
                    tournamentEventEntry.setTournamentEventFk(tournamentEvent.getId());
                    tournamentEventEntry.setTournamentEntryFk(tournamentEntry.getId());
                    tournamentEventEntry.setDateEntered(tournamentEntry.getDateEntered());
                    tournamentEventEntry.setStatus(EventEntryStatus.ENTERED);
                    tournamentEventEntry.setPrice(tournamentEvent.getFeeAdult() * 100);

                    updateDoublesPartnerProfileId(playerNameToProfileMap, playerEntriesDetail, eventName, tournamentEvent, tournamentEventEntry);

                    log.info("Creating event entry for event '" + eventName + "' " + tournamentEventEntry);
                    TournamentEventEntry savedTournamentEntry = tournamentEventEntryService.create(tournamentEventEntry);
                    newTournamentEventEntries.add(savedTournamentEntry);
                    importProgressInfo.evenEntriesAdded++;
                    importProgressInfo.phaseCompleted = (int) (((double) importProgressInfo.evenEntriesAdded / importProgressInfo.totalEventEntries) * 100.0);
                    if (importProgressInfo.evenEntriesAdded % 10 == 0) {
                        log.info("Event entries completed " + importProgressInfo.phaseCompleted + "%");
                    }
                } else {
                    // old event - maybe update doubles partners
                    TournamentEventEntry tournamentEventEntry = foundEventEntries.get(0);
                    boolean changed = updateDoublesPartnerProfileId(playerNameToProfileMap, playerEntriesDetail, eventName, tournamentEvent, tournamentEventEntry);
                    if (changed) {
                        tournamentEventEntryService.update(tournamentEventEntry);
                    }
                }
            }

            // find dropped events
            // check that all existing event entries are still valid
            for (TournamentEventEntry tournamentEventEntry : oldEventEntriesForPlayer) {
                // if current event list doesn't contain existing (old) event then remove it
                String existingEventName = eventFkToEventNameMap.get(tournamentEventEntry.getTournamentEventFk());
                if (!newEventNameList.contains(existingEventName)) {
                    log.info("Removing dropped entry in " + existingEventName + " event for player " + playerName);
                    tournamentEventEntryService.delete(tournamentEventEntry.getId());
                    importProgressInfo.evenEntriesDeleted++;
                    allTournamentEventEntries.remove(tournamentEventEntry);
                }
            }
        }

        // create doubles entries
        List<TournamentEvent> tournamentEvents = new ArrayList<>(eventNameToTournamentEventMap.values());
        List<TournamentEventEntry> combinedEventEntriesList = new ArrayList<>(allTournamentEventEntries);
        combinedEventEntriesList.addAll(newTournamentEventEntries);
        createDoublesPairs(userProfileToEntryMap, tournamentEvents, combinedEventEntriesList);

        importProgressInfo.phaseCompleted = 100;
    }

    /**
     *
     * @param playerNameToProfileMap
     * @param playerEntriesDetail
     * @param eventName
     * @param tournamentEvent
     * @param tournamentEventEntry
     * @return
     */
    private boolean updateDoublesPartnerProfileId(Map<String, String> playerNameToProfileMap,
                                                  Map<String, Object> playerEntriesDetail,
                                                  String eventName,
                                                  TournamentEvent tournamentEvent,
                                                  TournamentEventEntry tournamentEventEntry) {
        boolean changed = false;
        if (tournamentEvent.isDoubles()) {
            String doublesPartnerNames = (String) playerEntriesDetail.get("doublesPartnerNames");
            if (doublesPartnerNames != null) {
                // list of doubles events with partners in each
                String[] eventsAndPartners = doublesPartnerNames.split(";");
                for (String eventPartner : eventsAndPartners) {
                    if (eventPartner.startsWith(eventName)) {
                        String doublesPartnerName = eventPartner.substring(eventPartner.indexOf("->") + 2);
                        System.out.println("doublesPartnerName = " + doublesPartnerName);
                        String lastFirstName = convertToLastFirstName(doublesPartnerName, playerNameToProfileMap);
                        if (lastFirstName != null) {
                            System.out.println("lastFirstName = " + lastFirstName);
                            String doublesPartnerProfileId = playerNameToProfileMap.get(lastFirstName);
                            System.out.println("doublesPartnerProfileId = " + doublesPartnerProfileId);
                            String oldDoublesPartnerId = tournamentEventEntry.getDoublesPartnerProfileId();
                            tournamentEventEntry.setDoublesPartnerProfileId(doublesPartnerProfileId);
                            changed = StringUtils.equals(oldDoublesPartnerId, doublesPartnerProfileId);
                        }
                    }
                }
            }
        }
        return changed;
    }

    /**
     * Makes doubles pairs for all doubles events in this tournament
     *
     * @param userProfileToEntryMap
     * @param tournamentEvents
     * @param tournamentEventEntries
     */
    public void createDoublesPairs(
            Map<String, TournamentEntry> userProfileToEntryMap,
            List<TournamentEvent> tournamentEvents,
            List<TournamentEventEntry> tournamentEventEntries) {

        // event entries which have a partner specified
        List<TournamentEventEntry> validDoublesEventEntries = tournamentEventEntries.stream()
                .filter(tournamentEventEntry ->
                        tournamentEventEntry.getDoublesPartnerProfileId() != null)
                .toList();
        log.info("Found " + validDoublesEventEntries.size() + " valid doubles event out of a total " + tournamentEventEntries.size() + " event entries");

        // find doubles events ids
        List<TournamentEvent> doublesEvents = tournamentEvents.stream()
                .filter(TournamentEvent::isDoubles)
                .toList();
        log.info("Found " + doublesEvents.size() + " doubles events");

        Set<Long> tournamentEntryIds = new HashSet<>();
        for (TournamentEvent doublesEvent : doublesEvents) {
            List<TournamentEventEntry> doublesEventEntries = validDoublesEventEntries.stream()
                    .filter(tournamentEventEntry -> doublesEvent.getId() == tournamentEventEntry.getTournamentEventFk())
                    .toList();
            for (TournamentEventEntry doublesEventEntry : doublesEventEntries) {
                tournamentEntryIds.add(doublesEventEntry.getTournamentEntryFk());
            }
        }
        log.info("Found " + tournamentEntryIds.size() + " unique tournament entry IDs for players who entered any of " + doublesEvents.size() + " doubles events");

        // get tournament entries for only players who entered doubles events
        List<TournamentEntry> entriesForDoublesPlayersOnly = userProfileToEntryMap.values().stream()
                .filter(tournamentEntry -> tournamentEntryIds.contains(tournamentEntry.getId()))
                .toList();
        log.info("Found " + entriesForDoublesPlayersOnly.size() + " tournament entries for players who entered " + doublesEvents.size() + " doubles events");

        // find doubles entries for each of this event and make pairs
        for (TournamentEvent doublesEvent : doublesEvents) {
            List<DoublesPair> updatedDoubles = new ArrayList<>();
            List<TournamentEventEntry> doublesEventEntries = validDoublesEventEntries.stream()
                    .filter(tournamentEventEntry ->
                            doublesEvent.getId() == tournamentEventEntry.getTournamentEventFk())
                    .toList();
            log.info("Found " + doublesEventEntries.size() + " entries into doubles event " + doublesEvent.getName() + ". Creating doubles pairs");
            Set<String> uniquePairProfileCombinations = new HashSet<>();
            for (TournamentEventEntry playerEventEntry : doublesEventEntries) {
                String doublesPartnerProfileId = playerEventEntry.getDoublesPartnerProfileId();
                if (doublesPartnerProfileId != null) {
                    TournamentEntry partnerTournamentEntry = userProfileToEntryMap.get(doublesPartnerProfileId);
                    if (partnerTournamentEntry != null) {
                        long tournamentEntryFk = playerEventEntry.getTournamentEntryFk();
                        List<TournamentEntry> thisPlayerEntries = entriesForDoublesPlayersOnly.stream()
                                .filter(tournamentEntry -> tournamentEntry.getId() == tournamentEntryFk)
                                .toList();
                        if (!thisPlayerEntries.isEmpty()) {
                            TournamentEntry thisPlayerEntry = thisPlayerEntries.get(0);
                            String uniquePairProfile = thisPlayerEntry.getProfileId() + ":" + doublesPartnerProfileId;
                            String reversUniquePairProfile = doublesPartnerProfileId + ":" + thisPlayerEntry.getProfileId();
                            if (!(uniquePairProfileCombinations.contains(uniquePairProfile) || uniquePairProfileCombinations.contains(reversUniquePairProfile))) {
                                uniquePairProfileCombinations.add(uniquePairProfile);
                                uniquePairProfileCombinations.add(reversUniquePairProfile);

                                List<TournamentEventEntry> partnerEventEntries = doublesEventEntries.stream()
                                        .filter(tournamentEventEntry -> tournamentEventEntry.getTournamentEntryFk() == partnerTournamentEntry.getId())
                                        .toList();
                                if (!partnerEventEntries.isEmpty()) {
                                    TournamentEventEntry partnerPlayerEventEntry = partnerEventEntries.get(0);
                                    DoublesPair doublesPair = new DoublesPair();
                                    doublesPair.setTournamentEventFk(doublesEvent.getId());
                                    doublesPair.setPlayerAEventEntryFk(playerEventEntry.getId());
                                    doublesPair.setPlayerBEventEntryFk(partnerPlayerEventEntry.getId());
                                    int eligibilityRating = thisPlayerEntry.getEligibilityRating() + partnerTournamentEntry.getEligibilityRating();
                                    int seedRating = thisPlayerEntry.getSeedRating() + partnerTournamentEntry.getSeedRating();
                                    doublesPair.setEligibilityRating(eligibilityRating);
                                    doublesPair.setSeedRating(seedRating);

                                    updatedDoubles.add(doublesPair);
                                }
                            }
                        }
                    }
                }
            }

            if ((updatedDoubles.size() * 2) != doublesEventEntries.size()) {
                String message = "Created " + updatedDoubles.size() + " doubles pairs from " + doublesEventEntries.size() + " doubles event entries for event " + doublesEvent.getName();
                log.error(message);
//                throw new RuntimeException(message);
            }

            this.doublesService.deleteAllForEvent(doublesEvent.getId());
            this.doublesService.saveAllPairs(updatedDoubles);
            log.info("Created/updated " + updatedDoubles.size() + " doubles pairs for event " + doublesEvent.getName());
        }
    }

    /**
     *
     * @param playerEntriesDetails
     * @param emailsFileRepoPath
     * @param clubNamesToIdsMap
     * @param importProgressInfo
     * @return
     */
    private Map<String, String> getOrCreatePlayerAccounts(List<Map<String, Object>> playerEntriesDetails,
                                                          String emailsFileRepoPath,
                                                          Map<String, Long> clubNamesToIdsMap,
                                                          ImportProgressInfo importProgressInfo) {
        int createdProfiles = 0;
        int foundEmails = 0;
        Map<String, String> playerNameToAccountId = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2000);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DATE, 1);
        Date defaultDateOfBirth = calendar.getTime();

        importProgressInfo.phaseCompleted = 0;
        importProgressInfo.phaseName = "Caching user profiles";

        // get unique state names so we can make fewer queries for user profiles
        // map them to list of player names empty lists
        Map<String, List<String>> statePlayersMap = new HashMap<>();
        for (Map<String, Object> playerEntriesDetail : playerEntriesDetails) {
            String state = playerEntriesDetail.get("state").toString();
            if (!statePlayersMap.containsKey(state) && StringUtils.isNotEmpty(state)) {
                statePlayersMap.put(state, new ArrayList<>());
            }
        }

        // divide players by state
        for (Map<String, Object> playerEntriesDetail : playerEntriesDetails) {
            String state = playerEntriesDetail.get("state").toString();
            String playerName = playerEntriesDetail.get("playerName").toString();
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(state)) {
                List<String> statePlayersList = statePlayersMap.get(state);
                statePlayersList.add(playerName);
            }
        }

        List<UserProfile> existingUserProfiles = new ArrayList<>();
        for (String state : statePlayersMap.keySet()) {
            log.info("Listing profiles for state " + state);
            List<String> statePlayersList = statePlayersMap.get(state);
            Collection<UserProfile> userProfiles = userProfileService.listByStates(Collections.singletonList(state));
            log.info("Found " + userProfiles.size() + " in state " + state + ". Filtering out those who are not in the tournament");
            for (UserProfile userProfile : userProfiles) {
                String fullName = userProfile.getLastName() + ", " + userProfile.getFirstName();
                if (statePlayersList.contains(fullName) && !StringUtils.startsWith(userProfile.getLogin(), "swaveklorenc+")) {
                    existingUserProfiles.add(userProfile);
                }
            }
        }
        log.info("Found a total of " + existingUserProfiles.size() + " user profiles");

        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = 5;
        int initialOverallCompleted = importProgressInfo.overallCompleted;

        importProgressInfo.phaseName = "Creating/getting profiles";
        importProgressInfo.phaseCompleted = 0;

        Map<String, String> playerToEmailFromFileMap = readEmailAddressesFile(emailsFileRepoPath);
        Set<String> usedEmailAddresses = new HashSet<>();
        int playerProfilesProcessed = 0;
        for (Map<String, Object> playerEntriesDetail : playerEntriesDetails) {
            String playerName = (String) playerEntriesDetail.get("playerName");
            String state = (String) playerEntriesDetail.get("state");
            String clubName = (String) playerEntriesDetail.get("clubName");
            String[] nameComponents = playerName.split(",");
            if (nameComponents.length > 0) {
                final String lastName = nameComponents[0].trim();
                final String firstName = nameComponents[1].trim();
                List<UserProfile> userProfiles = existingUserProfiles.stream()
                        .filter(userProfile -> userProfile.getLastName().equals(lastName) && userProfile.getFirstName().equals(firstName))
                        .toList();

                if (!userProfiles.isEmpty()) {
                    log.info(userProfiles.size() + " player profiles found in cache for " + lastName + ", " + firstName);
                } else {
                    log.info("Trying to find user profile by first and last name for " + lastName + ", " + firstName);
                    Collection<UserProfile> userProfilesExact = userProfileService.list(firstName, lastName);
                    userProfiles = userProfilesExact.stream().toList();
                    log.info("Found " + userProfiles.size() + " profiles for " + lastName + ", " + firstName);
                }

                if (userProfiles.size() == 1) {
                    UserProfile userProfile = userProfiles.iterator().next();
                    playerNameToAccountId.put(playerName, userProfile.getUserId());
                    usedEmailAddresses.add(userProfile.getEmail());
                    UserProfileExt byProfileId = userProfileExtService.getByProfileId(userProfile.getUserId());
                    if (byProfileId != null) {
                        log.info("Found userprofileext with membership id " + byProfileId.getMembershipId() + " for userProfileId " + userProfile.getUserId());
                    } else {
                        log.info("Adding missing userprofileExt");
                        UsattPlayerRecord usattRecord = this.usattDataService.getPlayerByNames(firstName, lastName);
                        if (usattRecord == null) {
                            log.warn("Didn't find USATT record for player " + lastName + ", " + firstName + " creating it");
                            String gender = (String) playerEntriesDetail.get("gender");
                            usattRecord = new UsattPlayerRecord();
                            usattRecord.setFirstName(firstName);
                            usattRecord.setLastName(lastName);
                            usattRecord.setGender("Male".equals(gender) ? "M" : "F");
                            usattRecord.setDateOfBirth(defaultDateOfBirth);
                            usattRecord.setState(state);
                            usattRecord.setHomeClub(clubName);
                            UsattPlayerRecord usattPlayerRecord = usattDataService.linkPlayerToProfile(usattRecord, userProfile.getUserId());
                            log.info("Created USATT membership record with id " + usattPlayerRecord.getMembershipId());
                        } else {
                            Long clubFk = clubNamesToIdsMap.get(clubName);
                            long membershipId = usattRecord.getMembershipId();
                            UserProfileExt userProfileExt = new UserProfileExt();
                            userProfileExt.setProfileId(userProfile.getUserId());
                            userProfileExt.setMembershipId(membershipId);
                            userProfileExt.setClubFk(clubFk);
                            userProfileExtService.save(userProfileExt);
                            log.info("Created userProfileExt for for player " + lastName + ", " + firstName + " with membershiId " + membershipId);
                        }
                    }
                } else {
                    UserProfile bestMatchProfile = null;
                    UsattPlayerRecord usattRecord = this.usattDataService.getPlayerByNames(firstName, lastName);
                    Long membershipId = (usattRecord != null) ? usattRecord.getMembershipId() : null;
                    String profileToFind = null;
                    if (membershipId != null) {
                        try {
                            UserProfileExt profileByMembershipId = this.userProfileExtService.getByMembershipId(membershipId);
                            profileToFind = (profileByMembershipId != null) ? profileByMembershipId.getProfileId() : null;
                        } catch (Exception e) {
                            log.error("Couldn't find profile id for membership id " + membershipId, e);
                        }
                    }
                    log.info("USATT membership id for player " + lastName + ", " + firstName + " is " + membershipId);
                    for (UserProfile userProfile : userProfiles) {
                        log.info("Inspecting user profile " + userProfile);
                        if (StringUtils.equals(userProfile.getState(), state) && !StringUtils.startsWith(userProfile.getLogin(), "swaveklorenc+")) {
                            if (userProfile.getUserId().equals(profileToFind)) {
                                bestMatchProfile = userProfile;
                                log.info("Selected user profile matching USATT membership id " + membershipId + " for player " + lastName + ", " + firstName);
                                break;
                            }
                        } else {
                            bestMatchProfile = userProfile;
                        }
                    }

                    // lookup email address in from file
                    String emailAddress = null;
                    if (bestMatchProfile == null) {
                        if (playerToEmailFromFileMap.containsKey(playerName)) {
                            emailAddress = playerToEmailFromFileMap.get(playerName);
                            foundEmails++;
                        } else {
                            emailAddress = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@gmail.com";
                            emailAddress = emailAddress.replace(" ", "_");
                            emailAddress = emailAddress.replace("(", ".");
                            emailAddress = emailAddress.replace(")", ".");
                            emailAddress = emailAddress.replace("..", ".");
                            log.info("didn't find email for player " + playerName + " using generated email: " + emailAddress);
                        }
                    } else {
                        emailAddress = bestMatchProfile.getEmail();
                    }

                    // try to find user profile by email address when spelling of first name is different
                    if (bestMatchProfile == null) {
                        UserProfile profileForLoginId = userProfileService.getUserProfileForLoginId(emailAddress);
                        if (profileForLoginId != null) {
                            log.info("Found user profile by email address" + profileForLoginId);
                            // check that parent is not using the same email address as child
                            if (usedEmailAddresses.contains(emailAddress)) {
                                String[] addressParts = emailAddress.split("@");
                                emailAddress = addressParts[0] + "+" + firstName.toLowerCase() + "@" + addressParts[1];
                                log.info("Making unique email address: " + emailAddress);
                            } else {
                                bestMatchProfile = profileForLoginId;
                            }
                        }
                    }
                    usedEmailAddresses.add(emailAddress);

                    if (bestMatchProfile != null) {
                        playerNameToAccountId.put(playerName, bestMatchProfile.getUserId());
                    } else {
                        String gender = "Male";
                        Date dateOfBirth = defaultDateOfBirth;
                        if (usattRecord != null) {
                            gender = StringUtils.isEmpty(usattRecord.getGender())
                                    ? gender
                                    : (usattRecord.getGender().equals("M") ? "Male" : "Female");
                            dateOfBirth = (usattRecord.getDateOfBirth() != null) ? usattRecord.getDateOfBirth() : defaultDateOfBirth;
                            state = !StringUtils.isEmpty(state) ? state : usattRecord.getState();
                            membershipId = usattRecord.getMembershipId();
                        }
                        log.info("Creating profile for " + lastName + ", " + firstName + ", " + gender + ", " + state + ", " + emailAddress + ", " + membershipId);
                        // no profile for this player
                        UserProfile newUserProfile = new UserProfile();
                        newUserProfile.setFirstName(firstName);
                        newUserProfile.setLastName(lastName);
                        newUserProfile.setState(state);
                        newUserProfile.setGender(gender);
                        newUserProfile.setDateOfBirth(dateOfBirth);
                        newUserProfile.setEmail(emailAddress);
                        newUserProfile.setLogin(emailAddress);
                        newUserProfile.setMakeDefaultPassword(true);
                        UserProfile savedUserProfile = userProfileService.createProfile(newUserProfile);
                        String profileId = savedUserProfile.getUserId();

                        log.info("Created profile for " + lastName + ", " + firstName + ", " + profileId);
                        playerNameToAccountId.put(playerName, profileId);
                        playerEntriesDetail.put("newProfile", Boolean.TRUE);
                        createdProfiles++;

                        // create a new usatt record with 400,000 plus membership id
                        if (usattRecord == null) {
                            usattRecord = new UsattPlayerRecord();
                            usattRecord.setFirstName(firstName);
                            usattRecord.setLastName(lastName);
                            usattRecord.setGender("Male".equals(gender) ? "M" : "F");
                            usattRecord.setDateOfBirth(dateOfBirth);
                            usattRecord.setState(state);
                            usattRecord.setHomeClub(clubName);
                            usattDataService.linkPlayerToProfile(usattRecord, profileId);
                        } else {
                            Long clubFk = clubNamesToIdsMap.get(clubName);

                            UserProfileExt userProfileExt = new UserProfileExt();
                            userProfileExt.setProfileId(profileId);
                            userProfileExt.setClubFk(clubFk);
                            userProfileExt.setMembershipId(membershipId);
                            if (userProfileExtService.existsByMembershipId(membershipId)) {
                                UserProfileExt oldUserProfileExt = userProfileExtService.getByMembershipId(membershipId);
                                log.info("Deleting oldUserProfileExt = " + oldUserProfileExt);
                                userProfileExtService.delete(oldUserProfileExt.getProfileId());
                            }
                            userProfileExtService.save(userProfileExt);
                        }
                    }
                }
            }

            playerProfilesProcessed++;
            importProgressInfo.phaseCompleted = (int) (((double) playerProfilesProcessed / (double) playerEntriesDetails.size()) * 100.0);
            importProgressInfo.overallCompleted = initialOverallCompleted + ((importProgressInfo.phaseCompleted * 40) / 100);
        }

        log.info("createdProfiles = " + createdProfiles);
        log.info("foundEmails     = " + foundEmails);

        importProgressInfo.profilesCreated = createdProfiles;
        importProgressInfo.phaseCompleted = 100;

        return playerNameToAccountId;
    }

    /**
     *
     * @param emailsFileRepoPath
     * @return
     */
    private Map<String, String> readEmailAddressesFile(String emailsFileRepoPath) {
        Map<String, String> playerEmailsMap = new HashMap<>();
        if (emailsFileRepoPath != null) {
            try {
                String localPath = getEmailsFileLocalPath(emailsFileRepoPath);
                try (CSVReader csvReader = new CSVReader(new FileReader(localPath))) {
                    String[] values = null;
                    int rowNumber = 0;
                    int badRecordsNum = 0;
                    while ((values = csvReader.readNext()) != null) {
                        rowNumber++;
                        if (rowNumber == 1) {
                            boolean hasHeader = false;
                            for (String value : values) {
                                if (value.equals("LastName")) {
                                    hasHeader = true;
                                    break;
                                }
                            }
                            // skip header if present
                            if (hasHeader) {
                                continue;
                            }
                        }
                        int columnNum = 0;
                        if (values.length != 4) {
                            System.out.print("Insufficient values in record ");
                            for (String value : values) {
                                System.out.print(value + ", ");
                            }
                            System.out.println();
                            badRecordsNum++;
                            continue;
                        }
                        // Last Name	First Name	State, Email
                        String lastName = "";
                        String firstName = "";
                        String email = "";
                        String state = "";
                        for (String text : values) {
                            switch (columnNum) {
                                case 0:
                                    lastName = text;
                                    break;
                                case 1:
                                    firstName = text;
                                    break;
                                case 2:
                                    state = text;
                                    break;
                                case 3:
                                    email = text;
                                    break;
                                default:
                                    break;
                            }
                            columnNum++;
                        }
                        if (org.apache.commons.lang3.StringUtils.isEmpty(firstName) ||
                                org.apache.commons.lang3.StringUtils.isEmpty(lastName) ||
                                org.apache.commons.lang3.StringUtils.isEmpty(email)) {
                            System.out.print("Insufficient critical values in record");
                            for (String value : values) {
                                System.out.print(value + ", ");
                            }
                            System.out.println();
                            badRecordsNum++;
                            continue;
                        }
                        String fullName = lastName + ", " + firstName;
                        playerEmailsMap.put(fullName, email);
                    }

                    log.info("total records = " + rowNumber);
                    log.info("bad   records = " + badRecordsNum);
                }
            } catch (IOException | CsvValidationException | FileRepositoryException e) {
                log.error("Unable to read player emails file", e);
            }
        }
        return playerEmailsMap;
    }

    /**
     * Get a filepath of the local file in case file was uploaded to a repo
     *
     * @param emailsFileRepoPath
     * @return
     * @throws FileRepositoryException
     * @throws IOException
     */
    private String getEmailsFileLocalPath(String emailsFileRepoPath) throws FileRepositoryException, IOException {
        if (emailsFileRepoPath != null && emailsFileRepoPath.startsWith("C:\\")) {
            // used during testing
            return emailsFileRepoPath;
        } else {
            // get the file from repository
            IFileRepository fileRepository = this.fileRepositoryFactory.getFileRepository();
            FileInfo fileInfo = fileRepository.read(emailsFileRepoPath);

            // copy the file locally, because Slurper can't read from input stream - it may be remote in cloud storage
            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            tempDir += File.separator + "playeremails";
            File tempDirFile = new File(tempDir);
            tempDirFile.mkdirs();

            File outputFile = new File(tempDir, fileInfo.getFilename());
            outputFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            FileCopyUtils.copy(fileInfo.getFileInputStream(), fileOutputStream);

            return outputFile.getCanonicalPath();
        }
    }

    /**
     *
     * @param tournamentId
     * @param playerEntriesDetails
     * @param playerNameToProfileMap
     * @param importProgressInfo
     */
    private Map<String, TournamentEntry> createTournamentEntries(long tournamentId,
                                                                 List<Map<String, Object>> playerEntriesDetails,
                                                                 Map<String, String> playerNameToProfileMap,
                                                                 ImportProgressInfo importProgressInfo) {

        importProgressInfo.phaseCompleted = 0;
        int initialOverallCompleted = importProgressInfo.overallCompleted;
        // list all existing entries for this tournament
        List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournament(tournamentId);
        Map<String, TournamentEntry> userProfileIdToEntryMap = new HashMap<>();
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            userProfileIdToEntryMap.put(tournamentEntry.getProfileId(), tournamentEntry);
        }
        log.info("Found " + tournamentEntries.size() + " existing tournament entries");
        log.info("Found " + userProfileIdToEntryMap.keySet().size() + " profile ids for existing tournament entries");

        // find players who don't have tournament entry yet
        Collection<String> allUserProfileIds = playerNameToProfileMap.values();
        log.info("There are a total of " + allUserProfileIds.size() + " profile ids associated with entries");
        Set<String> userProfilesWithEntries = userProfileIdToEntryMap.keySet();
        List<String> profileIdsWithoutEntries = new ArrayList<>();
        for (String userProfileId : allUserProfileIds) {
            if (!userProfilesWithEntries.contains(userProfileId)) {
                profileIdsWithoutEntries.add(userProfileId);
            }
        }
        log.info("Found " + profileIdsWithoutEntries.size() + " profile ids WITHOUT with entries");

        // make tournament entries for new profiles
        for (Map<String, Object> playerEntriesDetail : playerEntriesDetails) {
            String playerName = (String) playerEntriesDetail.get("playerName");
            String profileId = playerNameToProfileMap.get(playerName);
            String ratings = (String) playerEntriesDetail.get("ratings");
            String[] twoRatings = ratings.split("/");
            int seedRating = (twoRatings.length > 0) ? Integer.parseInt(twoRatings[0].trim()) : 0;
            int eligibilityRating = (twoRatings.length > 1) ? Integer.parseInt(twoRatings[1].trim()) : 0;

            if (profileIdsWithoutEntries.contains(profileId)) {
                log.info("Adding tournament entry for " + playerName + " profileId " + profileId);
                TournamentEntry tournamentEntry = new TournamentEntry();
                tournamentEntry.setTournamentFk(tournamentId);
                tournamentEntry.setProfileId(profileId);
                tournamentEntry.setSeedRating(seedRating);
                tournamentEntry.setEligibilityRating(eligibilityRating);
                tournamentEntry.setDateEntered(new Date());
                tournamentEntry.setMembershipOption(MembershipType.NO_MEMBERSHIP_REQUIRED);
                TournamentEntry savedTournamentEntry = tournamentEntryService.create(tournamentEntry);
                userProfileIdToEntryMap.put(profileId, savedTournamentEntry);
                importProgressInfo.entriesAdded++;
            } else {
                // update seed/eligibility rating if changed
                TournamentEntry tournamentEntry = userProfileIdToEntryMap.get(profileId);
                if (tournamentEntry.getEligibilityRating() != eligibilityRating || tournamentEntry.getSeedRating() != seedRating) {
                    log.info("Updating tournament entry ratings for " + playerName + " profileId " + profileId);
                    tournamentEntry.setEligibilityRating(eligibilityRating);
                    tournamentEntry.setSeedRating(seedRating);
                    TournamentEntry savedTournamentEntry = tournamentEntryService.update(tournamentEntry);
                    userProfileIdToEntryMap.put(savedTournamentEntry.getProfileId(), savedTournamentEntry);
                    importProgressInfo.entriesUpdated++;
                }
            }

            importProgressInfo.phaseCompleted = (int) (((double) (importProgressInfo.entriesAdded + importProgressInfo.entriesUpdated)
                    / importProgressInfo.totalEntries) * 100.0);
            importProgressInfo.overallCompleted = initialOverallCompleted + ((importProgressInfo.phaseCompleted * 50) / 100);

            if (importProgressInfo.entriesAdded % 10 == 0) {
                log.info("Tournament entries added " + importProgressInfo.phaseCompleted + "%");
            }
        }

        return userProfileIdToEntryMap;
    }

    /**
     *
     * @param userProfileToEntryMap
     * @param playerNameToProfileMap
     * @param importProgressInfo
     */
    private void dropRemovedTournamentEntries(Map<String, TournamentEntry> userProfileToEntryMap,
                                              Map<String, String> playerNameToProfileMap,
                                              ImportProgressInfo importProgressInfo) {

        // profile ids of players who entered tournament
        Collection<String> validProfileIds = playerNameToProfileMap.values();
        // all existing and new tournament entries map
        for (String userProfileId : userProfileToEntryMap.keySet()) {
            if (!validProfileIds.contains(userProfileId)) {
                // remove tournament entry and events
                TournamentEntry tournamentEntry = userProfileToEntryMap.get(userProfileId);
                List<TournamentEventEntry> tournamentEventEntriesToDelete = tournamentEventEntryService.listAllForTournamentEntry(tournamentEntry.getId());
                List<Long> idsToDelete = tournamentEventEntriesToDelete.stream()
                        .map(TournamentEventEntry::getId)
                        .toList();
                tournamentEventEntryService.deleteByIds(idsToDelete);
                tournamentEntryService.delete(tournamentEntry.getId());
                importProgressInfo.evenEntriesDeleted += idsToDelete.size();
                importProgressInfo.entriesDeleted++;
            }
        }
    }


    /**
     * Looks up profile id based on first name(s) and last name
     *
     * @param doublesPartnerName
     * @param playerNameToProfileMap
     * @return
     */
    private String convertToLastFirstName(String doublesPartnerName, Map<String, String> playerNameToProfileMap) {
        String[] nameParts = doublesPartnerName.split(" ");
        boolean found = false;
        // index where last name starts in a string
        // not everyone has a simple first and last name e.g.  'Muhammad Umar Jan' is converted to  'Jan, Muhammad Umar'
        int start = 1;
        String fullName = null;
        do {
            fullName = "";
            StringBuilder firstName = new StringBuilder();
            StringBuilder lastName = new StringBuilder();
            for (int i = 0; i < nameParts.length; i++) {
                if (i < start) {
                    firstName.append(nameParts[i]).append(" ");
                } else {
                    lastName.append(nameParts[i]).append(" ");
                }
            }
            start++;
            firstName = new StringBuilder(firstName.toString().trim());
            lastName = new StringBuilder(lastName.toString().trim());
            fullName = lastName + ", " + firstName;
            found = playerNameToProfileMap.containsKey(fullName);
        } while (!found && start < nameParts.length);
        if (!found) {
            log.warn("Can't convert '" + doublesPartnerName + "' into last, first name format");
        }
        return (found) ? fullName : null;
    }

    /**
     *
     * @param tournamentId
     * @param uniqueEventNames
     * @return
     */
    private Map<String, TournamentEvent> getTournamentEventsMap(long tournamentId, Set<String> uniqueEventNames) {
        Map<String, TournamentEvent> eventNameToTournamentEventMap = new HashMap<>();
        // get or create event definitions
        Collection<TournamentEvent> tournamentEvents = tournamentEventEntityService.list(tournamentId, Pageable.unpaged());
        int maxOrdinalNumber = 0;
        for (TournamentEvent tournamentEvent : tournamentEvents) {
            eventNameToTournamentEventMap.put(tournamentEvent.getName(), tournamentEvent);
            maxOrdinalNumber = Math.max(maxOrdinalNumber, tournamentEvent.getOrdinalNumber());
        }

        Set<String> existingEvents = eventNameToTournamentEventMap.keySet();
        for (String eventName : uniqueEventNames) {
            if (!existingEvents.contains(eventName)) {
                TournamentEvent tournamentEvent = new TournamentEvent();
                tournamentEvent.setName(eventName);
                tournamentEvent.setTournamentFk(tournamentId);
                tournamentEvent.setOrdinalNumber(maxOrdinalNumber++);
                tournamentEvent.setDay(1);
                tournamentEvent.setStartTime(9.0);
                tournamentEvent.setDrawMethod(DrawMethod.SNAKE);
                tournamentEvent.setPlayersPerGroup(4);
                tournamentEvent.setDoubles(isDoubles(eventName));
                tournamentEvent.setSingleElimination(isSingleElimination(eventName));
                tournamentEvent.setFeeAdult(30);
                tournamentEvent.setFeeJunior(30);
                tournamentEvent.setNumberOfGames(5);
                tournamentEvent.setPlay3rd4thPlace(false);
                TournamentEvent savedTournamentEvent = tournamentEventEntityService.create(tournamentEvent);
                eventNameToTournamentEventMap.put(eventName, savedTournamentEvent);
            }
        }
        return eventNameToTournamentEventMap;
    }

    /**
     *
     * @param playerEntriesTable
     * @return
     */
    private List<Map<String, Object>> extractPlayerEntriesDetails(Element playerEntriesTable) {
        List<Map<String, Object>> entriesList = new ArrayList<>();
        if (playerEntriesTable != null) {
            Elements playerEntryRows = playerEntriesTable.select("tr");
            boolean headerRow = true;
            for (Element playerEntryRow : playerEntryRows) {
                if (headerRow) {
                    headerRow = false;
                    continue;
                }
                Elements playerEntryDetailsElement = playerEntryRow.select("td");
                if (!playerEntryDetailsElement.isEmpty()) {
                    Element playerNameTD = playerEntryDetailsElement.first();
                    String playerName = playerNameTD.text();
                    playerName = playerName.substring(1);
                    Elements stateTD = playerEntryDetailsElement.next();
                    String state = stateTD.first().text();
                    Elements clubNameTD = stateTD.next();
                    String clubName = clubNameTD.first().text();
                    Elements ratingsTD = clubNameTD.next();
                    String ratings = ratingsTD.first().text();
                    Elements eventsTD = ratingsTD.next();
                    List<String> eventNameList = new ArrayList<>();
                    Elements eventNameElements = eventsTD.select("b");
                    String doublesPartnerNames = null;
                    if (!eventNameElements.isEmpty()) {
                        for (Element eventNameElement : eventNameElements) {
                            String eventName = eventNameElement.attr("title");
                            // extract doubles partner name for this event
                            // event name - Partner: Teamed With first and last name or 'None Selected'
                            // or
                            // event name - Partner: Requesting Srinivas Chiravuri
                            Matcher matcher = doublesPartnerPattern.matcher(eventName);
                            if (matcher.matches()) {
                                eventName = matcher.group(1);
                                String partnerName = matcher.group(2);
                                System.out.println("eventName = " + eventName + ", partnerName = " + partnerName);
                                if (partnerName.startsWith("Teamed With ")) {
                                    partnerName = partnerName.substring("Teamed With ".length());
                                    // put them all in one string and separate multiple events with ;
                                    if (doublesPartnerNames != null) {
                                        doublesPartnerNames += ";";
                                    }
                                    doublesPartnerNames = eventName + "->" + partnerName;
                                }
                                System.out.println(playerName + " doublesPartnerNames = '" + doublesPartnerNames + "'");
                            } else if (eventName.contains("Teamed")) {
                                System.out.println("not matching doubles eventName = " + eventName);
                            }
                            eventNameList.add(eventName);
                        }
                    }
                    Map<String, Object> playerEntryDetailsMap = new HashMap<>();
                    playerEntryDetailsMap.put("playerName", playerName);
                    playerEntryDetailsMap.put("state", state);
                    playerEntryDetailsMap.put("clubName", clubName);
                    playerEntryDetailsMap.put("ratings", ratings);
                    playerEntryDetailsMap.put("events", eventNameList);
                    if (doublesPartnerNames != null) {
                        playerEntryDetailsMap.put("doublesPartnerNames", doublesPartnerNames);
                    }
                    entriesList.add(playerEntryDetailsMap);
                }
            }
        }
        return entriesList;
    }

    public Set<String> removeUnwantedProfiles(ImportProgressInfo importProgressInfo, String fileWithIds) {
        Set<String> profileIdsToDelete = new HashSet<>();
        importProgressInfo.phaseName = "Removing temporary profiles";
        importProgressInfo.phaseCompleted = 0;

        try (CSVReader csvReader = new CSVReader(new FileReader(fileWithIds))) {
            String[] values = null;
            int totalRemoved = 0;

            while ((values = csvReader.readNext()) != null) {
                String lastName = values[0].trim();
                String firstName = values[1].trim();
                String pid = values[2].trim();
                profileIdsToDelete.add(pid);
                Collection<UserProfile> listOfProfiles = userProfileService.list(firstName, lastName);
                if (listOfProfiles.size() == 1) {
                    for (UserProfile userProfile : listOfProfiles) {
                        String profileId = userProfile.getUserId();
                        userProfileService.deleteProfile(profileId);
                        profileIdsToDelete.add(profileId);
                    }
                }
                totalRemoved++;
                if (totalRemoved % 10 == 0) {
                    log.info("Removed profiles - " + totalRemoved);
                }
                importProgressInfo.phaseCompleted = (int) (((double) totalRemoved / 150) * 100.0);
                importProgressInfo.overallCompleted = importProgressInfo.phaseCompleted;
            }

            log.info("Removed profiles - " + totalRemoved);
            importProgressInfo.phaseCompleted = 100;
            importProgressInfo.overallCompleted = 100;
            importProgressInfo.status = "COMPLETED";
        } catch (IOException | CsvValidationException e) {
            log.error("Error removing", e);
            importProgressInfo.overallCompleted = 100;
            importProgressInfo.status = "FAILED";
        }
        return profileIdsToDelete;
    }

    // ===================================================================
    // Import tournament configuration
    // ===================================================================

    /**
     * Imports tournament to either new or existing tournament
     *
     * @param importTournamentRequest
     * @param importProgressInfo
     */
    public void importTournamentConfiguration(ImportTournamentRequest importTournamentRequest,
                                              ImportProgressInfo importProgressInfo) {
        importProgressInfo.phaseCompleted = 0;
        importProgressInfo.overallCompleted = 0;
        importProgressInfo.status = "RUNNING";
        importProgressInfo.phaseName = "Identifying events";

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        boolean hasPDFtoParse = StringUtils.isNotEmpty(importTournamentRequest.blankEntryFormPDFUrl);

        String url = BASE_OMNIPONG_URL + importTournamentRequest.playersUrl;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String playerListHTML = response.getBody();

        Map<String, Map<String, String>> eventNamesAndCodes = new HashMap<>();
        String playerListByEventURL = extractEventNamesAndCodes(playerListHTML, eventNamesAndCodes, importProgressInfo);
        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = hasPDFtoParse ? 5 : 30;

        importProgressInfo.phaseName = "Getting additional event information";
        importProgressInfo.phaseCompleted = 0;
        String url2 = BASE_OMNIPONG_URL + playerListByEventURL;
        RestTemplate restTemplate2 = new RestTemplate();
        ResponseEntity<String> response2 = restTemplate2.getForEntity(url2, String.class);
        String playerListByEventHTML = response2.getBody();
        extractAdditionalEventInfo(playerListByEventHTML, eventNamesAndCodes, importProgressInfo);
        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = hasPDFtoParse ? 5 : 60;

        importProgressInfo.phaseName = "Creating/updating tournament";
        importProgressInfo.phaseCompleted = 0;
        Tournament tournament = convertRequestToTournament(importTournamentRequest);
        importProgressInfo.tournamentId = tournament.getId();
        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = hasPDFtoParse ? 10: 80;

        importProgressInfo.phaseName = "Creating/updating events";
        importProgressInfo.phaseCompleted = 0;
        Collection<TournamentEvent> eventsCollection = createUpdateEvents(tournament, eventNamesAndCodes);
        List<TournamentEvent> tournamentEventList = eventsCollection.stream().toList();
        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = hasPDFtoParse ? 15 : 100;

        // only analyze the first time
        if (StringUtils.isEmpty(tournament.getVenueName()) && hasPDFtoParse) {
            // get additional tournament and event information for provided PDF
            TournamentAndEventsDTO tournamentAndEventsDTO = importTournamentConfiguration(importTournamentRequest.blankEntryFormPDFUrl, importProgressInfo);
            if (tournamentAndEventsDTO != null) {
                boolean tournamentChanged = mergeTournamentDetails(tournament, tournamentAndEventsDTO, importTournamentRequest.blankEntryFormPDFUrl);
                boolean eventsChanged = mergeEventDetails(tournamentEventList, tournamentAndEventsDTO);
                int totalPrizeMoney = calculateTotalPrizeMoney(tournamentEventList);
                tournament.setTotalPrizeMoney(totalPrizeMoney);

                if (tournamentChanged) {
                    this.tournamentService.saveTournament(tournament);
                }
                if (eventsChanged) {
                    this.tournamentEventEntityService.saveAll(tournamentEventList);
                }
            }
        }

        stopWatch.stop();

        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = 100;
        importProgressInfo.status = "COMPLETED";
    }

    /**
     * Merges additional tournament information read from a blank entry form into tournament
     *
     * @param tournament
     * @param tournamentAndEventsDTO
     * @param blankEntryFormPDFUrl
     */
    private boolean mergeTournamentDetails(Tournament tournament, TournamentAndEventsDTO tournamentAndEventsDTO, String blankEntryFormPDFUrl) {
        boolean changed = false;

        // extract from pdf into Tournament object
        Tournament tournamentFromPDF = new Tournament();
        tournamentFromPDF.setName(tournamentAndEventsDTO.getTournamentName());
        populateTournament(tournamentAndEventsDTO, tournamentFromPDF, blankEntryFormPDFUrl);

        // merge with incoming tournament object
        if (StringUtils.isEmpty(tournament.getVenueName())) {
            tournament.setVenueName(tournamentFromPDF.getVenueName());
            changed = true;
        }

        tournament.setStreetAddress(tournamentFromPDF.getStreetAddress());
        tournament.setZipCode(tournamentFromPDF.getZipCode());
        tournament.setConfiguration(tournamentFromPDF.getConfiguration());
        tournament.setContactName(tournamentFromPDF.getContactName());
        if (StringUtils.isNotEmpty(tournamentFromPDF.getEmail())) {
            tournament.setEmail(tournamentFromPDF.getEmail());
            changed = true;
        }

        return changed;
    }

    /**
     * Merges additional information from the blank entry form PDF into each event
     *
     * @param tournamentEventList
     * @param tournamentAndEventsDTO
     * @return
     */
    private boolean mergeEventDetails(List<TournamentEvent> tournamentEventList, TournamentAndEventsDTO tournamentAndEventsDTO) {
        boolean changed = false;
        // event details read from PDF
        Map<String, EventDTO> eventDTOMap = tournamentAndEventsDTO.getEvents().stream()
                .collect(Collectors.toMap(
                        EventDTO::getEventName, // Key is the event name
                        eventDTO -> eventDTO // Value is the EventDTO object
                        // (e1, e2) -> e1  // Use this if there are duplicate keys and you want to keep the first one
                ));

        for (TournamentEvent tournamentEvent : tournamentEventList) {
            EventDTO eventDTO = findEventDTO(tournamentEvent, eventDTOMap);
            if (eventDTO != null) {
                changed = mergeSingleEventDetails(tournamentEvent, eventDTO) || changed;
            }
        }
        return changed;
    }

    /**
     * Tries to find the exact match by name, and if it fails tries by rating or gender restriction
     *
     * @param tournamentEvent
     * @param eventDTOMap
     * @return
     */
    private @Nullable EventDTO findEventDTO(TournamentEvent tournamentEvent, Map<String, EventDTO> eventDTOMap) {
        // most TDs configure the same event names as they appear on the blank entry form
        // match by name first then
        String eventName = tournamentEvent.getName();
        EventDTO eventDTO = eventDTOMap.get(eventName);
        if (eventDTO != null) {
            return eventDTO;
        } else {
            // maybe it is a single giant round robin event
            if (eventDTOMap.size() == 1) {
                for (EventDTO value : eventDTOMap.values()) {
                    eventDTO = value;
                    break;
                }
            } else {
                // if it didn't match exactly by name, then find it another way
                log.info("Event " + eventName + " not found.  Looking up by max rating and other ways");
                log.info("tournamentEvent '" + eventName + "' day " + tournamentEvent.getDay() + " start time " + tournamentEvent.getStartTime());
                for (EventDTO eventDTO1 : eventDTOMap.values()) {
                    int maxPlayerRating = (StringUtils.isNotEmpty(eventDTO1.getMaxRating())) ? Integer.parseInt(eventDTO1.getMaxRating()) : 0;
                    int age = (StringUtils.isNotEmpty(eventDTO1.getAgeRestriction().getAge())) ? Integer.parseInt(eventDTO1.getAgeRestriction().getAge()) : 0;
                    AgeRestrictionType ageRestrictionType = (StringUtils.isNotEmpty(eventDTO1.getAgeRestriction().getType()))
                            ? AgeRestrictionType.valueOf(eventDTO1.getAgeRestriction().getType()) : AgeRestrictionType.NONE;
                    GenderRestriction genderRestriction = StringUtils.isNotEmpty(eventDTO1.getGenderRestriction())
                            ? GenderRestriction.valueOf(eventDTO1.getGenderRestriction()) : GenderRestriction.NONE;
                    double startTime = getStartingTime(eventDTO1.getStartTime());
                    log.info("eventDTO1 name '" + eventDTO1.getEventName() + "' day = " + eventDTO1.getDay() + " startTime = " + startTime);
                    // first match the even by day and start time - hopefully these are the same as on the blank entry form
                    if (eventDTO1.getDay() == tournamentEvent.getDay() && startTime == tournamentEvent.getStartTime()) {
                        // then by rating restriction
                        if (tournamentEvent.getMaxPlayerRating() != 0 && maxPlayerRating == tournamentEvent.getMaxPlayerRating()) {
                            if (eventDTO1.isDoubles() == tournamentEvent.isDoubles()) {
                                eventDTO = eventDTO1;
                                break;
                            }
                        } else if (eventDTO1.getEventName().toLowerCase().contains("open") && tournamentEvent.getName().toLowerCase().contains("open") && maxPlayerRating == tournamentEvent.getMaxPlayerRating()) {
                            ///  open doubles or open singles
                            if (eventDTO1.isDoubles() == tournamentEvent.isDoubles()) {
                                eventDTO = eventDTO1;
                                break;
                            }
                        } else if ((age > 0) && (age == tournamentEvent.getMaxPlayerAge() || age == tournamentEvent.getMinPlayerAge())) {
                            // finally by age restriction and gender
                            if (genderRestriction == tournamentEvent.getGenderRestriction() && ageRestrictionType == tournamentEvent.getAgeRestrictionType()) {
                                eventDTO = eventDTO1;
                                break;
                            }
                        }
                    }
                }
            }

            if (eventDTO != null) {
                log.info("Found eventDTO named '" + eventDTO.getEventName() + "' for tournamentEvent '" + tournamentEvent.getName() + "'");
            } else {
                log.info("Didn't find eventDTO for event named '" + eventDTO.getEventName() + "'");
            }
            return eventDTO;
        }
    }

    /**
     *
     * @param tournamentEvent
     * @param eventDTO
     * @return
     */
    private boolean mergeSingleEventDetails(TournamentEvent tournamentEvent, EventDTO eventDTO) {
        boolean changed = false;

        // convert EventDTO to TournamentEvent
        TournamentEvent tournamentEventFromPDF = populateSingleEvent(eventDTO);

        // transfer except for id, name, ordinal number because they may have been read from the database
        tournamentEvent.setOrdinalNumber(tournamentEventFromPDF.getOrdinalNumber());
        tournamentEvent.setSingleElimination(tournamentEventFromPDF.isSingleElimination());
        tournamentEvent.setDoubles(tournamentEventFromPDF.isDoubles());
        tournamentEvent.setMinPlayerRating(tournamentEventFromPDF.getMinPlayerRating());
        tournamentEvent.setMaxPlayerRating(tournamentEventFromPDF.getMaxPlayerRating());
        tournamentEvent.setMinPlayerAge(tournamentEventFromPDF.getMinPlayerAge());
        tournamentEvent.setMaxPlayerAge(tournamentEventFromPDF.getMaxPlayerAge());
        tournamentEvent.setAgeRestrictionType(tournamentEventFromPDF.getAgeRestrictionType());
        tournamentEvent.setAgeRestrictionDate(tournamentEventFromPDF.getAgeRestrictionDate());
        tournamentEvent.setGenderRestriction(tournamentEventFromPDF.getGenderRestriction());
        tournamentEvent.setEligibilityRestriction(tournamentEventFromPDF.getEligibilityRestriction());
        tournamentEvent.setPlayersPerGroup(tournamentEventFromPDF.getPlayersPerGroup());
        tournamentEvent.setDrawMethod(tournamentEventFromPDF.getDrawMethod());
        tournamentEvent.setPointsPerGame(tournamentEventFromPDF.getPointsPerGame());
        tournamentEvent.setNumberOfGames(tournamentEventFromPDF.getNumberOfGames());
        tournamentEvent.setNumberOfGamesSEPlayoffs(tournamentEventFromPDF.getNumberOfGamesSEPlayoffs());
        tournamentEvent.setNumberOfGamesSEQuarterFinals(tournamentEventFromPDF.getNumberOfGamesSEQuarterFinals());
        tournamentEvent.setNumberOfGamesSESemiFinals(tournamentEventFromPDF.getNumberOfGamesSESemiFinals());
        tournamentEvent.setNumberOfGamesSEFinals(tournamentEventFromPDF.getNumberOfGamesSEFinals());
        tournamentEvent.setPlay3rd4thPlace(tournamentEventFromPDF.isPlay3rd4thPlace());
        tournamentEvent.setPlayersToAdvance(tournamentEventFromPDF.getPlayersToAdvance());
        tournamentEvent.setAdvanceUnratedWinner(tournamentEventFromPDF.isAdvanceUnratedWinner());
        tournamentEvent.setPlayersToSeed(tournamentEventFromPDF.getPlayersToSeed());
        if (tournamentEvent.getFeeAdult() != tournamentEventFromPDF.getFeeAdult()) {
            tournamentEvent.setFeeAdult(tournamentEventFromPDF.getFeeAdult());
            changed = true;
        }
        if (tournamentEvent.getFeeJunior() != tournamentEventFromPDF.getFeeJunior()) {
            tournamentEvent.setFeeJunior(tournamentEventFromPDF.getFeeJunior());
            changed = true;
        }

        tournamentEvent.setConfiguration(tournamentEventFromPDF.getConfiguration());

        return changed;
    }

    /**
     *
     * @param importTournamentRequest
     * @return
     */
    public Tournament convertRequestToTournament(ImportTournamentRequest importTournamentRequest) {
        Tournament tournament = null;
        int starLevel = 0; // 2-Star or Non-sanctioned
        if (StringUtils.isNotEmpty(importTournamentRequest.tournamentStarLevel)) {
            if (importTournamentRequest.tournamentStarLevel.contains("-Star")) {
                starLevel = Integer.parseInt(importTournamentRequest.tournamentStarLevel.substring(0, 1));
            }
        }

        Date startDate = null;
        Date endDate = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy"); // 11/15/25
            if (StringUtils.isNotEmpty(importTournamentRequest.tournamentDates)) {
                // 09/27/25 - 09/28/25
                if (importTournamentRequest.tournamentDates.contains(" - ")) {
                    String[] twoDates = importTournamentRequest.tournamentDates.split(" - ");
                    if (twoDates.length == 2) {
                        startDate = dateFormat.parse(twoDates[0].trim());
                        endDate = dateFormat.parse(twoDates[1].trim());
                    }
                } else {
                    startDate = dateFormat.parse(importTournamentRequest.tournamentDates);
                    endDate = startDate;
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException("unable to extract start or end date " + importTournamentRequest.tournamentDates, e);
        }

        LocalDate tournamentStartLocalDate = convertToLocalDate(startDate);

        LocalDate localEligibilityDate = tournamentStartLocalDate.minusMonths(1);
        Date eligibilityDate = convertFromLocalDate(localEligibilityDate);

        LocalDate localLateEntryDate = tournamentStartLocalDate.minusWeeks(3);
        Date lateEntryDate = convertFromLocalDate(localLateEntryDate);

        LocalDate localFullRefundDate = tournamentStartLocalDate.minusWeeks(2);
        Date refundDate = convertFromLocalDate(localFullRefundDate);

        LocalDate localEntryCuttofDate = tournamentStartLocalDate.minusWeeks(2);
        Date entryCuttofDate = convertFromLocalDate(localEntryCuttofDate);

        if (importTournamentRequest.tournamentId == 0) {
            // create a tournament
            tournament = new Tournament();
            tournament.setName(importTournamentRequest.tournamentName);
            tournament.setCity(importTournamentRequest.tournamentCity);
            tournament.setState(importTournamentRequest.tournamentState);
            tournament.setStarLevel(starLevel);
            tournament.setStartDate(startDate);
            tournament.setEndDate(endDate);
            tournament.setContactName(importTournamentRequest.tournamentDirectorName);
            tournament.setPhone(importTournamentRequest.tournamentDirectorPhone);
            tournament.setEmail(importTournamentRequest.tournamentDirectorEmail);
            TournamentConfiguration tournamentConfiguration = new TournamentConfiguration();
            tournament.setConfiguration(tournamentConfiguration);
            if (StringUtils.isNotEmpty(importTournamentRequest.ballType)) {
                tournamentConfiguration.setBallType(importTournamentRequest.ballType);
            }
            tournamentConfiguration.setTournamentType(TournamentType.RatingsRestricted);
            tournamentConfiguration.setCheckInType(CheckInType.DAILY);
            tournamentConfiguration.setMaxDailyEvents(6);
            tournamentConfiguration.setMaxTournamentEvents(12);
            tournamentConfiguration.setNumberOfTables(10);
            tournamentConfiguration.setPricingMethod(PricingMethod.STANDARD);
            tournamentConfiguration.setEligibilityRestriction(EligibilityRestriction.OPEN);
            tournamentConfiguration.setEligibilityDate(eligibilityDate);
            tournamentConfiguration.setLateEntryDate(lateEntryDate);
            tournamentConfiguration.setRefundDate(refundDate);
            tournamentConfiguration.setEntryCutoffDate(entryCuttofDate);

            tournament = this.tournamentService.saveTournament(tournament);
        } else {
            // read existing tournament
            tournament = this.tournamentService.getByKey(importTournamentRequest.tournamentId);
        }
        return tournament;
    }

    /**
     *
     * @param playerListHTML
     * @param eventNamesAndCodes
     * @param importProgressInfo
     * @return
     */
    public String extractEventNamesAndCodes(String playerListHTML,
                                            Map<String, Map<String, String>> eventNamesAndCodes,
                                            ImportProgressInfo importProgressInfo) {
        // read events from the 'sort by events page to get starting dates and times of each event
        // maximum number of spots and how many are taken
        importProgressInfo.overallCompleted = 0;
        importProgressInfo.status = "RUNNING";
        importProgressInfo.phaseName = "Parsing list of player entries to get event names and codes";
        Document document = Jsoup.parse(playerListHTML, BASE_OMNIPONG_URL);

        String sortByEventUrl = null;

        Elements outerTableWithControls = document.select("table tr td.omnipong");
        if (!outerTableWithControls.isEmpty()) {
            Element firstTDElement = outerTableWithControls.first();

            Element playerEntriesTable = firstTDElement.selectFirst("table.omnipong");

            extractUniqueEventNamesAndCodes(playerEntriesTable, eventNamesAndCodes);

            // <input type="submit" Class="omnipong_blue4" name="Action" value="Sort by Event" onclick="open_window('T-tourney.asp?t=102&r=4750&h=','_self')" /><br>
            Element sortByEventButton = firstTDElement.selectFirst("input[value=Sort by Event]");
            if (sortByEventButton != null) {
                String onclick = sortByEventButton.attr("onclick");

                // Extract URL inside the JavaScript call
                if (onclick != null) {
                    Matcher m = Pattern.compile("'([^']+)'").matcher(onclick);
                    if (m.find()) {
                        sortByEventUrl = m.group(1);
                    }
                }
            }
        }

        return sortByEventUrl;
    }

    /**
     * Gets event names and codes
     *
     * @param playerEntriesTable
     * @param eventNameToEventInfoMap
     * @return
     */
    private void extractUniqueEventNamesAndCodes(Element playerEntriesTable,
                                                 Map<String, Map<String, String>> eventNameToEventInfoMap) {
        if (playerEntriesTable != null) {
            Elements playerEntryRows = playerEntriesTable.select("tr");
            boolean headerRow = true;
            for (Element playerEntryRow : playerEntryRows) {
                if (headerRow) {
                    headerRow = false;
                    continue;
                }
                Elements playerEntryDetailsElement = playerEntryRow.select("td");
                if (!playerEntryDetailsElement.isEmpty()) {
                    Elements stateTD = playerEntryDetailsElement.next();
                    Elements clubNameTD = stateTD.next();
                    Elements ratingsTD = clubNameTD.next();
                    Elements eventsTD = ratingsTD.next();
                    Elements eventNameElements = eventsTD.select("b");
                    if (!eventNameElements.isEmpty()) {
                        for (Element eventNameElement : eventNameElements) {
                            // <b title="Over 70 RR">&nbsp;&nbsp;OVR70</b>
                            String eventName = eventNameElement.attr("title");
                            Matcher matcher = doublesPartnerPattern.matcher(eventName);
                            if (matcher.matches()) {
                                eventName = matcher.group(1);
                            }
                            // remove doubles space from 'Under  900 RR' or 'Under  800 RR' because the 'sort by event page' has it removed
                            // so we wouldn't be able to match it.
                            eventName = eventName.replaceAll("\\s{2}", " ");
                            String eventCode = eventNameElement.text();
                            Map<String, String> eventProperties = eventNameToEventInfoMap.computeIfAbsent(eventName, k -> new HashMap<>());
                            if (eventProperties.isEmpty()) {
                                log.info("Adding event '%s' with code '%s'".formatted(eventName, eventCode));
                                eventProperties.put("eventCode", eventCode);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Extracts maximum number of entries per event and number already taken, event date and starting time.
     * @param playerListByEventHTML
     * @param eventNamesAndCodes
     * @param importProgressInfo
     */
    public void extractAdditionalEventInfo(String playerListByEventHTML,
                                           Map<String, Map<String, String>> eventNamesAndCodes,
                                           ImportProgressInfo importProgressInfo) {

        //                                             Open Singles - Max Slots: 128<br>Scheduled for: 10/25/2025 @ 3:00P
        Pattern headerPattern = Pattern.compile("(.*) - Max Slots: (\\w+) Scheduled for: ([\\d/]*) @ ([\\d:\\w]*)");
        //                                                    Under 3200 Doubles RR Scheduled for: 10/04/2025 @ 5:30P
        Pattern headerNoMaxEntriesPattern = Pattern.compile("(.*) Scheduled for: ([\\d/]*) @ ([\\d:\\w]*)");
        //                                            Total Entries: 107 - Remaining slots: 1
        Pattern footerPattern = Pattern.compile("Total Entries: (\\d*) - Remaining slots: (\\d*)");

        Document document = Jsoup.parse(playerListByEventHTML, BASE_OMNIPONG_URL);
        Elements outerTableWithControls = document.select("table tr td.omnipong");
        if (!outerTableWithControls.isEmpty()) {
            Element firstTDElement = outerTableWithControls.first();
            if (firstTDElement != null) {
                Elements eventEntriesTables = firstTDElement.select("table.omnipong");
                int ordinalNumber = 1;
                for (Element eventEntriesTable : eventEntriesTables) {
                    Elements rowsWithHeaders = eventEntriesTable.select("tr:has(th):not(:has(td))");
                    String lastEventName = null;
                    for (Element rowWithHeader : rowsWithHeaders) {
                        Elements headerElements = rowWithHeader.select("th");
                        if (!headerElements.isEmpty()) {
                            Element firstHeaderElement = headerElements.first();
                            String eventHeaderFooterText = firstHeaderElement.text().trim();

                            Matcher headerMatcher = headerPattern.matcher(eventHeaderFooterText);
                            if (headerMatcher.matches()) {
                                String eventName = headerMatcher.group(1);
                                String maxSlots = headerMatcher.group(2);
                                String eventDate = headerMatcher.group(3);
                                String eventStartingTime = headerMatcher.group(4);
                                Map<String, String> eventInfoMap = eventNamesAndCodes.get(eventName);
                                if (eventInfoMap != null) {
                                    eventInfoMap.put("maxSlots", maxSlots);
                                    eventInfoMap.put("eventDate", eventDate);
                                    eventInfoMap.put("eventStartingTime", eventStartingTime);
                                    eventInfoMap.put("ordinalNumber", Integer.toString(ordinalNumber));
                                    ordinalNumber++;
                                }
                                lastEventName = eventName;
                            }

                            Matcher headerNoMaxSlotsMatcher = headerNoMaxEntriesPattern.matcher(eventHeaderFooterText);
                            if (headerNoMaxSlotsMatcher.matches()) {
                                String eventName = headerNoMaxSlotsMatcher.group(1);
                                String eventDate = headerNoMaxSlotsMatcher.group(2);
                                String eventStartingTime = headerNoMaxSlotsMatcher.group(3);
                                Map<String, String> eventInfoMap = eventNamesAndCodes.get(eventName);
                                if (eventInfoMap != null) {
                                    eventInfoMap.put("eventDate", eventDate);
                                    eventInfoMap.put("eventStartingTime", eventStartingTime);
                                    eventInfoMap.put("ordinalNumber", Integer.toString(ordinalNumber));
                                    ordinalNumber++;
                                }
                                lastEventName = eventName;
                            }

                            Matcher footerMatcher = footerPattern.matcher(eventHeaderFooterText);
                            if (footerMatcher.matches()) {
                                String totalEntries = footerMatcher.group(1);
                                if (lastEventName != null) {
                                    Map<String, String> eventInfoMap = eventNamesAndCodes.get(lastEventName);
                                    if (eventInfoMap != null) {
                                        eventInfoMap.put("totalEntries", totalEntries);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param tournament
     * @param eventNamesAndCodes
     * @return
     */
    public Collection<TournamentEvent> createUpdateEvents(Tournament tournament, Map<String, Map<String, String>> eventNamesAndCodes) {
        // get current event definitions in case they already have them configured
        // find new events which were added since last import
        Collection<TournamentEvent> tournamentEvents = tournamentEventEntityService.list(tournament.getId(), Pageable.unpaged());
        Set<String> updatedEventNames = eventNamesAndCodes.keySet();
        Set<String> addedEventNames = new HashSet<>();
        for (String updatedEventName : updatedEventNames) {
            boolean found = false;
            for (TournamentEvent tournamentEvent : tournamentEvents) {
                if (updatedEventName.equals(tournamentEvent.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                addedEventNames.add(updatedEventName);
            }
        }

        Date tournamentStartDate = tournament.getStartDate();
        LocalDate tournamentStartLocalDate = convertToLocalDate(tournamentStartDate);
        // Scheduled for: 10/04/2025 @ 9:00A
        DateFormat eventStartDateFormat = new SimpleDateFormat("MM/dd/yyyy");

        // find maximum ordinalNumber so we can set newly added ones to the next available number
        int maxEventOrderNum = 0;
        for (TournamentEvent tournamentEvent : tournamentEvents) {
            maxEventOrderNum = Math.max(maxEventOrderNum, tournamentEvent.getOrdinalNumber());
        }
        List<TournamentEvent> updatedTournamentEvents = new ArrayList<>();
        for (String eventName : addedEventNames) {
            TournamentEvent tournamentEvent = new TournamentEvent();
            updatedTournamentEvents.add(tournamentEvent);

            Map<String, String> eventProperties = eventNamesAndCodes.get(eventName);
            String strOrdinalNumber = eventProperties.get("ordinalNumber");
            int ordinalNumber = StringUtils.isNotEmpty(strOrdinalNumber) ? Integer.parseInt(strOrdinalNumber) : ++maxEventOrderNum;
            tournamentEvent.setTournamentFk(tournament.getId());
            tournamentEvent.setName(eventName);
            tournamentEvent.setOrdinalNumber(ordinalNumber);
            tournamentEvent.setDrawMethod(DrawMethod.SNAKE);
            tournamentEvent.setPlayersPerGroup(4);
            tournamentEvent.setDoubles(isDoubles(eventName));
            tournamentEvent.setSingleElimination(isSingleElimination(eventName));
            tournamentEvent.setFeeAdult(30);
            tournamentEvent.setFeeJunior(30);
            tournamentEvent.setNumberOfGames(5);
            tournamentEvent.setPlay3rd4thPlace(false);
            tournamentEvent.setPlayersToAdvance(1);

            int day = getEventDay(eventProperties, eventStartDateFormat, tournamentStartLocalDate);
            tournamentEvent.setDay(day);

            String strEventStartingTime = eventProperties.get("eventStartingTime");
            double startTime = getStartingTime(strEventStartingTime);
            tournamentEvent.setStartTime(startTime);

            int maxSlots = getMaxSlots(eventProperties);
            tournamentEvent.setMaxEntries(maxSlots);

            String eventCode = eventProperties.get("eventCode");
            int maxRating = getMaxPlayerRating(eventCode);
            tournamentEvent.setMaxPlayerRating(maxRating);

            int minPlayerAge = getMinPlayerAge(eventCode);
            tournamentEvent.setMinPlayerAge(minPlayerAge);

            int maxPlayerAge = getMaxPlayerAge(eventCode);
            tournamentEvent.setMaxPlayerAge(maxPlayerAge);

            if (maxPlayerAge != 0) {
                // under 21, 19, 17 etc
                tournamentEvent.setAgeRestrictionType(AgeRestrictionType.AGE_UNDER_OR_EQUAL_ON_DAY_EVENT);
            } else if (minPlayerAge != 0) {
                // over 60, 70 etc.
                tournamentEvent.setAgeRestrictionType(AgeRestrictionType.AGE_OVER_AT_THE_END_OF_YEAR);
            }

            tournamentEvent.setGenderRestriction(genderRestriction(eventCode));
        }

        tournamentEventEntityService.saveAll(updatedTournamentEvents);

        // update existing event max slots if changed
        List<TournamentEvent> updateExistingEvents = new ArrayList<>();
        for (TournamentEvent tournamentEvent : tournamentEvents) {
            int maxEntries = tournamentEvent.getMaxEntries();
            boolean eventChanged = false;
            Map<String, String> eventProperties = eventNamesAndCodes.get(tournamentEvent.getName());
            int newMaxEntries = getMaxSlots(eventProperties);
            if (maxEntries != newMaxEntries) {
                tournamentEvent.setMaxEntries(newMaxEntries);
                eventChanged = true;
            }

            int numEntries = tournamentEvent.getNumEntries();
            String strTotalEntries = eventProperties.get("totalEntries");
            int newNumEntries = Integer.parseInt(strTotalEntries);
            if (newNumEntries != numEntries) {
                tournamentEvent.setNumEntries(newNumEntries);
                eventChanged = true;
            }

            if (eventChanged) {
                updateExistingEvents.add(tournamentEvent);
            }
        }

        if (!updateExistingEvents.isEmpty()) {
            tournamentEventEntityService.saveAll(updateExistingEvents);
        }

        return tournamentEventEntityService.list(tournament.getId(), Pageable.unpaged());
    }

    private boolean isSingleElimination(String eventName) {
        return !eventName.contains("RR");
    }

    private boolean isDoubles(String eventName) {
        return eventName.toLowerCase().contains("doubles");
    }

    /**
     *
     * @param eventCode
     * @return
     */
    public int getMaxPlayerRating(String eventCode) {
        int maxPlayerRating = 0;

        final Pattern singlesPattern = Pattern.compile("U(\\d{3,4})");
        Matcher singlesMatcher = singlesPattern.matcher(eventCode);
        if (singlesMatcher.matches()) {
            String rating = singlesMatcher.group(1);
            maxPlayerRating = Integer.parseInt(rating);
        }

        final Pattern doublesPattern = Pattern.compile("D(\\d{3,4})");
        Matcher doublesMatcher = doublesPattern.matcher(eventCode);
        if (doublesMatcher.matches()) {
            String rating = doublesMatcher.group(1);
            maxPlayerRating = Integer.parseInt(rating);
        }
        // U1900 means max 1899
        if (maxPlayerRating > 0) {
            maxPlayerRating--;
        }

        return maxPlayerRating;
    }

    /**
     *
     * @param eventCode
     * @return
     */
    public int getMinPlayerAge(String eventCode) {
        int minPlayerAge = 0;

        final Pattern seniorPattern = Pattern.compile("OVR(\\d{2})");  //  OVR70, OVR60 etc.
        Matcher seniorMatcher = seniorPattern.matcher(eventCode);
        if (seniorMatcher.matches()) {
            String age = seniorMatcher.group(1);
            minPlayerAge = Integer.parseInt(age);
        }

        return minPlayerAge;
    }

    /**
     *
     * @param eventCode
     * @return
     */
    public int getMaxPlayerAge(String eventCode) {
        int maxPlayerAge = 0;

        Matcher matcher = juniorEventPattern.matcher(eventCode);
        if (matcher.matches()) {
            String age = matcher.group(1);
            maxPlayerAge = Integer.parseInt(age);
        }

        return maxPlayerAge;
    }

    /**
     *
     * @param eventCode
     * @return
     */
    public GenderRestriction genderRestriction(String eventCode) {
        GenderRestriction genderRestriction = GenderRestriction.NONE;
        if (eventCode.equals("WOMEN")) {
            return GenderRestriction.FEMALE;
        } else  {
            Matcher matcher = juniorEventPattern.matcher(eventCode);
            if (matcher.matches()) {
                String gender = matcher.group(2);
                if (gender.startsWith("B")) {
                    genderRestriction = GenderRestriction.MALE;
                } else if (gender.startsWith("G")) {
                    genderRestriction = GenderRestriction.FEMALE;
                }
            }
        }

        return genderRestriction;
    }

    /**
     *
     * @param eventProperties
     * @return
     */
    private int getMaxSlots(Map<String, String> eventProperties) {
        int maxSlots = 64;
        try {
            String strMaxSlots = eventProperties.get("maxSlots");
            maxSlots = StringUtils.isNotEmpty(strMaxSlots) ? Integer.parseInt(strMaxSlots) : 64;
        } catch (NumberFormatException e) {
        }
        return maxSlots;
    }

    /**
     * Converts start time to double in 24 format. start times are like this 10:30 AM -> 10.5, 5:00 PM -> 17.0
     * @param strStartingTime
     * @return
     */
    private double getStartingTime(String strStartingTime) {
        double startTime = 9.0;  // 9:00 am default,
        if (StringUtils.isNotEmpty(strStartingTime)) {
            Matcher matcher = EVENT_START_TIME_PATTERN.matcher(strStartingTime);
            if (matcher.matches()) {
                int hours = Integer.parseInt(matcher.group(1).trim());
                int minutes = Integer.parseInt(matcher.group(2).trim());
                String am_pm = matcher.group(3);
                boolean morning = am_pm.startsWith("A") || (hours == 12);
                startTime = morning ? hours : (hours + 12);
                startTime += (minutes == 0) ? 0.0 : 0.5;
            }
        }
        return startTime;
    }

    /**
     *
     * @param eventProperties
     * @param eventStartDateFormat
     * @param tournamentStartLocalDate
     * @return
     */
    private int getEventDay(Map<String, String> eventProperties, DateFormat eventStartDateFormat, LocalDate tournamentStartLocalDate) {
        int day = 1;
        String eventDate = eventProperties.get("eventDate");
        try {
            if (StringUtils.isNotEmpty(eventDate)) {
                Date eventStartDate = eventStartDateFormat.parse(eventDate);
                LocalDate eventStartLocalDate = convertToLocalDate(eventStartDate);
                long daysDiff = ChronoUnit.DAYS.between(tournamentStartLocalDate, eventStartLocalDate);
                day = Math.toIntExact(daysDiff + 1);
            }
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse event start date " + eventDate, e);
        }
        return day;
    }

    /**
     *
     * @param dateToConvert
     * @return
     */
    private LocalDate convertToLocalDate(Date dateToConvert) {
        return LocalDate.ofInstant(
                dateToConvert.toInstant(), ZoneId.systemDefault());
    }

    private Date convertFromLocalDate(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * Checks if there are any missing accounts for a tournament and writes missing accounts to a CSV file for download
     *
     * @param tournamentId       tournament id to use when
     * @param playersUrl         players url where players entries are listed
     * @param importProgressInfo progress info
     */
    public void checkAccounts(long tournamentId, String playersUrl, ImportProgressInfo importProgressInfo) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        importProgressInfo.phaseCompleted = 0;
        importProgressInfo.overallCompleted = 0;

        importProgressInfo.status = "RUNNING";
        importProgressInfo.phaseName = "Parsing list of player entries";
        List<String> playerNamesAndState = extractPlayerNamesFromHTML(playersUrl);
        importProgressInfo.totalEntries = playerNamesAndState.size();
        log.info("Extracted " + playerNamesAndState.size() + " total entries");
        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = 5;

        importProgressInfo.phaseCompleted = 0;
        if (!playerNamesAndState.isEmpty()) {
            // find all player's usatt records matching them by last and first name
            // if player has no membership then membership id is 0
            List<UsattPlayerRecord> playerRecordsToCheckForProfiles = new ArrayList<>();
            List<String> missingAccountsList = findPlayersWithoutProfilesByNameAndState(playerNamesAndState, playerRecordsToCheckForProfiles, importProgressInfo);

            findPlayersWithoutProfilesByNameOnly(playerRecordsToCheckForProfiles, playerNamesAndState, missingAccountsList, importProgressInfo);
            int profilesExisting = playerNamesAndState.size() - missingAccountsList.size();

            // remove duplicates & sort before writing out
            Set<String> uniqueNamesSet = new HashSet<>(missingAccountsList);
            missingAccountsList.clear();
            missingAccountsList.addAll(uniqueNamesSet);
            log.info("Final unique missing accounts list has " + missingAccountsList.size());

            importProgressInfo.overallCompleted = 90;
            importProgressInfo.phaseCompleted = 0;

            importProgressInfo.phaseName = "Exporting missing players names to file";
            importProgressInfo.missingProfileFileRepoUrl = writeMissingAccountPlayersToFile(
                    missingAccountsList, importProgressInfo.jobId, importProgressInfo.tournamentId);

            importProgressInfo.phaseCompleted = 100;
            importProgressInfo.overallCompleted = 100;

            importProgressInfo.profilesExisting = profilesExisting;
            importProgressInfo.profilesMissing = missingAccountsList.size();
        }

        stopWatch.stop();
        log.info("Accounts check completed in " + stopWatch.toString());

        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = 100;
        importProgressInfo.status = "COMPLETED";
    }

    /**
     *
     * @param playerRecordsToCheckForProfiles
     * @param playerNamesAndState
     * @param missingAccountsList
     * @param importProgressInfo
     * @return
     */
    private void findPlayersWithoutProfilesByNameOnly(List<UsattPlayerRecord> playerRecordsToCheckForProfiles,
                                                      List<String> playerNamesAndState,
                                                      List<String> missingAccountsList, ImportProgressInfo importProgressInfo) {
        importProgressInfo.phaseName = "Looking for player profiles by name only";
        importProgressInfo.phaseCompleted = 0;

        List<String> playerNames = playerNamesAndState.stream()
                .map(playerNameAndState -> {
                    return playerNameAndState.substring(0, playerNameAndState.lastIndexOf(","));
                }).toList();

        // some players were not found because their state in Omnipong is not the same as in USATT player record or is missing
        // try to make another attempt to match them by looking for an existing profile
        // Player Ponomareva, Maria, IL is not in the tournament USATT ID 286616 because in
        log.info("Found " + playerRecordsToCheckForProfiles.size() + " who don't match fully in USATT record. Search for profiles by first and last name...");
        int profilesExisting = 0;
        Set<String> alreadySearchedPlayer = new HashSet<>();
        int totalToProcess = playerRecordsToCheckForProfiles.size();
        int playersProcessed = 0;
        int initialOverallCompleted = importProgressInfo.overallCompleted;
        for (UsattPlayerRecord usattPlayerRecord : playerRecordsToCheckForProfiles) {
            boolean found = false;
            String fullNameAndState = null;
            String fullName = usattPlayerRecord.getLastName() + ", " + usattPlayerRecord.getFirstName();
            if (!alreadySearchedPlayer.contains(fullName)) {
                alreadySearchedPlayer.add(fullName);
                log.info("Searching for profile for " + fullName);
                Collection<UserProfile> playerUserProfiles = this.userProfileService.list(usattPlayerRecord.getFirstName(), usattPlayerRecord.getLastName());
                for (UserProfile userProfile : playerUserProfiles) {
                    String firstName = userProfile.getFirstName();
                    String lastName = userProfile.getLastName();
                    String state = userProfile.getState();
                    state = (StringUtils.isEmpty(state)) ? "" : state;
                    fullNameAndState = lastName + ", " + firstName + ", " + state;
                    if (playerNamesAndState.contains(fullNameAndState)) {
                        found = true;
                        profilesExisting++;
//                        log.info("Found profile for player " + fullNameAndState);
                        break;
                    } else if (playerNames.contains(fullName)) {
                        // maybe state doesn't match but name matches
                        found = true;
                        profilesExisting++;
                        List<String> statesList = playerNamesAndState.stream()
                                .filter(playerNameAndState -> fullName.equals(playerNameAndState.substring(0, playerNameAndState.lastIndexOf(","))))
                                .map(playerNameAndState -> playerNameAndState.substring(playerNameAndState.lastIndexOf(",") + 1).trim())
                                .toList();
                        log.info("Player " + fullName + " has a different state in the entry '" + statesList + "' than in the USATT record '" + usattPlayerRecord.getState() + "'");
                    }
                }

                if (!found) {
                    log.info("Profile not found for " + fullNameAndState);
                    if (fullNameAndState != null && !missingAccountsList.contains(fullNameAndState)) {
                        missingAccountsList.add(fullNameAndState);
                    }
                }
            }
            playersProcessed++;
            importProgressInfo.phaseCompleted = (int) (((double) playersProcessed / totalToProcess) * 100.0);
            importProgressInfo.overallCompleted = initialOverallCompleted + ((importProgressInfo.phaseCompleted * 20) / 100);
        }
        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = 80;

        log.info("Found " + profilesExisting + " by querying UserProfiles by last and first name and confirming with state");
        log.info("Now only " + missingAccountsList.size() + " players are still missing");
    }

    /**
     * 
     * @param playersUrl
     * @return
     */
    private List<String> extractPlayerNamesFromHTML (String playersUrl) {
        List<String> playerNamesAndState = new ArrayList<>();

        log.info("Parsing " + playersUrl + " to find player entries");
        String url = BASE_OMNIPONG_URL + playersUrl;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String playerListHTML = response.getBody();
        Document document = Jsoup.parse(playerListHTML, BASE_OMNIPONG_URL);
        Elements outerTableWithControls = document.select("table tr td.omnipong");
        if (!outerTableWithControls.isEmpty()) {
            Element firstTDElement = outerTableWithControls.first();
            String tournamentName = firstTDElement.selectFirst("h3").text();
            // 2025 Edgeball Chicago International Open - Players by Name
            tournamentName = tournamentName.substring(0, tournamentName.indexOf(" - "));
            log.info(String.format("Checking accounts for players in tournament '%s'", tournamentName));
            Element playerEntriesTable = firstTDElement.selectFirst("table.omnipong");

            List<String> playerNames = new ArrayList<>();
            if (playerEntriesTable != null) {
                Elements playerEntryRows = playerEntriesTable.select("tr");
                boolean headerRow = true;
                for (Element playerEntryRow : playerEntryRows) {
                    if (headerRow) {
                        headerRow = false;
                        continue;
                    }
                    Elements playerEntryDetailsElement = playerEntryRow.select("td");
                    if (!playerEntryDetailsElement.isEmpty()) {
                        Element playerNameTD = playerEntryDetailsElement.first();
                        String playerName = playerNameTD.text();
                        playerName = playerName.substring(1);
                        Elements stateTD = playerEntryDetailsElement.next();
                        String state = stateTD.first().text();
                        playerNames.add(playerName);
                        String fullNameAndState = playerName + ", " + state;
                        playerNamesAndState.add(fullNameAndState);
                    }
                }
            }
        }
        return playerNamesAndState;
    }

    /**
     *
     * @param playerNamesAndState
     * @param playerRecordsToCheckForProfiles
     * @param importProgressInfo
     * @return
     */
    private List<String> findPlayersWithoutProfilesByNameAndState(List<String> playerNamesAndState,
                                                                  List<UsattPlayerRecord> playerRecordsToCheckForProfiles,
                                                                  ImportProgressInfo importProgressInfo) {

        importProgressInfo.phaseName = "Looking for player profiles by name and state";
        importProgressInfo.phaseCompleted = 0;
        // remove state from this list so we can use it for membership status call
        List<String> playerNames = playerNamesAndState.stream()
                .map(playerNameAndState -> {
                    return playerNameAndState.substring(0, playerNameAndState.lastIndexOf(","));
                }).toList();

        List<UsattPlayerRecord> usattPlayerRecordsByFullName = usattDataService.findMembershipStatus(playerNames);
        log.info("Found " + usattPlayerRecordsByFullName.size() + " USATT memberships when searching by last and first name");
        importProgressInfo.overallCompleted = 10;

        Map<Long, String> membershipIdToNameMap = new HashMap<>();
        int notMatchingCount = 0;
        int playersProcessed = 0;
        int totalPlayers = usattPlayerRecordsByFullName.size();
        // find those who have an exact match by last name, first name and state
        for (UsattPlayerRecord usattPlayerRecord : usattPlayerRecordsByFullName) {
            Long membershipId = usattPlayerRecord.getMembershipId();
            String firstName = usattPlayerRecord.getFirstName();
            String lastName = usattPlayerRecord.getLastName();
            String state = usattPlayerRecord.getState();
            // check if this player is in the tournament
            String fullName = lastName + ", " + firstName;
            String fullNameAndState = fullName + ", " + state;
            // include only players who's state matches as well since there are multiple players with the same last and first name
            if (playerNamesAndState.contains(fullNameAndState)) {
                if (membershipId != 0) {
                    membershipIdToNameMap.put(membershipId, fullNameAndState);
                }
            } else {
                log.warn(++notMatchingCount + ") Player " + fullNameAndState + " is not matching with our without state " + membershipId);
                playerRecordsToCheckForProfiles.add(usattPlayerRecord);
            }

            playersProcessed++;
            importProgressInfo.phaseCompleted = (int) (((double) playersProcessed / totalPlayers) * 100.0);
        }
        log.info("Found " + membershipIdToNameMap.size() + " current USATT members which match entries exactly by last name, first name, state");
        importProgressInfo.overallCompleted = 30;
        importProgressInfo.phaseCompleted = 0;
        // find players who have a profile matched to their membership id
        List<String> missingAccountsList = new ArrayList<>();
        int profilesExisting = 0;
        List<Long> membershipIds = new ArrayList<>(membershipIdToNameMap.keySet());
        List<UserProfileExt> existingUserProfiles = this.userProfileExtService.findByMembershipIds(membershipIds);
        importProgressInfo.overallCompleted = 40;
        int totalMembershipIds = membershipIds.size();
        int membershipIdsProcessed = 0;
        for (Long membershipId : membershipIdToNameMap.keySet()) {
            boolean profileExists = false;
            for (UserProfileExt userProfileExt : existingUserProfiles) {
                if (userProfileExt.getMembershipId().equals(membershipId)) {
                    profileExists = true;
                    break;
                }
            }
            if (!profileExists) {
                String fullNameAndState = membershipIdToNameMap.get(membershipId);
                missingAccountsList.add(fullNameAndState);
            } else {
                profilesExisting++;
            }
            membershipIdsProcessed++;
            importProgressInfo.phaseCompleted = (int) (((double) membershipIdsProcessed / totalMembershipIds) * 100.0);
        }
        importProgressInfo.phaseCompleted = 100;
        importProgressInfo.overallCompleted = 60;

        log.info("Found " + profilesExisting + " profiles which match these membership ids");
        log.info("Found " + missingAccountsList.size() + " players which don't have profiles");
        log.info("Found " + playerRecordsToCheckForProfiles.size() + " USATT player records to check for profile existence");

        return missingAccountsList;
    }

    /**
     *
     * @param missingAccountsList
     * @param jobId
     * @param tournamentId
     * @return
     */
    private String writeMissingAccountPlayersToFile(List<String> missingAccountsList, String jobId, long tournamentId) {
        String repositoryURL = null;
        try {
            // create report file path
            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            File tempFile = new File(tempDir + File.separator + "missing-accounts-" + jobId + ".csv");
            String missingAccountsPath = tempFile.getCanonicalPath();
            log.info("Writing missing accounts list for tournament " + tournamentId);
            log.info("to " + missingAccountsPath);

            missingAccountsList.sort(String::compareTo);

            // write header
            FileWriter fileWriter = new FileWriter(tempFile);
            fileWriter.write("LastName,FirstName,State,Email\n");
            // write players
            for (String missingAccountInfo : missingAccountsList) {
                missingAccountInfo = missingAccountInfo.replaceAll(", ", ",");
                missingAccountInfo += ",\n";
                fileWriter.write(missingAccountInfo);
            }
            fileWriter.flush();
            fileWriter.close();

            IFileRepository fileRepository = this.fileRepositoryFactory.getFileRepository();

            String filename = tempFile.getName();
            InputStream inputStream = new FileInputStream(tempFile);
            // write out the list of plaeyr names and states to a file in a repository so it can be downloaded
            String repositoryFolder = "missing-accounts-" + jobId;
            repositoryURL = fileRepository.save(inputStream, filename, repositoryFolder);
            tempFile.delete();

            return repositoryURL;
        } catch (IOException | FileRepositoryException e) {
            log.error("Error generating or copying missing accounts file", e);
            throw new RuntimeException("Error generating missing accounts file", e);
        }
    }

    /**
     * Special import method which extracts but doesn't convert to
     * @param blankEntryFormPdfURI
     * @param importProgressInfo
     * @return
     */
    public TournamentAndEventsDTO importTournamentConfiguration(String blankEntryFormPdfURI,
                                                                ImportProgressInfo importProgressInfo) {

        try {
            if (blankEntryFormPdfURI.endsWith(".pdf")) {
                importProgressInfo.phaseName = "Downloading blank entry form PDF";
                importProgressInfo.phaseCompleted = 0;
                String blankEntryFormPDFFileLocalPath = getBlankEntryFormPDFFileLocalPath(blankEntryFormPdfURI);
                importProgressInfo.overallCompleted = 20;

                File pdfFile = new File(blankEntryFormPDFFileLocalPath);
                String JSONString = this.blankEntryFormParserService.parseTournamentPdf(pdfFile, importProgressInfo);
                importProgressInfo.overallCompleted = 70;

                importProgressInfo.phaseName = "Creating tournament and event definition";
                importProgressInfo.phaseCompleted = 0;
                TournamentAndEventsDTO tournamentAndEventsDTO = blankEntryFormParserService.convertToCombinedObject(JSONString);
                importProgressInfo.overallCompleted = 75;
                return tournamentAndEventsDTO;
            } else if (blankEntryFormPdfURI.endsWith(".json")) {
                // for testing only to save on using AI, use expected json
                File jsonFile = new ClassPathResource(blankEntryFormPdfURI).getFile();
                if (jsonFile.exists() && jsonFile.length() > 0) {
                    String jsonString = Files.readString(Paths.get(jsonFile.getAbsolutePath()));
                    jsonString = jsonString.replaceAll("\r\n", "\n");
                    if (jsonString.endsWith("\n")) {
                        jsonString = jsonString.substring(0, jsonString.length() - 1);
                    }
                    TournamentAndEventsDTO tournamentAndEventsDTO = blankEntryFormParserService.convertToCombinedObject(jsonString);
                    importProgressInfo.overallCompleted = 75;
                    return tournamentAndEventsDTO;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

        /**
         *
         * @param blankEntryFormPdfURI
         * @param importProgressInfo
         */
    public void importTournamentConfigurationFromPDF(String blankEntryFormPdfURI,
                                                     ImportProgressInfo importProgressInfo) {

        try {
            importProgressInfo.status = "RUNNING";
            importProgressInfo.phaseName = "Reading blank entry form PDF";
            importProgressInfo.phaseCompleted = 0;
            String blankEntryFormPDFFileLocalPath = getBlankEntryFormPDFFileLocalPath(blankEntryFormPdfURI);
            File pdfFile = new File(blankEntryFormPDFFileLocalPath);
            String JSONString = this.blankEntryFormParserService.parseTournamentPdf(pdfFile, importProgressInfo);

            importProgressInfo.phaseName = "Creating tournament and event definition";
            importProgressInfo.phaseCompleted = 0;
            TournamentAndEventsDTO tournamentAndEventsDTO = blankEntryFormParserService.convertToCombinedObject(JSONString);
            String tournamentName = tournamentAndEventsDTO.getTournamentName();
            if (StringUtils.isEmpty(tournamentName)) {
                tournamentName = "My tournament";
            }
            Tournament tournament = null;
            try {
                if (tournamentService.existsByName(tournamentName)) {
                    tournament = this.tournamentService.getByName(tournamentName);
                } else {
                    tournament = new Tournament();
                    tournament.setName(tournamentName);
                }
            } catch (Exception e) {
                // ignore
            }

            importProgressInfo.phaseCompleted = 10;
            if (tournament != null) {
                // populate tournament and events
                populateTournament(tournamentAndEventsDTO, tournament, blankEntryFormPdfURI);
                importProgressInfo.phaseCompleted = 50;
                List<TournamentEvent> tournamentEvents = populateEvents(tournamentAndEventsDTO);
                importProgressInfo.phaseCompleted = 90;

                int totalPrizeMoney = calculateTotalPrizeMoney(tournamentEvents);
                tournament.setTotalPrizeMoney(totalPrizeMoney);

                // save them both
                Tournament savedTournament = this.tournamentService.saveTournament(tournament);
                importProgressInfo.tournamentId = savedTournament.getId();
                for (TournamentEvent tournamentEvent : tournamentEvents) {
                    tournamentEvent.setTournamentFk(savedTournament.getId());
                }
                this.tournamentEventEntityService.saveAll(tournamentEvents);
            }
            importProgressInfo.status = "COMPLETED";
        } catch (Exception e) {
            importProgressInfo.status = "FAILED";
            throw new RuntimeException("Error creating tournament from blank entry form", e);
        } finally {
            importProgressInfo.phaseCompleted = 100;
            importProgressInfo.overallCompleted = 100;
        }
    }

    /**
     *
     * @param tournamentEvents
     * @return
     */
    private int calculateTotalPrizeMoney(List<TournamentEvent> tournamentEvents) {
        int totalPrizeMoney = 0;
        for (TournamentEvent tournamentEvent : tournamentEvents) {
            List<PrizeInfo> prizeInfoList = (tournamentEvent.getConfiguration() != null) ? tournamentEvent.getConfiguration().getPrizeInfoList() : null;
            if (prizeInfoList != null && !prizeInfoList.isEmpty()) {
                int eventTotalPrizeMoney = 0;
                for (PrizeInfo prizeInfo : prizeInfoList) {
                    // for a range of places we need to multiply by number of places
                    int awardedForPlaceRangeEnd = prizeInfo.getAwardedForPlaceRangeEnd();
                    int numAwardedPlaces = (awardedForPlaceRangeEnd == 0) ?
                            1 : (awardedForPlaceRangeEnd - prizeInfo.getAwardedForPlace() + 1);
                    Integer prizeMoneyAmount = prizeInfo.getPrizeMoneyAmount();
                    int iPrizeMoneyAmount = (prizeMoneyAmount != null) ? prizeMoneyAmount.intValue() : 0;
                    eventTotalPrizeMoney += numAwardedPlaces * iPrizeMoneyAmount;
                }
                totalPrizeMoney += eventTotalPrizeMoney;
            }
        }

        return totalPrizeMoney;
    }

    /**
     * Extracts events from the array of events like this:
     *
     * @param tournamentAndEventsDTO
     * @return
     */
    private List<TournamentEvent> populateEvents(TournamentAndEventsDTO tournamentAndEventsDTO) {
        List<EventDTO> events = tournamentAndEventsDTO.getEvents();
        List<TournamentEvent> tournamentEventList = new ArrayList<>(events.size());
        if (!events.isEmpty()) {
            for (EventDTO event : events) {
                TournamentEvent tournamentEvent = populateSingleEvent(event);
                tournamentEventList.add(tournamentEvent);
            }
        }
        return tournamentEventList;
    }

    /**
     * Populates single tournament event from this type of information
     * "event_name": "Open Singles",
     * "day": "1",
     * "start_time": "8:45 AM",
     * "entry_fee": "60",
     * "is_doubles": false,
     * "single_elimination": false,
     * "max_rating": "",
     * "gender_restriction": "none",
     * "age_restriction": {
     * "type": "none",
     * "age": ""
     * },
     * "players_per_group": "4",
     *
     * @param eventDTO
     * @return
     */
    private @NotNull TournamentEvent populateSingleEvent(EventDTO eventDTO) {
        String eventName = eventDTO.getEventName();
        int ordinalNumber = eventDTO.getOrdinalNumber();
        int day = eventDTO.getDay();
        String strStartTime = StringUtils.isNotEmpty(eventDTO.getStartTime()) ? eventDTO.getStartTime() : "9:00 AM";
        double startTime = convertStartTime(strStartTime);
        int entryFee = Integer.parseInt(eventDTO.getEntryFee());
        int juniorEntryFee = entryFee;
        boolean isDoubles = eventDTO.isDoubles();
        boolean isSingleElimination = eventDTO.isSingleElimination();
        int playersPerGroup = eventDTO.getPlayersPerGroup();
        int maxRating = StringUtils.isNotEmpty(eventDTO.getMaxRating())
                ? Integer.parseInt(eventDTO.getMaxRating()) : 0;
        String strGenderRestriction = eventDTO.getGenderRestriction();
        GenderRestriction genderRestriction = (StringUtils.isNotEmpty(strGenderRestriction)) ?
                GenderRestriction.valueOf(strGenderRestriction.toUpperCase()) : GenderRestriction.NONE;
        AgeRestrictionType ageRestrictionType = AgeRestrictionType.NONE;
        int minimumPlayerAge = 0;
        int maximumPlayerAge = 0;
        Date ageRestictionDate = null;
        AgeRestrictionDTO ageRestrictionDTO = eventDTO.getAgeRestriction();
        if (ageRestrictionDTO != null) {
            String type = ageRestrictionDTO.getType();
             ageRestrictionType = AgeRestrictionType.valueOf(type);
            int age = StringUtils.isEmpty(ageRestrictionDTO.getAge()) ? 0 : Integer.parseInt(ageRestrictionDTO.getAge());
            if (ageRestrictionType == AgeRestrictionType.AGE_UNDER_OR_EQUAL_ON_DAY_EVENT) {
                maximumPlayerAge = age;
            } else if (ageRestrictionType == AgeRestrictionType.BORN_ON_OR_AFTER_DATE) {
                maximumPlayerAge = age;
            } else if (ageRestrictionType == AgeRestrictionType.AGE_OVER_AT_THE_END_OF_YEAR) {
                minimumPlayerAge = age;
            }
        }

        int maxEntries = eventDTO.getMaxEntries();
        boolean isPlay3rd4thPlace = eventDTO.isPlay3rd4thPlace();
        boolean isAdvanceUnratedPlayer = eventDTO.isAdvanceUnratedWinner();
        int playersToAdvance = eventDTO.getPlayersToAdvance();
        int playersToSeed = eventDTO.getPlayersToSeed();
        int numberOfGames = eventDTO.getNumberOfGames();
        int numberOfGamesSEPlayoffs = eventDTO.getNumberOfGamesSEPlayoffs();
        int numberOfGamesSEQuarterFinals = eventDTO.getNumberOfGamesSEQuarterFinals();
        int numberOfGamesSESemiFinals = eventDTO.getNumberOfGamesSESemiFinals();
        int numberOfGamesSEFinals = eventDTO.getNumberOfGamesSEFinals();
        int pointsPerGame = 11;
        DrawMethod drawMethod = DrawMethod.valueOf(eventDTO.getDrawMethod());

        TournamentEvent tournamentEvent = new TournamentEvent();
        tournamentEvent.setOrdinalNumber(ordinalNumber);
        tournamentEvent.setName(eventName);
        tournamentEvent.setDay(day);
        tournamentEvent.setStartTime(startTime);
        tournamentEvent.setMaxEntries(maxEntries);
        tournamentEvent.setNumEntries(0);
        tournamentEvent.setGenderRestriction(genderRestriction);
        tournamentEvent.setMaxPlayerRating(maxRating);
        tournamentEvent.setAgeRestrictionType(ageRestrictionType);
        tournamentEvent.setAgeRestrictionDate(ageRestictionDate);
        tournamentEvent.setMinPlayerAge(minimumPlayerAge);
        tournamentEvent.setMaxPlayerAge(maximumPlayerAge);
        tournamentEvent.setMaxPlayerRating(maxRating);
        tournamentEvent.setFeeAdult(entryFee);
        tournamentEvent.setFeeJunior(juniorEntryFee);
        tournamentEvent.setSingleElimination(isSingleElimination);
        tournamentEvent.setPlay3rd4thPlace(isPlay3rd4thPlace);
        tournamentEvent.setDoubles(isDoubles);
        tournamentEvent.setAdvanceUnratedWinner(isAdvanceUnratedPlayer);
        tournamentEvent.setPlayersToAdvance(playersToAdvance);
        tournamentEvent.setPlayersToSeed(playersToSeed);
        tournamentEvent.setPlayersPerGroup(playersPerGroup);
        tournamentEvent.setNumberOfGames(numberOfGames);
        tournamentEvent.setNumberOfGamesSEPlayoffs(numberOfGamesSEPlayoffs);
        tournamentEvent.setNumberOfGamesSEQuarterFinals(numberOfGamesSEQuarterFinals);
        tournamentEvent.setNumberOfGamesSESemiFinals(numberOfGamesSESemiFinals);
        tournamentEvent.setNumberOfGamesSEFinals(numberOfGamesSEFinals);
        tournamentEvent.setPointsPerGame(pointsPerGame);
        tournamentEvent.setDrawMethod(drawMethod);

        TournamentEventConfiguration configuration = tournamentEvent.getConfiguration();
        if (configuration == null) {
            configuration = new TournamentEventConfiguration();
            tournamentEvent.setConfiguration(configuration);
        }
        List<PrizeInfo> prizeInfoList = populatePrizes(eventDTO.getPrizes());
        configuration.setPrizeInfoList(prizeInfoList);

        return tournamentEvent;
    }

    /**
     * Convert string time to double representation
     * @param strStartTime
     * @return
     */
    private double convertStartTime(String strStartTime) {
        double startTime = 9.0;
        Pattern pattern = Pattern.compile("(\\d{1,2}):(\\d{2}) (AM|PM)");
        Matcher matcher = pattern.matcher(strStartTime);
        if (matcher.matches()) {
            int hour = Integer.parseInt(matcher.group(1));
            int minutes = Integer.parseInt(matcher.group(2));
            String strIsAM = matcher.group(3);
            startTime = hour;
            startTime += ("AM".equals(strIsAM)) ? 0.0 : 12.0;
            startTime += (minutes != 0) ? 0.5 : 0.0;
        }
        return startTime;
    }

    /**
     * Populates tournament data from JSON object extracted from PDF
     *
     * @param tournamentAndEventsDTO
     * @param tournament
     * @param blankEntryFormPdfURI
     */
    private void populateTournament(TournamentAndEventsDTO tournamentAndEventsDTO, Tournament tournament, String blankEntryFormPdfURI) {
        Date startDate = convertDate(tournamentAndEventsDTO.getStartDate());
        Date endDate =   convertDate(tournamentAndEventsDTO.getEndDate());
        endDate = (endDate == null) ? startDate : endDate;
        tournament.setStartDate(startDate);
        tournament.setEndDate(endDate);

        String starRating = tournamentAndEventsDTO.getStarRating();
        if (StringUtils.isNotEmpty(starRating)) {
            tournament.setStarLevel(Integer.parseInt(starRating));
        }

        TournamentConfiguration configuration = tournament.getConfiguration();
        if (configuration == null) {
            configuration = new TournamentConfiguration();
            tournament.setConfiguration(configuration);
        }

        Date eligibilityDate = convertDate(tournamentAndEventsDTO.getRatingEligibilityDate());
        if (eligibilityDate == null) {
            // Friday before the tournament
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_MONTH, -8);
            eligibilityDate = calendar.getTime();
        }
        Date entryCutoffDate = convertDate(tournamentAndEventsDTO.getEntryDeadlineDate());
        if (entryCutoffDate == null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_MONTH, -3);
            eligibilityDate = calendar.getTime();
        }
        Date fullRefundDate = convertDate(tournamentAndEventsDTO.getRefundDeadlineDate());
        Date lateEntryDate = convertDate(tournamentAndEventsDTO.getLateEntryDate());
        if (fullRefundDate == null) {
            fullRefundDate = entryCutoffDate;
        }
        if (lateEntryDate == null) {
            lateEntryDate = entryCutoffDate;
        }
        configuration.setEligibilityDate(eligibilityDate);
        configuration.setEntryCutoffDate(entryCutoffDate);
        configuration.setRefundDate(fullRefundDate);

        configuration.setLateEntryDate(lateEntryDate);
        configuration.setLateEntryFee(0);

        String registrationFee = tournamentAndEventsDTO.getRegistrationFee();
        if (!StringUtils.isEmpty(registrationFee)) {
            configuration.setRegistrationFee(Integer.parseInt(registrationFee));
        }

        String venueName = tournamentAndEventsDTO.getVenueName();
        VenueAddressDTO venueAddress = tournamentAndEventsDTO.getVenueAddress();
        if (venueAddress != null) {
            String streetAddress = venueAddress.getStreet();
            String city = venueAddress.getCity();
            String state = venueAddress.getState();
            String zip = venueAddress.getZip();
            tournament.setVenueName(venueName);
            tournament.setStreetAddress(streetAddress);
            tournament.setCity(city);
            tournament.setState(state);
            tournament.setZipCode(zip);
        }

        String navigableBlankEntryFormUrl = getNavigableBlankEntryFormUrl(blankEntryFormPdfURI);
        configuration.setBlankEntryUrl(navigableBlankEntryFormUrl);

        // contact information
        List<DirectorDTO> directors = tournamentAndEventsDTO.getDirectors();
        if (!directors.isEmpty()) {
            for (DirectorDTO director : directors) {
                String directorsName = director.getName();
                String phone = director.getPhone();
                String email = director.getEmail();
                tournament.setContactName(directorsName);
                tournament.setPhone(phone);
                tournament.setEmail(email);
                break;
            }
        }
        populateEquipment(tournamentAndEventsDTO.getEquipment(), configuration);

        // todo - extract from PDF
        configuration.setMaxDailyEvents(3);
        configuration.setMaxTournamentEvents(6);

        configuration.setCheckInType(CheckInType.DAILY);

        if (tournament.getName().toLowerCase().contains("team")) {
            configuration.setTournamentType(TournamentType.Teams);
        } else if (tournament.getName().toLowerCase().contains("robin")) {
            configuration.setTournamentType(TournamentType.RoundRobin);
        } else {
            configuration.setTournamentType(TournamentType.RatingsRestricted);
        }
    }

    /**
     *
     * @param equipment
     * @param configuration
     */
    private void populateEquipment(EquipmentDTO equipment, TournamentConfiguration configuration) {
        if (equipment != null) {
            String tables = equipment.getTables();
            if (!StringUtils.isEmpty(tables)) {
                configuration.setNumberOfTables(Integer.parseInt(tables));
            } else {
                configuration.setNumberOfTables(8);
            }
            String ballType = equipment.getBallType();
            // normalize ball type
            if (!StringUtils.isEmpty(ballType)) {
                // "N/A", "Nittaku Premium", "Nittaku Nexel", "Butterfly R40+", "JOOLA Prime", "Stiga Perform"
                if (ballType.toLowerCase().contains("butterfly")) {
                    ballType = "Butterfly R40+";
                } else if (ballType.toLowerCase().contains("joola")) {
                    ballType = "JOOLA Prime";
                } else if (ballType.toLowerCase().contains("nittaku")) {
                    if (ballType.toLowerCase().contains("premium")) {
                        ballType = "Nittaku Premium";
                    } else if (ballType.toLowerCase().contains("nexel")) {
                        ballType = "Nittaku Nexel";
                    }
                } else if (ballType.toLowerCase().contains("stiga")) {
                    ballType = "Stiga Perform";
                }
            }
            configuration.setBallType(ballType);
        }
    }

    /**
     *
     * @param prizes
     * @return
     */
    private List<PrizeInfo> populatePrizes (List<PrizeDTO> prizes) {
        List<PrizeInfo> prizeInfoList = new ArrayList<>(prizes.size());
        for (PrizeDTO prizeDTO : prizes) {
            int place = (StringUtils.isEmpty(prizeDTO.getPlace())) ? 0 : Integer.parseInt(prizeDTO.getPlace());
            int placeEnd = StringUtils.isEmpty(prizeDTO.getPlaceEnd()) ? 0 : Integer.parseInt(prizeDTO.getPlaceEnd());
            int prizeMoney = StringUtils.isEmpty(prizeDTO.getPrizeMoney()) ? 0 : Integer.parseInt(prizeDTO.getPrizeMoney());
            boolean isAward = !StringUtils.isEmpty(prizeDTO.getAward());
            PrizeInfo prizeInfo = new PrizeInfo(prizeDTO.getDivision(), place, placeEnd, prizeMoney, isAward);
            prizeInfoList.add(prizeInfo);
        }
        return prizeInfoList;
    }

    /**
     *
     * @param strDate
     * @return
     */
    private Date convertDate(String strDate) {
        if (StringUtils.isNotEmpty(strDate)) {
            try {
                return DateUtils.parseDate(strDate, "MM/dd/yyyy");
            } catch (ParseException e) {
                System.out.println(String.format("Unable to convert date: '%s'\ncause: %s", strDate, e));
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Gets local path of the pdf file
     * @param blankEntryFormURI
     * @return
     * @throws FileRepositoryException
     * @throws IOException
     */
    private String getBlankEntryFormPDFFileLocalPath(String blankEntryFormURI) throws FileRepositoryException, IOException {

        if (blankEntryFormURI != null) {
            if (blankEntryFormURI.startsWith("C:\\")) {
                // used during testing
                return blankEntryFormURI;
            } else if (blankEntryFormURI.startsWith(BASE_OMNIPONG_URL)) {
                // download the file from omnipong locally
                // Use a try-with-resources statement to ensure the stream is closed automatically
                URL pdfUrl = new URL(blankEntryFormURI);
                try (InputStream inputStream = pdfUrl.openStream()) {

                    IFileRepository fileRepository = fileRepositoryFactory.getFileRepository();
                    String originalFilename = getPdfFilename(blankEntryFormURI);
                    String storagePath = getStoragePath(originalFilename);
                    String repositoryDownloadUrl = fileRepository.save(inputStream, originalFilename, storagePath);
                    String repositoryUrl = extractPathFromUrl(repositoryDownloadUrl);
                    return copyRepositoryFileLocally(repositoryUrl);
                } catch (IOException e) {
                    // Handle exceptions like network errors, file access issues, etc.
                    log.error("An error occurred during PDF download or file copy.", e);
                    throw e; // Re-throw or handle as appropriate for your application
                }
            } else {
                return copyRepositoryFileLocally(blankEntryFormURI);
            }
        }
        return null;
    }

    /**
     * Copies the file locally so it can be read by server code.  The file might be in remote drive shared by multiple instances
     * @param blankEntryFormURI
     * @return
     * @throws FileRepositoryException
     * @throws IOException
     */
    private @NotNull String copyRepositoryFileLocally(String blankEntryFormURI) throws FileRepositoryException, IOException {
        // get the file from our repository
        IFileRepository fileRepository = this.fileRepositoryFactory.getFileRepository();
        FileInfo fileInfo = fileRepository.read(blankEntryFormURI);

        // copy the file locally, because Slurper can't read from input stream - it may be remote in cloud storage
        String tempDir = System.getenv("TEMP");
        tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
        tempDir += File.separator + "blankentryform";
        File tempDirFile = new File(tempDir);
        tempDirFile.mkdirs();

        File outputFile = new File(tempDir, fileInfo.getFilename());
        outputFile.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        FileCopyUtils.copy(fileInfo.getFileInputStream(), fileOutputStream);

        return outputFile.getCanonicalPath();
    }

    /**
     * Gets url which can be viewed in a new tab
     * @param blankEntryFormURI
     * @return
     */
    private String getNavigableBlankEntryFormUrl(String blankEntryFormURI) {
        if (blankEntryFormURI.contains(BLANK_ENTRY_FORM_FOLDER)) {
            // it is in our repository so
            // api/filerepository/viewpdf?path=/tournament/blankentryform/1112-51AtlantaGiantRR.pdf
            return clientHostUrl + "/" + IFileRepository.REPOSITORY_URL_ROOT + "/viewpdf?path=" + blankEntryFormURI;
        } else {
            return blankEntryFormURI;
        }
    }

    /**
     *
     * @param pdfFilename
     * @return
     */
    private String getStoragePath(String pdfFilename) {
        return BLANK_ENTRY_FORM_FOLDER;
    }

    /**
     *
     * @param repositoryDownloadUrl
     * @return
     */
    private String extractPathFromUrl(String repositoryDownloadUrl) {
        final String PATH_PARAM = "?path=";
        int pathStart = repositoryDownloadUrl.indexOf(PATH_PARAM);
        if (pathStart != -1) {
            return repositoryDownloadUrl.substring(pathStart + PATH_PARAM.length());
        } else {
            return repositoryDownloadUrl;
        }
    }

    /**
     * extract just the filename from the url
     * @param blankEntryFormURI
     * @return
     */
    private String getPdfFilename (String blankEntryFormURI) {
        return blankEntryFormURI.substring(blankEntryFormURI.lastIndexOf("/") + 1);
    }
}
