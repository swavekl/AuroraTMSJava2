package com.auroratms.reports;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service for producing a lodging report with a list of all players who entered the tournament,
 * leaving empty columns to log traveling party size, hotel names, and number of nights.
 */
@Service
@Slf4j
@Transactional
public class LodgingReportService {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentService tournamentService;

    private static final int MAIN_FONT_SIZE = 9;
    private static final int CELL_PADDING = 5;
    private static final int PLAYERS_PER_PAGE = 38;

    static final Color DARK_GRAY = new DeviceRgb(0xA0, 0xA0, 0xA0);
    static final Color LIGHT_GRAY = new DeviceRgb(0xD3, 0xD3, 0xD3);
    static final Color WHITE = new DeviceRgb(0xFF, 0xFF, 0xFF);

    public String generateReport(long tournamentId) {
        String reportFilename = null;
        try {
            Tournament tournament = tournamentService.getByKey(tournamentId);
            String tournamentName = tournament.getName();

            List<LodgingReportInfo> reportInfos = prepareReportData(tournamentId);

            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            File reportFile = new File(tempDir + File.separator + "lodging-list-" + tournamentId + ".pdf");
            reportFilename = reportFile.getCanonicalPath();
            log.info("Writing lodging report for tournament " + tournamentId + " to " + reportFilename);

            DateFormat timestampFormat = new SimpleDateFormat("hh:mm:ss aaa MM/dd/yyyy");
            String generatedOnDate = timestampFormat.format(new Date());

            PdfWriter writer = new PdfWriter(reportFilename);
            PdfDocument pdf = new PdfDocument(writer);

            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            Document document = new Document(pdf);
            document.setMargins(30, 30, 45, 30); // Margin allocation to prevent content overlapping our footer

            Table table = null;
            boolean addTable = true;

            SolidBorder thinCellBorder = new SolidBorder(0.5f);
            thinCellBorder.setColor(LIGHT_GRAY);

            TextAlignment[] cellAlignment = {
                    TextAlignment.LEFT,   // Name
                    TextAlignment.CENTER, // State
                    TextAlignment.CENTER, // Zip
                    TextAlignment.LEFT,   // Hotel Name
                    TextAlignment.CENTER, // Travelers
                    TextAlignment.CENTER  // Nights
            };

            int indexCounter = 0;

            for (LodgingReportInfo info : reportInfos) {
                if (addTable) {
                    addTable = false;
                    table = startTable(tournamentName, document, font, cellAlignment);
                }

                indexCounter++;

                String[] cellValues = info.toStringArray();
                for (int i = 0; i < cellValues.length; i++) {
                    String cellText = cellValues[i];
                    Cell playerInfoCell = new Cell().add(new Paragraph(cellText)
                            .setFont(font).setFontSize(MAIN_FONT_SIZE).setTextAlignment(cellAlignment[i]));

                    playerInfoCell.setBorder(thinCellBorder);
                    playerInfoCell.setBackgroundColor(WHITE);
                    playerInfoCell.setPaddingLeft(CELL_PADDING);
                    playerInfoCell.setPaddingRight(CELL_PADDING);
                    table.addCell(playerInfoCell);
                }

                if (indexCounter % PLAYERS_PER_PAGE == 0) {
                    document.add(table);
                    if (indexCounter < reportInfos.size()) {
                        document.add(new AreaBreak());
                    }
                    addTable = true;
                    table = null;
                }
            }

            if (table != null) {
                document.add(table);
            }

            if (reportInfos.isEmpty()) {
                Paragraph emptyReportParagraph = new Paragraph("No entries registered for this tournament");
                emptyReportParagraph.setFont(font);
                emptyReportParagraph.setFontSize(12);
                emptyReportParagraph.setBold();
                emptyReportParagraph.setTextAlignment(TextAlignment.CENTER);
                document.add(emptyReportParagraph);
            }

            // --- Two-Pass Architecture: Loops through the built document tree to render final total pages ---
            int totalPages = pdf.getNumberOfPages();
            for (int i = 1; i <= totalPages; i++) {
                PdfPage page = pdf.getPage(i);
                Rectangle pageSize = page.getPageSize();
                float x = 30; // Aligns with document left margin
                float y = pageSize.getBottom() + 20;

                String footerText = "Powered by Aurora TMS                                                Page %d of %d                                          %s"
                        .formatted(i, totalPages, generatedOnDate);

                PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdf);
                Canvas canvas = new Canvas(pdfCanvas, pageSize);

                // Set the layout metrics context directly onto the drawing canvas root
                canvas.setFont(font);
                canvas.setFontSize(10);

                // Uses raw String input variables to explicitly match iText 7.2.5's canvas signature definitions
                canvas.showTextAligned(footerText, x, y, TextAlignment.LEFT, VerticalAlignment.TOP, 0);
                canvas.close();
            }

            document.close();
            log.info("Finished lodging report for " + reportInfos.size() + " players");
        } catch (IOException e) {
            log.error("Unable to create lodging report ", e);
        }
        return reportFilename;
    }

    private Table startTable(String tournamentName, Document document, PdfFont font, TextAlignment[] cellAlignment) {
        Paragraph combinedHeader = new Paragraph("Lodging Report for " + tournamentName);
        combinedHeader.setFont(font).setFontSize(14).setBold().setTextAlignment(TextAlignment.CENTER);
        document.add(combinedHeader);

        String[] tableHeadersText = {"Player Name", "State", "Zip", "Hotel Name", "Travelers", "Nights"};
        float[] columnWidths = new float[]{9, 2, 3, 8, 3, 2};

        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth().setBorder(new SolidBorder(0));

        SolidBorder headerBorder = new SolidBorder(1.0f);
        headerBorder.setColor(DARK_GRAY);

        for (int i = 0; i < tableHeadersText.length; i++) {
            String columnTitle = tableHeadersText[i];
            Cell headerCell = new Cell().add(new Paragraph(columnTitle)
                    .setFont(font).setFontSize(MAIN_FONT_SIZE).setBold().setTextAlignment(cellAlignment[i]));
            headerCell.setBorder(headerBorder);
            headerCell.setBackgroundColor(DARK_GRAY);
            headerCell.setPaddingLeft(CELL_PADDING);
            headerCell.setPaddingRight(CELL_PADDING);
            table.addHeaderCell(headerCell);
        }

        return table;
    }

    private List<LodgingReportInfo> prepareReportData(long tournamentId) {
        List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournament(tournamentId);
        List<String> profileIds = new ArrayList<>();
        for (TournamentEntry entry : tournamentEntries) {
            profileIds.add(entry.getProfileId());
        }

        log.info("Found " + profileIds.size() + " tournament entries. Fetching profile info...");

        Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIds);
        List<UserProfile> userProfileList = new ArrayList<>(userProfiles);
        Comparator<UserProfile> comparator = Comparator.comparing(UserProfile::getLastName)
                .thenComparing(UserProfile::getFirstName);
        Collections.sort(userProfileList, comparator);

        List<LodgingReportInfo> sortedReportInfos = new ArrayList<>();

        for (UserProfile userProfile : userProfileList) {
            LodgingReportInfo info = new LodgingReportInfo();
            info.firstName = userProfile.getFirstName();
            info.lastName = userProfile.getLastName();

            String countryCode = userProfile.getCountryCode();
            String state = "US".equals(countryCode) ? userProfile.getState() : "FN";
            info.state = (state != null) ? state : "";
            info.zipCode = (userProfile.getZipCode() != null) ? userProfile.getZipCode() : "";

            info.hotelName = "";
            info.travelersCount = "";
            info.nightsCount = "";

            sortedReportInfos.add(info);
        }

        log.info("Finished collecting lodging report data");
        return sortedReportInfos;
    }

    private static class LodgingReportInfo {
        String firstName;
        String lastName;
        String state;
        String zipCode;
        String hotelName;
        String travelersCount;
        String nightsCount;

        public String[] toStringArray() {
            String[] strArray = new String[6];
            strArray[0] = lastName + ", " + firstName;
            strArray[1] = state;
            strArray[2] = zipCode;
            strArray[3] = hotelName;
            strArray[4] = travelersCount;
            strArray[5] = nightsCount;
            return strArray;
        }
    }
}
