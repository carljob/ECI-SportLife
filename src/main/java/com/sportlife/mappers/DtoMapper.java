package com.sportlife.mappers;

import com.sportlife.core.models.*;
import com.sportlife.dtos.response.*;
import java.math.BigDecimal;
import java.util.stream.Collectors;

public final class DtoMapper {

    private DtoMapper() {}

    public static ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .category(product.getCategory())
            .description(product.getDescription())
            .price(product.getPrice())
            .stock(product.getStock())
            .active(product.isActive())
            .build();
    }

    public static CartResponse toResponse(Cart cart) {
        BigDecimal total = cart.getItems().stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
            .userId(cart.getUserId())
            .items(cart.getItems().stream().map(DtoMapper::toResponse).collect(Collectors.toList()))
            .total(total)
            .build();
    }

    public static CartItemResponse toResponse(CartItem item) {
        return CartItemResponse.builder()
            .productId(item.getProductId())
            .productName(item.getProductName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .build();
    }

    public static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
            .id(order.getId())
            .userId(order.getUserId())
            .total(order.getTotal())
            .status(order.getStatus())
            .createdAt(order.getCreatedAt())
            .build();
    }

    public static PaymentResponse toResponse(Payment payment, String status) {
        return PaymentResponse.builder()
            .orderId(payment.getOrderId())
            .paymentId(payment.getId())
            .method(payment.getMethod())
            .amount(payment.getAmount())
            .approved(payment.isApproved())
            .status(status)
            .build();
    }
}
