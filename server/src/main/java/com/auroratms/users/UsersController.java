package com.auroratms.users;

import com.auroratms.AbstractOktaController;
import com.auroratms.profile.UserProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.okta.sdk.resource.user.ForgotPasswordResponse;
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

@RestController
@RequestMapping("api/users")
public class UsersController extends AbstractOktaController {

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
            UUID registrationToken = UUID.randomUUID();

            String userId = createUser(userRegistration, registrationToken.toString());

            String id = activateOktaUser(userId);

            suspendUser(userId);

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
            System.out.println("requestBody = " + requestBody);
            String result = makePostRequest(strRegisterUserURL, requestBody);

            Map<String, Object> jsonMap = objectMapper.readValue(result,
                    new TypeReference<Map<String, Object>>() {
                    });
            userId = jsonMap.get("id").toString();
            System.out.println("userId = " + userId);
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
            // get user profile which contains user id and token
            String userProfile = getUser(userRegistration.getEmail());
            String token = userRegistration.getSecondEmail();

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap = objectMapper.readValue(userProfile,
                    new TypeReference<Map<String, Object>>() {
                    });
            String userId = (String) jsonMap.get("id");
            System.out.println("userId = " + userId);
            Object profile = jsonMap.get("profile");
            String tokenFromURL = "";
            if (profile != null) {
                tokenFromURL = (String) ((Map<String, Object> )profile).get("secondEmail");
            }

            // check that the activation token is OK and only then unsuspend
            if (token.equals(tokenFromURL)) {
                unsuspendUser(userId);
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

    private void unsuspendUser(String userId) throws IOException {
        // POST /api/v1/users/${userId}/lifecycle/unsuspend
        String url = oktaServiceBase + "/api/v1/users/" + userId + "/lifecycle/unsuspend";
        makePostRequest(url, null);
    }

    /**
     * @param userRegistration
     * @return
     */
    @PostMapping("/login")
    @ResponseBody
    public
    @PreAuthorize("permitAll()")
    ResponseEntity<String> login(@RequestBody UserRegistration userRegistration) {
        try {
            // authenticate
            String requestBody = "grant_type=password"
                    + "&username=" + URLEncoder.encode(userRegistration.getEmail(), StandardCharsets.UTF_8.name())
                    + "&password=" + URLEncoder.encode(userRegistration.getPassword(), StandardCharsets.UTF_8.name())
                    + "&client_id=" + URLEncoder.encode(this.clientId, StandardCharsets.UTF_8.name())
                    + "&scope=" + URLEncoder.encode("openid offline_access", StandardCharsets.UTF_8.name());
            String url = oktaServiceBase + "/oauth2/default/v1/token";
            String loginResponse = makePostRequest(url, requestBody, "application/x-www-form-urlencoded", null);

            // get user profile so we have user profile id and basic user information
            String userResponse = getUser(userRegistration.getEmail());

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
            slimProfileNode.put ("firstName", profileMap.get("firstName").toString());
            slimProfileNode.put ("lastName", profileMap.get("lastName").toString());
            slimProfileNode.put ("email", profileMap.get("email").toString());
            loginJsonMap.put("profile", slimProfileNode);
            Object profileId = userJsonMap.get("id");
            loginJsonMap.put("id", profileId);
            String combinedResponse = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(loginJsonMap);
//            System.out.println("combinedResponse = " + combinedResponse);
            return new ResponseEntity<String>(combinedResponse, HttpStatus.OK);
        } catch (IOException e) {
//            System.out.println("e = " + e);
            return new ResponseEntity<String>("{\"message\": \"Login failed\"}", HttpStatus.UNAUTHORIZED);
        }
    }

    private void refreshAccessToken () {
//        POST https://${yourOktaDomain}/oauth2/default/v1/token \
//        accept:application/json \
//        authorization:'Basic MG9hYmg3M...' \
//        cache-control:no-cache \
//        content-type:application/x-www-form-urlencoded \
//        grant_type=refresh_token \
//        redirect_uri=http://localhost:8080 \
//        scope=offline_access%20openid \
//        refresh_token=MIOf-U1zQbyfa3MUfJHhvnUqIut9ClH0xjlDXGJAyqo
    }

//    private String authenticateUser(@RequestBody UserRegistration userRegistration) throws IOException {
//
//        String url = oktaServiceBase + "/api/v1/authn";
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        ObjectNode topLevelObjectNode = objectMapper.createObjectNode();
//        topLevelObjectNode.put("username", userRegistration.getEmail());
//        topLevelObjectNode.put("password", userRegistration.getPassword());
////                "relayState": "/myapp/some/deep/link/i/want/to/return/to",
//        ObjectNode optionsObjectNode = objectMapper.createObjectNode();
//        optionsObjectNode.put("multiOptionalFactorEnroll", false);
//        optionsObjectNode.put("warnBeforePasswordExpired", false);
//        topLevelObjectNode.set("options", optionsObjectNode);
//
//        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(topLevelObjectNode);
//
//        return makePostRequest(url, requestBody);
//    }
//
//    private String convertSessionToAccessToken(String sessionToken) throws IOException {
//
//        String accessToken = "";
//
//        String urlString = oktaServiceBase + "/oauth2/default/v1/authorize";
//        urlString += "?response_type=token";
//        urlString += "&scope=openid";
//        urlString += "&state=Af0ifjslDkj";
//        urlString += "&nonce=n-0S6_WzA2Mj";
//        urlString += "&client_id=" + clientId;
//        urlString += "&response_mode=fragment";
//        String encodeRedirectUri = URLEncoder.encode(this.redirectUri, "UTF-8");
//        urlString += "&redirect_uri=" + encodeRedirectUri;
//        urlString += "&sessionToken=" + sessionToken;
//
//        URL url = new URL(urlString);
//
//        // Call the /authorize endpoint by passing this sessionToken, which will return access_token
//        // in the redirect URI which can be then be extracted in your HttpClient.
//        // You can use the response_mode as fragment to the authorize endpoint which will then return
//        // the access_token as a hash fragment or by default it’s returned as a URL query param.
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("GET");
//        conn.setRequestProperty("Accept", "application/json");
////        conn.setRequestProperty("Content-Type", "application/json");
//        conn.setRequestProperty("Authorization", getAuthorizationHeaderValue());
//
//        int responseCode = conn.getResponseCode();
//        if (responseCode == HttpStatus.NOT_FOUND.value()) {
//            try {
//                Object content = conn.getContent();
//            } catch (IOException e) {
//                // https://gateway-pc:4200/implicit/callback#access_token=eyJraWQiOiIzVFlQQkUxOXo5V0lyczRWRWhKcGFIb0RzbklSRmk2bllBcnRaRkg3ajBZIiwiYWxnIjoiUlMyNTYifQ.eyJ2ZXIiOjEsImp0aSI6IkFULlVyX1UwOUQ4Q05qcEw5d0ZyQ0k2NEc1Z01KSFdUaTRhYXQ3OE1TbDVnS1kiLCJpc3MiOiJodHRwczovL2Rldi03NTgxMjAub2t0YXByZXZpZXcuY29tL29hdXRoMi9kZWZhdWx0IiwiYXVkIjoiYXBpOi8vZGVmYXVsdCIsImlhdCI6MTU4NjMxMTk4NCwiZXhwIjoxNTg2MzE1NTg0LCJjaWQiOiIwb2FocmN2M2doZEc1U0E2RTBoNyIsInVpZCI6IjAwdWhyb3FtOTJkWEsxYjh4MGg3Iiwic2NwIjpbIm9wZW5pZCJdLCJzdWIiOiJzd2F2ZWtsb3JlbmNAeWFob28uY29tIiwiZ3JvdXBzIjpbIkV2ZXJ5b25lIiwiQWRtaW5zIl19.jpNEogvuBaOlG77hYBCJzPOCc2I05Eoy7hwXRtOzsrFvcITGbbqG9kjneQjJu270mIfU6NQxPC8i-nMtSPwHnPHRPjN6b1sSUc4ihEc4pjPxtiOi1_tdLsNF_baRyM1qNaoOuraUbxrA4BH-UqWB3xVIZDT8M-wRIFxuWbxnonyQh13ZRtfrRSXQseNAGjhm8ZP7HZ61v5krwLIzRPvAspt5xfgt0JOCnA22rGi2X0Y5GOikUxZP1kvnd-LX38h9WimqUq1X5y7TRVeZ0Xtdln37uk73hdEyo14-lXdeDhskKB9X-qvJMvPnyr19NfZop5fDE8opfJzsB6ZxQ8LK9A&token_type=Bearer&expires_in=3600&scope=openid&state=Af0ifjslDkj)
//                String message = e.getMessage();
////                System.out.println("message = " + message);
//                int accessTokenBegin = message.indexOf("#") + "access_token=".length() + 1;
//                int accessTokenEnd = message.indexOf("&");
//                accessToken = message.substring(accessTokenBegin, accessTokenEnd);
////                System.out.println("access_token = " + accessToken);
//            }
//        }
//
//        return accessToken;
//    }

    @GetMapping("/forgotpassword/{email}")
    @PreAuthorize("permitAll()")
    @ResponseBody
    public String forgotPasswordStart (@PathVariable String email) {
        try {
            ForgotPasswordResponse forgotPasswordResponse = this.getClient()
                    .apiV1UsersUserIdCredentialsForgotPasswordPost(email);
            return "{ \"status\": \"SUCCESS\" }";
        } catch (Exception e) {
            return String.format("{ \"status\": \"ERROR\" , \"errorMessage\": \"%s\"}", e.getMessage());
        }
    }

    @PostMapping("/resetpassword")
    @PreAuthorize("permitAll()")
    @ResponseBody
    public String resetPassword (@RequestBody UserRegistration userRegistration) {
        String status = "ERROR";
        try {
            String stateToken = verifyRecoveryToken (userRegistration.getResetPasswordToken());

            String result = answerRecoveryQuestion(stateToken, "spinach");

            if ("PASSWORD_RESET".equals(result)) {
                status = resetPasswordInternal(stateToken, userRegistration.getPassword());
            }
        } catch (Exception e) {
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
        System.out.println("verifyRecoveryToken response = " + response);

        Map<String, Object> jsonMap = objectMapper.readValue(response,
                new TypeReference<Map<String, Object>>() {
                });

        // convert sessionToken into to accessToken
        return jsonMap.get("stateToken").toString();
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
        System.out.println("verifyRecoveryToken response = " + response);

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
//    /**
//     * @param userId
//     * @return
//     */
//    private String getEmailId(String userId) {
//        String emailId = null;
//        try {
//            // GET /api/v1/users/${userId}/emails
//            String lookupUserEmailsURL = oktaServiceBase + "/api/v1/users/" + userId + "/emails";
//
//            String result = makeGetRequest(lookupUserEmailsURL);
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String, Object> jsonMap = objectMapper.readValue(result,
//                    new TypeReference<Map<String, Object>>() {
//                    });
//            emailId = jsonMap.get("id").toString();
//            System.out.println("emailId = " + emailId);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return emailId;
//    }
//
//    /**
//     * @param userId
//     * @param emailId
//     */
//    private void sendVerificationEmail(String userId, String emailId) {
//        try {
//            // POST /api/v1/users/${userId}/emails/${emailId}/verify
//            String strRegisterUserURL = oktaServiceBase + "/api/v1/users/" + userId + "/emails/" + emailId + "/verify";
//
////            {
////                "redirectUri": "https://example.com/some/page?state=blah&custom=true",
////                    "expiresAt": "2017-06-14T00:17:57.000Z",
////                    "actions": {
////                "signOn": "REQUIRED"
////            }
//
//            Calendar now = Calendar.getInstance();
//            now.add(Calendar.HOUR, 1);
//            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//            String expiresAt = df.format(now);
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            ObjectNode topLevelObjectNode = objectMapper.createObjectNode();
//            topLevelObjectNode.put("redirectUri", "https://gateway-pc:4200/userprofile");
//            topLevelObjectNode.put("expiresAt", expiresAt);
//            ObjectNode actionsNode = objectMapper.createObjectNode();
//            actionsNode.put("signOn", "REQUIRED");
//            topLevelObjectNode.set("actions", actionsNode);
//            String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(topLevelObjectNode);
//
//            String result = makePostRequest(strRegisterUserURL, requestBody);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("e = " + e);
//        }
//    }



//        try {
//            AuthenticationClient authenticationClient = AuthenticationClients.builder()
//                    .setOrgUrl(this.oktaServiceBase)
//                    .build();
//            String relayState = null;
//            AuthenticationStateHandler stateHanlder = new AuthenticationStateHandlerAdapter() {
//                @Override
//                public void handleUnknown(AuthenticationResponse unknownResponse) {
//                    System.out.println("unknownResponse = " + unknownResponse);
//                }
//
//                @Override
//                public void handleSuccess(AuthenticationResponse successResponse) {
//                    String sessionToken = successResponse.getSessionToken();
//                    System.out.println("sessionToken = " + sessionToken);
//                    if (sessionToken != null) {
//                        try {
//                            String accessToken = convertSessionToAccessToken(sessionToken);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        // translate session token to access token
//                        Client client = getClient();
//                        String authorizationServerId = "default";
//                        AuthorizationServer authorizationServer = client.getAuthorizationServer(authorizationServerId);
//                        String name = authorizationServer.getName();
////                        authorizationServer.getRefreshTokenForClient()
////                        Session session = client.getSession(sessionToken);
////                        String login = session.getLogin();
////                        String accessToken = null;
////                        try {
////                            Tokens token = sessionClient.getTokens();
////                            accessToken = token.getAccessToken();
////                        } catch (AuthorizationException e) {
////                            //handle error
////                        }
//
//                    }
//                }
//            };
//            authenticationClient.authenticate(userRegistration.getEmail(),
//                    userRegistration.getPassword().toCharArray(),
//                    relayState, stateHanlder);
//        } catch (AuthenticationException e) {
//            e.printStackTrace();
//        }

