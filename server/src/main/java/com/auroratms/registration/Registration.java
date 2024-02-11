package com.auroratms.registration;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class Registration {
    // tournament entry id, or clinic or seminar etc.
    Long id;

    // tournament id, or clinic id etc.
    Long activityId;

    // name of the event e.g. tournament or clinic name
    String name;

    // registration type
    RegistrationEventType registrationEventType;

    // start and end date
    Date startDate;
    Date endDate;

    // total paid for registration
    Integer cost;

    // event specific information like number of entered events
    String info;

}
