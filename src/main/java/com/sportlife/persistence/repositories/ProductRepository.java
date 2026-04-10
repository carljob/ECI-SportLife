package com.sportlife.persistence.repositories;

import com.sportlife.persistence.entities.ProductEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    // Búsquedas que respetan el estado activo/inactivo
    List<ProductEntity> findByActiveTrue();
    List<ProductEntity> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    List<ProductEntity> findByCategoryIgnoreCaseAndActiveTrue(String category);

    // Queries legacy (usadas en tests de repositorio)
    List<ProductEntity> findByNameContainingIgnoreCase(String name);
    List<ProductEntity> findByCategoryIgnoreCase(String category);
}
