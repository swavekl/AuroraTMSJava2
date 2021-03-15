package com.auroratms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AbstractOktaController {

    @Value("${okta.client.token}")
    protected String api_token;

    @Value("${okta.client.orgUrl}")
    protected String oktaServiceBase;

    @Value("${okta.oauth2.client-id}")
    protected String clientId;

    protected String makePostRequest(String url, String requestBody) throws IOException {
        return makePostRequest(url, requestBody, "application/json", getAuthorizationHeaderValue());
    }

    /**
     * @param url
     * @param requestBody
     * @return
     * @throws IOException
     */
    protected String makePostRequest(String url, String requestBody, String contentType, String authorization) throws IOException {
        URL registerUserURL = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) registerUserURL.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", contentType);
        if (authorization != null) {
            conn.setRequestProperty("Authorization", authorization);
        }

        if (requestBody != null && requestBody.length() > 0) {
            conn.setDoOutput(true);
            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write(requestBody);
            os.flush();
            os.close();
        }

        if (conn.getResponseCode() != 200) {
            throwException(conn);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String output;
        String result = "";
        while ((output = br.readLine()) != null) {
            result += output;
        }
        br.close();

        conn.disconnect();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(result,
                new TypeReference<Map<String, Object>>() {
                });

//        String prettyPrintResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
//        System.out.println("POST prettyPrintResult = " + prettyPrintResult);

        return result;
    }

    /**
     * @param urlString
     * @return
     * @throws IOException
     */
    protected String makeGetRequest(String urlString) throws IOException {
        String result = makeGetRequestInternal(urlString);

//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Object> jsonMap = objectMapper.readValue(result,
//                new TypeReference<Map<String, Object>>() {
//                });
//
//        String prettyPrintResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
//        System.out.println("GET prettyPrintResult = " + prettyPrintResult);

        return result;
    }

    protected String makeGetRequestInternal(String urlString) throws IOException {
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

        conn.disconnect();
        return result.toString();
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
        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode()
                + "\nresponse message " + responseMessage
                + "\nerror message " + error);
    }

    protected String getAuthorizationHeaderValue() {
        return "SSWS " + api_token;
    }

    protected Client getClient() {
        return Clients.builder()
                .setOrgUrl(oktaServiceBase)
                .setClientCredentials(new TokenClientCredentials(api_token))
                .build();
    }
}
