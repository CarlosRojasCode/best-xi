package com.bestxi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Dice qué webs pueden llamar al backend desde el navegador (CORS), para toda la aplicación.
// La lista sale de "bestxi.cors-origenes" en application.yml, así que en el servidor
// se cambia sin tocar el código.

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origenesPermitidos;

    public CorsConfig(@Value("${bestxi.cors-origenes}") String[] origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(origenesPermitidos)
                .allowedMethods("GET");
    }
}
