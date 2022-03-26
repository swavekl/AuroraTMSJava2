package com.auroratms.tournamentprocessing;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.Date;

/**
 * Each time tournament director generates and submits reports this will be created
 If report needs to be updated and resubmitted we create another detail like this. */
@Entity
public class TournamentProcessingRequestDetail {

    // id of this data
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // date of creation
    private Date createdOn;

    // status of this processing request
    private TournamentProcessingRequestStatus status;

    // paths in repository where reports are stored
    private String pathTournamentReport;
    private String pathPlayerList;
    private String pathApplications;
    private String pathMembershipList;
    private String pathMatchResults;

    // id of the payment to pay for this request (i.e. tournament report)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_processing_request_fk", nullable = false)
    @JsonBackReference
    private TournamentProcessingRequest tournamentProcessingRequest;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public TournamentProcessingRequestStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentProcessingRequestStatus status) {
        this.status = status;
    }

    public String getPathTournamentReport() {
        return pathTournamentReport;
    }

    public void setPathTournamentReport(String pathTournamentReport) {
        this.pathTournamentReport = pathTournamentReport;
    }

    public String getPathPlayerList() {
        return pathPlayerList;
    }

    public void setPathPlayerList(String pathPlayerList) {
        this.pathPlayerList = pathPlayerList;
    }

    public String getPathApplications() {
        return pathApplications;
    }

    public void setPathApplications(String pathApplications) {
        this.pathApplications = pathApplications;
    }

    public String getPathMembershipList() {
        return pathMembershipList;
    }

    public void setPathMembershipList(String pathMembershipList) {
        this.pathMembershipList = pathMembershipList;
    }

    public String getPathMatchResults() {
        return pathMatchResults;
    }

    public void setPathMatchResults(String pathMatchResults) {
        this.pathMatchResults = pathMatchResults;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public TournamentProcessingRequest getTournamentProcessingRequest() {
        return tournamentProcessingRequest;
    }

    public void setTournamentProcessingRequest(TournamentProcessingRequest tournamentProcessingRequest) {
        this.tournamentProcessingRequest = tournamentProcessingRequest;
    }
}
