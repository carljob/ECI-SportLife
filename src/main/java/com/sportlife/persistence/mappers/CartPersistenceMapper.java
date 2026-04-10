package com.sportlife.persistence.mappers;

import com.sportlife.core.models.Cart;
import com.sportlife.core.models.CartItem;
import com.sportlife.persistence.entities.CartDocument;
import com.sportlife.persistence.entities.CartItemDocument;
import java.util.stream.Collectors;

public final class CartPersistenceMapper {

    private CartPersistenceMapper() {
    }

    public static Cart toModel(CartDocument doc) {
        return Cart.builder()
            .id(doc.getId())
            .userId(doc.getUserId())
            .items(doc.getItems().stream().map(item -> CartItem.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build()).collect(Collectors.toList()))
            .build();
    }

    public static CartDocument toDocument(Cart cart) {
        CartDocument doc = new CartDocument();
        doc.setId(cart.getId());
        doc.setUserId(cart.getUserId());
        doc.setItems(cart.getItems().stream().map(item -> {
            CartItemDocument itemDocument = new CartItemDocument();
            itemDocument.setProductId(item.getProductId());
            itemDocument.setProductName(item.getProductName());
            itemDocument.setQuantity(item.getQuantity());
            itemDocument.setUnitPrice(item.getUnitPrice());
            return itemDocument;
        }).collect(Collectors.toList()));
        return doc;
    }
}

