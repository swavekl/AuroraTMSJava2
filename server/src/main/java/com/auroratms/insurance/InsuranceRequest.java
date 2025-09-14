package com.auroratms.insurance;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

enum AdditionalInsuredRole {
    None,
    OwnerOfPremises,
    Sponsor,
    Other
}

@Entity
@Table()
public class InsuranceRequest implements Serializable {
    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orgName;
    private String orgStreetAddress;
    private String orgCity;
    private Integer orgZip;
    private String orgState;

    private Date requestDate;
    // person filling out the request
    private String contactName;
    private String contactPhoneNumber;
    private String contactEmail;

    // certificate holder data
    private String certFacilityName;
    private String certPersonName;
    private String certPersonPhoneNumber;
    private String certPersonEmail;
    private String certStreetAddress;
    private String certCity;
    private String certState;
    private Integer certZip;

    private String eventName;
    private Date eventStartDate;
    private Date eventEndDate;

    private boolean isAdditionalInsured = false;
    private String additionalInsuredName;

    // optional document for this agreement document
    @Column(length = 500)
    private String additionalInsuredAgreementUrl;

    private AdditionalInsuredRole additionalInsuredRole = AdditionalInsuredRole.None;
    private String otherRoleDescription;

    // request status - started, submitted, approved, rejected
    private InsuranceRequestStatus status;

    @Column(length = 500)
    private String certificateUrl;


    public InsuranceRequest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgStreetAddress() {
        return orgStreetAddress;
    }

    public void setOrgStreetAddress(String orgStreetAddress) {
        this.orgStreetAddress = orgStreetAddress;
    }

    public String getOrgCity() {
        return orgCity;
    }

    public void setOrgCity(String orgCity) {
        this.orgCity = orgCity;
    }

    public Integer getOrgZip() {
        return orgZip;
    }

    public void setOrgZip(Integer orgZip) {
        this.orgZip = orgZip;
    }

    public String getOrgState() {
        return orgState;
    }

    public void setOrgState(String orgState) {
        this.orgState = orgState;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getCertFacilityName() {
        return certFacilityName;
    }

    public void setCertFacilityName(String certFacilityName) {
        this.certFacilityName = certFacilityName;
    }

    public String getCertPersonName() {
        return certPersonName;
    }

    public void setCertPersonName(String certPersonName) {
        this.certPersonName = certPersonName;
    }

    public String getCertPersonPhoneNumber() {
        return certPersonPhoneNumber;
    }

    public void setCertPersonPhoneNumber(String certPersonPhoneNumber) {
        this.certPersonPhoneNumber = certPersonPhoneNumber;
    }

    public String getCertPersonEmail() {
        return certPersonEmail;
    }

    public void setCertPersonEmail(String certPersonEmail) {
        this.certPersonEmail = certPersonEmail;
    }

    public String getCertStreetAddress() {
        return certStreetAddress;
    }

    public void setCertStreetAddress(String certStreetAddress) {
        this.certStreetAddress = certStreetAddress;
    }

    public String getCertCity() {
        return certCity;
    }

    public void setCertCity(String certCity) {
        this.certCity = certCity;
    }

    public String getCertState() {
        return certState;
    }

    public void setCertState(String certState) {
        this.certState = certState;
    }

    public Integer getCertZip() {
        return certZip;
    }

    public void setCertZip(Integer certZip) {
        this.certZip = certZip;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Date getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(Date eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public Date getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(Date eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public boolean isAdditionalInsured() {
        return isAdditionalInsured;
    }

    public void setAdditionalInsured(boolean additionalInsured) {
        isAdditionalInsured = additionalInsured;
    }

    public String getAdditionalInsuredName() {
        return additionalInsuredName;
    }

    public void setAdditionalInsuredName(String additionalInsuredName) {
        this.additionalInsuredName = additionalInsuredName;
    }

    public AdditionalInsuredRole getAdditionalInsuredRole() {
        return additionalInsuredRole;
    }

    public void setAdditionalInsuredRole(AdditionalInsuredRole additionalInsuredRole) {
        this.additionalInsuredRole = additionalInsuredRole;
    }

    public String getOtherRoleDescription() {
        return otherRoleDescription;
    }

    public void setOtherRoleDescription(String otherRoleDescription) {
        this.otherRoleDescription = otherRoleDescription;
    }

    public InsuranceRequestStatus getStatus() {
        return status;
    }

    public void setStatus(InsuranceRequestStatus status) {
        this.status = status;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public String getAdditionalInsuredAgreementUrl() {
        return additionalInsuredAgreementUrl;
    }

    public void setAdditionalInsuredAgreementUrl(String additionalInsuredAggreementUrl) {
        this.additionalInsuredAgreementUrl = additionalInsuredAggreementUrl;
    }
}
