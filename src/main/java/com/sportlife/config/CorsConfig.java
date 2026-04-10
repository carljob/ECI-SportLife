package com.sportlife.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing).
 *
 * Permite que el frontend (en dominio diferente al de la API) pueda consumir
 * los endpoints REST. Sin esta configuración, el navegador bloquearía todas
 * las peticiones desde el frontend.
 *
 * En producción: reemplazar "*" en allowedOrigins por el dominio exacto del frontend.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*")           // En prod: "https://sportlife.com"
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
            .exposedHeaders("Authorization")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
