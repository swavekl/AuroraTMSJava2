package com.auroratms.email.campaign;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "email_campaign")
public class EmailCampaignEntity implements Serializable {

    // unique id of a campaign
    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // name of campaign e.g. 2024 Aurora Summer Open announcement
    private String name;

    // subject of the email
    private String subject;

    // text of the email body
    @Column(length = 10000)
    private String body;

    // date sent
    private Date dateSent;

    // name of the tournament for which it was sent out
    private String tournamentName;

    // number of emails sent
    private int emailsCount;

    // recipient filters and removed recipients
    @Column(length = 9000)
    String filterContentsJSON;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public int getEmailsCount() {
        return emailsCount;
    }

    public void setEmailsCount(int emailsCount) {
        this.emailsCount = emailsCount;
    }

    public String getFilterContentsJSON() {
        return filterContentsJSON;
    }

    public void setFilterContentsJSON(String filterContentsJSON) {
        this.filterContentsJSON = filterContentsJSON;
    }
}

