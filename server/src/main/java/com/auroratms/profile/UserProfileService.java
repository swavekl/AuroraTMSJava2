package com.auroratms.profile;

import com.google.gson.*;
import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;
import com.okta.sdk.impl.resource.DefaultUserBuilder;
import com.okta.sdk.resource.group.Group;
import com.okta.sdk.resource.group.GroupList;
import com.okta.sdk.resource.user.User;
import com.okta.sdk.resource.user.UserList;
import com.okta.sdk.resource.user.UserStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@CacheConfig(cacheNames = {"profiles"})
@Transactional
public class UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);
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

    @CachePut(key = "#result.userId")
    public UserProfile createProfile(UserProfile userProfile) {
        Client client = getClient();

        String password = userProfile.getFirstName() + "1234$";
        Map<String, Object> properties = new HashMap<>();
        properties.put("state", userProfile.getState());
        properties.put("zipCode", userProfile.getZipCode());
        properties.put("countryCode", userProfile.getCountryCode() != null ? userProfile.getCountryCode() : "US");
        properties.put("gender", userProfile.getGender() != null ? userProfile.getGender() : "M");
        Date dateOfBirth = userProfile.getDateOfBirth();
        if (dateOfBirth != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            properties.put("birthdate", dateFormat.format(dateOfBirth));
        }

        User user = new DefaultUserBuilder()
                .setFirstName(userProfile.getFirstName())
                .setLastName(userProfile.getLastName())
                .setEmail(userProfile.getEmail())
                .setLogin(userProfile.getLogin())
                .setPassword(password.toCharArray())
                .setSecurityQuestion("What is the food you least liked as a child?")
                .setSecurityQuestionAnswer("spinach")
                .setProfileProperties(properties)
                .buildAndCreate(client);

        return fromOktaUser(user);
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
     * Gets specified profiles in bulk
     * @param profileIds
     * @return
     */
    public Collection<UserProfile> listByProfileIds (List<String> profileIds) {
        Client client = getClient();
        List<UserProfile> profileList = new ArrayList<>(profileIds.size());
        if (profileIds.size() > 0) {
            int batchSize = 20;
            int fromIndex = 0;
            int toIndex = fromIndex + batchSize;
            toIndex = Math.min(toIndex, profileIds.size());
            while(toIndex <= profileIds.size()) {
                List<String> profileIdsBatch = profileIds.subList(fromIndex, toIndex);
                String filter = "";
                for (String profileId : profileIdsBatch) {
                    filter += (filter.length() > 0) ? " or " : "";
                    filter += String.format("(id eq \"%s\")", profileId);
                }
                UserList users = client.listUsers(null, filter, null, null, null);
                Collection<UserProfile> userProfilesBatch = toUserProfileList(users);
                profileList.addAll(userProfilesBatch);

                fromIndex = toIndex;
                if (toIndex == profileIds.size()) {
                    break;
                }
                toIndex = fromIndex + batchSize;
                toIndex = Math.min(toIndex, profileIds.size());
            }
        }

        return profileList;
    }

    /**
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
     * Paged query with filtering
     * @param limit
     * @param after
     * @param lastName
     * @return
     */
    public Map<String, Object> listPaged(int limit, String after, String lastName) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            String query = "?limit=" + limit;
            if (after != null) {
                query += "&after=" + after;
            }
            if (lastName != null) {
                String encodedLastName = lastName.replaceAll(" ", "%20");
                query += "&filter=profile.lastName%20eq%20%22" + encodedLastName +"%22";
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

            String path = oktaServiceBase + "/api/v1/users" + query;
//            System.out.println("path = " + path);
            Map<String, String> internalResponseMap = makeGetRequestInternal(path);
            String profiles = internalResponseMap.get("profiles");
            String nextAfter = internalResponseMap.get("after");
            JsonElement parser = JsonParser.parseString(profiles);
            JsonArray jsonArray = parser.getAsJsonArray();
            Collection<UserProfile> userProfileCollection = new ArrayList<>(limit);
            responseMap.put("profiles", userProfileCollection);
            responseMap.put("after", nextAfter);

            jsonArray.forEach(jsonElement -> {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject != null) {
                    UserProfile userProfile = new UserProfile();
                    userProfileCollection.add(userProfile);
                    userProfile.setUserId(getProfileValue(jsonObject, "id"));
                    userProfile.setUserStatus(getProfileValue(jsonObject, "status"));
                    JsonObject profileObject = jsonObject.getAsJsonObject("profile");
                    if (profileObject != null && !profileObject.isJsonNull()) {
                        userProfile.setFirstName(getProfileValue(profileObject, "firstName"));
                        userProfile.setLastName(getProfileValue(profileObject, "lastName"));
                        userProfile.setZipCode(getProfileValue(profileObject, "zipCode"));
                        userProfile.setMobilePhone(getProfileValue(profileObject, "mobilePhone"));
                        userProfile.setState(getProfileValue(profileObject, "state"));
                        userProfile.setCountryCode(getProfileValue(profileObject, "countryCode"));
                        userProfile.setEmail(getProfileValue(profileObject, "login"));
                        try {
                            String dateOfBirth = getProfileValue(profileObject, "birthdate");
                            if (dateOfBirth != null) {
                                userProfile.setDateOfBirth(dateFormat.parse(dateOfBirth));
                            }
                        } catch (ParseException e) {
                            // ignore
                        }
                    }
                }
            });
            // if this is the query for the first page
            if (after == null) {
                Client client = getClient();
                long usersCount = 0;
                if (lastName == null) {
                    // get the count of users in Everyone group
                    GroupList groups = client.listGroups("Everyone", null, "stats");
                    for (Group group : groups) {
                        Map<String, Object> embedded = group.getEmbedded();
                        if (embedded != null) {
                            Map<String, Object> stats = (Map<String, Object>) embedded.get("stats");
                            if (stats != null) {
                                usersCount = Long.valueOf(stats.get("usersCount").toString());
                                break;
                            }
                        }
                    }
                } else {
                    // if filter is present on the first call
                    if (userProfileCollection.size() < limit) {
                        // no need to requery if fewer then the list size were returned
                        usersCount = userProfileCollection.size();
                    } else {
                        // then get up to 200 users - should be enough for most of the cases
                        String filter = "profile.lastName eq \"" + lastName + "\"";
                        UserList users = client.listUsers(null, filter, null, null, null);
                        usersCount = users.stream().count();
                    }
                }
                responseMap.put("usersCount", usersCount);
            }

        } catch (Exception e) {
            log.error("Error getting list of users", e);
            throw new RuntimeException("Error retrieving list of user profiles", e);
        }

        return responseMap;
    }

    private String getProfileValue(JsonObject profileObject, String name) {
        JsonElement profileFieldJE = profileObject.get(name);
        return (profileFieldJE != null && !profileFieldJE.isJsonNull()) ? profileFieldJE.getAsString() : null;
    }

    private String getAuthorizationHeaderValue() {
        return "SSWS " + api_token;
    }

    private Map<String, String> makeGetRequestInternal(String urlString) throws IOException {
        URL url = new URL(urlString);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", getAuthorizationHeaderValue());

        if (conn.getResponseCode() != 200) {
            throwException(conn);
        }
        // read response into a string
        BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));
        String output;
        StringBuilder result = new StringBuilder();
        while ((output = br.readLine()) != null) {
            result.append(output);
        }

        // retrieve opaque cursor for getting next page
        String after = null;
        String linkHeaderValue = conn.getHeaderField("link");
        if (StringUtils.isNotEmpty(linkHeaderValue)) {
            // [<https://dev-758120.oktapreview.com/api/v1/users?after=000uy334yosnwut9j30h7&limit=10>; rel="next", <https://dev-758120.oktapreview.com/api/v1/users?limit=10>; rel="self"]
            String[] links = linkHeaderValue.split(",");
            for (String link : links) {
                if (link.endsWith("rel=\"next\"")) {
                    int start = link.indexOf("after=") + "after=".length();
                    int end = link.indexOf("&", start);
                    after = link.substring(start, end);
                    break;
                }
            }
        }
        conn.disconnect();

        Map<String, String> map = new HashMap<>();
        map.put("profiles", result.toString());
        map.put("after", after);
        return map;
    }

    /**
     *
     * @param conn
     * @throws IOException
     */
    private void throwException(HttpURLConnection conn) throws IOException {
        String responseMessage = conn.getResponseMessage();
        InputStream errorStream = conn.getErrorStream();

        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (errorStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        conn.disconnect();

        String error = textBuilder.toString();
        error = extractErrorDetails(error);

        throw new RuntimeException(error);
    }

    /**
     * Extrancts more informative error details
     * @param fullError
     * @return
     */
    private String extractErrorDetails (String fullError) {
        String error = fullError;
        try {
//            System.out.println("error = " + fullError);
            JsonElement parser = JsonParser.parseString(fullError);
            JsonObject errorJsonObject = parser.getAsJsonObject();
            if (errorJsonObject != null) {
                Object errorSummary = errorJsonObject.get("errorSummary");
                Object errorDescription = errorJsonObject.get("error_description");
                if (errorSummary != null) {
                    error = errorSummary.toString();
                } else if (errorDescription != null) {
                    error = errorDescription.toString();
                }
                // check if there is more detailed error summary
                JsonArray errorCauses = errorJsonObject.getAsJsonArray("errorCauses");
                if (errorCauses != null) {
                    for (int i = 0; i < errorCauses.size(); i++) {
                        JsonElement jsonElement = errorCauses.get(i);
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        if (jsonObject != null) {
                            JsonElement errorSummaryJsonElement = jsonObject.get("errorSummary");
                            if (errorSummaryJsonElement != null) {
                                error = errorSummaryJsonElement.toString();
                            }
                        }
                    }
                }
            }

            // remove starting and ending double quotes
            if (error != null && error.startsWith("\"")) {
                error = error.substring(1);
            }
            if (error != null && error.endsWith("\"")) {
                error = error.substring(0, error.length() - 1);
            }
        } catch (JsonIOException | JsonSyntaxException e) {
            System.out.println("error parsing error message: " + e);
        }
        return error;
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
        userProfile.setDivision(oktaUserProfile.getDivision());
        UserStatus userStatus = user.getStatus();
        userProfile.setUserStatus(userStatus.toString());

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
                .setCountryCode(userProfile.getCountryCode())
                .setDivision(userProfile.getDivision());
        // these are custom
        oktaUserProfile.put("gender", userProfile.getGender());
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String dateOfBirth = dateFormat.format(userProfile.getDateOfBirth());
        dateOfBirth = dateOfBirth.substring(0, dateOfBirth.lastIndexOf("T")) + "T00:00:00.000+0000";
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
        String profileId = null;
        if (iterator.hasNext()) {
            User oktaUser = iterator.next();
            profileId = oktaUser.getId();
        }
        return profileId;
    }

    /**
     * Gets user profile by login id i.e. email address
     * @param login
     * @return
     */
    public UserProfile getUserProfileForLoginId(String login) {
        String filter = "profile.login eq \"" + login + "\"";
        Client client = getClient();
        UserList users = client.listUsers(null, filter, null, null, null);
        Iterator<User> iterator = users.iterator();
        UserProfile userProfile = null;
        if (iterator.hasNext()) {
            User oktaUser = iterator.next();
            userProfile = fromOktaUser(oktaUser);
        }
        return userProfile;
    }

    /**
     * Lists users in particular group and division
     * @param groupName group name
     * @param division optional division
     * @return
     */
    public List<UserProfile> listUserInRole(String groupName, String division) {
        List<UserProfile> userProfileList = new ArrayList<>();
        Client client = getClient();
        GroupList groupList = client.listGroups(groupName, null, null);
        for (Group group : groupList) {
//            System.out.println("group name: " + group.getProfile().getName());
            UserList userList = group.listUsers();
            for (User user : userList) {
                com.okta.sdk.resource.user.UserProfile oktaUserProfile = user.getProfile();
//                System.out.println("okta user: " + oktaUserProfile.getFirstName() + " " + oktaUserProfile.getLastName() + ", division: " + oktaUserProfile.getDepartment() + ", division: " + oktaUserProfile.getDivision());
                if (division == null || oktaUserProfile.getDivision().contains(division)) {
                    UserProfile userProfile = fromOktaUser(user);
                    userProfileList.add(userProfile);
                }
            }
        }
        return userProfileList;
    }
//
//    /**
//     *
//     * @param groupName
//     * @param profileId
//     */
//    public void addUserToGroup(String groupName, String profileId) {
//        Client client = getClient();
//        User user = client.getUser(profileId);
//        boolean memberOfGroup = false;
//        GroupList groups = user.listGroups();
//        for (Group group : groups) {
//            if (group.getId().equals(groupName)) {
//                memberOfGroup = true;
//                break;
//            }
//        }
//
//        if (!memberOfGroup) {
//            com.okta.sdk.resource.user.UserProfile profile = user.getProfile();
//            System.out.println("Adding user " + profile.getFirstName() + " " + profile.getLastName() + " to group " + groupName);
//            user.addToGroup(groupName);
//            boolean addedSuccessfully = false;
//            groups = user.listGroups();
//            for (Group group : groups) {
//                if (group.getId().equals(groupName)) {
//                    addedSuccessfully = true;
//                    break;
//                }
//            }
//            if (addedSuccessfully) {
//                System.out.println("Successfully added member to group");
//            }
//        }
//    }

    /**
     *
     * @param profileId
     * @return
     */
    public List<String> getUserGroups(String profileId) {
        List<String> groups = new ArrayList<>();
        Client client = getClient();
        User user = client.getUser(profileId);
        GroupList groups1 = user.listGroups();
        for (Group group : groups1) {
            String name = group.getProfile().getName();
            groups.add(name);
        }

        return groups;
    }

    /**
     *
     * @param profileId
     * @param updatedGroups
     */
    public void updateUserGroups(String profileId, List<String> updatedGroups) {
        Client client = getClient();
        User user = client.getUser(profileId);

        // remove group
//        GroupList currentGroupList = user.listGroups();
//        for (Group group : currentGroupList) {
//            String groupName = group.getProfile().getName();
//            if (!updatedGroups.contains(groupName)) {
//                user.removeRole(group.getId());
//            }
//        }

        // add to groups
        GroupList userGroupList = user.listGroups();
        for (String updatedGroup : updatedGroups) {
            // check if already member of this group
            String groupToAddId = null;
            for (Group group : userGroupList) {
                String groupName = group.getProfile().getName();
                if (groupName.equals(updatedGroup)) {
                    groupToAddId = group.getId();
                    break;
                }
            }

            // not a member then add
            if (groupToAddId != null) {
                user.addToGroup(groupToAddId);
            }
        }
    }
}
