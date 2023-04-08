package com.auroratms.users;

import com.auroratms.AbstractOktaController;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.okta.sdk.resource.user.ForgotPasswordResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("api/users")
public class UsersController extends AbstractOktaController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    @Autowired
    private UserProfileExtService userProfileExtService;

    /**
     * @param userRegistration
     * @return
     */
    @PostMapping("/register")
    @ResponseBody
    public
    @PreAuthorize("permitAll()")
    ResponseEntity<String> registerUser(@RequestBody UserRegistration userRegistration) {
        try {
            logger.info("Registering new user " + userRegistration.getLastName() + ", " + userRegistration.getFirstName() + " email: " + userRegistration.getEmail());
            UUID registrationToken = UUID.randomUUID();

            String userId = createUser(userRegistration, registrationToken.toString());

            String id = activateOktaUser(userId);
            logger.info("Activated user " + userId);

            suspendUser(userId);
            logger.info("Temporarily suspended user " + userId + " until email is validated");
        } catch (Exception e) {
            String message = e.getMessage();
            message = message.substring(message.indexOf("{"));
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<String>("{\"status\":\"SUCCESS\"}", HttpStatus.OK);
    }


    /**
     * @param userRegistration
     * @param registrationToken
     * @return
     */
    private String createUser(@RequestBody UserRegistration userRegistration, String registrationToken) throws IOException {
        String userId = null;
            String strRegisterUserURL = oktaServiceBase + "/api/v1/users?activate=false";

            String secondEmail = registrationToken + "@gateway-pc.com";
//            System.out.println("secondEmail = " + secondEmail);

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode profileObjectNode = objectMapper.createObjectNode();
            profileObjectNode.put("firstName", userRegistration.getFirstName());
            profileObjectNode.put("lastName", userRegistration.getLastName());
            profileObjectNode.put("email", userRegistration.getEmail());
            profileObjectNode.put("login", userRegistration.getEmail());
            profileObjectNode.put("secondEmail", secondEmail);
            // gender and birth date must be filled but we didn't ask for them
            profileObjectNode.put("gender", "Male");
            SimpleDateFormat dateFormat = new SimpleDateFormat(UserProfileService.DATE_FORMAT);
            String dateOfBirth = dateFormat.format(new Date());
            profileObjectNode.put("birthdate", dateOfBirth);
            ObjectNode valueObjectNode = objectMapper.createObjectNode();
            valueObjectNode.put("value", userRegistration.getPassword());
//            ObjectNode passwordObjectNode = objectMapper.createObjectNode();
//            passwordObjectNode.set("password", valueObjectNode);

            ObjectNode recoveryQuestionObjectNode = objectMapper.createObjectNode();
            recoveryQuestionObjectNode.put("question", "What is the food you least liked as a child?");
            recoveryQuestionObjectNode.put("answer", "spinach");
            ObjectNode credentialsObjectNode = objectMapper.createObjectNode();
            credentialsObjectNode.set("password", valueObjectNode);
            credentialsObjectNode.set("recovery_question", recoveryQuestionObjectNode);

            ObjectNode topLevelObjectNode = objectMapper.createObjectNode();
            topLevelObjectNode.set("profile", profileObjectNode);
            topLevelObjectNode.set("credentials", credentialsObjectNode);
            /*
             "credentials": {
    "password" : { "value": "tlpWENT2m" },
    "recovery_question": {
      "question": "Who'\''s a major player in the cowboy scene?",
      "answer": "Annie Oakley"
    }

    "recovery_question" : {
        "question" : "What is the food you least liked as a child?"
      }
             */
            String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(topLevelObjectNode);
//            System.out.println("requestBody = " + requestBody);
            String result = makePostRequest(strRegisterUserURL, requestBody);

            Map<String, Object> jsonMap = objectMapper.readValue(result,
                    new TypeReference<Map<String, Object>>() {
                    });
            userId = jsonMap.get("id").toString();
            logger.info("Created user with userId = " + userId);
        return userId;
    }

    /**
     * @param userRegistration
     * @return
     */
    @PostMapping("/validateEmail")
    public @ResponseBody
    @PreAuthorize("permitAll()")
    ResponseEntity<String> validateEmail(@RequestBody UserRegistration userRegistration) {
        try {
            logger.info("Validating email " + userRegistration.getEmail());
            // get user profile which contains user id and token
            String userProfile = getUser(userRegistration.getEmail());
            String token = userRegistration.getSecondEmail();

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap = objectMapper.readValue(userProfile,
                    new TypeReference<Map<String, Object>>() {
                    });
            String userId = (String) jsonMap.get("id");
            Object profile = jsonMap.get("profile");
            String tokenFromURL = "";
            if (profile != null) {
                tokenFromURL = (String) ((Map<String, Object> )profile).get("secondEmail");
            }
            logger.info("Got token from url: " + tokenFromURL + " vs token from email: " + token);

            // check that the activation token is OK and only then unsuspend
            if (token.equals(tokenFromURL)) {
                logger.info("Unsuspending user with userId " + userId);
                String status = unsuspendUser(userId);
                String firstName = (String) ((Map<String, Object>) profile).get("firstName");
                String lastName = (String) ((Map<String, Object>) profile).get("lastName");
                logger.info("Email " + userRegistration.getEmail() + " validated successfully for user " + userId + " named " + lastName + ", " + firstName + " status is now: " + status);
            } else {
                return new ResponseEntity<String>("{\"status\":\"Activation failed\"}", HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            return new ResponseEntity<String>("{\"status\":\"Activation failed\"}", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<String>("{\"status\":\"Activation successful\"}", HttpStatus.OK);
    }

    /**
     * @param email
     * @return
     * @throws IOException
     */
    private String getUser(String email) throws IOException {
        String lookupUserIdUrl = oktaServiceBase + "/api/v1/users/" + email;
        return makeGetRequest(lookupUserIdUrl);
    }

    private String activateOktaUser(String userId) throws IOException {
        // make a second call to actually activate
        String strActivateUrl = oktaServiceBase + "/api/v1/users/" + userId + "/lifecycle/activate";

        return makePostRequest(strActivateUrl, null);
    }

    private void suspendUser(String userId) throws IOException {
        // POST /api/v1/users/${userId}/lifecycle/suspend
        String url = oktaServiceBase + "/api/v1/users/" + userId + "/lifecycle/suspend";
        makePostRequest(url, null);
    }

    private String unsuspendUser(String userId) throws IOException {
        // POST /api/v1/users/${userId}/lifecycle/unsuspend
        String url = oktaServiceBase + "/api/v1/users/" + userId + "/lifecycle/unsuspend";
        String response = makePostRequest(url, null);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(response,
                new TypeReference<Map<String, Object>>() {
                });
        String prettyPrintResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        System.out.println("POST unsuspendUser = " + prettyPrintResult);
        return "status";
    }

    /**
     * Normal login
     * @param userRegistration
     * @return
     */
    @PostMapping("/login")
    @ResponseBody
    public
    @PreAuthorize("permitAll()")
    ResponseEntity<String> login(@RequestBody UserRegistration userRegistration) {
        try {
            if (StringUtils.isNotEmpty(userRegistration.getEmail()) &&
                    StringUtils.isNotEmpty(userRegistration.getPassword())) {
                // authenticate
                String requestBody = "grant_type=password"
                        + "&username=" + URLEncoder.encode(userRegistration.getEmail(), StandardCharsets.UTF_8.name())
                        + "&password=" + URLEncoder.encode(userRegistration.getPassword(), StandardCharsets.UTF_8.name())
                        + "&client_id=" + URLEncoder.encode(this.clientId, StandardCharsets.UTF_8.name())
                        + "&scope=" + URLEncoder.encode("openid offline_access", StandardCharsets.UTF_8.name());
                String url = oktaServiceBase + "/oauth2/default/v1/token";
                String loginResponse = makePostRequest(url, requestBody, "application/x-www-form-urlencoded", null);
                // get user profile so we have user profile id and basic user information
                String combinedResponse = fetchUser(loginResponse, userRegistration.getEmail());
                return new ResponseEntity<String>(combinedResponse, HttpStatus.OK);
            } else {
                return new ResponseEntity<String>("{\"message\": \"Login failed\"}", HttpStatus.UNAUTHORIZED);
            }
        } catch (IOException e) {
            return new ResponseEntity<String>("{\"message\": \"Login failed\"}", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Quiet login when access token expires
     *
     * @param parameters
     * @return
     */
    @PostMapping("/loginquiet")
    @ResponseBody
    public
    @PreAuthorize("permitAll()")
    ResponseEntity<String> loginquiet(@RequestBody Map<String, String> parameters) {
        try {
            // authenticate using refresh token
            String refreshToken = parameters.get("refreshToken");
            String requestBody = "grant_type=refresh_token"
                    + "&scope=" + URLEncoder.encode("openid offline_access", StandardCharsets.UTF_8.name())
                    + "&client_id=" + URLEncoder.encode(this.clientId, StandardCharsets.UTF_8.name())
                    + "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8.name());
            String url = oktaServiceBase + "/oauth2/default/v1/token";
            String loginResponse = makePostRequest(url, requestBody, "application/x-www-form-urlencoded", null);

            String email = parameters.get("email");
            String combinedResponse = fetchUser(loginResponse, email);
//return new ResponseEntity<String>("{\"message\": \"Quiet login failed\"}", HttpStatus.BAD_REQUEST);
            return new ResponseEntity<String>(combinedResponse, HttpStatus.OK);
        } catch (Exception e) {
//            Failed : HTTP error code : 400
//            response message Bad Request
//            error message {"error":"invalid_grant","error_description":"The refresh token is invalid or expired."}
            String pattern = "\\w* : HTTP error code : (\\d{3})\\n[\\w\\s]*\\nerror message (\\{[\\\":\\w\\d,\\.\\s]*\\})";
            Pattern r = Pattern.compile(pattern);
            // Now create matcher object.
            String error = e.getMessage();
            Matcher m = r.matcher(error);
            String body = "{\"message\": \"Quiet login failed\"}";
            HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
            if (m.find( )) {
                String status = m.group(1);
                body = m.group(2);
                httpStatus = HttpStatus.valueOf(400);
            }
            return new ResponseEntity<String>(body, httpStatus);
        }
    }

    private String fetchUser(String loginResponse, String email) throws IOException {
        // get user profile so we have user profile id and basic user information
        String userResponse = getUser(email);

        // combine the information into one response
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> userJsonMap = objectMapper.readValue(userResponse,
                new TypeReference<Map<String, Object>>() {
                });
        Map<String, Object> loginJsonMap = objectMapper.readValue(loginResponse,
                new TypeReference<Map<String, Object>>() {
                });
        Map<String, Object> profileMap = (Map<String, Object>) userJsonMap.get("profile");
        ObjectNode slimProfileNode = objectMapper.createObjectNode();
        slimProfileNode.put("firstName", profileMap.get("firstName").toString());
        slimProfileNode.put("lastName", profileMap.get("lastName").toString());
        slimProfileNode.put("email", profileMap.get("email").toString());
        slimProfileNode.put("birthdate", profileMap.get("birthdate").toString());
        boolean isProfileComplete = this.isProfileComplete(profileMap);
        slimProfileNode.put("isProfileComplete", isProfileComplete);
        loginJsonMap.put("profile", slimProfileNode);
        Object profileId = userJsonMap.get("id");
        loginJsonMap.put("id", profileId);

        // if user profile id is already mapped to membership id then get it
        if(userProfileExtService.existsByProfileId(profileId.toString())) {
            UserProfileExt userProfileExt = userProfileExtService.getByProfileId(profileId.toString());
            if (userProfileExt != null) {
                slimProfileNode.put("membershipId", userProfileExt.getMembershipId().toString());
            }
        }

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(loginJsonMap);
    }

    /**
     * Checks if profile has been completed
     * @param profileMap
     * @return
     */
    private boolean isProfileComplete(Map<String, Object> profileMap) {
        return StringUtils.isNotEmpty((String) profileMap.get("firstName")) &&
                StringUtils.isNotEmpty((String) profileMap.get("lastName")) &&
                StringUtils.isNotEmpty((String) profileMap.get("mobilePhone")) &&
                StringUtils.isNotEmpty((String) profileMap.get("email")) &&
                StringUtils.isNotEmpty((String) profileMap.get("streetAddress")) &&
                StringUtils.isNotEmpty((String) profileMap.get("city")) &&
                StringUtils.isNotEmpty((String) profileMap.get("state")) &&
                StringUtils.isNotEmpty((String) profileMap.get("zipCode")) &&
                StringUtils.isNotEmpty((String) profileMap.get("countryCode")) &&
                StringUtils.isNotEmpty((String) profileMap.get("gender")) &&
                StringUtils.isNotEmpty((String) profileMap.get("birthdate"));
    }

    @GetMapping("/forgotpassword/{email}")
    @PreAuthorize("permitAll()")
    @ResponseBody
    public String forgotPasswordStart (@PathVariable String email) {
        try {
            logger.info("Starting Forgot password flow.  Emailing instructions to " + email);
            ForgotPasswordResponse forgotPasswordResponse = this.getClient()
                    .apiV1UsersUserIdCredentialsForgotPasswordPost(email);
            return "{ \"status\": \"SUCCESS\" }";
        } catch (Exception e) {
            logger.error("Error starting forgot password flow ", e);
            return String.format("{ \"status\": \"ERROR\" , \"errorMessage\": \"%s\"}", e.getMessage());
        }
    }

    @PostMapping("/resetpassword")
    @PreAuthorize("permitAll()")
    @ResponseBody
    public String resetPassword (@RequestBody UserRegistration userRegistration) {
        String status = "ERROR";
        try {
            logger.info("Resetting password for user");
            String stateToken = verifyRecoveryToken (userRegistration.getResetPasswordToken());

            String result = answerRecoveryQuestion(stateToken, "spinach");
            logger.info("Recover question answered result is " + result);
            if ("PASSWORD_RESET".equals(result)) {
                status = resetPasswordInternal(stateToken, userRegistration.getPassword());
            }
            logger.info("Reset password status " + status);
        } catch (Exception e) {
            logger.error("Error resetting password", e);
            return String.format("{ \"status\": \"ERROR\" , \"errorMessage\": \"%s\"}", e.getMessage());
        }
        return String.format("{ \"status\" : \"%s\" }", status);
    }

    /**
     *
     * @param resetPasswordToken
     * @return
     */
    private String verifyRecoveryToken(String resetPasswordToken) throws IOException {
        String url = oktaServiceBase + "/api/v1/authn/recovery/token";
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode topLevelObjectNode = objectMapper.createObjectNode();
        topLevelObjectNode.put("recoveryToken",resetPasswordToken);
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(topLevelObjectNode);

        String response = makePostRequest(url, requestBody);

        Map<String, Object> jsonMap = objectMapper.readValue(response,
                new TypeReference<Map<String, Object>>() {
                });
        String stateToken = jsonMap.get("stateToken").toString();
        Map<String, Object>  embedded = (Map<String, Object>) jsonMap.get("_embedded");
        if (embedded != null) {
            Map<String, Object> user = (Map<String, Object>) embedded.get("user");
            Map<String, Object> profile = (Map<String, Object>) user.get("profile");
            String login = (String) profile.get("login");
            String firstName = (String) profile.get("firstName");
            String lastName = (String) profile.get("lastName");
            logger.info("Verified recoveryToken for user " + login + ", " + lastName + ", " + firstName);
        }

        // convert sessionToken into to accessToken
        return stateToken;
    }

    /**
     *
     * @param stateToken
     * @param newPassword
     */
    private String resetPasswordInternal(String stateToken, String newPassword) throws IOException {
                    /*
curl -v -X POST \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "Authorization: SSWS ${api_token}" \
-d '{
  "stateToken": "00lMJySRYNz3u_rKQrsLvLrzxiARgivP8FB_1gpmVb",
  "newPassword": "Ch-ch-ch-ch-Changes!"
}' "https://${yourOktaDomain}/api/v1/authn/credentials/reset_password"

             */
        String url = oktaServiceBase + "/api/v1/authn/credentials/reset_password";
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode topLevelObjectNode = objectMapper.createObjectNode();
        topLevelObjectNode.put("stateToken", stateToken);
        topLevelObjectNode.put("newPassword", newPassword);
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(topLevelObjectNode);
        String response = makePostRequest(url, requestBody);

/*
{
  "expiresAt": "2015-11-03T10:15:57.000Z",
  "status": "SUCCESS",
  "sessionToken": "00t6IUQiVbWpMLgtmwSjMFzqykb5QcaBNtveiWlGeM",

 */
        Map<String, Object> jsonMap = objectMapper.readValue(response,
                new TypeReference<Map<String, Object>>() {
                });

        // convert sessionToken into to accessToken
        String status = jsonMap.get("status").toString();
        return status;
    }

    /**
     *
     * @param stateToken
     * @param answer
     */
    private String answerRecoveryQuestion (String stateToken, String answer) throws IOException {
/*
POST \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "Authorization: SSWS ${api_token}" \
-d '{
  "stateToken": "00lMJySRYNz3u_rKQrsLvLrzxiARgivP8FB_1gpmVb",
  "answer": "Annie Oakley"
}' "https://${yourOktaDomain}/api/v1/authn/recovery/answer"
 */
        String url = oktaServiceBase + "/api/v1/authn/recovery/answer";
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode topLevelObjectNode = objectMapper.createObjectNode();
        topLevelObjectNode.put("stateToken", stateToken);
        topLevelObjectNode.put("answer", answer);
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(topLevelObjectNode);

        String response = makePostRequest(url, requestBody);

/*
{
  "stateToken": "00lMJySRYNz3u_rKQrsLvLrzxiARgivP8FB_1gpmVb",
  "expiresAt": "2015-11-03T10:15:57.000Z",
  "status": "PASSWORD_RESET",
  "_embedded": {

 */
        Map<String, Object> jsonMap = objectMapper.readValue(response,
                new TypeReference<Map<String, Object>>() {
                });

        // convert sessionToken into to accessToken
        return jsonMap.get("status").toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Utility functions
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

}
