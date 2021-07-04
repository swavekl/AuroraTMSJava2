package com.auroratms.draw.notification.event;


import com.auroratms.draw.DrawType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@NoArgsConstructor
public class DrawsEvent {

    // id of event for which the draws were generated
    private long eventId;

    // type of draw that was generated - round robin or single elimination
    private DrawType drawType;

    // generated, deleted, updated
    private DrawAction action;

    public DrawsEvent(long eventId, DrawType drawType, DrawAction action) {
        this.eventId = eventId;
        this.drawType = drawType;
        this.action = action;
    }
}

