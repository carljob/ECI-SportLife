package com.sportlife.core.services;

import com.sportlife.core.models.Cart;
import com.sportlife.core.models.Order;
import com.sportlife.core.models.OrderItem;
import com.sportlife.core.models.OrderStatus;
import com.sportlife.core.models.Payment;
import com.sportlife.handlers.BusinessException;
import com.sportlife.handlers.ResourceNotFoundException;
import com.sportlife.persistence.entities.OrderEntity;
import com.sportlife.persistence.entities.PaymentDocument;
import com.sportlife.persistence.repositories.OrderRepository;
import com.sportlife.persistence.repositories.PaymentRepository;
import com.sportlife.persistence.repositories.ProductRepository;
import com.sportlife.persistence.mappers.OrderPersistenceMapper;
import com.sportlife.persistence.mappers.PaymentPersistenceMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public Order checkout(Long userId) {
        Cart cart = cartService.getCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("El carrito esta vacio");
        }

        Order order = Order.builder()
            .userId(userId)
            .items(cart.getItems().stream().map(item -> OrderItem.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build()).collect(Collectors.toList()))
            .total(cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .status(OrderStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        // Atomic-like stock update per item for MVP scope.
        order.getItems().forEach(item -> {
            var product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
            int remaining = product.getStock() - item.getQuantity();
            if (remaining < 0) {
                throw new BusinessException("Stock insuficiente para " + product.getName());
            }
            product.setStock(remaining);
            productRepository.save(product);
        });

        OrderEntity saved = orderRepository.save(OrderPersistenceMapper.toEntity(order));
        cartService.clearCart(userId);
        return OrderPersistenceMapper.toModel(saved);
    }

    @Override
    @Transactional
    public Payment processPayment(Long orderId, String method, BigDecimal amount) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        if (amount.compareTo(orderEntity.getTotal()) < 0) {
            throw new BusinessException("El monto es menor al total de la orden");
        }

        Payment payment = Payment.builder()
            .orderId(orderId)
            .method(method)
            .amount(amount)
            .approved(true)
            .build();

        PaymentDocument savedPayment = paymentRepository.save(PaymentPersistenceMapper.toDocument(payment));
        orderEntity.setStatus(OrderStatus.PAID);
        orderRepository.save(orderEntity);

        return PaymentPersistenceMapper.toModel(savedPayment);
    }
}


