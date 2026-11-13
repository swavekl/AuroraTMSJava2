package com.auroratms.users;

import com.auroratms.AbstractOktaController;
import com.auroratms.profile.UserProfileExt;
import com.auroratms.profile.UserProfileExtService;
import com.auroratms.profile.UserProfileService;
import com.auroratms.utils.EmailService;
import com.auroratms.users.emailverification.EmailVerificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.okta.sdk.resource.api.UserCredApi;
import com.okta.sdk.resource.model.ForgotPasswordResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/users")
public class UsersController extends AbstractOktaController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    @Autowired
    private UserProfileExtService userProfileExtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Value("${client.host.url}")
    private String clientHostUrl;

    /**
     * Starts public self-registration with application-managed email verification:
     * <ol>
     *   <li>Create the Okta user in {@code STAGED} state so it cannot sign in yet.</li>
     *   <li>Create a cryptographically random, expiring verification token; persist only its hash in the application database.</li>
     *   <li>After the token record is committed, send the custom verification email containing the raw token link.</li>
     *   <li>When the user submits that link, {@code validateEmail} claims the token and activates the associated Okta user without Okta email.</li>
     *   <li>The daily cleanup job removes expired token records and, after the configured retention period, notifies and deletes eligible abandoned accounts.</li>
     * </ol>
     * If the custom email cannot be sent, the staged account and token remain available for the resend flow.
     */
    @PostMapping("/register")
    @ResponseBody
    public
    @PreAuthorize("permitAll()")
    ResponseEntity<String> registerUser(@RequestBody UserRegistration userRegistration) {
        try {
            logger.info("Registering new user " + userRegistration.getLastName() + ", " + userRegistration.getFirstName() + " email: " + userRegistration.getEmail());
            String userId = createUser(userRegistration);
            EmailVerificationService.TokenIssue tokenIssue = emailVerificationService.createForNewUser(userId);
            boolean verificationEmailSent = sendVerificationEmail(userRegistration.getEmail(),
                    userRegistration.getFirstName(), tokenIssue.token());

            try {
                logger.info("Sending email to admin notifying of new user creation.");
                Map<String, Object> templateModel = new HashMap<>();
                templateModel.put("playerName", "%s %s".formatted(userRegistration.getFirstName(), userRegistration.getLastName()));
                templateModel.put("playerEmail", userRegistration.getEmail());
                emailService.sendMessageUsingThymeleafTemplate("swaveklorenc@yahoo.com", null,
                        "New User Registration", "user-registration/new-user-registration.html", templateModel);
            } catch (Exception notificationError) {
                logger.error("Could not send administrator notification for new registration");
            }
            String response = "{\"status\":\"SUCCESS\",\"profileId\":\"%s\",\"verificationEmailSent\":%s}"
                    .formatted(userId, verificationEmailSent);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error registering user " + userRegistration.getEmail(), e);
            String message = e.getMessage();
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/registerbytd")
    @ResponseBody
    public
    @PreAuthorize("permitAll()")
//    @PreAuthorize("hasAuthority('TournamentDirectors') or hasAuthority('Admins')")
    ResponseEntity<String> registerUserByTD(@RequestBody UserRegistration userRegistration) {
        try {
            logger.info("Registering new user " + userRegistration.getLastName() + ", " + userRegistration.getFirstName() + " email: " + userRegistration.getEmail() + " by Tournament Director");
            String userId = createUser(userRegistration);

            activateOktaUser(userId);
            logger.info("Activated user " + userId);
            return new ResponseEntity<String>("{\"status\":\"SUCCESS\", \"profileId\": \"" + userId + "\"}", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error registering user " + userRegistration.getEmail(), e);
            String message = e.getMessage();
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param userRegistration
     * @return
     */
    private String createUser(@RequestBody UserRegistration userRegistration) throws IOException {
        String userId = null;
            String strRegisterUserURL = oktaServiceBase + "/api/v1/users?activate=false";

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode profileObjectNode = objectMapper.createObjectNode();
            profileObjectNode.put("firstName", userRegistration.getFirstName());
            profileObjectNode.put("lastName", userRegistration.getLastName());
            profileObjectNode.put("email", userRegistration.getEmail());
            profileObjectNode.put("login", userRegistration.getEmail());
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

    @GetMapping("isregistered/{email}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> isUserRegistered(@PathVariable String email) {
        try {
            String userProfile = this.getUser(email);
            boolean isUserRegistered = (userProfile != null);
            String response = "{\"userRegistered\": %b }".formatted(isUserRegistered);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Validates only the submitted token, atomically claims it, and resolves the Okta user ID from its database record.
     * Activates a staged user without triggering an Okta email, then consumes the token; the client cannot choose
     * which Okta account to activate. Invalid tokens return a generic result without account lookup information.
     */
    @PostMapping("/validateEmail")
    public @ResponseBody
    @PreAuthorize("permitAll()")
    ResponseEntity<String> validateEmail(@RequestBody Map<String, String> request) {
        String rawToken = request.get("token");
        if (!StringUtils.isNotEmpty(rawToken)) {
            return ResponseEntity.badRequest().body("{\"status\":\"INVALID\"}");
        }
        String tokenHash = emailVerificationService.hashToken(rawToken);
        try {
            EmailVerificationService.VerificationClaim claim = emailVerificationService.claim(tokenHash).orElse(null);
            if (claim == null) {
                return ResponseEntity.badRequest().body("{\"status\":\"INVALID\"}");
            }
            try {
                String userJson = getOktaUserById(claim.oktaUserId());
                Map<String, Object> user = new ObjectMapper().readValue(userJson, new TypeReference<>() { });
                String status = String.valueOf(user.get("status"));
                if ("STAGED".equals(status)) {
                    activateOktaUserWithoutEmail(claim.oktaUserId());
                } else if (!"ACTIVE".equals(status)) {
                    emailVerificationService.releaseClaim(tokenHash);
                    return ResponseEntity.badRequest().body("{\"status\":\"INVALID\"}");
                }
                emailVerificationService.markUsed(tokenHash);
                return ResponseEntity.ok("{\"status\":\"SUCCESS\"}");
            } catch (Exception activationError) {
                emailVerificationService.releaseClaim(tokenHash);
                logger.error("Could not activate user after email verification", activationError);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("{\"status\":\"FAILED\"}");
            }
        } catch (Exception e) {
            logger.error("Email verification failed", e);
            return ResponseEntity.badRequest().body("{\"status\":\"INVALID\"}");
        }
    }

    /**
     * Rotates the token and resends mail only for a staged account found by the server-side email lookup.
     * Applies source and per-account limits while keeping the response identical for unknown and ineligible accounts.
     */
    @PostMapping("/resend-verification")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> resendVerification(@RequestBody Map<String, String> request,
                                                     HttpServletRequest servletRequest) {
        String email = request.get("email");
        logger.info("Handling account verification resend request.");
        String processingStage = "source request limit";
        try {
            if (StringUtils.isEmpty(email)) {
                logger.info("Skipping verification resend because no email was supplied.");
            } else if (!emailVerificationService.allowSourceRequest(servletRequest.getRemoteAddr())) {
                logger.info("Skipping verification resend because the source request limit was reached.");
            } else {
                processingStage = "Okta user lookup";
                String json = getUser(email.trim());
                Map<String, Object> user = new ObjectMapper().readValue(json, new TypeReference<>() { });
                String userStatus = String.valueOf(user.get("status"));
                if ("STAGED".equals(userStatus)) {
                    String userId = String.valueOf(user.get("id"));
                    processingStage = "verification token rotation";
                    EmailVerificationService.TokenIssue issue = emailVerificationService.regenerateForResend(userId);
                    Map<String, Object> profile = (Map<String, Object>) user.get("profile");
                    processingStage = "verification email delivery";
                    boolean emailSent = sendVerificationEmail(email.trim(), String.valueOf(profile.get("firstName")), issue.token());
                    logger.info("Verification resend processing completed; email send succeeded={}", emailSent);
                } else {
                    logger.info("Skipping verification resend because the Okta user status is {}.", userStatus);
                }
            }
        } catch (Exception exception) {
            logger.warn("Verification resend processing failed during {} ({}).",
                    processingStage, exception.getClass().getSimpleName());
        }
        return ResponseEntity.accepted().body("{\"status\":\"SUCCESS\",\"message\":\"Another email was sent. Please check again.\"}");
    }

    /** Builds the configured application verification link and sends it without persisting or logging the raw token. */
    private boolean sendVerificationEmail(String email, String firstName, String rawToken) {
        try {
            String url = clientHostUrl + "/ui/home/userwelcome?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                    + "&token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8)
                    + "&firstName=" + URLEncoder.encode(firstName, StandardCharsets.UTF_8);
            Map<String, Object> model = new HashMap<>();
            model.put("firstName", firstName);
            model.put("email", email);
            model.put("activationUrl", url);
            emailService.sendMessageUsingThymeleafTemplate(email, null, "Welcome to TTAurora!",
                    "email-verification/account-activation", model);
            return true;
        } catch (Exception e) {
            logger.error("Could not send account verification email");
            return false;
        }
    }

    /**
     * @param email
     * @return
     * @throws IOException
     */
    String getUser(String email) throws IOException {
        String lookupUserIdUrl = oktaServiceBase + "/api/v1/users/" + URLEncoder.encode(email, StandardCharsets.UTF_8);
        return makeGetRequest(lookupUserIdUrl);
    }

    /** Fetches the current Okta user state needed to validate or reconcile a claimed verification. */
    String getOktaUserById(String userId) throws IOException {
        return makeGetRequest(oktaServiceBase + "/api/v1/users/" + userId);
    }

    /** Activates a verified staged user while suppressing Okta's built-in activation email. */
    void activateOktaUserWithoutEmail(String userId) throws IOException {
        makePostRequest(oktaServiceBase + "/api/v1/users/" + userId + "/lifecycle/activate?sendEmail=false", null);
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

    private void unsuspendUser(String userId) throws IOException {
        // POST /api/v1/users/${userId}/lifecycle/unsuspend
        String url = oktaServiceBase + "/api/v1/users/" + userId + "/lifecycle/unsuspend";
        String response = makePostRequest(url, null);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(response,
                new TypeReference<Map<String, Object>>() {
                });
//        String prettyPrintResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
//        System.out.println("POST unsuspendUser = " + prettyPrintResult);
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
                logger.error("User %s failed to login because username or password were not provided".formatted(userRegistration.getEmail()));
                return new ResponseEntity<String>("{\"message\": \"Login failed\"}", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            logger.error("User %s failed to login due to: %s".formatted(userRegistration.getEmail(), e.getMessage()));
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
//refreshToken = "A" + refreshToken.substring(1);  // make invalid token
            String requestBody = "grant_type=refresh_token"
                    + "&scope=" + URLEncoder.encode("openid offline_access", StandardCharsets.UTF_8.name())
                    + "&client_id=" + URLEncoder.encode(this.clientId, StandardCharsets.UTF_8.name())
                    + "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8.name());
            String url = oktaServiceBase + "/oauth2/default/v1/token";
            String loginResponse = makePostRequest(url, requestBody, "application/x-www-form-urlencoded", null);

            String email = parameters.get("email");
            String combinedResponse = fetchUser(loginResponse, email);
            return new ResponseEntity<String>(combinedResponse, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Quiet login error", e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
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
        slimProfileNode.put("state", profileMap.get("state") != null ? profileMap.get("state").toString() : "" );
        slimProfileNode.put("country", profileMap.get("countryCode") != null ? profileMap.get("countryCode").toString() : "");
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
    public String forgotPasswordStart(@PathVariable String email) {
        try {
            logger.info("Starting Forgot password flow.  Emailing instructions to " + email);
            UserCredApi userCredApi = new UserCredApi(this.getClient());
            ForgotPasswordResponse response = userCredApi.forgotPassword(email, true);
            return "{ \"status\": \"SUCCESS\" }";
        } catch (Exception e) {
            logger.error("Error starting forgot password flow ", e);
            return "{ \"status\": \"ERROR\" , \"errorMessage\": \"%s\"}".formatted(e.getMessage());
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
            String message = e.getMessage();
            return "{ \"status\": \"ERROR\" , \"errorMessage\": \"%s\"}".formatted(message);
        }
        return "{ \"status\" : \"%s\" }".formatted(status);
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
