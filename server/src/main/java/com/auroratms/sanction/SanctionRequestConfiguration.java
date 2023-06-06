package com.auroratms.sanction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class SanctionRequestConfiguration {

    private Date alternateStartDate;
    private Date alternateEndDate;

    private String webLinkURL;

    private String venueStreetAddress;
    private String venueCity;
    private String venueState;
    private String venueZipCode;

    private String clubName;
    private Date clubAffiliationExpiration;

    private String contactPersonName;
    private String contactPersonPhone;
    private String contactPersonEmail;
    private String contactPersonStreetAddress;
    private String contactPersonCity;
    private String contactPersonState;
    private String contactPersonZip;

    private String tournamentRefereeName;
    private String tournamentRefereeRank;
    private Date tournamentRefereeMembershipExpires;

    private String tournamentDirectorName;

    private int totalPrizeMoney;
    private int sanctionFee;

    private List<SanctionCategory> categories;

    private String approvalRejectionNotes;
    private String blankEntryFormUrl;

    public String convertToContent(SanctionRequest sanctionRequest) {
        this.setAlternateStartDate(sanctionRequest.getAlternateStartDate());
        this.setAlternateEndDate(sanctionRequest.getAlternateEndDate());
        this.setWebLinkURL(sanctionRequest.getWebLinkURL());
        this.setVenueStreetAddress(sanctionRequest.getVenueStreetAddress());
        this.setVenueCity(sanctionRequest.getVenueCity());
        this.setVenueState(sanctionRequest.getVenueState());
        this.setVenueZipCode(sanctionRequest.getVenueZipCode());
        this.setClubName(sanctionRequest.getClubName());
        this.setClubAffiliationExpiration(sanctionRequest.getClubAffiliationExpiration());
        this.setContactPersonName(sanctionRequest.getContactPersonName());
        this.setContactPersonPhone(sanctionRequest.getContactPersonPhone());
        this.setContactPersonEmail(sanctionRequest.getContactPersonEmail());
        this.setContactPersonStreetAddress(sanctionRequest.getContactPersonStreetAddress());
        this.setContactPersonCity(sanctionRequest.getContactPersonCity());
        this.setContactPersonState(sanctionRequest.getContactPersonState());
        this.setContactPersonZip(sanctionRequest.getContactPersonZip());
        this.setTournamentRefereeName(sanctionRequest.getTournamentRefereeName());
        this.setTournamentRefereeRank(sanctionRequest.getTournamentRefereeRank());
        this.setTournamentRefereeMembershipExpires(sanctionRequest.getTournamentRefereeMembershipExpires());
        this.setTournamentDirectorName(sanctionRequest.getTournamentDirectorName());
        this.setTotalPrizeMoney(sanctionRequest.getTotalPrizeMoney());
        this.setSanctionFee(sanctionRequest.getSanctionFee());
        this.setCategories(sanctionRequest.getCategories());
        this.setApprovalRejectionNotes(sanctionRequest.getApprovalRejectionNotes());
        this.setBlankEntryFormUrl(sanctionRequest.getBlankEntryFormUrl());
        String content = null;
        try {
            StringWriter stringWriter = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.writeValue(stringWriter, this);
            content = stringWriter.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    public static void convertFromContent(String content, SanctionRequest sanctionRequest) {
        if (content != null) {
            try {
                // convert from JSON to configuration
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                SanctionRequestConfiguration configuration = mapper.readValue(content,SanctionRequestConfiguration.class);

                sanctionRequest.setAlternateStartDate(configuration.getAlternateStartDate());
                sanctionRequest.setAlternateEndDate(configuration.getAlternateEndDate());
                sanctionRequest.setWebLinkURL(configuration.getWebLinkURL());
                sanctionRequest.setVenueStreetAddress(configuration.getVenueStreetAddress());
                sanctionRequest.setVenueCity(configuration.getVenueCity());
                sanctionRequest.setVenueState(configuration.getVenueState());
                sanctionRequest.setVenueZipCode(configuration.getVenueZipCode());
                sanctionRequest.setClubName(configuration.getClubName());
                sanctionRequest.setClubAffiliationExpiration(configuration.getClubAffiliationExpiration());
                sanctionRequest.setContactPersonName(configuration.getContactPersonName());
                sanctionRequest.setContactPersonPhone(configuration.getContactPersonPhone());
                sanctionRequest.setContactPersonEmail(configuration.getContactPersonEmail());
                sanctionRequest.setContactPersonStreetAddress(configuration.getContactPersonStreetAddress());
                sanctionRequest.setContactPersonCity(configuration.getContactPersonCity());
                sanctionRequest.setContactPersonState(configuration.getContactPersonState());
                sanctionRequest.setContactPersonZip(configuration.getContactPersonZip());
                sanctionRequest.setTournamentRefereeName(configuration.getTournamentRefereeName());
                sanctionRequest.setTournamentRefereeRank(configuration.getTournamentRefereeRank());
                sanctionRequest.setTournamentRefereeMembershipExpires(configuration.getTournamentRefereeMembershipExpires());
                sanctionRequest.setTournamentDirectorName(configuration.getTournamentDirectorName());
                sanctionRequest.setTotalPrizeMoney(configuration.getTotalPrizeMoney());
                sanctionRequest.setSanctionFee(configuration.getSanctionFee());
                sanctionRequest.setCategories(configuration.getCategories());
                sanctionRequest.setApprovalRejectionNotes(configuration.getApprovalRejectionNotes());
                sanctionRequest.setBlankEntryFormUrl(configuration.getBlankEntryFormUrl());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
