package com.auroratms.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Region information for US States
 */
public class USRegionsInfo {
    private static final Map<String, String[]> regionInfos;

    static
    {
        regionInfos = new HashMap();
        regionInfos.put("East", new String [] {"CT", "DE", "DC", "ME", "MD", "MA", "NH", "NJ", "NY", "PA", "RI", "VT", "VA", "WV"});
        regionInfos.put("Midwest", new String [] {"IL", "IN", "KY", "MI", "OH"});
        regionInfos.put("Mountain", new String [] {"CO", "NE", "NM", "UT", "WY"});
        regionInfos.put("North", new String [] {"IA", "MN", "ND", "SD", "WI"});
        regionInfos.put("Northwest", new String [] {"AK", "ID", "MT", "OR", "WA"});
        regionInfos.put("Pacific", new String [] {"AZ", "CA", "HI", "NV"});
        regionInfos.put("South Central", new String [] {"AR", "KS", "LA", "MO", "OK", "TX"});
        regionInfos.put("Southeast", new String [] {"AL", "FL", "GA", "MS", "NC", "SC", "TN"});
    }

    public static String lookupRegionFromState(String stateToLookup) {
        for (Map.Entry<String, String[]> regionInfo : regionInfos.entrySet()) {
            String[] regionStates = regionInfo.getValue();
            String stateFound = Arrays.stream(regionStates)
                    .filter(state -> state.equals(stateToLookup))
                    .findAny()
                    .orElse(null);

            if (stateFound != null) {
                return regionInfo.getKey();
            }
        }
        return null;
    }

}
