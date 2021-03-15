package com.auroratms.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.vault.config.EnvironmentVaultConfiguration;

/**
 * Class for configuring connection to vault server from a properties file vault.properties
 */
@Configuration
@PropertySource("vault.properties")
@Import(EnvironmentVaultConfiguration.class)
public class VaultConfiguration {
}
