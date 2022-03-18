package com.auroratms.tournamentprocessing;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.List;

/**
 * Tournament Processing request for submitting results and other reports
 * for processing to USATT
 */
@Entity
public class TournamentProcessingRequest {
    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // if of the tournament
    long tournamentId;

    // name of tournament - to avoid having to go into db to get the names
    String tournamentName;

    // status of this processing request
    TournamentProcessingRequestStatus status;

    // list of details with each report
    @OneToMany(mappedBy = "tournamentProcessingRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TournamentProcessingRequestDetail> details;

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

    public TournamentProcessingRequestStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentProcessingRequestStatus status) {
        this.status = status;
    }

    public List<TournamentProcessingRequestDetail> getDetails() {
        return details;
    }

    public void setDetails(List<TournamentProcessingRequestDetail> details) {
        this.details = details;
    }
}
