package com.auroratms.email.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.vault.repository.mapping.Secret;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Secret
public class EmailServerConfigurationEntity implements Serializable {

    // tournament director profile id
    @Id
    private String profileId;

    // SMTP email server host name
    private String serverHost;

    // SMTP email server port number
    private int serverPort;

    // user for logging in using simple authentication
    private String userId;

    // password for logging
    private String password;
}
