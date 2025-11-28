package com.auroratms.utils.pdfdto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Top level class used for extraction of data from PDF text and expressing it as JSON.
 * This way we don't have to maintain a JSON schema file.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL) // Omit any field that is null
// The @JsonPropertyOrder annotation forces the generated JSON schema to match the
// order of properties as they appear in this list, which reflects the class definition.
@JsonPropertyOrder({
        "tournament_name",
        "star_rating",
        "tournament_dates",
        "rating_eligibility_date",
        "rating_seeding_date",
        "entry_deadline_date",
        "refund_deadline_date",
        "late_entry_date",
        "registration_fee",
        "venue_name",
        "venue_address",
        "website_url",
        "referee",
        "umpires",
        "directors",
        "equipment"
})
public class TournamentDTO {

    // --- Core Information ---
    @JsonProperty("tournament_name")
    private String tournamentName;

    @JsonProperty("star_rating")
    private String starRating;

    @JsonProperty("tournament_dates")
    private List<String> tournamentDates; // Retain List<String> for flexibility

    // --- Deadline/Eligibility Dates ---

    @JsonProperty("rating_eligibility_date")
    private String ratingEligibilityDate;

    @JsonProperty("rating_seeding_date")
    private String ratingSeedingDate;

    @JsonProperty("entry_deadline_date")
    private String entryDeadlineDate;

    @JsonProperty("refund_deadline_date")
    private String refundDeadlineDate;

    @JsonProperty("late_entry_date")
    private String lateEntryDate;

    @JsonProperty("registration_fee")
    private String registrationFee;

    // --- Venue and Contact Information (Flattened) ---

    // Flattened Venue Name/URL
    @JsonProperty("venue_name")
    private String venueName;

    @JsonProperty("website_url")
    private String websiteUrl;

    // Nested Address object is mandatory, so we keep it nested for structure
    @JsonProperty("venue_address")
    private VenueAddressDTO venueAddress;

    // --- Officials and Directors ---

    // Referee is mandatory in your schema, so it remains a nested object
    @JsonProperty("referee")
    private OfficialDTO referee;

    // Umpires list made OPTIONAL via JsonInclude.NON_EMPTY
    @JsonInclude(Include.NON_EMPTY)
    @JsonProperty("umpires")
    private List<OfficialDTO> umpires;

    // Directors list made OPTIONAL via JsonInclude.NON_EMPTY
    @JsonInclude(Include.NON_EMPTY)
    @JsonProperty("directors")
    private List<DirectorDTO> directors;

    // --- Equipment ---

    // Equipment is mandatory in your schema, so it remains a nested object
    @JsonProperty("equipment")
    private EquipmentDTO equipment;
}