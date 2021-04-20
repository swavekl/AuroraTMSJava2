package com.auroratms.profile;

import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@Service
@CacheConfig(cacheNames = {"profiles"})
@Transactional
public class UserProfileService {

    @Value("${okta.client.orgUrl}")
    protected String oktaServiceBase;

    @Value("${okta.client.token}")
    protected String api_token;

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Autowired
    private SidService sidService;

    /**
     *
     * @param userId
     * @return
     */
    @Cacheable(key = "#userId")
    public UserProfile getProfile (String userId) {
        Client client = getClient();

        User user = client.getUser(userId);
        UserProfile userProfile = fromOktaUser (user);
        return userProfile;
    }

    /**
     * Updates user profile
     * @param userProfile
     */
    @CachePut(key = "#result.userId")
    public UserProfile updateProfile (UserProfile userProfile) {
        Client client = getClient();

        // get current user
        User currentUser = client.getUser(userProfile.getUserId());
        // update the profile
        toOktaUserProfile(userProfile, currentUser);

        User updatedOktaUser = currentUser.update();
        // user changed email - update it in the acl_sid table
        if (!userProfile.getEmail().equals(userProfile.getLogin())) {
            this.sidService.updateSid(userProfile.getLogin(), userProfile.getEmail());
        }
        return fromOktaUser (updatedOktaUser);
    }

    /**
     *
     * @return
     */
    public Collection<UserProfile> list () {
        Client client = getClient();
        UserList users = client.listUsers();
        return toUserProfileList(users);
    }

    /**
     *
     * @param firstName
     * @param lastName
     * @return
     */
    public Collection<UserProfile> list (String firstName, String lastName) {
        String filter1 = (firstName != null) ? "profile.firstName eq \"" + firstName + "\"" : null;
        String filter2 = (lastName != null) ? "profile.lastName eq \"" + lastName + "\"" : null;
        String filter = (filter1 != null) ? filter1 : "";
        filter += (filter1 != null && filter2 != null) ? " and " : "";
        filter += (filter2 != null) ? filter2 : "";
        Client client = getClient();
        UserList users = client.listUsers(null, filter, null, null, null);

        // convert all users to user profiles
        return toUserProfileList(users);
    }

    /**
     * Converts list of okta users to userprofiles
     * @param users
     * @return
     */
    private Collection<UserProfile> toUserProfileList(UserList users) {
        Iterator<User> iterator = users.iterator();
        Collection<UserProfile> userProfiles = new ArrayList<>();
        while (iterator.hasNext()) {
            User oktaUser = iterator.next();
            UserProfile userProfile = fromOktaUser(oktaUser);
            userProfiles.add(userProfile);
        }
        return userProfiles;
    }

    /**
     * Converts from Okta user to user profile
     * @param user
     * @return
     */
    private UserProfile fromOktaUser(User user) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(user.getId());

        com.okta.sdk.resource.user.UserProfile oktaUserProfile = user.getProfile();
        userProfile.setFirstName(oktaUserProfile.getFirstName());
        userProfile.setLastName(oktaUserProfile.getLastName());
        userProfile.setMobilePhone(oktaUserProfile.getMobilePhone());
        userProfile.setLogin(oktaUserProfile.getLogin());
        userProfile.setEmail(oktaUserProfile.getEmail());
        userProfile.setStreetAddress(oktaUserProfile.getStreetAddress());
        userProfile.setCity(oktaUserProfile.getCity());
        userProfile.setState(oktaUserProfile.getState());
        userProfile.setZipCode(oktaUserProfile.getZipCode());
        userProfile.setCountryCode(oktaUserProfile.getCountryCode());
        userProfile.setGender((String) oktaUserProfile.get("gender"));
        try {
            String dateOfBirth = (String) oktaUserProfile.get("birthdate");
            if (dateOfBirth != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                userProfile.setDateOfBirth (dateFormat.parse(dateOfBirth));
            }
        } catch (ParseException e) {

        }
        return userProfile;
    }

    /**
     *
     * @param userProfile
     * @param currentUser
     */
    private void toOktaUserProfile(UserProfile userProfile, User currentUser) {
        com.okta.sdk.resource.user.UserProfile oktaUserProfile = currentUser.getProfile();
        oktaUserProfile
                .setFirstName(userProfile.getFirstName())
                .setLastName(userProfile.getLastName())
                .setEmail(userProfile.getEmail())
                .setMobilePhone(userProfile.getMobilePhone())
                .setStreetAddress(userProfile.getStreetAddress())
                .setCity(userProfile.getCity())
                .setState(userProfile.getState())
                .setZipCode(userProfile.getZipCode())
                .setCountryCode(userProfile.getCountryCode());
        // these are custom
        oktaUserProfile.put("gender", userProfile.getGender());
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String dateOfBirth = dateFormat.format(userProfile.getDateOfBirth());
        oktaUserProfile.put("birthdate", dateOfBirth);
    }

    protected Client getClient() {
        return Clients.builder()
                .setOrgUrl(oktaServiceBase)
                .setClientCredentials(new TokenClientCredentials(api_token))
                .build();
    }

    /**
     * Gets user profile id (an alphanumeric string) corresponding to the login
     * @param login log in Okta system - i.e. email address which was used to create an account, separate from primary email
     * @return
     */
    public String getProfileByLoginId(String login) {
        String filter = "profile.login eq \"" + login + "\"";
        Client client = getClient();
        UserList users = client.listUsers(null, filter, null, null, null);
        Iterator<User> iterator = users.iterator();
        Collection<UserProfile> userProfiles = new ArrayList<>();
        String profileId = null;
        while (iterator.hasNext()) {
            User oktaUser = iterator.next();
            profileId = oktaUser.getId();
            break;
        }
        return profileId;
    }
}