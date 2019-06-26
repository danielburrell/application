package uk.co.solong.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Loads properties from a file called ${APP_ENV}.properties or
 * default.properties if APP_ENV is not set.
 */
@Configuration
@PropertySource("classpath:${APP_ENV:default}.properties")
public class PropertyPlaceholderConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}