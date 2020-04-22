package com.auroratms;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@PropertySource(value = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
@EnableTransactionManagement
public class TestDataSourceConfig {
}
