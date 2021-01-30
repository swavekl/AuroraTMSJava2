package com.auroratms.profile;

import com.auroratms.AbstractOktaController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * REST API controller for managing user profile
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class UserProfileController extends AbstractOktaController {

    @Autowired
    private UserProfileService userProfileService;

    /**
     * Gets user profile
     *
     * @param userId
     * @return
     */
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping("/profiles/{userId}")
    @ResponseBody
    public ResponseEntity<UserProfile> getProfile(@PathVariable String userId) {
        try {
            UserProfile userProfile = userProfileService.getProfile(userId);
            return new ResponseEntity<UserProfile>(userProfile, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("e = " + e);
            return new ResponseEntity<UserProfile>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Updates user profile
     *
     * @param userProfile
     * @param userId
     * @return
     */
    @PutMapping("/profiles/{userId}")
    public ResponseEntity update(@RequestBody UserProfile userProfile, @PathVariable String userId) {
        try {
            userProfileService.updateProfile(userProfile);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/profiles")
    @PreAuthorize("hasAuthority('TournamentDirector')")
    public ResponseEntity<Collection<UserProfile>> list() {
        try {
            Collection<UserProfile> userProfiles = userProfileService.list();
            return new ResponseEntity(userProfiles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/profilessearch")
    public ResponseEntity<Collection<UserProfile>> list(@RequestParam(name = "firstName", required = false) String firstName,
                                                        @RequestParam(name = "lastName", required = false) String lastName) {
        try {
            Collection<UserProfile> userProfiles = userProfileService.list(firstName, lastName);
            return new ResponseEntity(userProfiles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
