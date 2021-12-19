package com.auroratms.sanction;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SanctionCategoryCriteria {
    private String name;
    private int points;
    private boolean selected;
    private String requiredForStarLevel;
}
