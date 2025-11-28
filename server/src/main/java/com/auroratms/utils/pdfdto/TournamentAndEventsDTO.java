package com.auroratms.utils.pdfdto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Top level class for accessing tournament and events information
 * It is not used for JSON schema generation (see TournamentDTO)
 */
@Data
@NoArgsConstructor
public class TournamentAndEventsDTO extends TournamentDTO{

    /**
     * List of all extracted event objects.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("events")
    private List<EventDTO> events;

}
