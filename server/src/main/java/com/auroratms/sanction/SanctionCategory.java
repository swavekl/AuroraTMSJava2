package com.auroratms.sanction;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SanctionCategory {
    private String title;

    private String name;

    // select one item in category or all that apply
    private boolean selectOne;

    private int selectedValue;

    private List<SanctionCategoryCriteria> criteria;

}
