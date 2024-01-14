package com.auroratms.email.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.vault.repository.mapping.Secret;
@Data
@NoArgsConstructor
@Secret
public class EmailServerConfigurationEntity {

    // tournament director profile id
    @Id
    private String id;

    // SMTP email server host name
    private String serverHost;

    // SMTP email server port number
    private int serverPort;

    // user for logging in using simple authentication
    private String userId;

    // password for logging
    private String password;
}
