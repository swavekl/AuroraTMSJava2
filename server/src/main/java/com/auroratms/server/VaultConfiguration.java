package com.auroratms.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.vault.config.EnvironmentVaultConfiguration;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.repository.configuration.EnableVaultRepositories;

/**
 * Class for configuring connection to vault server from a properties file vault.properties
 */
@Configuration
@PropertySource("vault.properties")
@Import(EnvironmentVaultConfiguration.class)
// enables CRUD repository implementation which connects to Vault
@EnableVaultRepositories(basePackages = "com.auroratms")
public class VaultConfiguration {
}
