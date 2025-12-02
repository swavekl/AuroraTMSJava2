package com.auroratms.utils.fuzzymatch;

import com.fasterxml.jackson.annotation.JsonProperty;

// Record to hold one matched pair of event names
public record EventMatch(
        // The exact name from the first list (List A: Website/Source)
        @JsonProperty("list_a_name")
        String listAName,

        // The best semantic match from the second list (List B: Entry Form/Target)
        @JsonProperty("list_b_name")
        String listBName
) {}