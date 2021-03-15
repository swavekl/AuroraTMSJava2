package com.auroratms.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.vault.annotation.VaultPropertySource;

@Configuration
@VaultPropertySource(value = "secret/auroratms",
        renewal = VaultPropertySource.Renewal.RENEW)
public class ApplicationSecretProperties {

    // environment variables which include properties fetched from the vault at /secrets/auroratms
    @Autowired
    Environment environment;

    public String getJwkSetUri() {
        return (this.environment != null) ? this.environment.getProperty("jwkSetUri") : "";
    }


}
