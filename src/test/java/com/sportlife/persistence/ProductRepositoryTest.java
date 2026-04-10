package com.sportlife.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sportlife.persistence.entities.ProductEntity;
import com.sportlife.persistence.repositories.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldFilterByCategoryIgnoringCase() {
        ProductEntity product = new ProductEntity();
        product.setName("Camiseta Dry");
        product.setCategory("ROPA");
        product.setDescription("Test");
        product.setPrice(new BigDecimal("50"));
        product.setStock(5);
        productRepository.save(product);

        var result = productRepository.findByCategoryIgnoreCase("ropa");

        assertEquals(1, result.size());
        assertEquals("Camiseta Dry", result.get(0).getName());
    }
}

