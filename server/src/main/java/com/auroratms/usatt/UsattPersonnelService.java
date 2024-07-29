package com.auroratms.usatt;

import com.auroratms.profile.UserProfile;
import com.auroratms.profile.UserProfileService;
import com.auroratms.users.UserRoles;
import com.auroratms.utils.USRegionsInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (startLevel >= 4) {
            return "National";
        } else {
            return USRegionsInfo.lookupRegionFromState(venueState);
        }
    }

    /**
     *
     * @param userProfile
     */
    public void save (UserProfile userProfile) {
        userProfileService.updateProfile(userProfile);
    }
}
