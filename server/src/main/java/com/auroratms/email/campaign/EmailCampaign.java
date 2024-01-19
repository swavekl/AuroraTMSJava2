package com.auroratms.email.campaign;

import java.util.Date;
import java.util.List;

public class EmailCampaign {

    // unique id of a campaign
    private Long id;

    // name of campaign e.g. 2024 Aurora Summer Open announcement
    private String name;

    // subject of the email
    private String subject;

    // text of the email body
    private String body;

    // date sent
    private Date dateSent;

    // name of the tournament for which it was sent out
    private String tournamentName;

    // number of emails sent
    private int emailsCount;

    // this part will be variable and will be stored as JSON
    private List<Long> recipientFilters;

    // recipients to remove from the list
    private List<FilterConfiguration.Recipient> removedRecipients;

    public EmailCampaign() {
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getBody() {
        return this.body;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

    public List<Long> getRecipientFilters() {
        return recipientFilters;
    }

    public void setRecipientFilters(List<Long> recipientFilters) {
        this.recipientFilters = recipientFilters;
    }

    public List<FilterConfiguration.Recipient> getRemovedRecipients() {
        return removedRecipients;
    }

    public void setRemovedRecipients(List<FilterConfiguration.Recipient> removedRecipients) {
        this.removedRecipients = removedRecipients;
    }

    public EmailCampaignEntity convertToEntity() {
        EmailCampaignEntity emailCampaignEntity = new EmailCampaignEntity();
        emailCampaignEntity.setId(this.getId());
        emailCampaignEntity.setName(this.getName());
        emailCampaignEntity.setSubject(this.getSubject());
        emailCampaignEntity.setBody(this.getBody());
        emailCampaignEntity.setDateSent(this.getDateSent());
        emailCampaignEntity.setTournamentName(this.getTournamentName());
        emailCampaignEntity.setEmailsCount(this.getEmailsCount());
        FilterConfiguration filterConfiguration = new FilterConfiguration();
        filterConfiguration.setRecipientFilters(this.getRecipientFilters());
        filterConfiguration.setRemovedRecipients(this.getRemovedRecipients());
        String content = filterConfiguration.convertToJSON();
        emailCampaignEntity.setFilterContentsJSON(content);

        return emailCampaignEntity;
    }

    public EmailCampaign convertFromEntity(EmailCampaignEntity emailCampaignEntity) {
        this.id = emailCampaignEntity.getId();
        this.name = emailCampaignEntity.getName();
        this.subject = emailCampaignEntity.getSubject();
        this.body = emailCampaignEntity.getBody();
        this.dateSent = emailCampaignEntity.getDateSent();
        this.tournamentName = emailCampaignEntity.getTournamentName();
        this.emailsCount = emailCampaignEntity.getEmailsCount();
        String content = emailCampaignEntity.getFilterContentsJSON();
        FilterConfiguration filterConfiguration = FilterConfiguration.convertFromJSON(content);
        this.recipientFilters = filterConfiguration.getRecipientFilters();
        this.removedRecipients = filterConfiguration.getRemovedRecipients();

        return this;
    }
}

