package com.auroratms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class AbstractOktaController {

    protected String api_token = "009u9-0UY7v9lEcDKvpUL_lrEjH_khEaZ4sA0JWbfM";

    protected String oktaServiceBase = "https://dev-758120.oktapreview.com";

    /**
     * @param url
     * @param requestBody
     * @return
     * @throws IOException
     */
    protected String makePostRequest(String url, String requestBody) throws IOException {
        URL registerUserURL = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) registerUserURL.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", getAuthorizationHeaderValue());

        if (requestBody != null && requestBody.length() > 0) {
            conn.setDoOutput(true);
            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write(requestBody);
            os.flush();
            os.close();
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
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

        String prettyPrintResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        System.out.println("POST prettyPrintResult = " + prettyPrintResult);

        return result;
    }

    /**
     * @param urlString
     * @return
     * @throws IOException
     */
    protected String makeGetRequest(String urlString) throws IOException {
        String result = makeGetRequestInternal(urlString);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(result,
                new TypeReference<Map<String, Object>>() {
                });

        String prettyPrintResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        System.out.println("GET prettyPrintResult = " + prettyPrintResult);

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
            String responseMessage = conn.getResponseMessage();
            InputStream errorStream = conn.getErrorStream();
            byte [] errorText = new byte [1024];
            int read = errorStream.read(errorText);
            StringBuffer buffer = new StringBuffer();
            while (read > 0) {
                buffer.append(errorText);
                read = errorStream.read(errorText);
                System.out.println("read = " + read);
                System.out.println("errorText = " + errorText);
            }
            errorStream.close();
            String error = buffer.toString();
            System.out.println("error = " + error);
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode() + "\nresponse message " + responseMessage);
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
