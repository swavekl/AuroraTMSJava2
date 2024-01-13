package com.auroratms.email.config;

import org.springframework.data.repository.CrudRepository;

public interface EmailServerConfigurationRepository extends CrudRepository<EmailServerConfigurationEntity, String> {
}
