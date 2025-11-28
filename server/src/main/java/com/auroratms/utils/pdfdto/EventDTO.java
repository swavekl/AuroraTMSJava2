package com.auroratms.utils.pdfdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a single tournament event, including its rules and prizes.
 */
@Data
@NoArgsConstructor
@JsonPropertyOrder({
        "ordinal_number",
        "event_name",
        "day",
        "start_time",
        "entry_fee",
        "is_doubles",
        "max_entries",
        "single_elimination",
        "max_rating",
        "gender_restriction",
        "age_restriction",
        "players_per_group",
        "draw_method",
        "number_of_games",
        "number_of_games_se_playoffs",
        "number_of_games_se_quarter_finals",
        "number_of_games_se_semi_finals",
        "number_of_games_se_finals",
        "prizes"
})
public class EventDTO {

    @JsonProperty("ordinal_number")
    private String ordinalNumber;

    @JsonProperty("event_name")
    private String eventName;

    private String day;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("entry_fee")
    private String entryFee;

    @JsonProperty("is_doubles")
    private boolean isDoubles;

    // maximum entries, 0 if no limit
    @JsonProperty("max_entries")
    private int maxEntries;

    @JsonProperty("single_elimination")
    private boolean singleElimination;

    @JsonProperty("max_rating")
    private String maxRating;

    @JsonProperty("gender_restriction")
    private String genderRestriction; // Enum: "NONE", "MALE", "FEMALE"

    @JsonProperty("age_restriction")
    private AgeRestrictionDTO ageRestriction;

    @JsonProperty("players_per_group")
    private String playersPerGroup;

    @JsonProperty("draw_method")
    private String drawMethod;

    private List<PrizeDTO> prizes;

    // best of 3, 5, 7 or 9 games per match in the main round (i.e. round robin)
    @JsonProperty("number_of_games")
    private int numberOfGames;

    // in single elimination round or if event is a single elimination only
    // number of games in rounds prior to quarter finals e.g. 5
    @JsonProperty("number_of_games_se_playoffs")
    private int numberOfGamesSEPlayoffs = 5;

    // number of games in quarter, semi finals and 3rd/4th place matches
    @JsonProperty("number_of_games_se_quarter_finals")
    private int numberOfGamesSEQuarterFinals = 5;

    @JsonProperty("number_of_games_se_semi_finals")
    private int numberOfGamesSESemiFinals = 5;

    @JsonProperty("number_of_games_se_finals")
    private int numberOfGamesSEFinals = 5;

}

