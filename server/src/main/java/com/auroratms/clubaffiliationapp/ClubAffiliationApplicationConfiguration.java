package com.auroratms.clubaffiliationapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * This is the data that we don't query on and if there is a need to change it
 * it will not affect the database schema
 */
@Data
@NoArgsConstructor
public class ClubAffiliationApplicationConfiguration {

    // mailing site address
    private String mailingCorrespondentsName;
    private String mailingStreetAddress;
    private String mailingCity;
    private String mailingState;
    // 5 or Zip+4 code
    private String mailingZipCode;

    private String clubAdminName;
    private String clubAdminEmail;

    private String clubPhoneNumber;
    private String clubPhoneNumber2;

    private String clubWebsite;

    // Club officers (president, vice-president, secretary, treasurer)
    private String presidentName;
    private String presidentEmail;
    private String presidentPhoneNumber;

    private String vicePresidentName;
    private String vicePresidentEmail;
    private String vicePresidentPhoneNumber;

    private String secretaryName;
    private String secretaryEmail;
    private String secretaryPhoneNumber;

    private String treasurerName;
    private String treasurerEmail;
    private String treasurerPhoneNumber;

    boolean hasMembershipStructure;
    private String membershipStructure;

    private int membersCount;
    private int tablesCount;

    private String programs;
    private boolean hasBankAccount;

    private List<PlayingSite> alternatePlayingSites;

    /**
     *
     * @param application
     * @return
     */
    public String convertToContent(ClubAffiliationApplication application) {
        this.setMailingCorrespondentsName(application.getMailingCorrespondentsName());
        this.setMailingStreetAddress(application.getMailingStreetAddress());
        this.setMailingCity(application.getMailingCity());
        this.setMailingState(application.getMailingState());
        this.setMailingZipCode(application.getMailingZipCode());
        this.setClubAdminName(application.getClubAdminName());
        this.setClubAdminEmail(application.getClubAdminEmail());
        this.setClubPhoneNumber(application.getClubPhoneNumber());
        this.setClubPhoneNumber2(application.getClubPhoneNumber2());
        this.setClubWebsite(application.getClubWebsite());

        this.setPresidentName(application.getPresidentName());
        this.setPresidentEmail(application.getPresidentEmail());
        this.setPresidentPhoneNumber(application.getPresidentPhoneNumber());

        this.setVicePresidentName(application.getVicePresidentName());
        this.setVicePresidentEmail(application.getVicePresidentEmail());
        this.setVicePresidentPhoneNumber(application.getVicePresidentPhoneNumber());

        this.setSecretaryName(application.getSecretaryName());
        this.setSecretaryEmail(application.getSecretaryEmail());
        this.setSecretaryPhoneNumber(application.getSecretaryPhoneNumber());

        this.setTreasurerName(application.getTreasurerName());
        this.setTreasurerEmail(application.getTreasurerEmail());
        this.setTreasurerPhoneNumber(application.getTreasurerPhoneNumber());

        this.setHasMembershipStructure(application.isHasMembershipStructure());
        this.setMembershipStructure(application.getMembershipStructure());

        this.setMembersCount(application.getMembersCount());
        this.setTablesCount(application.getTablesCount());

        this.setPrograms(application.getPrograms());
        this.setHasBankAccount(application.isHasBankAccount());

        this.setAlternatePlayingSites(application.getAlternatePlayingSites());
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

    /**
     *
     * @param content
     * @param application
     */
    public static void convertFromContent (String content, ClubAffiliationApplication application) {
        if (content != null) {
            try {
                // convert from JSON to configuration
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                ClubAffiliationApplicationConfiguration configuration = mapper.readValue(content, ClubAffiliationApplicationConfiguration.class);

                // fill the application fields
                application.setMailingCorrespondentsName(configuration.getMailingCorrespondentsName());
                application.setMailingStreetAddress(configuration.getMailingStreetAddress());
                application.setMailingCity(configuration.getMailingCity());
                application.setMailingState(configuration.getMailingState());
                application.setMailingZipCode(configuration.getMailingZipCode());
                application.setClubAdminName(configuration.getClubAdminName());
                application.setClubAdminEmail(configuration.getClubAdminEmail());
                application.setClubPhoneNumber(configuration.getClubPhoneNumber());
                application.setClubPhoneNumber2(configuration.getClubPhoneNumber2());
                application.setClubWebsite(configuration.getClubWebsite());

                application.setPresidentName(configuration.getPresidentName());
                application.setPresidentEmail(configuration.getPresidentEmail());
                application.setPresidentPhoneNumber(configuration.getPresidentPhoneNumber());

                application.setVicePresidentName(configuration.getVicePresidentName());
                application.setVicePresidentEmail(configuration.getVicePresidentEmail());
                application.setVicePresidentPhoneNumber(configuration.getVicePresidentPhoneNumber());

                application.setSecretaryName(configuration.getSecretaryName());
                application.setSecretaryEmail(configuration.getSecretaryEmail());
                application.setSecretaryPhoneNumber(configuration.getSecretaryPhoneNumber());

                application.setTreasurerName(configuration.getTreasurerName());
                application.setTreasurerEmail(configuration.getTreasurerEmail());
                application.setTreasurerPhoneNumber(configuration.getTreasurerPhoneNumber());

                application.setHasMembershipStructure(configuration.isHasMembershipStructure());
                application.setMembershipStructure(configuration.getMembershipStructure());

                application.setMembersCount(configuration.getMembersCount());
                application.setTablesCount(configuration.getTablesCount());

                application.setPrograms(configuration.getPrograms());
                application.setHasBankAccount(configuration.isHasBankAccount());

                application.setAlternatePlayingSites(configuration.getAlternatePlayingSites());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
