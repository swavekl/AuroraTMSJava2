package com.auroratms.clubaffiliationapp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Club affiliation application.  This class objects are sent to frontend.  They are not persisted
 * The entity ClubAffiliationApplicationEntity is persisted
 */
@Data
@NoArgsConstructor
public class ClubAffiliationApplication {
    // unique id
    private Long id;

    // club name e.g. Fox Valley Table Tennis club
    private String name;
    // e.g Eola Community Center
    private String buildingName;

    // playing site address
    private String streetAddress;
    private String city;
    private String state;
    // 5 or Zip+4 code
    private String zipCode;

    private ClubAffiliationApplicationStatus status;

    // date when USATT affiliation expires
    private Date affiliationExpirationDate;

    // payment id if the application fee of $75 was paid
    private Long paymentId;

    // Wednesday & Friday - 6:30 - 9:30PM
    private String hoursAndDates;

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

    public ClubAffiliationApplicationEntity convertToEntity() {
        ClubAffiliationApplicationEntity entity = new ClubAffiliationApplicationEntity();
        entity.setId(this.getId());
        entity.setName(this.getName());
        entity.setBuildingName(this.getBuildingName());
        entity.setStreetAddress(this.getStreetAddress());
        entity.setCity(this.getCity());
        entity.setState(this.getState());
        entity.setZipCode(this.getZipCode());
        entity.setStatus(this.getStatus());
        entity.setAffiliationExpirationDate(this.getAffiliationExpirationDate());
        entity.setPaymentId(this.getPaymentId());
        entity.setHoursAndDates(this.getHoursAndDates());

        ClubAffiliationApplicationConfiguration configuration = new ClubAffiliationApplicationConfiguration();
        String content = configuration.convertToContent(this);
        // content must be less than 6000 chars
//        System.out.println("content.length() = " + content.length());
        entity.setContent(content);

        return entity;
    }

    public ClubAffiliationApplication convertFromEntity(ClubAffiliationApplicationEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.buildingName = entity.getBuildingName();
        this.streetAddress = entity.getStreetAddress();
        this.city = entity.getCity();
        this.state = entity.getState();
        this.zipCode = entity.getZipCode();
        this.status = entity.getStatus();
        this.status = (this.status != null) ? this.status : ClubAffiliationApplicationStatus.New;
        this.affiliationExpirationDate = entity.getAffiliationExpirationDate();
        this.paymentId = entity.getPaymentId();
        this.hoursAndDates = entity.getHoursAndDates();
        String content = entity.getContent();
        ClubAffiliationApplicationConfiguration.convertFromContent(content, this);
        return this;
    }

}
