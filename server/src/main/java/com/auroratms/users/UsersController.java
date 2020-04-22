package com.auroratms.users;

import com.auroratms.AbstractOktaController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
            System.out.println("registrationToken = " + registrationToken);

            String userId = createUser(userRegistration, registrationToken.toString());

            String id = activateOktaUser(userId);

            suspendUser(userId);

        } catch (IOException e) {
            return new ResponseEntity<>("{\"status\":\"Registration failed\"}", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<String>("{\"status\":\"Registration succeeded\"}", HttpStatus.OK);
    }


    /**
     * @param userRegistration
     * @param registrationToken
     * @return
     */
    private String createUser(@RequestBody UserRegistration userRegistration, String registrationToken) {
        String userId = null;
        try {
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
            ObjectNode valueObjectNode = objectMapper.createObjectNode();
            valueObjectNode.put("value", userRegistration.getPassword());
            ObjectNode passwordObjectNode = objectMapper.createObjectNode();
            passwordObjectNode.set("password", valueObjectNode);
            ObjectNode topLevelObjectNode = objectMapper.createObjectNode();
            topLevelObjectNode.set("profile", profileObjectNode);
            topLevelObjectNode.set("credentials", passwordObjectNode);
            String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(topLevelObjectNode);

            String result = makePostRequest(strRegisterUserURL, requestBody);

            Map<String, Object> jsonMap = objectMapper.readValue(result,
                    new TypeReference<Map<String, Object>>() {
                    });
            userId = jsonMap.get("id").toString();
            System.out.println("userId = " + userId);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("e = " + e);
        }
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
        String response = null;
        try {
            // log in user using username and password and get sessionToken in return
            String authenticateUserResponse = authenticateUser(userRegistration);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap = objectMapper.readValue(authenticateUserResponse,
                    new TypeReference<Map<String, Object>>() {
                    });

            // convert sessionToken into to accessToken
            String sessionToken = jsonMap.get("sessionToken").toString();
            String accessToken = convertSessionToAccessToken(sessionToken);
            // get profile and return it along with access token
            Map<String, Object> embedded = (Map<String, Object>) jsonMap.get("_embedded");
            Map<String, Object> user = (Map<String, Object>) embedded.get("user");
            user.put("accessToken", accessToken);
            System.out.println("accessToken = " + accessToken);
            response = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
        } catch (IOException e) {
            return new ResponseEntity<String>("{\"message\": \"Login failed\"}", HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<String>(response, HttpStatus.OK);
    }

    private String authenticateUser(@RequestBody UserRegistration userRegistration) throws IOException {
        String url = oktaServiceBase + "/api/v1/authn";

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode topLevelObjectNode = objectMapper.createObjectNode();
        topLevelObjectNode.put("username", userRegistration.getEmail());
        topLevelObjectNode.put("password", userRegistration.getPassword());
//                "relayState": "/myapp/some/deep/link/i/want/to/return/to",
        ObjectNode optionsObjectNode = objectMapper.createObjectNode();
        optionsObjectNode.put("multiOptionalFactorEnroll", false);
        optionsObjectNode.put("warnBeforePasswordExpired", false);
        topLevelObjectNode.set("options", optionsObjectNode);

        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(topLevelObjectNode);

        return makePostRequest(url, requestBody);
    }

    private String convertSessionToAccessToken(String sessionToken) throws IOException {

        String accessToken = "";

        String urlString = oktaServiceBase + "/oauth2/default/v1/authorize";
        urlString += "?response_type=token";
        urlString += "&scope=openid";
        urlString += "&state=Af0ifjslDkj";
        urlString += "&nonce=n-0S6_WzA2Mj";
//        url += "&prompt=none";
        urlString += "&client_id=0oahrcv3ghdG5SA6E0h7";
        urlString += "&response_mode=fragment";
        urlString += "&redirect_uri=https%3A%2F%2Fgateway-pc:4200/implicit/callback";
        urlString += "&sessionToken=" + sessionToken;

        URL url = new URL(urlString);

        // Call the /authorize endpoint by passing this sessionToken, which will return access_token
        // in the redirect URI which can be then be extracted in your HttpClient.
        // You can use the response_mode as fragment to the authorize endpoint which will then return
        // the access_token as a hash fragment or by default it’s returned as a URL query param.
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
//        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", getAuthorizationHeaderValue());

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpStatus.NOT_FOUND.value()) {
            try {
                Object content = conn.getContent();
            } catch (IOException e) {
                // https://gateway-pc:4200/implicit/callback#access_token=eyJraWQiOiIzVFlQQkUxOXo5V0lyczRWRWhKcGFIb0RzbklSRmk2bllBcnRaRkg3ajBZIiwiYWxnIjoiUlMyNTYifQ.eyJ2ZXIiOjEsImp0aSI6IkFULlVyX1UwOUQ4Q05qcEw5d0ZyQ0k2NEc1Z01KSFdUaTRhYXQ3OE1TbDVnS1kiLCJpc3MiOiJodHRwczovL2Rldi03NTgxMjAub2t0YXByZXZpZXcuY29tL29hdXRoMi9kZWZhdWx0IiwiYXVkIjoiYXBpOi8vZGVmYXVsdCIsImlhdCI6MTU4NjMxMTk4NCwiZXhwIjoxNTg2MzE1NTg0LCJjaWQiOiIwb2FocmN2M2doZEc1U0E2RTBoNyIsInVpZCI6IjAwdWhyb3FtOTJkWEsxYjh4MGg3Iiwic2NwIjpbIm9wZW5pZCJdLCJzdWIiOiJzd2F2ZWtsb3JlbmNAeWFob28uY29tIiwiZ3JvdXBzIjpbIkV2ZXJ5b25lIiwiQWRtaW5zIl19.jpNEogvuBaOlG77hYBCJzPOCc2I05Eoy7hwXRtOzsrFvcITGbbqG9kjneQjJu270mIfU6NQxPC8i-nMtSPwHnPHRPjN6b1sSUc4ihEc4pjPxtiOi1_tdLsNF_baRyM1qNaoOuraUbxrA4BH-UqWB3xVIZDT8M-wRIFxuWbxnonyQh13ZRtfrRSXQseNAGjhm8ZP7HZ61v5krwLIzRPvAspt5xfgt0JOCnA22rGi2X0Y5GOikUxZP1kvnd-LX38h9WimqUq1X5y7TRVeZ0Xtdln37uk73hdEyo14-lXdeDhskKB9X-qvJMvPnyr19NfZop5fDE8opfJzsB6ZxQ8LK9A&token_type=Bearer&expires_in=3600&scope=openid&state=Af0ifjslDkj)
                String message = e.getMessage();
//                System.out.println("message = " + message);
                int accessTokenBegin = message.indexOf("#") + "access_token=".length() + 1;
                int accessTokenEnd = message.indexOf("&");
                accessToken = message.substring(accessTokenBegin, accessTokenEnd);
//                System.out.println("access_token = " + accessToken);
            }
        }

        return accessToken;
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

