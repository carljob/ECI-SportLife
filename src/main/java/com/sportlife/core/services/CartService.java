package com.sportlife.core.services;

import com.sportlife.core.models.Cart;

public interface CartService {
    Cart addProduct(Long userId, Long productId, Integer quantity);
    Cart getCart(Long userId);
    Cart updateQuantity(Long userId, Long productId, Integer quantity);
    void clearCart(Long userId);
}

