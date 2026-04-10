package com.sportlife.core.services;

import com.sportlife.core.models.Cart;
import com.sportlife.core.models.CartItem;
import com.sportlife.core.models.Product;
import com.sportlife.core.validators.StockValidator;
import com.sportlife.persistence.entities.CartDocument;
import com.sportlife.persistence.mappers.CartPersistenceMapper;
import com.sportlife.persistence.repositories.CartRepository;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;
    private final StockValidator stockValidator;

    @Override
    public Cart addProduct(Long userId, Long productId, Integer quantity) {
        // Paso 1 del flujo de carrito: validar existencia de producto y stock solicitado.
        Product product = productService.getDetail(productId);
        stockValidator.validate(product.getStock(), quantity);

        Cart cart = getOrCreateCart(userId);
        CartItem existing = cart.getItems().stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .orElse(null);

        if (existing == null) {
            cart.getItems().add(CartItem.builder()
                .productId(productId)
                .productName(product.getName())
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .build());
        } else {
            // Regla del endpoint POST /items: si el item existe, la cantidad se acumula.
            int newQuantity = existing.getQuantity() + quantity;
            stockValidator.validate(product.getStock(), newQuantity);
            existing.setQuantity(newQuantity);
        }

        CartDocument saved = cartRepository.save(CartPersistenceMapper.toDocument(cart));
        return CartPersistenceMapper.toModel(saved);
    }

    @Override
    public Cart getCart(Long userId) {
        return getOrCreateCart(userId);
    }

    @Override
    public Cart updateQuantity(Long userId, Long productId, Integer quantity) {
        Product product = productService.getDetail(productId);
        stockValidator.validate(product.getStock(), quantity);

        Cart cart = getOrCreateCart(userId);
        // Regla del endpoint PATCH /items: establece cantidad exacta (no acumulativa).
        cart.getItems().stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .ifPresent(item -> item.setQuantity(quantity));

        CartDocument saved = cartRepository.save(CartPersistenceMapper.toDocument(cart));
        return CartPersistenceMapper.toModel(saved);
    }

    @Override
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cartRepository::delete);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
            .map(CartPersistenceMapper::toModel)
            .orElseGet(() -> Cart.builder().userId(userId).items(new ArrayList<>()).build());
    }
}

