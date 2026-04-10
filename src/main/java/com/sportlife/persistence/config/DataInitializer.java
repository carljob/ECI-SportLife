package com.sportlife.persistence.config;

import com.sportlife.persistence.entities.ProductEntity;
import com.sportlife.persistence.repositories.ProductRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Carga datos de prueba al arrancar la aplicación (solo si la BD está vacía).
 * Patrón: CommandLineRunner — se ejecuta después de que el contexto de Spring
 * está completamente inicializado.
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final ProductRepository productRepository;

    @Bean
    CommandLineRunner seedProducts() {
        return args -> {
            if (productRepository.count() > 0) return;

            productRepository.save(build("Balon Pro",        "futbol",   "Balon profesional talla 5",         120_000, 25));
            productRepository.save(build("Camiseta Runner",  "ropa",     "Camiseta transpirable manga corta",   65_000, 40));
            productRepository.save(build("Mancuernas 10kg",  "gym",      "Par de mancuernas de hierro",        210_000, 10));
            productRepository.save(build("Zapatillas Trail", "running",  "Zapatillas de trail running",        350_000, 15));
            productRepository.save(build("Casco Ciclismo",   "ciclismo", "Casco certificado CE",              180_000,  8));
            productRepository.save(build("Colchoneta Yoga",  "gym",      "Colchoneta antideslizante 6mm",      55_000, 20));
        };
    }

    private ProductEntity build(String name, String category, String description, int price, int stock) {
        ProductEntity p = new ProductEntity();
        p.setName(name);
        p.setCategory(category);
        p.setDescription(description);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setActive(true);
        return p;
    }
}
