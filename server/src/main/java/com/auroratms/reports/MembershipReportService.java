package com.auroratms.reports;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.tournamententry.MembershipType;
import com.auroratms.tournamententry.TournamentEntry;
import com.auroratms.tournamententry.TournamentEntryService;
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
import com.itextpdf.layout.element.AreaBreak;
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
    private TournamentEntryService tournamentEntryService;

    // 11/08/2003 - mm/dd/yyyy
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

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
                renderApplication (reportData, canvas, document, pageSize, appOnPage, usattLogoFile);

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

    private void renderApplication(ReportData reportData, PdfCanvas canvas, Document document, Rectangle pageSize, int appOnPage, File usattLogoFile) throws FileNotFoundException, MalformedURLException {
        // add border
        int spaceBetweenApps = 20;
        float width = pageSize.getWidth();
        float height = pageSize.getHeight();
        float frameWidth = width - document.getRightMargin() - document.getLeftMargin();
        float totalAvailableHeight = height - document.getTopMargin() - document.getBottomMargin();
        float frameHeight = totalAvailableHeight / 3;
        float frameLLX = document.getLeftMargin();
        float frameLLY = (document.getBottomMargin()) + ((3 - appOnPage) * frameHeight) + (spaceBetweenApps / 2);

        canvas.rectangle(frameLLX, frameLLY, frameWidth, frameHeight - spaceBetweenApps);
        canvas.stroke();

        float imageX = document.getLeftMargin() + 10;
        float imageY = height - document.getTopMargin() - document.getBottomMargin() - 20;
        imageY = imageY + ((appOnPage - 1) * frameHeight);
        System.out.println("appOnPage = " + appOnPage);
        System.out.println("imageY = " + imageY);

        ImageData usattLogoData = ImageDataFactory.create(usattLogoFile.getAbsolutePath());
//        Image usattLogo = new Image(usattLogoData);
//        usattLogo.scaleToFit(73f, 32f);

//        canvas.addImageAt(usattLogoData, imageX, imageY, false);
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

        List<ReportData> reportDataList = new ArrayList<>(userProfileList.size());
        for (UserProfile userProfile : userProfileList) {
            String playerProfileId = userProfile.getUserId();
            ReportData reportData = new ReportData();
            reportData.userProfile = userProfile;
            reportData.membershipType = profileIdToPurchasedMembershipTypesMap.get(playerProfileId);
            reportData.userProfileExt = profileIdToUserExtProfileMap.get(playerProfileId);
            reportDataList.add(reportData);
        }
        return reportDataList;
    }

    private class ReportData {
        MembershipType membershipType;
        UserProfile userProfile;
        UserProfileExt userProfileExt;
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
}
