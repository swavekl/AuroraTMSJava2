package com.auroratms.reports;

import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class DeclarationOfComplianceReportService {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    ResourceLoader resourceLoader;

    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public String generateReport(long tournamentId) {
        String reportFilename = null;
        try {
            // create report file path
            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            File reportFile = new File(tempDir + File.separator + "declaration-of-compliance-" + tournamentId + ".pdf");
            reportFilename = reportFile.getCanonicalPath();
            log.info("Writing declaration of compliance for tournament " + tournamentId);
            log.info("to " + reportFilename);

            Resource resource = resourceLoader.getResource("classpath:pdftemplates/tournament-director-declaration-of-compliance.pdf");
            String pdfTemplate = resource.getFile().getAbsolutePath();

            PdfDocument pdf = new PdfDocument(new PdfReader(pdfTemplate), new PdfWriter(reportFilename));

            PdfAcroForm form = PdfAcroForm.getAcroForm(pdf, true);
            Map<String, PdfFormField> fields = form.getFormFields();

            // fill values
            Tournament tournament = tournamentService.getByKey(tournamentId);
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            String strToday = dateFormat.format(new Date());
            fields.get("I").setValue(tournament.getContactName());
            fields.get("following USATT Sanctioned Tournament").setValue(tournament.getName());
            fields.get("Signature of Tournament Director 2").setValue(strToday);
            fields.get("Printed Name of Tournament Director 1").setValue(tournament.getContactName());
            fields.get("Printed Name of Tournament Director 2").setValue(tournament.getEmail());
            fields.get("Tournament Director Mobile Phone").setValue(tournament.getPhone());
            fields.get("Address of Tournament Location 1").setValue(tournament.getVenueName());
            fields.get("Address of Tournament Location 2").setValue(tournament.getStreetAddress());
            fields.get("Address of Tournament Location 3").setValue(tournament.getCity() + ", " + tournament.getState() + ", " + tournament.getZipCode());

            pdf.close();

            log.info("Finished declaration of compliance for " + tournamentId + " tournament");

        } catch (IOException e) {
            log.error("Unable to create declaration of compliance report ", e);
        }
        return reportFilename;
    }

//            //Initialize PDF writer
//            PdfWriter writer = new PdfWriter(reportFilename);
//
//            //Initialize PDF document
//            PdfDocument pdf = new PdfDocument(writer);
//
//            // Initialize document
//            Document document = new Document(pdf);
//            document.setMargins(10, 20, 10, 20);
//
//            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
////            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
//
//            addUSATTLogo(document);
//
//            addTitle(document, font);
//            int fontSize = 10;
//
//            generateBody(document, font, fontSize, tournament);
//
//            generateSignatures(document, font, fontSize, tournament);
//
//            document.close();
//
//
//    private void addUSATTLogo(Document document) {
//        try {
//            PdfDocument pdfDocument = document.getPdfDocument();
//            if (pdfDocument != null) {
//                PdfPage page = pdfDocument.addNewPage();
//                if (page != null) {
//                    Rectangle pageSize = page.getPageSize();
//
//                    float width = pageSize.getWidth();
//                    float height = pageSize.getHeight();
//
//                    Resource resource = resourceLoader.getResource("classpath:images/usatt-logo-horizontal-large.jpg");
//                    byte[] imageBytes = StreamUtils.copyToByteArray(resource.getInputStream());
//
//                    ImageData usattLogoData = ImageDataFactory.create(imageBytes);
//                    float imageWidth = usattLogoData.getWidth() * 0.45f;
//                    float imageHeight = usattLogoData.getHeight() * 0.45f;
//                    float imageX = (width / 2) - (imageWidth / 2);
//                    float imageY = height - document.getTopMargin() - document.getBottomMargin() - 100;
//                    Image usattLogo = new Image(usattLogoData);
//                    usattLogo.scaleToFit(imageWidth, imageHeight);
//                    usattLogo.setFixedPosition(imageX, imageY);
//                    document.add(usattLogo);
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void addTitle(Document document, PdfFont font) {
//        String title = "Tournament Director Declaration of Compliance with USATT Safe Sport Policy";
//        Paragraph paragraph = new Paragraph(title);
//        paragraph.setFont(font);
//        paragraph.setFontSize(14);
//        paragraph.setUnderline();
//        paragraph.setBold();
//        paragraph.setTextAlignment(TextAlignment.CENTER);
//        paragraph.setMarginTop(140);
//        document.add(paragraph);
//    }
//
//    private void generateBody(Document document, PdfFont font, int fontSize, Tournament tournament) {
//        String strParagraph = String.format("I, %s in my capacity as the Tournament Director for the " +
//                "following USATT Sanctioned Tournament: %s state the following:", tournament.getContactName(), tournament.getName());
//
//        Paragraph paragraph = new Paragraph(strParagraph);
//        paragraph.setFont(font);
//        paragraph.setFontSize(fontSize);
//        document.add(paragraph);
//
//        String strParagraph2 = "I am familiar with the USATT Safe Sport Policy, including the Minor Athlete Abuse Prevention Policy [“MAAPP”].";
//        Paragraph paragraph2 = new Paragraph(strParagraph2);
//        paragraph2.setFont(font);
//        paragraph2.setFontSize(fontSize);
//        document.add(paragraph2);
//
//        String strParagraph3 = "I completed the following steps in conjunction with this USATT Sanctioned Tournament:";
//        Paragraph paragraph3 = new Paragraph(strParagraph3);
//        paragraph3.setFont(font);
//        paragraph3.setFontSize(fontSize);
//        document.add(paragraph3);
//
//        // Create numbered list
//        String [] listText = {
//           "Confirmed that all persons serving in Positions of Authority for this tournament were Safe Sport Compliant " +
//                   "at all times relevant to this tournament;",
//
//           "To the best of my ability, undertook all known reasonable steps to communicate the USATT Safe Sport Policy " +
//                   "to Tournament Participants prior to the start of the tournament;",
//
//           "Included the USATT Safe Sport Policy Entry Blank Template in the tournament registration process;",
//
//           "To the best of my ability, confirmed that all Adult Participants in this tournament who have authority over or " +
//              "regular contact with minor athletes, of whom I am aware, were Safe Sport Trained at all times relevant to this tournament;",
//
//           "Confirmed that all persons listed on the USATT Suspended Member List were excluded from participation in " +
//               "this tournament; and",
//
//           "To the best of my ability, communicated and enforced the USATT Coaching Policy."
//        };
//
//        List numberedList = new List(ListNumberingType.DECIMAL);
//        numberedList.setFont(font);
//        numberedList.setFontSize(fontSize);
//        for (String itemText : listText) {
//            ListItem listItem = new ListItem(itemText);
//            listItem.setMarginBottom(8);
//            listItem.setPaddingLeft(20);
//            numberedList.add(listItem);
//        }
//        document.add(numberedList);
//    }
//
//    private void generateSignatures(Document document, PdfFont font, int fontSize, Tournament tournament) {
//
//    }
}
