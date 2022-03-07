package com.auroratms.reports;

import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.MembershipType;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.auroratms.tournamententry.MembershipType.*;

@Service
@Slf4j
public class TournamentReportService {

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentService tournamentService;

    static final int FONT_SIZE = 10;
    static final int NOTES_FONT_SIZE = 8;
    static final float INNER_TABLE_BORDER = 2;

    static final Color WHITE = new DeviceRgb(0xFF, 0xFF, 0xFF);

    // 11/08/2003 - mm/dd/yyyy
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public String generateReport(long tournamentId, String card4Digits, String remarks) {
        String reportFilename = null;
        try {
            // get tournament information
            Tournament tournament = tournamentService.getByKey(tournamentId);

            // collect the data
            List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournament(tournamentId);
            List<MembershipTableData> membershipTableData = getPurchasedMembershipInformation(tournamentEntries);
            double totalDonations = getTotalDonations(tournamentEntries);

//            String card4Digits = getCreditCardForPayment (tournament);

            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            File reportFile = new File(tempDir + File.separator + "tournament-report-" + tournamentId + ".pdf");
            reportFilename = reportFile.getCanonicalPath();
            log.info("Writing tournament report for tournament " + tournamentId);
            log.info("to " + reportFilename);

            //Initialize PDF writer
            PdfWriter writer = new PdfWriter(reportFilename);

            //Initialize PDF document
            PdfDocument pdf = new PdfDocument(writer);

            // Initialize document
            Document document = new Document(pdf);
            document.setMargins(30, 30, 10, 30);

            PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);

            Paragraph paragraph = new Paragraph("USATT Tournament Report")
                    .setFont(font).setFontSize(14).setBold().setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15).setMarginTop(5);
            document.add(paragraph);

            Table tournamentTable = makeTournamentTable(tournament, font, fontBold);
            document.add(tournamentTable);

            Table commissionedMembershipsTable = makeMembershipsTable(membershipTableData, totalDonations, font, card4Digits);
            document.add(commissionedMembershipsTable);

            addFootnotesParagraphs(document, font);

            String remarksText = "Remarks: " + remarks;
            Paragraph remarksParagraph = new Paragraph(remarksText)
                    .setFont(font).setFontSize(FONT_SIZE).setBold()
                    .setBorder(new SolidBorder(INNER_TABLE_BORDER))
                    .setPaddingLeft(10).setPaddingRight(10).setHeight(100f);
            document.add(remarksParagraph);

            addFooterSection(document, font);

            addDocumentRevisionAndBorder(document, font);

            document.close();

            log.info("Finished tournament report");
        } catch (IOException e) {
            log.error("Unable to create tournament report ", e);
        } finally {
        }
        return reportFilename;
    }

    /**
     * @param document
     * @param font
     */
    private void addFooterSection(Document document, PdfFont font) {
        String[] penalties = {
                "*** All payments and reports are required no later than 14 days following the tournament ***",
                "Penalties may apply for late submission of payments, reports and forms."
        };
        for (String penalty : penalties) {
            Paragraph paragraph = new Paragraph(penalty)
                    .setFont(font).setFontSize(FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER)
                    .setPaddingLeft(10).setPaddingRight(10)
                    .setMarginBottom(0).setMarginTop(0);
            document.add(paragraph);
        }

        String[] finalNotes = {
                "Send the following items along with this report to:",
                "USA Table Tennis, 4065 Sinton Rd #120, Colorado Springs, CO 80907",
                "1) White copy of all membership applications; 2) Player entry list; 3) Blank tournament entry form; 4) Waiver of liability forms",
                "5) Check for membership and processing fees.",
                "Please refer to \"Tournament Report Instructions\" for further instructions. Thank you."
        };
        for (String finalNote : finalNotes) {
            Paragraph paragraph = new Paragraph(finalNote)
                    .setFont(font).setFontSize(NOTES_FONT_SIZE).setTextAlignment(TextAlignment.LEFT).
                    setPaddingLeft(10).setPaddingRight(10)
                    .setMarginBottom(0).setMarginTop(0);
            document.add(paragraph);
        }
    }

    /**
     * @param document
     * @param font
     */
    private void addFootnotesParagraphs(Document document, PdfFont font) {
        String[] footnotes = {
                "1-Junior must be 17 years old or younger.",
                "2-Junior 3 year must be 14 or younger",
                "3-College student must have a photo copy of either a valid registration card or student ID. Must be full time college or graduate student",
                "4-Household is defined as not more than two adults and any number of minor childres living at the same address.",
                "Membership must include birthdays of all family members."
        };

        for (String footnote : footnotes) {
            Paragraph paragraph = new Paragraph(footnote)
                    .setFont(font).setFontSize(NOTES_FONT_SIZE)
                    .setMarginBottom(0).setMarginTop(0);
            document.add(paragraph);
        }
    }

    /**
     * @param tournamentEntries
     * @return
     */
    private double getTotalDonations(List<TournamentEntry> tournamentEntries) {
        double totalDonations = 0;
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            totalDonations += tournamentEntry.getUsattDonation();
        }
        return totalDonations;
    }

    /**
     * @param membershipTableData
     * @param donationsTotal
     * @param font
     * @param card4Digits
     * @return
     */
    private Table makeMembershipsTable(List<MembershipTableData> membershipTableData, double donationsTotal, PdfFont font, String card4Digits) throws IOException {

        TextAlignment[] cellAlignment = {
                TextAlignment.LEFT, TextAlignment.LEFT, TextAlignment.RIGHT, TextAlignment.CENTER,
                TextAlignment.RIGHT, TextAlignment.RIGHT};

        String[] tableHeadersText = {"Membership Type", "Term", "Cost", "", "No. Sold", "Total"};
        float[] columnWidths = new float[]{3, 1, 1, 1, 1, 1};

        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth().setBorder(new SolidBorder(INNER_TABLE_BORDER))
                .setMarginTop(5).setMarginBottom(5);

        String currencySymbol = "$";

        SolidBorder whiteBorder = new SolidBorder(0);
        whiteBorder.setColor(WHITE);

        // table header
        Cell headerCell = new Cell(1, 6).add(new Paragraph("USATT Membership/Tournament Passes Sold")
                .setFont(font).setFontSize(FONT_SIZE).setBold()
                .setTextAlignment(TextAlignment.CENTER)).setBorder(whiteBorder);
        table.addHeaderCell(headerCell);
        for (int i = 0; i < tableHeadersText.length; i++) {
            headerCell = new Cell().add(new Paragraph(tableHeadersText[i])
                    .setFont(font).setFontSize(FONT_SIZE).setBold()
                    .setTextAlignment(cellAlignment[i])).setBorder(whiteBorder);
            if (i == 0) {
                headerCell.setPaddingLeft(10);
            } else if (i == tableHeadersText.length - 1) {
                headerCell.setPaddingRight(10);
            }
            table.addHeaderCell(headerCell);
        }

        // commissioned membership data
        double totalMembershipFees = 0;
        for (MembershipTableData data : membershipTableData) {
            if (isCommissionedMembership(data.membershipType)) {
                makeMembershipRow(font, table, currencySymbol, whiteBorder, data);
                totalMembershipFees += data.total;
            }
        }

        double discount = 0.1 * totalMembershipFees;
        double netMembershipFees = totalMembershipFees - discount;

        final String CURRENCY_FORMAT = "%s%,.2f";

        String strTotalMembershipFees = String.format(CURRENCY_FORMAT, currencySymbol, totalMembershipFees);
        String strDiscount = String.format(CURRENCY_FORMAT, currencySymbol, discount);
        String strNetMembershipFees = String.format(CURRENCY_FORMAT, currencySymbol, netMembershipFees);

        makeTotalsRow(font, table, whiteBorder, "Total Membership Fees", strTotalMembershipFees, 5);

        PdfFont italicFont = PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC);
        Cell noteCell = new Cell(1, 3).add(new Paragraph("For 0-4 Star Tournaments. Does not apply to US Open or NA Teams")
                        .setFont(italicFont).setFontSize(FONT_SIZE).setTextAlignment(TextAlignment.LEFT))
                .setBorder(whiteBorder).setPaddingLeft(10);
        table.addCell(noteCell);

        makeTotalsRow(font, table, whiteBorder, "Less 10% discount", strDiscount, 2);
        makeTotalsRow(font, table, whiteBorder, "Net Membership fees", strNetMembershipFees, 5);

        // non-commissioned membership totals
        double nonCommisionedMembershipTotal = 0;
        for (MembershipTableData data : membershipTableData) {
            if (!isCommissionedMembership(data.membershipType)) {
                makeMembershipRow(font, table, currencySymbol, whiteBorder, data);
                nonCommisionedMembershipTotal += data.total;
            }
        }
        double totalMembershipFeesDue = netMembershipFees + nonCommisionedMembershipTotal;
        String strTotalMembershipFeesDue = String.format(CURRENCY_FORMAT, currencySymbol, totalMembershipFeesDue);
        makeTotalsRow(font, table, whiteBorder, "Total Membership Fees Due", strTotalMembershipFeesDue, 5);

        // Total Optional Donations to TT Team USA National Program
        String strDonationsTotal = String.format(CURRENCY_FORMAT, currencySymbol, donationsTotal);
        makeTotalsRow(italicFont, table, whiteBorder, "Total Optional Donations to TT Team USA National Program", strDonationsTotal, 5);

        double grandTotalDue = totalMembershipFeesDue + donationsTotal;
        String strGrandTotalDue = String.format(CURRENCY_FORMAT, currencySymbol, grandTotalDue);
        makeTotalsRow(font, table, whiteBorder, "Total Amount", strGrandTotalDue, 5);

        // make footer row with credit card information if used to pay for tournament report
        if (card4Digits != null) {
            String ccInfo = "PLEASE PROVIDE LAST FOUR DIGITS OF CARD ON FILE: " + card4Digits;
            Cell footerCell = new Cell(1, 6).add(new Paragraph(ccInfo)
                    .setFont(font).setFontSize(FONT_SIZE).setBold()
                    .setTextAlignment(TextAlignment.CENTER)).setBorder(whiteBorder);
            table.addFooterCell(footerCell);
        }

        return table;
    }

    private boolean isCommissionedMembership(MembershipType membershipType) {
        return membershipType == BASIC_PLAN || membershipType == PRO_PLAN;
    }

    /**
     * @param font
     * @param table
     * @param whiteBorder
     * @param lineText
     * @param value
     * @param colSpan
     */
    private void makeTotalsRow(PdfFont font, Table table, SolidBorder whiteBorder, String lineText, String value, int colSpan) {
        Cell cell1 = new Cell(1, colSpan).add(new Paragraph(lineText)
                .setFont(font).setFontSize(FONT_SIZE).setTextAlignment(TextAlignment.RIGHT)).setBorder(whiteBorder);
        table.addCell(cell1);

        Cell cell2 = new Cell().add(new Paragraph(value)
                        .setFont(font).setFontSize(FONT_SIZE).setTextAlignment(TextAlignment.RIGHT)).setBorder(whiteBorder)
                .setPaddingRight(10);
        table.addCell(cell2);
    }

    /**
     * @param font
     * @param table
     * @param currencySymbol
     * @param whiteBorder
     * @param data
     */
    private void makeMembershipRow(PdfFont font, Table table, String currencySymbol, SolidBorder whiteBorder, MembershipTableData data) {
        Cell cell1 = new Cell().add(new Paragraph(data.strMembershipType)
                .setFont(font).setFontSize(FONT_SIZE).setTextAlignment(TextAlignment.LEFT)).setBorder(whiteBorder).setPaddingLeft(10);
        table.addCell(cell1);
        Cell cell2 = new Cell().add(new Paragraph(data.membershipTerm)
                .setFont(font).setFontSize(FONT_SIZE).setTextAlignment(TextAlignment.LEFT)).setBorder(whiteBorder);
        table.addCell(cell2);
        Cell cell3 = new Cell().add(new Paragraph(currencySymbol + Integer.toString(data.cost))
                .setFont(font).setFontSize(FONT_SIZE).setTextAlignment(TextAlignment.RIGHT)).setBorder(whiteBorder);
        table.addCell(cell3);
        Cell cell4 = new Cell().add(new Paragraph("x")
                .setFont(font).setFontSize(FONT_SIZE).setTextAlignment(TextAlignment.CENTER)).setBorder(whiteBorder);
        table.addCell(cell4);
        Cell cell5 = new Cell().add(new Paragraph(Integer.toString(data.numberSold))
                .setFont(font).setFontSize(FONT_SIZE).setTextAlignment(TextAlignment.RIGHT)).setBorder(whiteBorder);
        table.addCell(cell5);
        Cell cell6 = new Cell().add(new Paragraph(currencySymbol + Integer.toString(data.total))
                .setFont(font).setFontSize(FONT_SIZE).setTextAlignment(TextAlignment.RIGHT)).setBorder(whiteBorder).setPaddingRight(10);
        table.addCell(cell6);
    }

    /**
     * @param tournament
     * @param font
     * @param fontBold
     * @return
     */
    private Table makeTournamentTable(Tournament tournament, PdfFont font, PdfFont fontBold) {
        // prepare data
        String tournamentName = tournament.getName();
        String strTournamentDate = dateFormat.format(tournament.getStartDate());
        // from sanction form ?
        String organization = "Fox Valley Table Tennis Club";
        String cityStateZip = tournament.getCity() + ", " + tournament.getState() + " " + tournament.getZipCode();
        // TODO - need to get current user
        String submittedByName = tournament.getContactName();
        String submittedByPhoneNumber = tournament.getPhone();
        String submittedByCityStateZip = tournament.getCity() + ", " + tournament.getState() + " " + tournament.getZipCode();


        float[] columnWidths = new float[]{1, 2, 1, 1};
        SolidBorder whiteBorder = new SolidBorder(0);
        whiteBorder.setColor(WHITE);
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth().setBorder(new SolidBorder(INNER_TABLE_BORDER));
        addTournamentInfoRow(table, font, fontBold, whiteBorder, "Tournament Name", tournamentName, 3);
        addTournamentInfoRow(table, font, fontBold, whiteBorder, "Date of Tournament", strTournamentDate, 3);
        addTournamentInfoRow(table, font, fontBold, whiteBorder, "Club/Organization", organization, 3);
        addTournamentInfoRow(table, font, fontBold, whiteBorder, "City/State/Zip", cityStateZip, 3);
        // place on the same line
        addTournamentInfoRow(table, font, fontBold, whiteBorder, "Submitted by name", submittedByName, 1);
        addTournamentInfoRow(table, font, fontBold, whiteBorder, "Phone", submittedByPhoneNumber, 1);

        addTournamentInfoRow(table, font, fontBold, whiteBorder, "City/State/Zip", submittedByCityStateZip, 3);

        return table;
    }

    /**
     * @param table
     * @param font
     * @param fontBold
     * @param whiteBorder
     * @param lineText
     * @param value
     * @param columnSpan
     */
    private void addTournamentInfoRow(Table table, PdfFont font, PdfFont fontBold, SolidBorder whiteBorder, String lineText, String value, int columnSpan) {
        // tournament name
        Cell cell1 = new Cell().add(new Paragraph(lineText)
                        .setFont(fontBold).setFontSize(FONT_SIZE).setTextAlignment(TextAlignment.LEFT))
                .setBorder(whiteBorder).setPaddingLeft(10);

        table.addCell(cell1);

        Cell cell2 = new Cell(1, columnSpan).add(new Paragraph(value)
                        .setFont(font).setFontSize(FONT_SIZE).setTextAlignment(TextAlignment.LEFT))
                .setBorder(whiteBorder).setUnderline( 0.1f, -2f);
        table.addCell(cell2);
    }

    /**
     * @param tournamentEntries
     * @return
     */
    private List<MembershipTableData> getPurchasedMembershipInformation(List<TournamentEntry> tournamentEntries) {

        List<MembershipTableData> membershipTableData = new ArrayList<>();
        MembershipTableData data = new MembershipTableData();
        data.membershipType = BASIC_PLAN;
        data.membershipTerm = "1 Year";
        data.strMembershipType = "Basic Plan";
        data.cost = 25;
        membershipTableData.add(data);

        data = new MembershipTableData();
        data.membershipType = PRO_PLAN;
        data.membershipTerm = "1 Year";
        data.strMembershipType = "Pro Plan";
        data.cost = 75;
        membershipTableData.add(data);

        data = new MembershipTableData();
        data.membershipType = TOURNAMENT_PASS_JUNIOR;
        data.membershipTerm = "";
        data.strMembershipType = "Tournament Pass Junior (17 and under)";
        data.cost = 20;
        membershipTableData.add(data);

        data = new MembershipTableData();
        data.membershipType = TOURNAMENT_PASS_ADULT;
        data.membershipTerm = "";
        data.strMembershipType = "Tournament Pass Adult";
        data.cost = 50;
        membershipTableData.add(data);

        data = new MembershipTableData();
        data.membershipType = LIFETIME;
        data.membershipTerm = "Life";
        data.strMembershipType = "Lifetime";
        data.cost = 1300;
        membershipTableData.add(data);

        // count the number of memberships of all types
        Map<MembershipType, Integer> membershipTypeToCountMap = new HashMap<>();
        membershipTypeToCountMap.put(TOURNAMENT_PASS_JUNIOR, new Integer(0));
        membershipTypeToCountMap.put(MembershipType.TOURNAMENT_PASS_ADULT, new Integer(0));
        membershipTypeToCountMap.put(BASIC_PLAN, new Integer(0));
        membershipTypeToCountMap.put(MembershipType.PRO_PLAN, new Integer(0));
        membershipTypeToCountMap.put(MembershipType.LIFETIME, new Integer(0));
        // get all tournament entries for this tournament and collect player profiles ids
        // of players who bought some type of membership
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            MembershipType membershipOption = tournamentEntry.getMembershipOption();
            if (membershipOption != MembershipType.NO_MEMBERSHIP_REQUIRED) {
                Integer count = membershipTypeToCountMap.get(membershipOption);
                count++;
                membershipTypeToCountMap.put(membershipOption, count);
            }
        }

        // transfer counts and calculate totals
        for (MembershipTableData datum : membershipTableData) {
            Integer count = membershipTypeToCountMap.get(datum.membershipType);
            datum.numberSold = count;
            datum.total = datum.cost * datum.numberSold;
        }

        return membershipTableData;
    }

    private class MembershipTableData {
        MembershipType membershipType;
        String strMembershipType;
        String membershipTerm;
        int cost;
        int numberSold;
        int total;
    }

    private void addDocumentRevisionAndBorder(Document document, PdfFont font) {
        try {
            PdfDocument pdfDocument = document.getPdfDocument();
            if (pdfDocument != null) {
                PdfPage page = pdfDocument.getPage(1);
                if (page != null) {
                    Rectangle pageSize = page.getPageSize();

                    // add border
                    float width = pageSize.getWidth();
                    float height = pageSize.getHeight();

                    // Define a PdfCanvas instance
                    PdfCanvas canvas = new PdfCanvas(page);
                    document.getLeftMargin();
                    // Add a rectangle 0, 0 is in bottom left corner
                    canvas.rectangle(document.getLeftMargin() - 5,
                            document.getBottomMargin() + 15,
                            width - document.getRightMargin() - document.getLeftMargin() + 10,
                            height - document.getTopMargin() - document.getBottomMargin() - 50);
                    canvas.stroke();

                    File usattLogoFile = ResourceUtils.getFile("classpath:images/usatt-logo-horizontal.jpg");
                    ImageData usattLogoData = ImageDataFactory.create(usattLogoFile.getAbsolutePath());
                    Image usattLogo = new Image(usattLogoData);
                    usattLogo.scaleToFit(73f, 32f);
                    float imageX = document.getLeftMargin() + 10;
                    float imageY = height - document.getTopMargin() - document.getBottomMargin() - 20;
                    canvas.addImageAt(usattLogoData, imageX, imageY, true);

                    // add form revision
                    float x = pageSize.getRight() - document.getRightMargin();
                    float y = pageSize.getBottom() + document.getBottomMargin() + 10;
                    String footerText = "Form T-109 (revised 02/21/2018)";
                    Paragraph footerPara = new Paragraph(footerText).setFont(font).setFontSize(10);
                    document.showTextAligned(footerPara, x, y, 1, TextAlignment.RIGHT, VerticalAlignment.TOP, 0);
                }
            }
        } catch (Exception e) {
            log.error("Unable to add footer for page 1", e);
        }
    }


}
