package com.auroratms.sanction;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class SanctionRequest {

    // unique id
    private Long id;

    String tournamentName;
    Date startDate;
    Date endDate;
    Date requestDate;
    SanctionRequestStatus status;
    int starLevel;

    // regional or national coordinator
    String coordinatorFirstName;
    String coordinatorLastName;

    // email retrieved from frontend table
    String coordinatorEmail;

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


    public SanctionRequestEntity convertToEntity() {
        SanctionRequestEntity sanctionRequestEntity = new SanctionRequestEntity();
        sanctionRequestEntity.setId(this.getId());
        sanctionRequestEntity.setTournamentName(this.getTournamentName());
        sanctionRequestEntity.setStartDate(this.getStartDate());
        sanctionRequestEntity.setEndDate(this.getEndDate());
        sanctionRequestEntity.setRequestDate(this.getRequestDate());
        sanctionRequestEntity.setStatus(this.getStatus());
        sanctionRequestEntity.setStarLevel(this.getStarLevel());
        sanctionRequestEntity.setCoordinatorFirstName(this.getCoordinatorFirstName());
        sanctionRequestEntity.setCoordinatorLastName(this.getCoordinatorLastName());
        sanctionRequestEntity.setCoordinatorEmail(this.getCoordinatorEmail());
        SanctionRequestConfiguration configuration = new SanctionRequestConfiguration();

        String content = configuration.convertToContent(this);
        sanctionRequestEntity.setRequestContentsJSON(content);

        return sanctionRequestEntity;
    }

    public SanctionRequest convertFromEntity(SanctionRequestEntity sanctionRequestEntity) {
        this.id = sanctionRequestEntity.getId();
        this.tournamentName = sanctionRequestEntity.getTournamentName();
        this.startDate = sanctionRequestEntity.getStartDate();
        this.endDate = sanctionRequestEntity.getEndDate();
        this.requestDate = sanctionRequestEntity.getRequestDate();
        this.status = sanctionRequestEntity.getStatus();
        this.starLevel = sanctionRequestEntity.getStarLevel();
        this.coordinatorFirstName = sanctionRequestEntity.getCoordinatorFirstName();
        this.coordinatorLastName = sanctionRequestEntity.getCoordinatorLastName();
        this.coordinatorEmail = sanctionRequestEntity.getCoordinatorEmail();

        String content = sanctionRequestEntity.getRequestContentsJSON();
        SanctionRequestConfiguration.convertFromContent(content, this);

        return this;
    }


}
