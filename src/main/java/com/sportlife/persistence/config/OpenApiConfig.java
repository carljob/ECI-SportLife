package com.sportlife.persistence.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI 3 / Swagger UI.
 * Acceso: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI sportLifeOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("SportLife API")
                .description("MVP REST API para gestión de usuarios, catálogo de productos, " +
                    "carrito de compras y pagos. Desarrollado por DOSW Company.")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("DOSW Company")
                    .email("dev@dosw.com"))
                .license(new License().name("MIT")))
            .servers(List.of(
                new Server().url("http://localhost:" + serverPort).description("Desarrollo local"),
                new Server().url("https://sportlife-api.azurewebsites.net").description("Producción Azure")
            ));
    }
}
