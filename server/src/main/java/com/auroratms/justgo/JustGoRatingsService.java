package com.auroratms.justgo;

import com.auroratms.usatt.UsattPlayerRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JustGoRatingsService {

    private static final Logger logger = LoggerFactory.getLogger(JustGoRatingsService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${justgo.api.base-url:https://api-sandbox.justgo.com/api/v2.2}")
    private String baseUrl;

    @Value("${justgo.api.key:}")
    private String apiKey;

    @Value("${justgo.api.default-ranking-type:Tournament}")
    private String defaultRankingType = "Rating";

    // for rating as of date
    private final static DateFormat AS_OF_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    // for player record DOB
    private final static DateFormat DOB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");  // "1969-10-05",

    // cached token and expiry time
    private String cachedToken;

    // token expiry time in milliseconds
    private long tokenExpiryTime = 0;

    // Refresh 1 minute before actual expiry
    private static final long REFRESH_BUFFER = 60000;

    /**
     * Find player record by full name
     *
     * @param firstName
     * @param lastName
     * @return
     */
    public UsattPlayerRecord findPlayerRecordByName(String firstName, String lastName) {
        if (StringUtils.isBlank(lastName)) {
            throw new IllegalArgumentException("lastName is required");
        }
        if (StringUtils.isBlank(firstName)) {
            throw new IllegalArgumentException("firstName is required");
        }
        JsonNode playerData = this.findMemberIdByFullNameInternal(firstName, lastName);
        if (playerData != null) {
            return toPlayerRecord(playerData);
        } else {
            return null;
        }
    }

    /**
     * Find player record by membership id
     *
     * @param membershipId
     * @return
     */
    public UsattPlayerRecord findPlayerRecordByMembershipId(Long membershipId) {
        if (membershipId == null) {
            throw new IllegalArgumentException("membershipId is required");
        }
        JsonNode playerData = findMemberIdByUsattMemberhipId(membershipId);
        if (playerData != null) {
            return toPlayerRecord(playerData);
        } else {
            return null;
        }
    }

    /**
     * Find player record by membership id
     * @param membershipId
     * @return
     */
    private JsonNode findMemberIdByUsattMemberhipId(Long membershipId) {
        String bearerToken = getValidToken();

        String strModifiedBefore = AS_OF_DATE_FORMAT.format(new Date());
        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/Members/FindByAttributes")
                .queryParam("MemberId", membershipId)
                .queryParam("ModifiedBefore", strModifiedBefore)
                .toUriString();
        logger.info("JustGo URL: {} for membershipId {}", url, membershipId);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("JustGo member lookup failed with status " + response.getStatusCode());
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode members = root.path("data");
            if (!members.isArray() || members.isEmpty()) {
                throw new IllegalStateException("No JustGo member found for membershipId=" + membershipId);
            }

            return members.get(0);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse JustGo member lookup response", e);
        }

    }

    private UsattPlayerRecord toPlayerRecord(JsonNode node) {
        UsattPlayerRecord playerRecord = new UsattPlayerRecord();
        playerRecord.setFirstName(node.path("firstName").asText());
        playerRecord.setLastName(node.path("lastName").asText());
        playerRecord.setMembershipId(node.path("memberId").asLong());
        String gender = node.path("gender").asText();
        playerRecord.setGender("Male".equalsIgnoreCase(gender) ? "M" : "F");
        playerRecord.setState(node.path("county").asText());
        playerRecord.setCity(node.path("town").asText());
        playerRecord.setZip(node.path("postCode").asText());
        playerRecord.setCountry(node.path("country").asText());
        try {
            String strDOB = node.path("dob").asText();
            Date dateOfBirth = (StringUtils.isNotEmpty(strDOB)) ? DOB_DATE_FORMAT.parse(strDOB) : new Date();
            playerRecord.setDateOfBirth(dateOfBirth);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return playerRecord;
    }

    /**
     * Calls JustGo API sequence (Auth -> FindByAttributes -> Competitions/Rankings)
     *
     * @param firstName
     * @param lastName
     * @return
     */
    public Integer getTournamentRatingByFullName(String firstName, String lastName) {
        return getTournamentRatingByFullNameAsOfDate(firstName, lastName, new Date());
    }

    /**
     * Calls JustGo API sequence (Auth -> FindByAttributes -> Competitions/Rankings)
     * and returns tournament rating for the first matched member by last name.
     */
    public Integer getTournamentRatingByFullNameAsOfDate(String firstName, String lastName, Date asOfDate) {
        if (StringUtils.isBlank(lastName)) {
            throw new IllegalArgumentException("lastName is required");
        }
        if (StringUtils.isBlank(firstName)) {
            throw new IllegalArgumentException("firstName is required");
        }

        String justGoMemberId = findMemberIdByFullName(lastName, firstName);
        JsonNode rankingsNode = getRankings(justGoMemberId, asOfDate);

        Integer rating = extractTournamentRating(rankingsNode);
        if (rating == null) {
            logger.warn("No ranking rows returned for lastName={} memberId={}; defaulting rating to 0", lastName, justGoMemberId);
            return 0;
        }

        return rating;
    }

    /**
     * Internal method to ensure we have a valid token.
     * Uses synchronized to prevent multiple threads from authenticating at once.
     */
    private synchronized String getValidToken() {
        if (cachedToken == null || System.currentTimeMillis() >= tokenExpiryTime) {
            logger.info("Token expired or missing. Re-authenticating with JustGo...");
            authenticate();
        }
        return cachedToken;
    }

    /**
     * Authenticates with JustGo API and returns bearer token.
     *
     * @return
     */
    private String authenticate() {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalStateException("justgo.api.key is not configured");
        }
        String url = baseUrl + "/Auth";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setContentType(MediaType.valueOf("application/json-patch+json"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("secret", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("JustGo auth failed with status " + response.getStatusCode());
        }

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            String accessToken = json.path("data").path("accessToken").asText(null);
            String tokenType = json.path("data").path("tokenType").asText("Bearer");
            if (StringUtils.isBlank(accessToken)) {
                throw new IllegalStateException("JustGo auth token is missing in response");
            }
            // JustGo usually returns "expiresIn" in seconds.
            // If not provided, default to a safe value like 3600 (1 hour).
            long expiresInSeconds = json.path("data").path("expiresIn").asLong(3600);

            this.cachedToken = "Bearer " + accessToken;
            this.tokenExpiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000) - REFRESH_BUFFER;

            return tokenType + " " + accessToken;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse JustGo auth response", e);
        }
    }

    /**
     * Finds the member ID for a given full name by querying an external service.
     *
     * @param lastName  The last name of the member to be looked up.
     * @param firstName The first name of the member to be looked up.
     * @return The member ID associated with the given full name.
     */
    public String findMemberIdByFullName(String lastName, String firstName) {
        JsonNode chosen = this.findMemberIdByFullNameInternal(firstName, lastName);
        if (chosen == null) {
            throw new IllegalStateException("JustGo member is missing for lastName=" + lastName);
        }
        String memberId = chosen.path("id").asText(null);
        if (StringUtils.isBlank(memberId)) {
            throw new IllegalStateException("JustGo memberId is missing for lastName=" + lastName);
        }
        return memberId;

    }

    /**
     * Finds the member record for a given full name by querying an external service.
     *
     * @param firstName The first name of the member to be looked up.
     * @param lastName  The last name of the member to be looked up.
     * @return player data record
     * @throws IllegalStateException If the lookup fails, no member matches the given full name,
     *                               or the response is invalid or missing required data.
     */
    private JsonNode findMemberIdByFullNameInternal(String firstName, String lastName) {
        String bearerToken = getValidToken();

        String strModifiedBefore = AS_OF_DATE_FORMAT.format(new Date());
        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/Members/FindByAttributes")
                .queryParam("LastName", lastName)
//                .queryParam("ModifiedBefore", strModifiedBefore)
                .queryParam("ModifiedAfter", strModifiedBefore)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("JustGo member lookup failed with status " + response.getStatusCode());
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode members = root.path("data");
            if (!members.isArray() || members.isEmpty()) {
                throw new IllegalStateException("No JustGo member found for lastName=" + lastName);
            }

            JsonNode chosen = null;
            for (JsonNode member : members) {
                if (lastName.equalsIgnoreCase(member.path("lastName").asText()) &&
                        firstName.equalsIgnoreCase(member.path("firstName").asText())) {
                    chosen = member;
                    break;
                }
            }
            if (chosen == null) {
                chosen = members.get(0);
            }
            return chosen;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse JustGo member lookup response", e);
        }
    }

    /**
     * Calls JustGo API to get rankings for a given member.
     *
     * @param justGoMemberId
     * @param asOfDate
     * @return
     */
    private JsonNode getRankings(String justGoMemberId, Date asOfDate) {
        String bearerToken = this.getValidToken();

        String url = baseUrl + "/Competitions/Rankings";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        headers.setContentType(MediaType.valueOf("application/json-patch+json"));

        // 2026-03-31T00:27:39.612Z
        String strAsOfDate = AS_OF_DATE_FORMAT.format(asOfDate);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("memberId", justGoMemberId);
        body.put("type", defaultRankingType);
        body.put("date", strAsOfDate);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("JustGo rankings call failed with status " + response.getStatusCode());
        }

        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse JustGo rankings response", e);
        }
    }

    /**
     * Extracts the tournament rating from the rankings response.
     *
     * @param rankingsNode
     * @return
     */
    private Integer extractTournamentRating(JsonNode rankingsNode) {
        JsonNode dataNode = rankingsNode.path("data");

        Integer rating = findIntegerField(dataNode, "finalRating");
        if (rating == null) {
            logger.warn("Unable to find tournament rating in JustGo response: {}", rankingsNode);
        }

        return rating;
    }

    /**
     * Recursively searches for an integer field in a JSON node.
     *
     * @param node
     * @param fieldName
     * @return
     */
    private Integer findIntegerField(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        if (node.isObject()) {
            JsonNode candidate = node.get(fieldName);
            if (candidate != null && candidate.isNumber()) {
                return candidate.asInt();
            }
            var fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                Integer nested = findIntegerField(entry.getValue(), fieldName);
                if (nested != null) {
                    return nested;
                }
            }
            return null;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                Integer nested = findIntegerField(item, fieldName);
                if (nested != null) {
                    return nested;
                }
            }
        }

        return null;
    }
}
