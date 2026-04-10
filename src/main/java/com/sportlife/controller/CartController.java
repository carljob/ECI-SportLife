package com.sportlife.controller;

import com.sportlife.core.services.CartService;
import com.sportlife.dtos.request.AddToCartRequest;
import com.sportlife.dtos.request.UpdateCartItemRequest;
import com.sportlife.dtos.response.CartResponse;
import com.sportlife.mappers.DtoMapper;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** Requisito del enunciado: agregar productos al carrito. */
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable Long userId, @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(DtoMapper.toResponse(cartService.addProduct(userId, request.getProductId(), request.getQuantity())));
    }

    /** Requisito del enunciado: ver resumen de carrito (items, cantidades, subtotales y total). */
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(DtoMapper.toResponse(cartService.getCart(userId)));
    }

    /** Requisito del enunciado: actualizar cantidad de un item en carrito. */
    @PatchMapping("/{userId}/items")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable Long userId, @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(DtoMapper.toResponse(cartService.updateQuantity(userId, request.getProductId(), request.getQuantity())));
    }
}

