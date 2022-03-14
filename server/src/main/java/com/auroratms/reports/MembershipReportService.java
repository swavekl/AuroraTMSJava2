package com.auroratms.reports;

import com.auroratms.club.ClubEntity;
import com.auroratms.club.ClubService;
import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournament.Tournament;
import com.auroratms.tournament.TournamentService;
import com.auroratms.tournamententry.MembershipType;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
import com.auroratms.usatt.UsattDataService;
import com.auroratms.usatt.UsattPlayerRecord;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Report listing who bought which membership level.  The report has the following format
 *
 * Membership#,LastName,FirstName,DOB,Email,Gender,Address#1,City,State,Zip,Phone#,Citizenship,RepresentingCountry,ProductID,YearCount
 * 224026,Albulescu,Julian,11/08/2003,julianalbulescu1@gmail.com,M,2730 Ridge Road,Highland Park,IL,60035,847-602-0191,,,56,1,
 */
@Service
@Slf4j
public class MembershipReportService {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private UsattDataService usattDataService;

    @Autowired
    private TournamentEntryService tournamentEntryService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private ClubService clubService;

    // 11/08/2003 - mm/dd/yyyy
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    // list of clubs that players are from
    private List<ClubEntity> allMemberClubs;

    public String generateMembershipReport(long tournamentId) {
        String reportFilename = null;
        FileWriter fileWriter = null;
        try {
            // prepare data for report
            List<ReportData> reportDataList = prepareReportData(tournamentId);

            // craete report file path
            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            File reportFile = new File(tempDir + File.separator + "usatt-players-" + tournamentId + ".csv");
            reportFilename = reportFile.getCanonicalPath();
            log.info("Writing membership report for tournament " + tournamentId);
            log.info("to " + reportFilename);

            // write header
            fileWriter = new FileWriter(reportFile);
            fileWriter.write("Membership#,LastName,FirstName,DOB,Email,Gender,Address#1,City,State,Zip,Phone#,Citizenship,RepresentingCountry,ProductID,YearCount\n");
            // write report body
            for (ReportData reportData : reportDataList) {
                String reportLine = makeReportLine(reportData);
                if (!reportLine.isEmpty()) {
                    fileWriter.write(reportLine);
                }
            }
            log.info("Finished player membership report for " + reportDataList.size() + " players");

        } catch (IOException e) {
            log.error("Unable to create membership report ", e);
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
     * Generate applications
     * @param tournamentId
     * @return
     */
    public String generateMembershipApplications(long tournamentId) {
        String reportFilename = null;
        try {
            // prepare data for report
            List<ReportData> reportDataList = prepareReportData(tournamentId);

            // get tournament information
            Tournament tournament = tournamentService.getByKey(tournamentId);
            String contactName = tournament.getContactName();
            String strTournamentDate = dateFormat.format(tournament.getStartDate());

            // create report file path
            String tempDir = System.getenv("TEMP");
            tempDir = (StringUtils.isEmpty(tempDir)) ? System.getenv("TMP") : tempDir;
            File reportFile = new File(tempDir + File.separator + "usatt-applications-" + tournamentId + ".pdf");
            reportFilename = reportFile.getCanonicalPath();
            log.info("Writing USATT applications for tournament " + tournamentId);
            log.info("to " + reportFilename);

            //Initialize PDF writer
            PdfWriter writer = new PdfWriter(reportFilename);

            //Initialize PDF document
            PdfDocument pdf = new PdfDocument(writer);

            // Initialize document
            Document document = new Document(pdf);
            document.setMargins(10, 20, 10, 20);

            PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);

            File usattLogoFile = ResourceUtils.getFile("classpath:images/usatt-logo-horizontal.jpg");

            PdfDocument pdfDocument = document.getPdfDocument();

            int count = 0;
            int pageNumber = 1;

            PdfPage page = pdfDocument.addNewPage(pageNumber);
            Rectangle pageSize = page.getPageSize();
            PdfCanvas canvas = new PdfCanvas(page);
            for (ReportData reportData : reportDataList) {
                int appOnPage = (count % 3) + 1;
                renderMemberInformation (reportData, document, font, contactName, strTournamentDate);
                renderFrame(canvas, document, pageSize, appOnPage, usattLogoFile);

                count++;
                if (count % 3 == 0) {
                    pageNumber++;
                    // page break
                    page = pdfDocument.addNewPage(pageNumber);
                    canvas = new PdfCanvas(page);
                }
            }

            document.close();
            log.info("Finished USATT applications for " + reportDataList.size() + " players");

        } catch (IOException e) {
            log.error("Unable to USATT applications ", e);
        } finally {
        }
        return reportFilename;
    }

    /**
     * @param reportData
     * @param document
     * @param font
     * @param contactName
     * @param strTournamentDate
     */
    private void renderMemberInformation(ReportData reportData, Document document, PdfFont font, String contactName, String strTournamentDate) {
        int fontSize = 14;
        Paragraph headerParagraph = new Paragraph(
                "Membership Application                                     USATT Copy")
                .setFont(font).setFontSize(fontSize).setBold()
                .setTextAlignment(TextAlignment.RIGHT).setPaddingRight(10)
                .setMarginTop(15).setMarginBottom(20);
        document.add(headerParagraph);

        // ( ) New (X) Renewal Membership Number (if renewal) 224026
        Date membershipExpirationDate = reportData.usattPlayerRecord.getMembershipExpirationDate();
        boolean isNew = UsattDataService.isNewMembership(membershipExpirationDate);
        String strNew = (isNew) ? "X" : " ";
        String strRenewal = (!isNew) ? "X" : " ";
        String strMembershipId = isNew ? "" : Long.toString(reportData.userProfileExt.getMembershipId());
        String line1 = String.format("New (%s)\tRenewal (%s)\t\t\t\t\t\t\t\t\t\tMembership Number (if renewal) %s",
                strNew, strRenewal, strMembershipId);

        // Date of Birth: Sex: (X) Male ( ) Female
        String strDateOfBirth = (reportData.userProfile.getDateOfBirth() != null) ? dateFormat.format(reportData.userProfile.getDateOfBirth()) : "          ";
        String strMale = (reportData.userProfile.getGender().equals("Male")) ? "X" : " ";
        String strFemale = (reportData.userProfile.getGender().equals("Female")) ? "X" : " ";
        String line2 = String.format("Date of Birth:\t\t%s\t\t\t\t\t\t\t\t\t\t\t\t\tSex: (%s) Male\t(%s) Female", strDateOfBirth, strMale, strFemale);

        String line3 = String.format("Name:\t\t\t\t\t%s %s", reportData.userProfile.getFirstName(), reportData.userProfile.getLastName());
        String line4 = String.format("Address:\t\t\t\t%s", reportData.userProfile.getStreetAddress());
        String paddedCity = StringUtils.rightPad(reportData.userProfile.getCity(), 60);
        String line5 = String.format("City:\t\t\t\t\t%sState: %s\tZip: %s", paddedCity, reportData.userProfile.getState(), reportData.userProfile.getZipCode());

        String strExpirationDate = (membershipExpirationDate != null && !isNew) ? dateFormat.format(membershipExpirationDate) : "          ";
        String paddedPhone = StringUtils.rightPad(reportData.userProfile.getMobilePhone(), 60);
        String line6 = String.format("Home phone:\t\t%sExpired: %s", paddedPhone, strExpirationDate);

        String line7 = String.format("E-mail:\t\t\t\t%s", reportData.userProfile.getEmail());

        String clubName = getClubName(reportData.userProfileExt.getClubFk());
        String line8 = String.format("Affiliated Club:\t%s", clubName);

        // Membership: Junior Tournament Pass
        String strMembershipType = getMembershipName (reportData.membershipType);
        String line9 = String.format("Membership:\t\t%s", strMembershipType);

        // Sold By:
        String paddedContactName = StringUtils.rightPad(contactName, 50);
        String line10 = String.format("Sold By:\t\t\t\t%s\t\t\t\t\tDate: %s", paddedContactName, strTournamentDate);

        String [] lines = { line1, line2, line3, line4, line5, line6, line7, line8, line9, line10};
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Paragraph paragraph = new Paragraph(line)
                    .setFont(font).setFontSize(fontSize)
                    .setPaddingLeft(10).setMarginBottom(0).setMarginTop(0);
            if (i == lines.length - 1) {
                paragraph.setMarginBottom(15);
            }
            document.add(paragraph);
        }
    }

    /**
     *
     * @param clubFk
     * @return
     */
    private String getClubName(Long clubFk) {
        String clubName = "";
        if (clubFk != null) {
            for (ClubEntity clubEntity : allMemberClubs) {
                if (clubEntity.getId() == clubFk) {
                    clubName = clubEntity.getClubName();
                    break;
                }
            }
        }
        return clubName;
    }

    /**
     *
     * @param canvas
     * @param document
     * @param pageSize
     * @param appOnPage
     * @param usattLogoFile
     * @throws FileNotFoundException
     * @throws MalformedURLException
     */
    private void renderFrame(PdfCanvas canvas, Document document, Rectangle pageSize, int appOnPage, File usattLogoFile) throws FileNotFoundException, MalformedURLException {
        // add border
        int spaceBetweenApps = 20;
        float width = pageSize.getWidth();
        float height = pageSize.getHeight();
        float frameWidth = width - document.getRightMargin() - document.getLeftMargin();
        float totalAvailableHeight = height - document.getTopMargin() - document.getBottomMargin();
        float frameHeight = totalAvailableHeight / 3;
        float frameLLX = document.getLeftMargin();
        float frameLLY = (document.getBottomMargin()) + ((3 - appOnPage) * frameHeight) + (spaceBetweenApps / 2);
        Rectangle frameRectangle = new Rectangle(frameLLX, frameLLY, frameWidth, frameHeight - spaceBetweenApps);
        canvas.rectangle(frameRectangle);
        canvas.stroke();

        ImageData usattLogoData = ImageDataFactory.create(usattLogoFile.getAbsolutePath());
        float imageWidth = usattLogoData.getWidth() / 2;
        float imageHeight = usattLogoData.getHeight() / 2;
        float imageX = frameRectangle.getLeft() + 5;
        float imageY = frameRectangle.getTop() - imageHeight - 5;

        Image usattLogoImage = new Image(usattLogoData);
        usattLogoImage.scaleToFit(imageWidth, imageHeight);
        usattLogoImage.setFixedPosition(imageX, imageY);
        document.add(usattLogoImage);
    }

    /**
     *
     * @param tournamentId
     * @return
     */
    private List<ReportData> prepareReportData(long tournamentId) {
        // get all tournament entries for this tournament and collect player profiles ids
        // of players who bought some type of membership
        List<TournamentEntry> tournamentEntries = tournamentEntryService.listForTournament(tournamentId);
        Set<String> uniqueProfileIdsSet = new HashSet<>();
        Map<String, MembershipType> profileIdToPurchasedMembershipTypesMap = new HashMap<>();
        for (TournamentEntry tournamentEntry : tournamentEntries) {
            MembershipType membershipOption = tournamentEntry.getMembershipOption();
            if (membershipOption != MembershipType.NO_MEMBERSHIP_REQUIRED) {
                uniqueProfileIdsSet.add(tournamentEntry.getProfileId());
                profileIdToPurchasedMembershipTypesMap.put(tournamentEntry.getProfileId(), membershipOption);
            }
        }

        // get profile information for those who bought it
        List<String> profileIds = new ArrayList<>(uniqueProfileIdsSet);
        Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIds);
        List<UserProfile> userProfileList = new ArrayList<>(userProfiles);
        Comparator<UserProfile> comparator = Comparator.comparing(UserProfile::getLastName)
                .thenComparing(UserProfile::getFirstName);
        Collections.sort(userProfileList, comparator);

        // get profile id to membership id map
        Map<String, UserProfileExt> profileIdToUserExtProfileMap = userProfileExtService.findByProfileIds(profileIds);

        List<Long> membershipIds = new ArrayList<>(userProfileList.size());
        for (UserProfileExt userProfileExt : profileIdToUserExtProfileMap.values()) {
            membershipIds.add(userProfileExt.getMembershipId());
        }

        // get expiration dates
        List<UsattPlayerRecord> usattPlayerRecordList = usattDataService.findAllByMembershipIdIn(membershipIds);

        List<ReportData> reportDataList = new ArrayList<>(userProfileList.size());

        Set<Long> uniqueClubIds = new HashSet<>();
        for (UserProfile userProfile : userProfileList) {
            String playerProfileId = userProfile.getUserId();
            ReportData reportData = new ReportData();
            reportData.userProfile = userProfile;
            reportData.membershipType = profileIdToPurchasedMembershipTypesMap.get(playerProfileId);
            reportData.userProfileExt = profileIdToUserExtProfileMap.get(playerProfileId);
            Long membershipId = reportData.userProfileExt.getMembershipId();
            for (UsattPlayerRecord usattPlayerRecord : usattPlayerRecordList) {
                if (membershipId.equals(usattPlayerRecord.getMembershipId())) {
                    reportData.usattPlayerRecord = usattPlayerRecord;
                    break;
                }
            }

            reportDataList.add(reportData);

            if (reportData.userProfileExt.getClubFk() != null) {
                uniqueClubIds.add(reportData.userProfileExt.getClubFk());
            }
        }

        List<Long> clubIds = new ArrayList<>(uniqueClubIds);
        allMemberClubs = clubService.findAllByIdIn(clubIds);
        return reportDataList;
    }

    private class ReportData {
        MembershipType membershipType;
        UserProfile userProfile;
        UserProfileExt userProfileExt;
        UsattPlayerRecord usattPlayerRecord;
    }

    /**
     * Creates report line
     * Membership#,LastName,FirstName,DOB,Email,Gender,Address#1,City,State,Zip,Phone#,Citizenship,RepresentingCountry,ProductID,YearCount
     * 224026,Albulescu,Julian,11/08/2003,julianalbulescu1@gmail.com,M,2730 Ridge Road,Highland Park,IL,60035,847-602-0191,,,56,1,
     *
     * @param reportData
     * @return
     */
    private String makeReportLine(ReportData reportData) {
        MembershipType membershipType = reportData.membershipType;
        UserProfile userProfile = reportData.userProfile;
        UserProfileExt userProfileExt = reportData.userProfileExt;

        Long membershipId = userProfileExt.getMembershipId();

        String formattedDOB = dateFormat.format(userProfile.getDateOfBirth());
        String gender = (userProfile.getGender().equals("Male")) ? "M" : "F";
        String countryCode = userProfile.getCountryCode();
        String citizenship = countryCode.equals("US") ? "" : "FN";
        String representingCountry = countryCode.equals("US") ? "" : countryCode;
        int productId = getProductId(membershipType);
        int yearCount = getYearCount(membershipType);

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%d\n", membershipId,
                userProfile.getLastName(), userProfile.getFirstName(), formattedDOB, userProfile.getEmail(), gender,
                userProfile.getStreetAddress(), userProfile.getCity(), userProfile.getState(), userProfile.getZipCode(), userProfile.getMobilePhone(),
                citizenship, representingCountry, productId, yearCount);
    }

    /**
     * Gets USATT product id
     * @param membershipType
     * @return
     */
    private int getProductId(MembershipType membershipType) {
        int productId = 0;
        switch(membershipType) {
            case TOURNAMENT_PASS_ADULT:
                productId = 61;
                break;
            case TOURNAMENT_PASS_JUNIOR:
                productId = 72;
                break;
            case BASIC_PLAN:
                productId = 70;
                break;
            case PRO_PLAN:
                productId = 53;
                break;
            case LIFETIME:
                productId = 59;
                break;
        }
        return productId;
    }

    /**
     * Gets USATT product id
     * @param membershipType
     * @return
     */
    private int getYearCount(MembershipType membershipType) {
        int yearCount = 0;
        switch(membershipType) {
            case TOURNAMENT_PASS_ADULT:
            case TOURNAMENT_PASS_JUNIOR:
                yearCount = 0;
                break;
            case BASIC_PLAN:
            case PRO_PLAN:
                yearCount = 1;
                break;
            case LIFETIME:
                yearCount = 100;
                break;
        }
        return yearCount;
    }

    private String getMembershipName(MembershipType membershipType) {
        String strMembershipType = "";
        switch(membershipType) {
            case TOURNAMENT_PASS_ADULT:
                strMembershipType = "Adult Tournament Pass";
                break;
            case TOURNAMENT_PASS_JUNIOR:
                strMembershipType = "Junior Tournament Pass";
                break;
            case BASIC_PLAN:
                strMembershipType = "Basic Membership";
                break;
            case PRO_PLAN:
                strMembershipType = "Pro Membership";
                break;
            case LIFETIME:
                strMembershipType = "Lifetime Membership";
                break;
        }
        return strMembershipType;
    }

}
