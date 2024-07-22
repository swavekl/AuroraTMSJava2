package com.auroratms.tournamentprocessing;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Tournament Processing request for submitting results and other reports
 * for processing to USATT
 */
@Entity
public class TournamentProcessingRequest implements Serializable {
    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // if of the tournament
    long tournamentId;

    // name of tournament - to avoid having to go into db to get the names
    String tournamentName;

    // status of this processing request - derived from the status of last detail
    private TournamentProcessingRequestStatus requestStatus = TournamentProcessingRequestStatus.New;

    // list of details with each report
    @OneToMany(mappedBy = "tournamentProcessingRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TournamentProcessingRequestDetail> details;

    // remarks to be placed on the tournament report
    @Transient
    private String remarks;

    // last 4 digits of a credit card to be placed on tournament report
    @Transient
    private String ccLast4Digits;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public List<TournamentProcessingRequestDetail> getDetails() {
        return details;
    }

    public void setDetails(List<TournamentProcessingRequestDetail> details) {
        this.details = details;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCcLast4Digits() {
        return ccLast4Digits;
    }

    public void setCcLast4Digits(String ccLast4Digits) {
        this.ccLast4Digits = ccLast4Digits;
    }

    public TournamentProcessingRequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(TournamentProcessingRequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }
}
