package com.auroratms.tournament;

import com.auroratms.event.TournamentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Set;

/**
 * This tournament object is not persisted. It configuration part which is stored by TournamentEntity
 * as JSON string in the content column.  This forces us to add fields in two places but allows
 * us to potentially not have to change database schema when we add new piece of information.
 */
@Data
@NoArgsConstructor
public class Tournament {
    private Long id;

    private String name;

    // venue information
    private String venueName;
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private Date startDate;
    private Date endDate;
    private int starLevel;
    // tournament director contact information
    private String contactName;
    private String email;
    private String phone;

    // total number of entries
    private int numEntries;
    // number of event spots taken vs all that are available
    private int numEventEntries;
    // maximum number of event entries
    private int maxNumEventEntries;

    // information that is not queryable
    private TournamentConfiguration configuration;

    private Set<TournamentEvent> events;

    /**
     * converts configuration to its JSON representation
     *
     * @return
     */
    public TournamentEntity convertToEntity() {
        TournamentEntity entity = new TournamentEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setVenueName(venueName);
        entity.setStreetAddress(streetAddress);
        entity.setCity(city);
        entity.setState(state);
        entity.setZipCode(zipCode);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setStarLevel(starLevel);
        entity.setContactName(contactName);
        entity.setEmail(email);
        entity.setPhone(phone);
        entity.setNumEntries(numEntries);
        entity.setNumEventEntries(numEventEntries);
        entity.setMaxNumEventEntries(maxNumEventEntries);
//        entity.setEvents(events);
        // convert from configuration to JSON
        if (configuration != null) {
            try {
                StringWriter stringWriter = new StringWriter();
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.writeValue(stringWriter, configuration);
                String content = stringWriter.toString();
                entity.setContent(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return entity;
    }

    /**
     * Converts from JSON representation to configuration
     */
    public Tournament convertFromEntity(TournamentEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.venueName = entity.getVenueName();
        this.streetAddress = entity.getStreetAddress();
        this.city = entity.getCity();
        this.state = entity.getState();
        this.zipCode = entity.getZipCode();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.starLevel = entity.getStarLevel();
        this.contactName = entity.getContactName();
        this.email = entity.getEmail();
        this.phone = entity.getPhone();
        this.numEntries = entity.getNumEntries();
        this.numEventEntries = entity.getNumEventEntries();
        this.maxNumEventEntries = entity.getMaxNumEventEntries();
//        this.events = entity.getEvents();

        configuration = null;
        // convert from JSON to configuration
        String content = entity.getContent();
        if (content != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                configuration = mapper.readValue(content, TournamentConfiguration.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;
    }
}
