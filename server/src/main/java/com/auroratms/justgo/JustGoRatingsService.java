package com.auroratms.justgo;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class JustGoRatingsService {

    private static final Logger logger = LoggerFactory.getLogger(JustGoRatingsService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${justgo.api.base-url:https://api-sandbox.justgo.com/api/v2.2}")
    private String baseUrl;

    @Value("${justgo.api.key:}")
    private String apiKey;

    @Value("${justgo.api.default-ranking-type:Rating}")
    private String defaultRankingType = "Rating";

    private final static DateFormat AS_OF_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private String cachedToken;
    private long tokenExpiryTime = 0;
    private static final long REFRESH_BUFFER = 60000;

    private static DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Find player record by full name
     */
    public ApiPlayerDto findPlayerRecordByName(String firstName, String lastName) {
        if (StringUtils.isBlank(lastName)) {
            throw new IllegalArgumentException("lastName is required");
        }
        if (StringUtils.isBlank(firstName)) {
            throw new IllegalArgumentException("firstName is required");
        }
        JsonNode playerData = this.findMemberIdByFullNameInternal(firstName, lastName);
        if (playerData != null) {
            return objectMapper.convertValue(playerData, ApiPlayerDto.class);
        }
        return null;
    }

    /**
     * Find player record by membership id
     */
    public ApiPlayerDto findPlayerRecordByMembershipId(Long membershipId) {
        if (membershipId == null) {
            throw new IllegalArgumentException("membershipId is required");
        }
        JsonNode playerData = findMemberIdByUsattMemberhipId(membershipId);
        if (playerData != null) {
            return objectMapper.convertValue(playerData, ApiPlayerDto.class);
        }
        return null;
    }

    /**
     * Find player record by membership id
     */
    public ApiPlayerDto findPlayerRecordByGUID(String justGoId) {
        if (justGoId == null) {
            throw new IllegalArgumentException("justGoId is required");
        }
        JsonNode playerData = findMemberByGUID(justGoId);
        if (playerData != null) {
            ApiPlayerDto apiPlayerDto = objectMapper.convertValue(playerData, ApiPlayerDto.class);
//            "memberships": [
//            {
//                "name": "Silver",
//                "status": "Active",
//                "endDate": "2026-07-02",
//            } ]
            JsonNode memberships = playerData.get("memberships");
            if (!memberships.isArray() || memberships.isEmpty()) {
                throw new IllegalStateException("Membership not found for justGoId " + justGoId);
            }

            for (JsonNode membership : memberships) {
                String endDate = (membership.get("endDate") != null) ? membership.get("endDate").asText() : null;
                if (endDate != null) {
                    apiPlayerDto.setMembershipExpirationDate(endDate);
                }
                JsonNode status = membership.get("status");
                if (status != null && "Active".equalsIgnoreCase(status.asText())) {
                    String name = (membership.get("name") != null) ? membership.get("name").asText() : null;
                    if (name != null) {
                        apiPlayerDto.setMembershipType(name);
                    }
                    break;
                }
            }
            return apiPlayerDto;
        }
        return null;
    }

    private JsonNode findMemberIdByUsattMemberhipId(Long membershipId) {
        String bearerToken = getValidToken();

        String strModifiedAfter = this.getJustGoCutoverDate();
        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/Members/FindByAttributes")
                .queryParam("memberNumber", membershipId)
//                .queryParam("ModifiedAfter", strModifiedAfter)
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

    /**
     *
     * @param justGoId
     * @return
     */
    private JsonNode findMemberByGUID(String justGoId) {
        String bearerToken = getValidToken();

        // https://api-sandbox.justgo.com/api/v2.2/Members/718dfa8b-3604-4dcc-afe6-25d0e85cb6bd
        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/Members/" + justGoId)
                .toUriString();
        logger.info("JustGo URL: {} for member {}", url, justGoId);

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
            return root.path("data");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse JustGo member lookup response", e);
        }
    }

    public int getTournamentRatingByFullName(String firstName, String lastName) {
        return getTournamentRatingByFullNameAsOfDate(firstName, lastName, new Date());
    }

    public int getTournamentRatingByFullNameAsOfDate(String firstName, String lastName, Date asOfDate) {
        if (StringUtils.isBlank(lastName)) {
            throw new IllegalArgumentException("lastName is required");
        }
        if (StringUtils.isBlank(firstName)) {
            throw new IllegalArgumentException("firstName is required");
        }

        String justGoMemberId = findMemberIdByFullName(lastName, firstName);
        JsonNode rankingsNode = getRankings(justGoMemberId, asOfDate);

        String rating = extractTournamentRating(rankingsNode);
        if (StringUtils.isEmpty(rating)) {
            logger.warn("No ranking rows returned for lastName={} memberId={}; defaulting rating to 0", lastName, justGoMemberId);
            return 0;
        } else {
            return Integer.parseInt(rating);
        }
    }

    private synchronized String getValidToken() {
        if (cachedToken == null || System.currentTimeMillis() >= tokenExpiryTime) {
            logger.info("Token expired or missing. Re-authenticating with JustGo...");
            authenticate();
        }
        return cachedToken;
    }

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
            long expiresInSeconds = json.path("data").path("expiresIn").asLong(3600);

            this.cachedToken = tokenType + " " + accessToken;
            this.tokenExpiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000) - REFRESH_BUFFER;

            return this.cachedToken;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse JustGo auth response", e);
        }
    }

    public String findMemberIdByFullName(String lastName, String firstName) {
        JsonNode chosen = this.findMemberIdByFullNameInternal(firstName, lastName);
        if (chosen == null) {
            throw new IllegalStateException("JustGo member is missing for lastName=" + lastName + ", " + firstName);
        }
        String memberId = chosen.path("id").asText(null);
        if (StringUtils.isBlank(memberId)) {
            throw new IllegalStateException("JustGo memberId is missing for lastName=" + lastName);
        }
        return memberId;
    }

    private JsonNode findMemberIdByFullNameInternal(String firstName, String lastName) {
        String bearerToken = getValidToken();

        String strModifiedAfter = getJustGoCutoverDate();
        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/Members/FindByAttributes")
                .queryParam("LastName", lastName)
                .queryParam("FirstName", firstName)
                .queryParam("ModifiedAfter", strModifiedAfter)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("JustGo member lookup failed with status " + response.getStatusCode());
        }

        String body = response.getBody();
        try {
            JsonNode root = objectMapper.readTree(body);
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
            return chosen;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse JustGo member lookup response: " + body, e);
        }
    }

    private @NotNull String getJustGoCutoverDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JANUARY, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date asOfDate = calendar.getTime();
        return AS_OF_DATE_FORMAT.format(asOfDate);
    }

    private JsonNode getRankings(String justGoMemberId, Date asOfDate) {
        String bearerToken = this.getValidToken();

        String url = baseUrl + "/Competitions/Rankings";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        headers.setContentType(MediaType.valueOf("application/json-patch+json"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("memberId", justGoMemberId);
        body.put("type", defaultRankingType);
        if (asOfDate != null) {
            String strAsOfDate = AS_OF_DATE_FORMAT.format(asOfDate);
            body.put("date", strAsOfDate);
        }

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

    private String extractTournamentRating(JsonNode rankingsNode) {
        JsonNode dataNode = rankingsNode.path("data");

        String rating = findRatingField(dataNode, "finalRating");
        if (rating == null) {
            logger.warn("Unable to find tournament rating in JustGo response: {}", rankingsNode);
        }

        return rating;
    }

    private String findRatingField(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        if (node.isObject()) {
            JsonNode candidate = node.get(fieldName);
            if (candidate != null && candidate.isNumber()) {
                return candidate.asText();
            }
            var fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String nested = findRatingField(entry.getValue(), fieldName);
                if (nested != null) {
                    return nested;
                }
            }
            return null;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                String nested = findRatingField(item, fieldName);
                if (nested != null) {
                    return nested;
                }
            }
        }

        return null;
    }

    public List<ApiPlayerDto> findChangedPlayers(Date asOfDate) {
        List<ApiPlayerDto> players = new ArrayList<>();

        String bearerToken = getValidToken();

        String strModifiedAfter = AS_OF_DATE_FORMAT.format(asOfDate);
        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/Members/FindByAttributes")
                .queryParam("ModifiedAfter", strModifiedAfter)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("JustGo member lookup failed with status " + response.getStatusCode());
        }

        String body = response.getBody();
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode members = root.path("data");
            if (!members.isArray() || members.isEmpty()) {
                throw new IllegalStateException("No JustGo members found");
            }

            for (JsonNode member : members) {
                ApiPlayerDto playerDto = objectMapper.convertValue(member, ApiPlayerDto.class);
                if (playerDto.getId() != null) {
                    JsonNode rankingsNode = this.getRankings(playerDto.getId(), asOfDate);
                    String rating = extractTournamentRating(rankingsNode);
                    if (rating != null) {
                        playerDto.setTournamentRating(rating);
                    }
                }
                players.add(playerDto);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse JustGo member lookup response: " + body, e);
        }
        return players;
    }

    /**
     *
     * @param pageRequest
     * @return
     */
    public Page<ApiPlayerDto> listPlayers(PageRequest pageRequest) {
        int pageSize = pageRequest.getPageSize();
        int pageNumber = pageRequest.getPageNumber();
        String bearerToken = getValidToken();

        String strModifiedAfter = getJustGoCutoverDate();
        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/Members/FindByAttributes")
                .queryParam("ModifiedAfter", strModifiedAfter)
                .queryParam("PageNumber", pageNumber)
                .queryParam("PageSize", pageSize)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("JustGo member lookup failed with status " + response.getStatusCode());
        }

        String body = response.getBody();
        try {
            JsonNode root = objectMapper.readTree(body);
            int totalRecords = root.path("totalRecords").asInt();
            JsonNode members = root.path("data");
            if (!members.isArray() || members.isEmpty()) {
                return new PageImpl<>(Collections.EMPTY_LIST, pageRequest, totalRecords);
            }

            List<ApiPlayerDto> players = new ArrayList<>(pageSize);
            for (JsonNode member : members) {
                ApiPlayerDto playerDto = objectMapper.convertValue(member, ApiPlayerDto.class);
                if (playerDto.getId() != null) {
                    playerDto.setDob(convertDateToOutputFormat(playerDto.getDob()));
                    JsonNode rankingsNode = this.getRankings(playerDto.getId(), null);
                    String rating = extractTournamentRating(rankingsNode);
                    if (rating != null) {
                        playerDto.setTournamentRating(rating);
                    }
                    populateActiveMembershipDetails(playerDto);
                }
                players.add(playerDto);
            }
            return new PageImpl<>(players, pageRequest, totalRecords);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse JustGo member lookup response: " + body, e);
        }
    }

    /**
     *
     * @param playerDto
     */
    public void populateActiveMembershipDetails(ApiPlayerDto playerDto) {
        if (playerDto == null || playerDto.getId() == null) {
            return;
        }

        // Define input and output date formats
        try {
            String bearerToken = getValidToken();

            String url = UriComponentsBuilder
                    .fromUriString(baseUrl + "/Members/" + playerDto.getId())
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    JsonNode.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode responseNode = response.getBody();
                JsonNode dataNode = responseNode.get("data");
                playerDto.setMembershipType("Historical Membership");

                if (dataNode != null && dataNode.has("memberships")) {
                    JsonNode membershipsNode = dataNode.get("memberships");

                    if (membershipsNode.isArray()) {
                        for (JsonNode membership : membershipsNode) {
                            String status = membership.path("status").asText();
                            String name = membership.path("name").asText(null);
                            String endDateStr = membership.path("endDate").asText(null);

                            playerDto.setMembershipType(name);
                            if (endDateStr != null && !endDateStr.isEmpty()) {
                                String formattedDate = convertDateToOutputFormat(endDateStr);
                                playerDto.setMembershipExpirationDate(formattedDate);
                            }
                            // set first active or expired membership
                            if ("Active".equalsIgnoreCase(status)) {
                                break;
                            }
                        }
                    } else {
                        logger.warn("JustGo membership lookup for player " + playerDto.getFirstName() + " " + playerDto.getLastName() + " is not an array");
                    }
                } else {
                    logger.warn("JustGo membership lookup for player " + playerDto.getFirstName() + " " + playerDto.getLastName() + " didn't return information");
                }
                if ("Historical Membership".equalsIgnoreCase(playerDto.getMembershipType())) {
                    logger.warn("JustGo membership lookup for player " + playerDto.getFirstName() + " " + playerDto.getLastName() + " didn't return information - setting to Historical Membership");
                }

                // 2. Extract Primary Organisation (Club) Name
                JsonNode orgsNode = dataNode.get("organisations");
                if (orgsNode != null && orgsNode.isArray() && !orgsNode.isEmpty()) {
                    String primaryClubName = null;

                    for (JsonNode org : orgsNode) {
                        boolean isPrimary = org.path("isPrimary").asBoolean(false);
                        if (isPrimary) {
                            primaryClubName = org.path("organisationName").asText(null);
                            break;
                        }
                    }

                    // Fallback to the first organization if none was explicitly marked as primary
                    if (primaryClubName == null) {
                        primaryClubName = orgsNode.get(0).path("organisationName").asText(null);
                    }

                    playerDto.setClubName(primaryClubName); // Adjust setter name if different in your DTO
                }
            } else {
                logger.error("Failed to fetch member details for ID "
                        + playerDto.getId() + ". Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error retrieving membership details for member ID "
                    + playerDto.getId() + ": ", e);
        }
    }

    /**
     *
     * @param dateToFormat
     * @return
     */
    private String convertDateToOutputFormat (String dateToFormat) {
        // Parse "2026-07-02" and format to "07/02/2026"
        if (StringUtils.isNotEmpty(dateToFormat)) {
            try {
                LocalDate date = LocalDate.parse(dateToFormat, inputFormatter);
                return date.format(outputFormatter);
            } catch (Exception e) {
                return dateToFormat;
            }
        } else {
            return dateToFormat;
        }
    }

    /**
     * Map ApiPlayerDto into OpenCSV bean UsattPlayerCsvDto
     */
    public UsattPlayerCsvDto mapToCsvDto(ApiPlayerDto apiDto) {
        UsattPlayerCsvDto csvDto = new UsattPlayerCsvDto();
        csvDto.setMembershipId(apiDto.getMemberNumber());
        csvDto.setFirstName(apiDto.getFirstName());
        csvDto.setLastName(apiDto.getLastName());
        csvDto.setDateOfBirth(apiDto.getDob());
        csvDto.setZipCode(apiDto.getPostCode());
        csvDto.setGender(apiDto.getGender());
        csvDto.setCityTown(apiDto.getTown());
        csvDto.setState(apiDto.getCounty());
        csvDto.setLatestMembership(apiDto.getMembershipType());
        csvDto.setLatestMembershipExpiryDate(apiDto.getMembershipExpirationDate());
        csvDto.setFinalRating(apiDto.getTournamentRating());
        csvDto.setPrimaryClub(apiDto.getClubName());
        csvDto.setJustGoId(apiDto.getId());
        return csvDto;
    }

    // /api/v2.2/Members/FindByAttributes with Membership set to Silver, Bronze, Gold etc.
    // paged.  This eliminates historical i.e. expired membership records leaving about 20
    /**
     *
     ""
     AdultTournamentPass
     Bronze
     Coach
     Foreign Athlete Pass
     Gold
     Lifetime
     Silver
     Test membership
     Tournament Pass

     filter out those whose membership is 'Lapsed'
     */

    public Page<ApiPlayerDto> listPlayersByMembership(String membershipType, Pageable pageable) throws IllegalStateException {
        String bearerToken = getValidToken();

        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/Members/FindByAttributes")
                .queryParam("Membership", membershipType)
                .queryParam("PageNumber", pageable.getPageNumber())
                .queryParam("PageSize", pageable.getPageSize())
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, request, JsonNode.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("JustGo member lookup failed with status " + response.getStatusCode());
        }

        JsonNode responseBody = response.getBody();
        List<ApiPlayerDto> players = new ArrayList<>();
        long totalElements = 0;

        // Retrieve data node and total count if provided in response headers or wrapper
        JsonNode dataNode = responseBody.has("data") ? responseBody.get("data") : responseBody;

        if (dataNode.isArray()) {
            for (JsonNode memberNode : dataNode) {
                // Map standard primitive fields via ObjectMapper
                ApiPlayerDto playerDto = objectMapper.convertValue(memberNode, ApiPlayerDto.class);

                // 3. Fetch Tournament Ratings separately using GUID
                if (playerDto.getId() != null) {
                    try {
                        JsonNode rankingsNode = this.getRankings(playerDto.getId(), null);
                        String rating = extractTournamentRating(rankingsNode);
                        if (rating != null) {
                            playerDto.setTournamentRating(rating);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to fetch rankings for player ID " + playerDto.getId() + ": " + e.getMessage());
                    }
                    populateActiveMembershipDetails(playerDto);
                }

                players.add(playerDto);
            }
        }

        // Extract total count if returned by API wrapper (e.g. responseBody.path("totalRecords").asLong())
        totalElements = responseBody.path("totalRecords").asLong(players.size());

        return new PageImpl<>(players, pageable, totalElements);
    }
}
