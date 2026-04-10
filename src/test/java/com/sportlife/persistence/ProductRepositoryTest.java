package com.sportlife.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.sportlife.persistence.entities.ProductEntity;
import com.sportlife.persistence.repositories.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Tests de repositorio JPA para ProductRepository.
 * @DataJpaTest configura H2 en memoria automáticamente.
 */
@DataJpaTest
class ProductRepositoryTest {

    @Autowired ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void findByCategoryIgnoreCase_shouldReturnMatchingProducts() {
        productRepository.save(buildProduct("Camiseta Dry", "ROPA", 50_000));
        productRepository.save(productRepository.save(buildProduct("Short Rojo", "ropa", 35_000)));
        productRepository.save(buildProduct("Mancuernas", "GYM", 200_000));

        List<ProductEntity> result = productRepository.findByCategoryIgnoreCase("ropa");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p ->
            p.getCategory().equalsIgnoreCase("ropa")));
    }

    @Test
    void findByNameContainingIgnoreCase_shouldReturnPartialMatches() {
        productRepository.save(buildProduct("Balon Pro", "futbol", 120_000));
        productRepository.save(buildProduct("Balon Niño", "futbol", 60_000));
        productRepository.save(buildProduct("Camiseta Runner", "ropa", 65_000));

        List<ProductEntity> result = productRepository.findByNameContainingIgnoreCase("balon");

        assertEquals(2, result.size());
    }

    @Test
    void findByCategoryIgnoreCase_shouldReturnEmptyListWhenNoMatch() {
        productRepository.save(buildProduct("Balon Pro", "futbol", 120_000));

        List<ProductEntity> result = productRepository.findByCategoryIgnoreCase("ciclismo");

        assertTrue(result.isEmpty());
    }

    // ── Helper ────────────────────────────────────────────────────────
    private ProductEntity buildProduct(String name, String category, int price) {
        ProductEntity p = new ProductEntity();
        p.setName(name);
        p.setCategory(category);
        p.setDescription("Descripcion de prueba");
        p.setPrice(new BigDecimal(price));
        p.setStock(10);
        return p;
    }
}
