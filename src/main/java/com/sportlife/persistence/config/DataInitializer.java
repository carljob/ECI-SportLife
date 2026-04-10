package com.sportlife.persistence.config;

import com.sportlife.persistence.entities.ProductEntity;
import com.sportlife.persistence.repositories.ProductRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final ProductRepository productRepository;

    @Bean
    CommandLineRunner seedProducts() {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }
            productRepository.save(build("Balon Pro", "futbol", "Balon profesional talla 5", new BigDecimal("120000"), 25));
            productRepository.save(build("Camiseta Runner", "ropa", "Camiseta transpirable", new BigDecimal("65000"), 40));
            productRepository.save(build("Mancuernas 10kg", "gym", "Par de mancuernas", new BigDecimal("210000"), 10));
        };
    }

    private ProductEntity build(String name, String category, String description, BigDecimal price, Integer stock) {
        ProductEntity product = new ProductEntity();
        product.setName(name);
        product.setCategory(category);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        return product;
    }
}

