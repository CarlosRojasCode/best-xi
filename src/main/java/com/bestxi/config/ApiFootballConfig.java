package com.bestxi.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

// Prepara un "teléfono" (RestClient) con la URL correcta y la clave metida en la llamada

@Configuration
@EnableConfigurationProperties(ApiFootballProperties.class)
public class ApiFootballConfig {

    @Bean
    public RestClient apiFootballRestClient(ApiFootballProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("x-apisports-key", properties.getKey())
                .build();
    }
}
