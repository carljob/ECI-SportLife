package com.sportlife.persistence.mappers;

import com.sportlife.core.models.Order;
import com.sportlife.core.models.OrderItem;
import com.sportlife.persistence.entities.OrderEntity;
import com.sportlife.persistence.entities.OrderItemEmbeddable;
import java.util.stream.Collectors;

public final class OrderPersistenceMapper {

    private OrderPersistenceMapper() {
    }

    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setUserId(order.getUserId());
        entity.setTotal(order.getTotal());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setItems(order.getItems().stream().map(item -> {
            OrderItemEmbeddable emb = new OrderItemEmbeddable();
            emb.setProductId(item.getProductId());
            emb.setProductName(item.getProductName());
            emb.setQuantity(item.getQuantity());
            emb.setUnitPrice(item.getUnitPrice());
            return emb;
        }).collect(Collectors.toList()));
        return entity;
    }

    public static Order toModel(OrderEntity entity) {
        return Order.builder()
            .id(entity.getId())
            .userId(entity.getUserId())
            .total(entity.getTotal())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .items(entity.getItems().stream().map(item -> OrderItem.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build()).collect(Collectors.toList()))
            .build();
    }
}

