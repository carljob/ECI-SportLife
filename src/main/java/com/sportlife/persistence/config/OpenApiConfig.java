package com.sportlife.persistence.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sportLifeOpenApi() {
        return new OpenAPI().info(new Info()
            .title("SportLife API")
            .description("MVP REST API para catalogo, carrito y checkout")
            .version("v1")
            .contact(new Contact().name("SportLife Team")));
    }
}

