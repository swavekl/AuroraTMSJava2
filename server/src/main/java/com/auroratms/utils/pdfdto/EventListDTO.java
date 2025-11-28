package com.auroratms.utils.pdfdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The root DTO for the Tournament Event Extraction Schema.
 * Contains a list of all extracted events.  It is used to generate JSON schema to be passed to LLM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventListDTO {

    private List<EventDTO> events;
}