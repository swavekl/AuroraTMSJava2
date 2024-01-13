package com.auroratms.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.vault.config.EnvironmentVaultConfiguration;
import org.springframework.vault.repository.configuration.EnableVaultRepositories;

/**
 * Class for configuring connection to vault server from a properties file vault.properties
 */
@Configuration
@PropertySource("file:/usr/local/auroratms/vault.properties")
@Import(EnvironmentVaultConfiguration.class)
// enables CRUD repository implementation which connects to Vault
@EnableVaultRepositories(basePackages = {"com.auroratms.account", "com.auroratms.email.config"})
public class VaultConfiguration {
}
