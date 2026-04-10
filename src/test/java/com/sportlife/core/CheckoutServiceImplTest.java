package com.sportlife.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sportlife.core.models.Cart;
import com.sportlife.core.models.CartItem;
import com.sportlife.core.models.OrderStatus;
import com.sportlife.core.services.CartService;
import com.sportlife.core.services.CheckoutServiceImpl;
import com.sportlife.handlers.BusinessException;
import com.sportlife.persistence.entities.OrderEntity;
import com.sportlife.persistence.entities.ProductEntity;
import com.sportlife.persistence.repositories.OrderRepository;
import com.sportlife.persistence.repositories.PaymentRepository;
import com.sportlife.persistence.repositories.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {

    @Mock CartService cartService;
    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock PaymentRepository paymentRepository;

    @InjectMocks CheckoutServiceImpl checkoutService;

    // ── Helper ────────────────────────────────────────────────────────
    private Cart cartWithOneItem(long productId, int quantity, BigDecimal price) {
        return Cart.builder()
            .userId(10L)
            .items(List.of(CartItem.builder()
                .productId(productId)
                .productName("Balon Pro")
                .quantity(quantity)
                .unitPrice(price)
                .build()))
            .build();
    }

    // ── Tests de checkout ─────────────────────────────────────────────

    @Test
    void checkout_shouldCreateOrderAndDiscountStock() {
        Cart cart = cartWithOneItem(7L, 2, new BigDecimal("100"));

        ProductEntity product = new ProductEntity();
        product.setId(7L);
        product.setName("Balon Pro");
        product.setStock(10);

        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1L);
        savedOrder.setUserId(10L);
        savedOrder.setTotal(new BigDecimal("200"));
        savedOrder.setStatus(OrderStatus.PENDING);

        when(cartService.getCart(10L)).thenReturn(cart);
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);

        var result = checkoutService.checkout(10L);

        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("200"), result.getTotal());
        assertEquals(8, product.getStock(), "El stock debería haberse descontado en 2 unidades");
        verify(cartService).clearCart(10L);
    }

    @Test
    void checkout_shouldThrowWhenCartIsEmpty() {
        Cart emptyCart = Cart.builder().userId(10L).items(List.of()).build();
        when(cartService.getCart(10L)).thenReturn(emptyCart);

        assertThrows(BusinessException.class, () -> checkoutService.checkout(10L));
    }

    @Test
    void checkout_shouldThrowWhenStockInsufficient() {
        Cart cart = cartWithOneItem(7L, 20, new BigDecimal("100")); // pide 20, solo hay 5

        ProductEntity product = new ProductEntity();
        product.setId(7L);
        product.setName("Balon Pro");
        product.setStock(5);

        when(cartService.getCart(10L)).thenReturn(cart);
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));

        assertThrows(BusinessException.class, () -> checkoutService.checkout(10L));
    }

    // ── Tests de pago ─────────────────────────────────────────────────

    @Test
    void processPayment_shouldMarkOrderAsPaidWhenAmountSufficient() {
        OrderEntity order = new OrderEntity();
        order.setId(3L);
        order.setTotal(new BigDecimal("150"));
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var payment = checkoutService.processPayment(3L, "CARD", new BigDecimal("150"));

        assertTrue(payment.isApproved(), "El pago debe estar aprobado");
        assertEquals(OrderStatus.PAID, order.getStatus(), "La orden debe pasar a PAID");
    }

    @Test
    void processPayment_shouldMarkOrderAsRejectedWhenAmountInsufficient() {
        OrderEntity order = new OrderEntity();
        order.setId(4L);
        order.setTotal(new BigDecimal("200"));
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var payment = checkoutService.processPayment(4L, "CARD", new BigDecimal("100")); // menos del total

        assertFalse(payment.isApproved(), "El pago debe estar rechazado");
        assertEquals(OrderStatus.REJECTED, order.getStatus(), "La orden debe pasar a REJECTED");
    }
}
