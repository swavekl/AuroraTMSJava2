package com.auroratms.usatt;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.users.UserRoles;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Service for getting various USATT officials and employees
 */
@Service
public class UsattPersonnelService {

    public static final String DEPARTMENT_TOURNAMENTS = "Tournaments";
    public static final String DEPARTMENT_REFEREES_UMPIRES = "RefereesUmpires";
    public static final String DEPARTMENT_COORDINATORS = "Coordinators";

    @Autowired
    private UserProfileService userProfileService;

    /**
     * Gets all employees and officials
     * @return
     */
    public List<UserProfile> getAll() {
        return this.userProfileService.listUserInRole(UserRoles.USATTTournamentManagers, null);
    }

    /**
     *
     * @param role
     * @return
     */
    public UserProfile getPersonInRole(String role) {
        List<UserProfile> userProfileList = this.userProfileService.listUserInRole(role, null);
        if (userProfileList.size() > 0) {
            return userProfileList.get(0);
        } else {
            return null;
        }
    }

    /**
     *
     * @param region
     * @return
     */
    public UserProfile getSanctionCoordinator(String region) {
        UserProfile coordinatorProfile = null;
        List<UserProfile> userProfileList = userProfileService.listUserInRole(UserRoles.USATTSanctionCoordinators, region);
        for (UserProfile userProfile : userProfileList) {
            if (userProfile.getDivision().contains(region)) {
                coordinatorProfile = userProfile;
                break;
            }
        }
        return coordinatorProfile;
    }

    /**
     * Determines sanction coordinator for a region or national
     * @param startLevel
     * @param venueState
     * @return
     */
    public String getSanctionCoordinatorRegion(int startLevel, String venueState) {
        Map<String, String[]> regionsInfo = new HashMap();
        regionsInfo.put("East", new String [] {"CT", "DE", "DC", "ME", "MD", "MA", "NH", "NJ", "NY", "PA", "RI", "VT", "VA", "WV"});
        regionsInfo.put("Midwest", new String [] {"IL", "IN", "KY", "MI", "OH"});
        regionsInfo.put("Mountain", new String [] {"CO", "NE", "NM", "UT", "WY"});
        regionsInfo.put("North", new String [] {"IA", "MN", "ND", "SD", "WI"});
        regionsInfo.put("Northwest", new String [] {"AK", "ID", "MT", "OR", "WA"});
        regionsInfo.put("Pacific", new String [] {"AZ", "CA", "HI", "NV"});
        regionsInfo.put("South Central", new String [] {"AR", "KS", "LA", "MO", "OK", "TX"});
        regionsInfo.put("Southeast", new String [] {"AL", "FL", "GA", "MS", "NC", "SC", "TN"});
        if (startLevel >= 4) {
            return "National";
        } else {
            for (String region : regionsInfo.keySet()) {
                String[] states = regionsInfo.get(region);
                String stateFound = Arrays.stream(states)
                        .filter(state -> state.equals(venueState))
                        .findAny()
                        .orElse(null);

                if (stateFound != null) {
                    return region;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param userProfile
     */
    public void save (UserProfile userProfile) {
        userProfileService.updateProfile(userProfile);
    }
}
