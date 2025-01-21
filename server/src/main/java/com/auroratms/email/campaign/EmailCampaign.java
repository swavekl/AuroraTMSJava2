package com.auroratms.email.campaign;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class EmailCampaign implements Serializable {

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

    // if true ignore recipient filter and get all recipients in the database
    private boolean allRecipients;

    // if true exclude those from the list who are already registered for this tournament
    private boolean excludeRegistered;

    // state abbreviations to filter by
    private List<String> stateFilters;

    // treat body of this email as html when sending
    boolean htmlEmail;

    // path of the uploaded file containing recipients last name, first name and email address
    private String uploadedRecipientsFile;

    // if true send email to uploaded recipients
    private boolean includeUploadedRecipients;

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

    public boolean isHtmlEmail() {
        return htmlEmail;
    }

    public void setHtmlEmail(boolean htmlEmail) {
        this.htmlEmail = htmlEmail;
    }

    public List<String> getStateFilters() {
        return stateFilters;
    }

    public void setStateFilters(List<String> stateFilters) {
        this.stateFilters = stateFilters;
    }

    public boolean isAllRecipients() {
        return allRecipients;
    }

    public void setAllRecipients(boolean allRecipients) {
        this.allRecipients = allRecipients;
    }

    public boolean isExcludeRegistered() {
        return excludeRegistered;
    }

    public void setExcludeRegistered(boolean excludeRegistered) {
        this.excludeRegistered = excludeRegistered;
    }

    public String getUploadedRecipientsFile() {
        return uploadedRecipientsFile;
    }

    public void setUploadedRecipientsFile(String uploadedRecipientsFile) {
        this.uploadedRecipientsFile = uploadedRecipientsFile;
    }

    public boolean isIncludeUploadedRecipients() {
        return includeUploadedRecipients;
    }

    public void setIncludeUploadedRecipients(boolean includeUploadedRecipients) {
        this.includeUploadedRecipients = includeUploadedRecipients;
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
        FilterConfiguration filterConfiguration = getFilterConfiguration();
        String content = filterConfiguration.convertToJSON();
        emailCampaignEntity.setFilterContentsJSON(content);
        emailCampaignEntity.setHtmlEmail(this.isHtmlEmail());

        return emailCampaignEntity;
    }

    public FilterConfiguration getFilterConfiguration() {
        FilterConfiguration filterConfiguration = new FilterConfiguration();
        filterConfiguration.setRecipientFilters(this.getRecipientFilters());
        filterConfiguration.setRemovedRecipients(this.getRemovedRecipients());
        filterConfiguration.setStateFilters(this.getStateFilters());
        filterConfiguration.setAllRecipients(this.isAllRecipients());
        filterConfiguration.setExcludeRegistered(this.isExcludeRegistered());
        filterConfiguration.setUploadedRecipientsFile(this.getUploadedRecipientsFile());
        filterConfiguration.setIncludeUploadedRecipients(this.isIncludeUploadedRecipients());
        return filterConfiguration;
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
        this.stateFilters = filterConfiguration.getStateFilters();
        if (filterConfiguration.getAllRecipients() != null) {
            this.allRecipients = filterConfiguration.getAllRecipients();
        } else {
            this.allRecipients = this.stateFilters != null && !this.stateFilters.isEmpty();
        }
        this.excludeRegistered = (filterConfiguration.getExcludeRegistered() != null) ? filterConfiguration.getExcludeRegistered() : false;
        this.htmlEmail = emailCampaignEntity.isHtmlEmail();
        this.uploadedRecipientsFile = filterConfiguration.getUploadedRecipientsFile();
        this.includeUploadedRecipients = filterConfiguration.isIncludeUploadedRecipients() != null ? filterConfiguration.isIncludeUploadedRecipients() : false;

        return this;
    }

    @Override
    public String toString() {
        return "EmailCampaign{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dateSent=" + dateSent +
                ", tournamentName='" + tournamentName + '\'' +
                ", emailsCount=" + emailsCount +
                ", recipientFilters=" + recipientFilters +
                ", removedRecipients=" + removedRecipients +
                ", allRecipients=" + allRecipients +
                ", excludeRegistered=" + excludeRegistered +
                ", stateFilters=" + stateFilters +
                ", htmlEmail=" + htmlEmail +
                ", uploadedRecipientsFile=" + uploadedRecipientsFile +
                ", toUploadedRecipients=" + includeUploadedRecipients +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}

