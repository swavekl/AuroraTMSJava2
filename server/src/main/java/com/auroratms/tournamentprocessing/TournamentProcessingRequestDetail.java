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

    // profile id of the person who created this request
    @Column(length = 50)
    private String createdByProfileId;

    // status of this processing request
    private TournamentProcessingRequestStatus status;

    // paths in repository where reports are stored
    private String pathTournamentReport;
    private String pathPlayerList;
    private String pathApplications;
    private String pathMembershipList;
    private String pathMatchResults;

    // flags to indicate which reports to generate
    private boolean generateTournamentReport;
    private boolean generateApplications;
    private boolean generatePlayerList;
    private boolean generateMatchResults;
    private boolean generateMembershipList;

    // id of the payment to pay for this request (i.e. tournament report)
    private Long paymentId;

    // amount to be paid if any
    private int amountToPay = 0;

    // date of payment
    private Date paidOn;

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

    public String getCreatedByProfileId() {
        return createdByProfileId;
    }

    public void setCreatedByProfileId(String createdByProfileId) {
        this.createdByProfileId = createdByProfileId;
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

    public int getAmountToPay() {
        return amountToPay;
    }

    public void setAmountToPay(int amountToPay) {
        this.amountToPay = amountToPay;
    }

    public Date getPaidOn() {
        return paidOn;
    }

    public void setPaidOn(Date paidOn) {
        this.paidOn = paidOn;
    }

    public boolean isGenerateTournamentReport() {
        return generateTournamentReport;
    }

    public void setGenerateTournamentReport(boolean generateTournamentReport) {
        this.generateTournamentReport = generateTournamentReport;
    }

    public boolean isGenerateApplications() {
        return generateApplications;
    }

    public void setGenerateApplications(boolean generateApplications) {
        this.generateApplications = generateApplications;
    }

    public boolean isGeneratePlayerList() {
        return generatePlayerList;
    }

    public void setGeneratePlayerList(boolean generatePlayerList) {
        this.generatePlayerList = generatePlayerList;
    }

    public boolean isGenerateMatchResults() {
        return generateMatchResults;
    }

    public void setGenerateMatchResults(boolean generateMatchResults) {
        this.generateMatchResults = generateMatchResults;
    }

    public boolean isGenerateMembershipList() {
        return generateMembershipList;
    }

    public void setGenerateMembershipList(boolean generateMembershipList) {
        this.generateMembershipList = generateMembershipList;
    }
}
