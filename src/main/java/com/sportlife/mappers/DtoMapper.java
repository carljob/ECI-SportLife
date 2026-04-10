package com.sportlife.mappers;

import com.sportlife.core.models.Cart;
import com.sportlife.core.models.CartItem;
import com.sportlife.core.models.Order;
import com.sportlife.core.models.Payment;
import com.sportlife.core.models.Product;
import com.sportlife.dtos.response.CartItemResponse;
import com.sportlife.dtos.response.CartResponse;
import com.sportlife.dtos.response.OrderResponse;
import com.sportlife.dtos.response.PaymentResponse;
import com.sportlife.dtos.response.ProductResponse;
import java.math.BigDecimal;
import java.util.stream.Collectors;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .category(product.getCategory())
            .description(product.getDescription())
            .price(product.getPrice())
            .stock(product.getStock())
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
            .approved(payment.isApproved())
            .status(status)
            .build();
    }
}

