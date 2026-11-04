package com.bestxi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

// Guarda 2 datos, la url de API-Football y la key secreta

@ConfigurationProperties(prefix = "apifootball")
@Getter
@Setter
public class ApiFootballProperties {

    private String baseUrl;
    private String key;
}
