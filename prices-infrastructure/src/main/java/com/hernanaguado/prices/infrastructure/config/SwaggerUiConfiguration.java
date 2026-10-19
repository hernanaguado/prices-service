package com.hernanaguado.prices.infrastructure.config;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Serves Swagger UI from the static contract ({@code static/openapi.yaml}) instead of a contract generated
 * from code. With {@code springdoc.api-docs.enabled=false} springdoc disables its auto-configuration, so the
 * UI needs these beans, as documented by springdoc.
 */
@Configuration(proxyBeanMethods = false)
class SwaggerUiConfiguration {

    @Bean
    SpringDocConfiguration springDocConfiguration() {
        return new SpringDocConfiguration();
    }

    @Bean
    SpringDocConfigProperties springDocConfigProperties() {
        return new SpringDocConfigProperties();
    }

    @Bean
    ObjectMapperProvider objectMapperProvider(SpringDocConfigProperties springDocConfigProperties) {
        return new ObjectMapperProvider(springDocConfigProperties);
    }
}
