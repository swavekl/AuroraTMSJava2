package com.auroratms.sanction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "sanction_request")
public class SanctionRequestEntity implements Serializable {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String tournamentName;
    Date startDate;
    Date endDate;
    Date requestDate;
    SanctionRequestStatus status;
    int starLevel;

    // tournament region e.g. National for 4 star and up or region e.g. Midwest where tournament place for 3 star and lower
    String coordinatorRegion;

    // regional or national coordinator
    String coordinatorFirstName;
    String coordinatorLastName;

    // email retrieved from frontend table
    String coordinatorEmail;

    // contents of the request in JSON format
    // this should enable us to modify this request in the future without having to modify the database schema
    @Column(length = 9000)
    String requestContentsJSON;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public SanctionRequestStatus getStatus() {
        return status;
    }

    public void setStatus(SanctionRequestStatus status) {
        this.status = status;
    }

    public int getStarLevel() {
        return starLevel;
    }

    public void setStarLevel(int starLevel) {
        this.starLevel = starLevel;
    }

    public String getCoordinatorRegion() {
        return coordinatorRegion;
    }

    public void setCoordinatorRegion(String coordinatorRegion) {
        this.coordinatorRegion = coordinatorRegion;
    }

    public String getCoordinatorFirstName() {
        return coordinatorFirstName;
    }

    public void setCoordinatorFirstName(String coordinatorFirstName) {
        this.coordinatorFirstName = coordinatorFirstName;
    }

    public String getCoordinatorLastName() {
        return coordinatorLastName;
    }

    public void setCoordinatorLastName(String coordinatorLastName) {
        this.coordinatorLastName = coordinatorLastName;
    }

    public String getCoordinatorEmail() {
        return coordinatorEmail;
    }

    public void setCoordinatorEmail(String coordinatorEmail) {
        this.coordinatorEmail = coordinatorEmail;
    }

    public String getRequestContentsJSON() {
        return requestContentsJSON;
    }

    public void setRequestContentsJSON(String requestContentsJSON) {
        this.requestContentsJSON = requestContentsJSON;
    }
}
