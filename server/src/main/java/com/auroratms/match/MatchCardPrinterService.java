package com.auroratms.match;

import com.auroratms.event.TournamentEventEntity;
import com.auroratms.event.TournamentEventEntityService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
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
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import org.apache.commons.lang3.StringUtils;
//import org.dom4j.DocumentException;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class MatchCardPrinterService {

    @Autowired
    private MatchCardService matchCardService;

    @Autowired
    private TournamentEventEntityService tournamentEventEntityService;

    @Autowired
    private TournamentService tournamentService;

    static final int MATCHES_PER_PAGE = 10;
    static final int DOUBLES_MATCHES_PER_PAGE = 6;

    static final Color LIGHT_GRAY = new DeviceRgb(0xF5, 0xF5, 0xF5); // white smoke

    static final int CELL_PADDING = 3;

    public String getMatchCardAsPDF(long matchCardId) {
        MatchCard matchCard = matchCardService.getMatchCardWithPlayerProfiles(matchCardId);
        long eventFk = matchCard.getEventFk();

        TournamentEventEntity event = tournamentEventEntityService.get(eventFk);

        Tournament tournament = tournamentService.getByKey(event.getTournamentFk());

        String matchCardFilename = null;

        try {
            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
//            File matchCardFile = File.createTempFile("match-card", ".pdf", new File(tempDir));
            File matchCardFile = new File(tempDir + File.separator + "match-card-" + matchCardId + ".pdf");
            matchCardFilename = matchCardFile.getCanonicalPath();

            //Initialize PDF writer
            PdfWriter writer = new PdfWriter(matchCardFilename);

            //Initialize PDF document
            PdfDocument pdf = new PdfDocument(writer);

            // Initialize document
            Document document = new Document(pdf);
            document.setMargins(30, 30, 10, 30);

            generateContent(document, matchCard, tournament, event);

            document.close();
//        } catch (DocumentException e) {
//            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return matchCardFilename;
    }

    /**
     *
     * @param document
     * @param matchCard
     * @param tournament
     * @param event
     * @throws DocumentException
     * @throws IOException
     */
    private void generateContent(Document document, MatchCard matchCard, Tournament tournament, TournamentEventEntity event) throws IOException {
//    private void generateContent(Document document, MatchCard matchCard, Tournament tournament, TournamentEventEntity event) throws DocumentException, IOException {
        String tournamentName = tournament.getName();
//        tournamentName = "2021 US National Table Tennis Championships";
        String eventName = event.getName();
        String startTime = getStartTime(matchCard);
//        String startTime = "10:30 AM";

        List<Match> matches = matchCard.getMatches();
        int matchesPerPage = (!event.isDoubles()) ? MATCHES_PER_PAGE : DOUBLES_MATCHES_PER_PAGE;

        int numPages = matches.size() / matchesPerPage;
        numPages += (matches.size() % matchesPerPage > 0) ? 1 : 0;

        PdfFont pdfFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        for (int page = 0; page < numPages; page++) {
            Paragraph paragraph = new Paragraph(tournamentName);
            paragraph.setFont(pdfFont);
            paragraph.setFontSize(22);
            paragraph.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(paragraph);

            // event header table
            Table eventInfoTable = addEventInfoTable(eventName, matchCard, startTime, pdfFont);
            document.add(eventInfoTable);
            int startingMatchNum = (page * matchesPerPage) + 1;

            Paragraph paragraph3 = new Paragraph("Please circle winning player NAME.  DO NOT circle individual scores. Mark default with an X.");
            paragraph3.setFont(pdfFont);
            paragraph3.setFontSize(10);
            paragraph3.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(paragraph3);

            // add matches table
            Table matchesTable = addMatchScoresTable(matchCard, startingMatchNum, event.isDoubles(), matchesPerPage, pdfFont);
            document.add(matchesTable);

            addPageNumber (page, numPages, pdfFont, document);
            if (page < (numPages - 1)) {
                document.add(new AreaBreak());
            }
        }
    }

    /**
     * Gets start time
     * @param matchCard
     * @return
     */
    private String getStartTime(MatchCard matchCard) {
        double dStartTime = matchCard.getStartTime();
        Date today = new Date();
        int hours = (int) Math.floor(dStartTime);
        double dMinutes = dStartTime - hours;
        int minutes = (int) Math.round(60 * dMinutes);
        Date todayWithHours = DateUtils.setHours(today, hours);
        Date todayWithHoursAndMinutes = DateUtils.setMinutes(todayWithHours, minutes);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");  // 9:30 AM
        return simpleDateFormat.format(todayWithHoursAndMinutes);
    }

    /**
     *
     * @param pageNumber
     * @param numPages
     * @param font
     * @param document
     */
    private void addPageNumber(int pageNumber, int numPages, PdfFont font, Document document) {
        try {
            pageNumber = pageNumber + 1;
            String footerText = String.format("Page %d of %d", pageNumber, numPages);
            Paragraph footerPara = new Paragraph(footerText).setFont(font).setFontSize(10);
            PdfDocument pdfDocument = document.getPdfDocument();
            PdfPage page = pdfDocument.getPage(pageNumber);
            Rectangle pageSize = page.getPageSize();
            float x = (pageSize.getWidth() / 2) - 20;
            float y = pageSize.getBottom() + 30;
            document.showTextAligned(footerPara, x, y, pageNumber, TextAlignment.LEFT, VerticalAlignment.TOP, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param eventName
     * @param matchCard
     * @param startTime
     * @param font
     * @return
     */
    private Table addEventInfoTable(String eventName, MatchCard matchCard, String startTime, PdfFont font) {
        int groupNum = matchCard.getGroupNum();
        String assignedTables = matchCard.getAssignedTables();
//        assignedTables = "120, 121";
        String [] tableHeadersText = {"Event", "Group", "Table(s)", "Time"};
        String [] cellValues = {eventName, ("" + groupNum), assignedTables, startTime};
        float [] columnWidths = new float[]{5, 1, 2, 2};
        // for single matches in single elimination rounds
        if (matchCard.getRound() > 0) {
            tableHeadersText = new String[]{"Event", "Round", "Table", "Time"};
            columnWidths = new float[]{5, 3, 1, 2};
            String roundName = getRoundName(matchCard.getRound());
//            eventName = "Boys' Singles U17-Prelims";
//            assignedTables = "1";
            cellValues = new String [] {eventName, roundName, assignedTables, startTime};
        }

        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth().setBorder(new SolidBorder(0));
        Stream.of(tableHeadersText)
                .forEach(columnTitle -> {
                    Cell headerCell = new Cell().add(new Paragraph(columnTitle).setFont(font).setFontSize(10));
                    headerCell.setBorder(new SolidBorder(0));
                    headerCell.setBackgroundColor(LIGHT_GRAY);
                    headerCell.setPaddingLeft(CELL_PADDING);
                    table.addHeaderCell(headerCell);
                });
        Stream.of(cellValues)
                .forEach(cellText -> {
                    Cell headerCell = new Cell().add(new Paragraph(cellText).setFont(font).setFontSize(18));
                    headerCell.setBorder(new SolidBorder(0));
                    headerCell.setPaddingLeft(CELL_PADDING);
                    table.addHeaderCell(headerCell);
                });
        return table;
    }

    /**
     *
     * @param round
     * @return
     */
    private String getRoundName(int round) {
        switch (round) {
            case 2:
                return "Final";
            case 4:
                return "Semi-Final";
            case 8:
                return "Quarter-Final";
            default:
                return "Round of " + round;
        }
    }

    /**
     * @param matchCard
     * @param startingMatchNum
     * @param matchesPerPage
     * @param font
     * @return
     */
    private Table addMatchScoresTable(MatchCard matchCard, int startingMatchNum, boolean isDoubles, int matchesPerPage, PdfFont font) {
        int numberOfGames = matchCard.getNumberOfGames();
        int numColumns = 5 + numberOfGames;

        String[] headerLine1Text = {"", "Games"};
        String[] headerLine2Text = new String[numColumns];
        headerLine2Text[0] = "#";  // match number
        headerLine2Text[1] = " ";  // player code
        headerLine2Text[2] = "Rat.";
        headerLine2Text[3] = (!isDoubles) ? "Player Name" : "Player Names";  // player name
        headerLine2Text[4] = "Def.";  // if any player defaulted
        for (int game = 1; game <= numberOfGames; game++) {
            headerLine2Text[4 + game] = "" + game;
        }

        float[] pointColumnWidths = new float[numColumns];
        pointColumnWidths[0] = 25f;
        pointColumnWidths[1] = 15f;
        pointColumnWidths[2] = 35f;
        pointColumnWidths[3] = 250f;
        pointColumnWidths[4] = 30f;
        for (int game = 1; game <= numberOfGames; game++) {
            pointColumnWidths[4 + game] = 60f;
        }

        Table table = new Table(pointColumnWidths);
        Stream.of(headerLine1Text).forEach((headerText -> {
            boolean firstHeader = ("".equals(headerText));
            int colSpan = (firstHeader) ? numColumns - numberOfGames : numberOfGames;
            Cell headerCell = new Cell(1, colSpan);
            headerCell.add(new Paragraph(headerText).setTextAlignment(TextAlignment.CENTER));
            headerCell.setFont(font);
            headerCell.setBorder(new SolidBorder(1));
            headerCell.setPadding(CELL_PADDING);
            headerCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
            headerCell.setBackgroundColor(LIGHT_GRAY);
            table.addHeaderCell(headerCell);
        }));

        Stream.of(headerLine2Text).forEach(headerText -> {
            Cell headerCell = new Cell();
            headerCell.add(new Paragraph(headerText).setTextAlignment(TextAlignment.CENTER));
            headerCell.setFont(font);
            headerCell.setBorder(new SolidBorder(1));
            headerCell.setPadding(CELL_PADDING);
            headerCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
            headerCell.setBackgroundColor(LIGHT_GRAY);
            table.addHeaderCell(headerCell);
        });

        // make a table for each match
        List<Match> matches = matchCard.getMatches();
        Map<String, String> profileIdToNameMap = matchCard.getProfileIdToNameMap();
        for (Match match : matches) {
            int matchNum = match.getMatchNum();
            if (matchNum >= startingMatchNum && matchNum < startingMatchNum + matchesPerPage) {
                // match number
                Cell matchNumCell = new Cell(2, 1);
                matchNumCell.add(new Paragraph("" + matchNum).setTextAlignment(TextAlignment.CENTER));
                matchNumCell.setFont(font);
                matchNumCell.setPadding(CELL_PADDING);
                matchNumCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
                matchNumCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
                table.addCell(matchNumCell);

                String playerAName = profileIdToNameMap.get(match.getPlayerAProfileId());
                String playerBName = profileIdToNameMap.get(match.getPlayerBProfileId());

                addPlayerLineCells(table, match.getPlayerALetter(), match.getPlayerARating(), playerAName, numberOfGames);
                addPlayerLineCells(table, match.getPlayerBLetter(), match.getPlayerBRating(), playerBName, numberOfGames);

                // separator row for all except last one
                if (matchNum < (startingMatchNum + matchesPerPage - 1)) {
                    addSeparatorRow(table, numColumns);
                }
            }
        }

        return table;
    }

    private void addSeparatorRow(Table table, int numColumns) {
        Cell separatorCell = new Cell(1, numColumns);
        separatorCell.setHeight(4);
        separatorCell.setBackgroundColor(LIGHT_GRAY);
        table.addCell(separatorCell);
    }

    /**
     * @param table
     * @param playerLetter
     * @param playerRating
     * @param playerNames
     * @param numberOfGames
     */
    private void addPlayerLineCells(Table table, Character playerLetter, int playerRating, String playerNames, int numberOfGames) {
        Cell letterCodeCell = new Cell().add(new Paragraph("" + playerLetter).setTextAlignment(TextAlignment.CENTER));
        letterCodeCell.setPadding(CELL_PADDING);
        letterCodeCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
//        letterCodeCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
        table.addCell(letterCodeCell);

        Cell ratingCell = new Cell().add(new Paragraph("" + playerRating).setTextAlignment(TextAlignment.RIGHT));
        ratingCell.setPadding(CELL_PADDING);
        ratingCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        ratingCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
        table.addCell(ratingCell);

        Cell playerNameCell = new Cell();
        if (playerNames.contains("/")) {
            String[] playerNameArray = playerNames.split("/");
            for (String playerName : playerNameArray) {
                playerNameCell.add(new Paragraph(playerName));
            }
        } else {
            playerNameCell.add(new Paragraph(playerNames));
        }
        playerNameCell.setPadding(CELL_PADDING);
        playerNameCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(playerNameCell);

        Cell defaultCell = new Cell().add(new Paragraph(" "));
        defaultCell.setPadding(CELL_PADDING);
        table.addCell(defaultCell);

        for (int game = 1; game <= numberOfGames; game++) {
            Cell gameScoreCell = new Cell().add(new Paragraph(" "));
            gameScoreCell.setPadding(CELL_PADDING);
            table.addCell(gameScoreCell);
        }
    }
}
