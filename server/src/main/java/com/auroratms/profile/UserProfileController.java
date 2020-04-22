package com.auroratms.profile;

import com.auroratms.AbstractOktaController;
import com.okta.sdk.client.Client;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * REST API controller for managing user profile
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class UserProfileController extends AbstractOktaController {

    /**
     * Gets user profile
     * @param userId
     * @return
     */
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping("/profiles/{userId}")
    @ResponseBody
    public ResponseEntity<UserProfile> getProfile(@PathVariable String userId) {
        try {
            Client client = getClient();

            User user = client.getUser(userId);
            UserProfile userProfile = fromOktaUser (user);
            return new ResponseEntity<UserProfile>(userProfile, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<UserProfile>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Converts from Okta user to user profile
     * @param user
     * @return
     */
    private UserProfile fromOktaUser(User user) {
        com.okta.sdk.resource.user.UserProfile oktaUserProfile = user.getProfile();
        return new UserProfile(user.getId(), oktaUserProfile);
    }

    /**
     * Updates user profile
     * @param userProfile
     * @param userId
     * @return
     */
    @PutMapping ("/profiles/{userId}")
    public ResponseEntity update (@RequestBody UserProfile userProfile, @PathVariable String userId) {
        try {
            Client client = getClient();

            // get current user
            User currentUser = client.getUser(userProfile.getUserId());
            // update the profile
            com.okta.sdk.resource.user.UserProfile currentUserProfile = currentUser.getProfile();
            currentUserProfile
                    .setFirstName(userProfile.getFirstName())
                    .setLastName(userProfile.getLastName())
                    .setEmail(userProfile.getEmail())
                    .setMobilePhone(userProfile.getMobilePhone());
            currentUserProfile.put("city", userProfile.getCity());
            currentUserProfile.put("state", userProfile.getState());
            currentUserProfile.put("zipCode", userProfile.getZipCode());

            currentUser.update();
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/profiles")
    @PreAuthorize("hasAuthority('TournamentDirector')")
    public ResponseEntity<Collection<UserProfile>> list() {
        try {
            Client client = getClient();
            UserList users = client.listUsers();

            // convert all users to user profiles
            Iterator<User> iterator = users.iterator();
            Collection<UserProfile> userProfiles = new ArrayList<>();
            while (iterator.hasNext()) {
                User oktaUser = iterator.next();
                UserProfile userProfile = fromOktaUser(oktaUser);
                userProfiles.add(userProfile);
            }
            return new ResponseEntity(userProfiles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

}
