package com.sportlife.persistence.mappers;

import com.sportlife.core.models.Product;
import com.sportlife.persistence.entities.ProductEntity;

public final class ProductPersistenceMapper {

    private ProductPersistenceMapper() {}

    public static Product toModel(ProductEntity entity) {
        return Product.builder()
            .id(entity.getId())
            .name(entity.getName())
            .category(entity.getCategory())
            .description(entity.getDescription())
            .price(entity.getPrice())
            .stock(entity.getStock())
            .active(entity.isActive())
            .build();
    }

    public static ProductEntity toEntity(Product model) {
        ProductEntity entity = new ProductEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setCategory(model.getCategory());
        entity.setDescription(model.getDescription());
        entity.setPrice(model.getPrice());
        entity.setStock(model.getStock());
        entity.setActive(model.isActive());
        return entity;
    }
}
