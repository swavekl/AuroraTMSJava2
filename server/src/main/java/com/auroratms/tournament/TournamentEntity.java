package com.auroratms.tournament;

import lombok.NonNull;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * This tournament object IS persisted.
 */
@Entity
@Table(name = "tournament")
public class TournamentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(length = 100)
    private String name;

    // venue information
    @Column(length = 100)
    private String venueName;
    private String streetAddress;
    @Column(length = 100)
    private String city;
    @Column(length = 40)
    private String state;
    @Column(length = 20)
    private String zipCode;
    private Date startDate;
    private Date endDate;
    private int starLevel;

    // tournament director contact information
    @Column(length = 60)
    private String contactName;
    @Column(length = 60)
    private String email;
    @Column(length = 30)
    private String phone;

    // total number of entries
    private int numEntries;
    // number of event spots taken vs all that are available
    private int numEventEntries;
    // maximum number of event entries
    private int maxNumEventEntries;

    // to avoid having to change database schema each time we add new field to configuration
    // we will persist configuration as JSON in this field.
    @Column(length = 6000)
    private String content;

//    // events of this tournament
//    @OneToMany(mappedBy="tournamentEntity")
//    private Set<TournamentEventEntity> events = new HashSet<>();

    // total prize money
    private int totalPrizeMoney;

    // if true will be displayed to players in the list.  If false it is still being configured.
    private boolean ready;

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

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
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

    public int getStarLevel() {
        return starLevel;
    }

    public void setStarLevel(int starLevel) {
        this.starLevel = starLevel;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getNumEntries() {
        return numEntries;
    }

    public void setNumEntries(int numEntries) {
        this.numEntries = numEntries;
    }

    public int getNumEventEntries() {
        return numEventEntries;
    }

    public void setNumEventEntries(int numEventEntries) {
        this.numEventEntries = numEventEntries;
    }

    public int getMaxNumEventEntries() {
        return maxNumEventEntries;
    }

    public void setMaxNumEventEntries(int maxNumEventEntries) {
        this.maxNumEventEntries = maxNumEventEntries;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

//    public Set<TournamentEventEntity> getEvents() {
//        return events;
//    }
//
//    public void setEvents(Set<TournamentEventEntity> events) {
//        this.events = events;
//    }


    public int getTotalPrizeMoney() {
        return totalPrizeMoney;
    }

    public void setTotalPrizeMoney(int totalPrizeMoney) {
        this.totalPrizeMoney = totalPrizeMoney;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TournamentEntity entity = (TournamentEntity) o;
        return starLevel == entity.starLevel &&
                id.equals(entity.id) &&
                name.equals(entity.name) &&
                Objects.equals(venueName, entity.venueName) &&
                Objects.equals(streetAddress, entity.streetAddress) &&
                Objects.equals(city, entity.city) &&
                Objects.equals(state, entity.state) &&
                Objects.equals(zipCode, entity.zipCode) &&
                Objects.equals(startDate, entity.startDate) &&
                Objects.equals(endDate, entity.endDate) &&
                Objects.equals(contactName, entity.contactName) &&
                Objects.equals(email, entity.email) &&
                Objects.equals(phone, entity.phone) &&
                Objects.equals(numEntries, entity.numEntries) &&
                Objects.equals(numEventEntries, entity.numEventEntries) &&
                Objects.equals(maxNumEventEntries, entity.maxNumEventEntries) &&
                Objects.equals(totalPrizeMoney, entity.totalPrizeMoney) &&
                Objects.equals(ready, entity.ready) &&
                Objects.equals(content, entity.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, venueName, streetAddress, city, state, zipCode, startDate, endDate, starLevel, contactName, email, phone, totalPrizeMoney, content, ready);
    }
}
