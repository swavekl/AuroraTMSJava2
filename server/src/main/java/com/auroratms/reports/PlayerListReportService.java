package com.auroratms.reports;

import com.auroratms.event.TournamentEvent;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.match.Match;
import com.auroratms.match.MatchCard;
import com.auroratms.match.MatchCardService;
import com.auroratms.match.MatchService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service for producing report with a list of players who actually played at the tournament
 */
@Service
@Slf4j
@Transactional
public class PlayerListReportService {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private UsattDataService usattDataService;

    // 11/08/2003 - mm/dd/yyyy
    private DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");

    private static final int MAIN_FONT_SIZE = 9;
    private static final int CELL_PADDING = 5;
    private static final int PLAYERS_PER_PAGE = 40;

    static final Color DARK_GRAY = new DeviceRgb(0xA0, 0xA0, 0xA0); // darker gray
    static final Color LIGHT_GRAY = new DeviceRgb(0xF5, 0xF5, 0xF5); // white smoke
    static final Color WHITE = new DeviceRgb(0xFF, 0xFF, 0xFF); // white smoke

    public String generateReport(long tournamentId) {
        String reportFilename = null;
        FileWriter fileWriter = null;
        try {
            Tournament tournament = tournamentService.getByKey(tournamentId);
            String tournamentName = tournament.getName();

            // get data and write it out
            List<PlayerReportInfo> playerReportInfos = prepareReportData(tournamentId);

            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            File reportFile = new File(tempDir + File.separator + "player-list-" + tournamentId + ".pdf");
            reportFilename = reportFile.getCanonicalPath();
            log.info("Writing player list report for tournament " + tournamentId);
            log.info("to " + reportFilename);

            // report generation date and time
            DateFormat timestampFormat = new SimpleDateFormat("hh:mm:ss aaa MM/dd/yyyy");
            String generatedOnDate = timestampFormat.format(new Date());

            //Initialize PDF writer
            PdfWriter writer = new PdfWriter(reportFilename);

            //Initialize PDF document
            PdfDocument pdf = new PdfDocument(writer);

            // Initialize document
            Document document = new Document(pdf);
            document.setMargins(30, 30, 10, 30);

            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            int numPages = playerReportInfos.size() / PLAYERS_PER_PAGE;
            numPages += (playerReportInfos.size() % PLAYERS_PER_PAGE > 0) ? 1 : 0;

            int pageNum = 1;
            Table table = null;
            boolean addTable = true;

            SolidBorder whiteBorder = new SolidBorder(0);
            whiteBorder.setColor(WHITE);

            SolidBorder grayBorder = new SolidBorder(0);
            grayBorder.setColor(LIGHT_GRAY);
            TextAlignment[] cellAlignment = {
                    TextAlignment.RIGHT, TextAlignment.RIGHT, TextAlignment.LEFT, TextAlignment.RIGHT,
                    TextAlignment.CENTER, TextAlignment.CENTER, TextAlignment.CENTER,
                    TextAlignment.RIGHT, TextAlignment.RIGHT
            };

            int pageRowNumber = 1;

            for (PlayerReportInfo info : playerReportInfos) {
                if (addTable) {
                    addTable = false;
                    table = startTable(tournamentName, document, font, cellAlignment);
                    pageRowNumber = 1;
                }

                Color backgroundColor = (pageRowNumber % 2 == 1) ? WHITE : LIGHT_GRAY;
                SolidBorder borderColor = (pageRowNumber % 2 == 1) ? whiteBorder : grayBorder;
                pageRowNumber++;

                String[] cellValues = info.toStringArray();
                for (int i = 0; i < cellValues.length; i++) {
                    String cellText = cellValues[i];
                    Cell playerInfoCell = new Cell().add(new Paragraph(cellText)
                            .setFont(font).setFontSize(MAIN_FONT_SIZE).setTextAlignment(cellAlignment[i]));
                    playerInfoCell.setBorder(borderColor);
                    playerInfoCell.setBackgroundColor(backgroundColor);
                    int padding = (i == 0) ? 0 : CELL_PADDING;
                    playerInfoCell.setPaddingLeft(padding);
//                    playerInfoCell.setPaddingTop(2);
//                    playerInfoCell.setPaddingBottom(2);
                    table.addHeaderCell(playerInfoCell);
                }

                if (info.playerNumber % PLAYERS_PER_PAGE == 0) {
                    // add current table to document
                    document.add(table);

                    addFooter(document, pageNum, numPages, font, generatedOnDate);
                    pageNum++;

                    // add page break except for after last player
                    if (info.playerNumber < playerReportInfos.size()) {
                        document.add(new AreaBreak());
                    }
                    // start new table
                    addTable = true;
                    table = null;

                }
            }

            if (table != null) {
                document.add(table);
                addFooter(document, pageNum, numPages, font, generatedOnDate);
            }
            if (playerReportInfos.size() == 0) {
                Paragraph emptyReportParagraph = new Paragraph("No players participated in tournament");
                emptyReportParagraph.setFont(font);
                emptyReportParagraph.setFontSize(12);
                emptyReportParagraph.setBold();
                emptyReportParagraph.setTextAlignment(TextAlignment.CENTER);
                document.add(emptyReportParagraph);
            }

            document.close();

            log.info("Finished player list report for " + playerReportInfos.size() + " players");
        } catch (IOException e) {
            log.error("Unable to create player list report ", e);
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return reportFilename;
    }

    /**
     *
     * @param tournamentName
     * @param document
     * @param font
     * @param cellAlignment
     * @return
     */
    private Table startTable(String tournamentName, Document document, PdfFont font, TextAlignment[] cellAlignment) {
        Paragraph paragraph = new Paragraph(tournamentName);
        paragraph.setFont(font);
        paragraph.setFontSize(14);
        paragraph.setBold();
        paragraph.setTextAlignment(TextAlignment.CENTER);
        document.add(paragraph);

        Paragraph paragraph2 = new Paragraph("USATT Player Listing");
        paragraph2.setFont(font);
        paragraph2.setFontSize(12);
        paragraph2.setBold();
        paragraph2.setTextAlignment(TextAlignment.CENTER);
        document.add(paragraph2);

        String[] tableHeadersText = {"Count", "USATT#", "Name", "Rating", "State", "Zip", "Sex", "Birthdate", "Expires"};
        float[] columnWidths = new float[]{2, 2, 8, 2, 1, 2, 1, 2, 2};

        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth().setBorder(new SolidBorder(0));

        SolidBorder grayBorder = new SolidBorder(0);
        grayBorder.setColor(DARK_GRAY);

        for (int i = 0; i < tableHeadersText.length; i++) {
            String columnTitle = tableHeadersText[i];
            Cell headerCell = new Cell().add(new Paragraph(columnTitle)
                    .setFont(font).setFontSize(MAIN_FONT_SIZE).setBold().setTextAlignment(cellAlignment[i]));
            headerCell.setBorder(grayBorder);
            headerCell.setBackgroundColor(DARK_GRAY);
            int padding = (i == 0) ? 0 : CELL_PADDING;
            headerCell.setPaddingLeft(padding);
            table.addHeaderCell(headerCell);
        }

        return table;
    }

    /**
     * @param document
     * @param pageNumber
     * @param numPages
     * @param font
     * @param generatedOnDate
     */
    private void addFooter(Document document, int pageNumber, int numPages, PdfFont font, String generatedOnDate) {
        try {
            PdfDocument pdfDocument = document.getPdfDocument();
            if (pdfDocument != null) {
                PdfPage page = pdfDocument.getPage(pageNumber);
                if (page != null) {
                    Rectangle pageSize = page.getPageSize();
                    float x = document.getLeftMargin();
                    float y = pageSize.getBottom() + 30;
                    String footerText = String.format(
                    "Powered by Aurora TMS                                                Page %d of %d                                          %s",
                            pageNumber, numPages, generatedOnDate);
                    Paragraph footerPara = new Paragraph(footerText).setFont(font).setFontSize(10);
                    document.showTextAligned(footerPara, x, y, pageNumber, TextAlignment.LEFT, VerticalAlignment.TOP, 0);
                }
            }
        } catch (Exception e) {
            log.error("Unable to add footer for page " + pageNumber, e);
        }
    }

    /**
     *
     * @param tournamentId
     * @return
     */
    private List<PlayerReportInfo> prepareReportData (long tournamentId) {
        Set<String> uniqueProfileIdsSet = new HashSet<>();
        // get all events in this tournament
        Collection<TournamentEvent> allEvents = this.tournamentEventEntityService.list(tournamentId, Pageable.unpaged());
        for (TournamentEvent tournamentEvent : allEvents) {
            // doubles matches results are not sent to USATT so players who only played in doubles events are not counted?
            if (!tournamentEvent.isDoubles()) {
                Long eventId = tournamentEvent.getId();
                // get all match cards for this event
                log.info("Getting all match cards for event " + tournamentEvent.getName());
                List<MatchCard> allMatchCardsForEvent = matchCardService.findAllForEvent(eventId);

                // sort them so RR is followed by SE
                Comparator<MatchCard> comparator = Comparator
                        .comparing(MatchCard::getDrawType)
                        .thenComparing(Comparator.comparing(MatchCard::getRound).reversed())
                        .thenComparing(MatchCard::getGroupNum);
                Collections.sort(allMatchCardsForEvent, comparator);
                List<Match> matchesForEvent = matchService.findAllByMatchCardIn(allMatchCardsForEvent);
                for (Match match : matchesForEvent) {
                    if (match.isMatchFinished(tournamentEvent.getNumberOfGames(), tournamentEvent.getPointsPerGame())) {
                        if (!match.isSideADefaulted()) {
                            uniqueProfileIdsSet.add(match.getPlayerAProfileId());
                        }
                        if (!match.isSideBDefaulted()) {
                            uniqueProfileIdsSet.add(match.getPlayerBProfileId());
                        }
                    }
                }
            }
        }
        log.info("Found " + uniqueProfileIdsSet.size() + " unique player ids who played matches");

        // get profile information for those players who played at least one match
        List<String> profileIds = new ArrayList<>(uniqueProfileIdsSet);
        // get profile id to membership id map
        Map<String, UserProfileExt> profileIdToUserExtProfileMap = userProfileExtService.findByProfileIds(profileIds);

        Map<String, PlayerReportInfo> profileIdToReportInfoMap = new HashMap<>(profileIds.size());

        // sort players alphabetically by last name first name
        Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIds);
        List<UserProfile> userProfileList = new ArrayList<>(userProfiles);
        Comparator<UserProfile> comparator = Comparator.comparing(UserProfile::getLastName)
                .thenComparing(UserProfile::getFirstName);
        Collections.sort(userProfileList, comparator);

        // get membership expiration dates from the USATT record service
        List<Long> membershipIds = new ArrayList<>(profileIds.size());
        for (UserProfile userProfile : userProfileList) {
            UserProfileExt userProfileExt = profileIdToUserExtProfileMap.get(userProfile.getUserId());
            if (userProfileExt != null) {
                membershipIds.add(userProfileExt.getMembershipId());
            }
        }

        // put the expiration dates in the map by membership id
        Map<Long, Date> membershipIdToExpirationDateMap = new HashMap<>();
        List<UsattPlayerRecord> usattPlayerRecordList = usattDataService.findAllByMembershipIdIn(membershipIds);
        for (UsattPlayerRecord usattPlayerRecord : usattPlayerRecordList) {
            membershipIdToExpirationDateMap.put(usattPlayerRecord.getMembershipId(), usattPlayerRecord.getMembershipExpirationDate());
        }

        int playerNumber = 0;

        for (UserProfile userProfile : userProfileList) {
            String playerProfileId = userProfile.getUserId();
            PlayerReportInfo playerReportInfo = new PlayerReportInfo();
            playerReportInfo.playerNumber = ++playerNumber;
            playerReportInfo.firstName = userProfile.getFirstName();
            playerReportInfo.lastName = userProfile.getLastName();
            playerReportInfo.gender = (userProfile.getGender().equals("Male")) ? "M" : "F";
            Date dateOfBirth = userProfile.getDateOfBirth();
            playerReportInfo.dateOfBirth = (dateOfBirth != null) ? dateFormat.format(dateOfBirth) : "";

            String countryCode = userProfile.getCountryCode();
            String state = countryCode.equals("US") ? userProfile.getState() : "FN";
            playerReportInfo.state = (state != null) ? state : "";

            playerReportInfo.zipCode = (userProfile.getZipCode() != null) ? userProfile.getZipCode() : "";

            UserProfileExt userProfileExt = profileIdToUserExtProfileMap.get(playerProfileId);
            if (userProfileExt != null) {
                playerReportInfo.membershipId = userProfileExt.getMembershipId();
            } else {
                playerReportInfo.membershipId = 0;
                log.warn("Unable to find membership id for player " + playerProfileId + " ( " + userProfile.getLastName() + ", "
                        + userProfile.getFirstName() + ") - setting membership id to 0 and skipping this player's data in the report");
            }

            if (playerReportInfo.membershipId != 0) {
                Date membershipExpirationDate = membershipIdToExpirationDateMap.get(playerReportInfo.membershipId);
                playerReportInfo.membershipExpirationDate = (membershipExpirationDate != null) ? dateFormat.format(membershipExpirationDate) : "T.B.D.";
            } else {
                playerReportInfo.membershipExpirationDate = "T.B.D.";
            }

            profileIdToReportInfoMap.put(playerProfileId, playerReportInfo);
        }

        // get tournament entry with tournament rating
        List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournament(tournamentId);
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            // if player played
            PlayerReportInfo playerReportInfo = profileIdToReportInfoMap.get(tournamentEntry.getProfileId());
            if (playerReportInfo != null) {
                playerReportInfo.rating = tournamentEntry.getEligibilityRating();
            }
        }

        // sort after extraction from the map
        List<PlayerReportInfo> sortedPlayerReportInfos = new ArrayList<>(profileIdToReportInfoMap.values());
        Comparator<PlayerReportInfo> comparator2 = Comparator.comparing(PlayerReportInfo::getLastName)
                .thenComparing(PlayerReportInfo::getFirstName);
        Collections.sort(sortedPlayerReportInfos, comparator2);

        log.info("Finished collecting player list data");

        return sortedPlayerReportInfos;
    }

    // Class for holding report data for one row
    private class PlayerReportInfo {
        int playerNumber;
        String firstName;
        String lastName;
        String gender;
        long membershipId; // 0 if not a member yet
        int rating;  // rating for this tournament
        String state;
        String zipCode;
        String dateOfBirth;
        String membershipExpirationDate;

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        /**
         * Needed by PDF table writer
         * @return
         */
        public String[] toStringArray() {
            // "Count", "USATT#", "Name", "Rating", "State", "Zip", "Sex", "Birthdate", "Expires"
            String [] strArray = new String[9];
            strArray[0] = Integer.toString(playerNumber);
            strArray[1] = Long.toString(membershipId);
            strArray[2] = lastName + ", " + firstName;
            strArray[3] = Integer.toString(rating);
            strArray[4] = state;
            strArray[5] = zipCode;
            strArray[6] = gender;
            strArray[7] = dateOfBirth;
            strArray[8] = membershipExpirationDate;
            return strArray;
        }
    }
}
