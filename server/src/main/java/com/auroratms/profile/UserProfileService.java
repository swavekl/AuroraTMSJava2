package com.auroratms.profile;

import com.auroratms.users.UserRoles;
import com.google.gson.*;
import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.client.Clients;
import com.okta.sdk.helper.PaginationUtil;
import com.okta.sdk.impl.resource.DefaultUserBuilder;
import com.okta.sdk.resource.api.*;
import com.okta.sdk.resource.client.ApiClient;
import com.okta.sdk.resource.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


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

    private static final String APPLICATION_JSON = "application/json";

    @Autowired
    private SidService sidService;

    /**
     *
     * @param userId
     * @return
     */
    @Cacheable(key = "#userId")
    public UserProfile getProfile (String userId) {
        UserApi userApi = getUserApi();
        UserGetSingleton userGetSingleton = userApi.getUser(userId, APPLICATION_JSON, "true");
        return fromOktaUser (userId, userGetSingleton.getProfile(), userGetSingleton.getStatus());
    }

    /**
     * Updates user profile
     * @param userProfile
     */
    @CachePut(key = "#result.userId")
    public UserProfile updateProfile (UserProfile userProfile) {
        // get current user
        UserApi userApi = getUserApi();
        UserGetSingleton userGetSingleton = userApi.getUser(userProfile.getUserId(), APPLICATION_JSON, "true");
        // update the profile
        com.okta.sdk.resource.model.UserProfile oktaUserProfile = toOktaUserProfile(userProfile, userGetSingleton);

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setProfile(oktaUserProfile);
        User updatedOktaUser = userApi.updateUser(userProfile.getUserId(), updateUserRequest, true);

        // user changed email - update it in the acl_sid table
        if (!userProfile.getEmail().equals(userProfile.getLogin())) {
            this.sidService.updateSid(userProfile.getLogin(), userProfile.getEmail());
        }
        return fromOktaUser (userProfile.getUserId(), updatedOktaUser.getProfile(), updatedOktaUser.getStatus());
    }

    /**
     * Creates a profile
     * @param userProfile
     * @return
     */
    @CachePut(key = "#result.userId")
    public UserProfile createProfile(UserProfile userProfile) {
        DefaultUserBuilder defaultUserBuilder = new DefaultUserBuilder();
        defaultUserBuilder
                .setFirstName(userProfile.getFirstName())
                .setLastName(userProfile.getLastName())
                .setEmail(userProfile.getEmail())
                .setLogin(userProfile.getLogin())
                .setSecurityQuestion("What is the food you least liked as a child?")
                .setSecurityQuestionAnswer("spinach");

        String password = (userProfile.isMakeDefaultPassword()) ? "Secret1234$" : userProfile.getFirstName() + "1234$";
        defaultUserBuilder.setPassword(password.toCharArray());
        Map<String, Object> properties = new HashMap<>();
        defaultUserBuilder.setCustomProfileProperty("state", userProfile.getState());
        defaultUserBuilder.setCustomProfileProperty("zipCode", userProfile.getZipCode());
        defaultUserBuilder.setCustomProfileProperty("countryCode", userProfile.getCountryCode() != null ? userProfile.getCountryCode() : "US");
        defaultUserBuilder.setCustomProfileProperty("gender", userProfile.getGender() != null ? userProfile.getGender() : "M");
        Date dateOfBirth = userProfile.getDateOfBirth();
        if (dateOfBirth != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            defaultUserBuilder.setCustomProfileProperty("birthdate", dateFormat.format(dateOfBirth));
        }

        UserApi userApi = getUserApi();
        User user = defaultUserBuilder.buildAndCreate(userApi);

        return fromOktaUser(user.getId(), user.getProfile(), user.getStatus());
    }

    /**
     *
     * @param userId
     */
    @CacheEvict(key = "#userId")
    public void deleteProfile (String userId) {
        UserApi userApi = getUserApi();
        userApi.deleteUser(userId, false, null);
    }

    /**
     *
     * @return
     */
    public Collection<UserProfile> list() {
        Collection<UserProfile> userProfiles = new ArrayList<>();

        UserApi userApi = getUserApi();
        String after = null;
        do {
            List<User> users = userApi.listUsers(APPLICATION_JSON,
                    null, after, 200, null, null, null, null);
            users.forEach(user -> userProfiles.add(fromOktaUser(user.getId(), user.getProfile(), user.getStatus())));

            after = PaginationUtil.getAfter(userApi.getApiClient());
        } while (StringUtils.isNotBlank(after));

        return userProfiles;
    }

    /**
     * Lists users by state
     * @param statesList
     * @return
     */
    public Collection<UserProfile> listByStates (List<String> statesList) {
        UserApi userApi = getUserApi();
        List<UserProfile> profileList = new ArrayList<>();
        if (!statesList.isEmpty()) {
            int batchSize = 10;
            int fromIndex = 0;              
            int toIndex = fromIndex + batchSize;
            toIndex = Math.min(toIndex, statesList.size());
            while(true) {
                List<String> statesBatch = statesList.subList(fromIndex, toIndex);
                StringBuilder search = new StringBuilder();
                for (String state : statesBatch) {
                    search.append((search.length() > 0) ? " or " : "");
                    search.append("(profile.state eq \"%s\")".formatted(state));
                }
                String after = null;
                do {
                    List<User> users = userApi.listUsers(APPLICATION_JSON,
                            null, after, 200, null, search.toString(), null, null);
                    users.forEach(user -> profileList.add(fromOktaUser(user.getId(), user.getProfile(), user.getStatus())));

                    after = PaginationUtil.getAfter(userApi.getApiClient());
                } while (StringUtils.isNotBlank(after));

                fromIndex = toIndex;
                if (toIndex == statesList.size()) {
                    break;
                }
                toIndex = fromIndex + batchSize;
                toIndex = Math.min(toIndex, statesList.size());
            }
        }
        return profileList;
    }

    /**
     * Gets specified profiles in bulk
     * @param profileIds
     * @return
     */
    public Collection<UserProfile> listByProfileIds (List<String> profileIds) {
        UserApi userApi = getUserApi();
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
                    filter += "(id eq \"%s\")".formatted(profileId);
                }
                List<User> users = userApi.listUsers(APPLICATION_JSON,
                        null, null, 20, filter, null, null, null);
                users.forEach(user -> profileList.add(fromOktaUser(user.getId(), user.getProfile(), user.getStatus())));

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
        UserApi userApi = getUserApi();
        List<User> users = userApi.listUsers(APPLICATION_JSON,
                null, null, 200, filter, null, null, null);
        List<UserProfile> profileList = new ArrayList<>(users.size());
        users.forEach(user -> profileList.add(fromOktaUser(user.getId(), user.getProfile(), user.getStatus())));

        // convert all users to user profiles
        return profileList;
    }

    /**
     * Paged query with filtering
     *
     * @param limit
     * @param after
     * @param lastName
     * @param status
     * @return
     */
    public Map<String, Object> listPaged(int limit, String after, String lastName, String status) {
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
            if (StringUtils.isNotEmpty(status)) {
                query += (lastName == null) ? "&filter=" : "%20and%20";
                String encodedStatus = status.replaceAll(" ", "%20");
                query += "status%20eq%20%22" + encodedStatus + "%22";
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
                        userProfile.setGender(getProfileValue(profileObject, "gender"));
                        userProfile.setZipCode(getProfileValue(profileObject, "zipCode"));
                        userProfile.setMobilePhone(getProfileValue(profileObject, "mobilePhone"));
                        userProfile.setState(getProfileValue(profileObject, "state"));
                        userProfile.setCountryCode(getProfileValue(profileObject, "countryCode"));
                        userProfile.setLogin(getProfileValue(profileObject, "login"));
                        userProfile.setEmail(getProfileValue(profileObject, "email"));
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
                long usersCount = 0;
                if (lastName == null && StringUtils.isEmpty(status)) {
                    // get the count of users in Everyone group
                    GroupApi groupApi = new GroupApi(getClient());
                    List<Group> groups = groupApi.listGroups(UserRoles.Everyone, null, null, 1, "stats", null, null, null);
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
                        String filter = "";
                        if (lastName != null) {
                            filter = "profile.lastName eq \"" + lastName + "\"";
                        }
                        if (StringUtils.isNotEmpty(status)) {
                            filter += (filter.isEmpty()) ? "" : " and ";
                            filter += "status eq \"" + status + "\"";
                        }
                        UserApi userApi = getUserApi();
                        List<User> users = userApi.listUsers(APPLICATION_JSON,
                                null, null, 200, filter, null, null, null);
                        usersCount = users.size();
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
        conn.setRequestProperty("Accept", APPLICATION_JSON);
        conn.setRequestProperty("Content-Type", APPLICATION_JSON);
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
     * Converts from Okta user to user profile
     *
     * @return
     */
    private UserProfile fromOktaUser(String userId, com.okta.sdk.resource.model.UserProfile oktaUserProfile, UserStatus userStatus) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
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
        userProfile.setGender((String) oktaUserProfile.additionalProperties.get("gender"));
        try {
            String dateOfBirth = (String) oktaUserProfile.additionalProperties.get("birthdate");
            if (dateOfBirth != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                userProfile.setDateOfBirth (dateFormat.parse(dateOfBirth));
            }
        } catch (ParseException e) {

        }
        userProfile.setDivision(oktaUserProfile.getDivision());
        userProfile.setUserStatus(userStatus.toString());

        return userProfile;
    }

    /**
     * @param userProfile
     * @param userGetSingleton
     * @return
     */
    private com.okta.sdk.resource.model.UserProfile toOktaUserProfile(UserProfile userProfile, UserGetSingleton userGetSingleton) {
        com.okta.sdk.resource.model.UserProfile oktaUserProfile = userGetSingleton.getProfile();
        oktaUserProfile
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .email(userProfile.getEmail())
                .mobilePhone(userProfile.getMobilePhone())
                .streetAddress(userProfile.getStreetAddress())
                .city(userProfile.getCity())
                .state(userProfile.getState())
                .zipCode(userProfile.getZipCode())
                .countryCode(userProfile.getCountryCode())
                .division(userProfile.getDivision());
        // these are custom
        oktaUserProfile.getAdditionalProperties().put("gender", userProfile.getGender());
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String dateOfBirth = dateFormat.format(userProfile.getDateOfBirth());
        dateOfBirth = dateOfBirth.substring(0, dateOfBirth.lastIndexOf("T")) + "T00:00:00.000+0000";
        oktaUserProfile.getAdditionalProperties().put("birthdate", dateOfBirth);
        return oktaUserProfile;
    }

    protected ApiClient getClient() {
        return Clients.builder()
                .setOrgUrl(oktaServiceBase)
                .setClientCredentials(new TokenClientCredentials(api_token))
                .build();
    }

    protected UserApi getUserApi() {
        return new UserApi(getClient());
    }

    /**
     * Gets user profile id (an alphanumeric string) corresponding to the login
     * @param login log in Okta system - i.e. email address which was used to create an account, separate from primary email
     * @return
     */
    public String getProfileByLoginId(String login) {
        String filter = "profile.login eq \"" + login + "\"";
        UserApi userApi = getUserApi();
        List<User> users = userApi.listUsers(APPLICATION_JSON,
                null, null, 10, filter, null, null, null);
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
        UserApi userApi = getUserApi();
        List<User> users = userApi.listUsers(APPLICATION_JSON,
                null, null, 10, filter, null, null, null);
        Iterator<User> iterator = users.iterator();
        UserProfile userProfile = null;
        if (iterator.hasNext()) {
            User oktaUser = iterator.next();
            userProfile = fromOktaUser(oktaUser.getId(), oktaUser.getProfile(), oktaUser.getStatus());
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

        GroupApi groupApi = new GroupApi(getClient());
        String after = null;
        do {
            List<User> groupUsers = groupApi.listGroupUsers(groupName, after, null);
            for (User user : groupUsers) {
                String userDivision = user.getProfile().getDivision();
                if (division == null || (userDivision != null && userDivision.contains(division))) {
                    userProfileList.add(fromOktaUser(user.getId(), user.getProfile(), user.getStatus()));
                }
            }

            after = PaginationUtil.getAfter(groupApi.getApiClient());
        } while (StringUtils.isNotBlank(after));

        return userProfileList;
    }

    /**
     * Gets user roles this user has
     * @param profileId od of a user
     * @return
     */
    public List<String> getUserRoles(String profileId) {
        List<String> userRoles = new ArrayList<>();

        UserResourcesApi userResourcesApi = new UserResourcesApi(getClient());
        List<Group> groups = userResourcesApi.listUserGroups(profileId);
        for (Group group : groups) {
            userRoles.add(group.getProfile().getName());
        }

        return userRoles;
    }

    /**
     * Updates user roles this user has
     * @param profileId profile id of a user
     * @param updatedRoles new list of roles
     */
    public void updateUserRoles(String profileId, List<String> updatedRoles) {
        if (!updatedRoles.contains(UserRoles.Everyone)) {
            updatedRoles.add(UserRoles.Everyone);
        }

        // Get currently assigned user roles (groups)
        UserResourcesApi userResourcesApi = new UserResourcesApi(getClient());
        List<Group> groups = userResourcesApi.listUserGroups(profileId);
        List<String> existingRoles = new ArrayList<>();
        for (Group group : groups) {
            existingRoles.add(group.getProfile().getName());
        }
        List<String> addedRoles = updatedRoles.stream()
                .filter(groupName -> !existingRoles.contains(groupName))
                .toList();
        List<String> removedRoles = existingRoles.stream()
                .filter(groupName -> !updatedRoles.contains(groupName))
                .toList();

        GroupApi groupApi = new GroupApi(getClient());
        List<Group> allGroups = groupApi.listGroups(null, null, null, 50, null, null, null, null);

        // add new roles
        for (String addedRole : addedRoles) {
            for (Group group : allGroups) {
                if (group.getProfile().getName().equals(addedRole)) {
                    groupApi.assignUserToGroup(group.getId(), profileId);
                    break;
                }
            }
        }

        // remove user from unwanted roles
        for (String removedRole : removedRoles) {
            for (Group group : allGroups) {
                if (group.getProfile().getName().equals(removedRole)) {
                    groupApi.unassignUserFromGroup(group.getId(), profileId);
                    break;
                }
            }
        }
    }
}
