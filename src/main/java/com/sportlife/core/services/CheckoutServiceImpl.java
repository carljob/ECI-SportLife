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

    /**
     * Crea una orden a partir del carrito del usuario y descuenta el stock.
     * Operación transaccional: si falla el descuento de cualquier producto
     * se hace rollback completo (ningún stock se modifica).
     */
    @Override
    @Transactional
    public Order checkout(Long userId) {
        Cart cart = cartService.getCart(userId);
        // Regla de negocio: no se permite checkout con carrito vacío.
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("El carrito esta vacio");
        }

        // Calcular total
        BigDecimal total = cart.getItems().stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Construir items de la orden
        var orderItems = cart.getItems().stream().map(item -> OrderItem.builder()
            .productId(item.getProductId())
            .productName(item.getProductName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .build()).collect(Collectors.toList());

        Order order = Order.builder()
            .userId(userId)
            .items(orderItems)
            .total(total)
            .status(OrderStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        // Descontar stock de forma transaccional
        order.getItems().forEach(item -> {
            var product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Producto no encontrado: " + item.getProductId()));
            int remaining = product.getStock() - item.getQuantity();
            if (remaining < 0) {
                throw new BusinessException(
                    "Stock insuficiente para: " + product.getName()
                    + " (disponible: " + product.getStock() + ")");
            }
            product.setStock(remaining);
            productRepository.save(product);
        });

        // Se persiste la orden ya con estado PENDING.
        OrderEntity saved = orderRepository.save(OrderPersistenceMapper.toEntity(order));
        // El carrito se limpia después de crear la orden.
        cartService.clearCart(userId);
        return OrderPersistenceMapper.toModel(saved);
    }

    /**
     * Procesa el pago de una orden existente.
     * Si el monto es suficiente → PAID.
     * Si el monto es insuficiente → REJECTED (stock NO se revierte en MVP).
     */
    @Override
    @Transactional
    public Payment processPayment(Long orderId, String method, BigDecimal amount) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + orderId));

        // Aprobación basada en monto enviado vs total de la orden.
        boolean approved = amount.compareTo(orderEntity.getTotal()) >= 0;

        Payment payment = Payment.builder()
            .orderId(orderId)
            .method(method)
            .amount(amount)
            .approved(approved)
            .build();

        PaymentDocument savedPayment = paymentRepository.save(
            PaymentPersistenceMapper.toDocument(payment));

        // Si aprobado => PAID; si no => REJECTED.
        orderEntity.setStatus(approved ? OrderStatus.PAID : OrderStatus.REJECTED);
        orderRepository.save(orderEntity);

        return PaymentPersistenceMapper.toModel(savedPayment);
    }
}
