package com.sportlife.persistence.mappers;

import com.sportlife.core.models.Product;
import com.sportlife.persistence.entities.ProductEntity;

public final class ProductPersistenceMapper {

    private ProductPersistenceMapper() {
    }

    public static Product toModel(ProductEntity entity) {
        return Product.builder()
            .id(entity.getId())
            .name(entity.getName())
            .category(entity.getCategory())
            .description(entity.getDescription())
            .price(entity.getPrice())
            .stock(entity.getStock())
            .build();
    }
}

