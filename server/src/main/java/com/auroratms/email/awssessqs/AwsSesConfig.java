package com.auroratms.email.awssessqs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
public class AwsSesConfig {

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    // Default to empty string if Vault leaves it null
    @Value("${spring.cloud.aws.credentials.session-token:#{''}}")
    private String sessionToken;

    @Bean
    public SesV2Client sesV2Client() {
        StaticCredentialsProvider credentialsProvider;

        // Apply our fallback logic!
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            System.out.println("Using AWS Basic Credentials (IAM User Mode)");
            credentialsProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );
        } else {
            System.out.println("Using AWS STS Dynamic Credentials (with Token)");
            credentialsProvider = StaticCredentialsProvider.create(
                    AwsSessionCredentials.create(accessKey, secretKey, sessionToken)
            );
        }

        // Return the v2 client instead of v1
        return SesV2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
