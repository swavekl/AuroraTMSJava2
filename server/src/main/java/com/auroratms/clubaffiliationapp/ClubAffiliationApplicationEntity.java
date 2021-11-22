package com.auroratms.clubaffiliationapp;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * This is entity which should contain only the fields by which we can query
 */
@Entity
@Table(name = "clubaffiliationapplication")
public class ClubAffiliationApplicationEntity {
    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // club name e.g. Fox Valley Table Tennis club
    private String name;
    // e.g Eola Community Center
    private String buildingName;

    // playing site address
    private String streetAddress;
    private String city;
    private String state;
    // 5 or Zip+4 code
    private String zipCode;

    // Wednesday & Friday - 6:30 - 9:30PM
    private String hoursAndDates;

    // date when USATT affiliation expires
    private Date affiliationExpirationDate;

    private ClubAffiliationApplicationStatus status;

    // payment id if the application fee was paid
    private Long paymentId;

    // to avoid having to change database schema each time we add new field to configuration
    // we will persist configuration as JSON in this field.
    @Column(length = 6000)
    private String content;

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

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Date getAffiliationExpirationDate() {
        return affiliationExpirationDate;
    }

    public void setAffiliationExpirationDate(Date affiliationExpirationDate) {
        this.affiliationExpirationDate = affiliationExpirationDate;
    }

    public ClubAffiliationApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ClubAffiliationApplicationStatus status) {
        this.status = status;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHoursAndDates() {
        return hoursAndDates;
    }

    public void setHoursAndDates(String hoursAndDates) {
        this.hoursAndDates = hoursAndDates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClubAffiliationApplicationEntity entity = (ClubAffiliationApplicationEntity) o;
        return Objects.equals(id, entity.id) && name.equals(entity.name) && Objects.equals(buildingName, entity.buildingName) && Objects.equals(streetAddress, entity.streetAddress) && city.equals(entity.city) && state.equals(entity.state) && Objects.equals(zipCode, entity.zipCode) && Objects.equals(hoursAndDates, entity.hoursAndDates) && Objects.equals(affiliationExpirationDate, entity.affiliationExpirationDate) && status == entity.status && Objects.equals(paymentId, entity.paymentId) && Objects.equals(content, entity.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, buildingName, streetAddress, city, state, zipCode, hoursAndDates, affiliationExpirationDate, status, paymentId, content);
    }
}
