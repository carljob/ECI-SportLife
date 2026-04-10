package com.sportlife.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.sportlife.core.models.Cart;
import com.sportlife.core.models.CartItem;
import com.sportlife.core.models.OrderStatus;
import com.sportlife.core.services.CartService;
import com.sportlife.core.services.CheckoutServiceImpl;
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

    @Mock
    private CartService cartService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    @Test
    void shouldCheckoutAndDiscountStock() {
        Cart cart = Cart.builder().userId(10L).items(List.of(CartItem.builder()
            .productId(7L)
            .productName("Balon")
            .quantity(2)
            .unitPrice(new BigDecimal("100"))
            .build())).build();

        ProductEntity product = new ProductEntity();
        product.setId(7L);
        product.setName("Balon");
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
        assertEquals(8, product.getStock());
    }

    @Test
    void shouldProcessPaymentAndMarkOrderAsPaid() {
        OrderEntity order = new OrderEntity();
        order.setId(3L);
        order.setTotal(new BigDecimal("150"));
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var payment = checkoutService.processPayment(3L, "CARD", new BigDecimal("150"));

        assertEquals(3L, payment.getOrderId());
        assertTrue(payment.isApproved());
        assertEquals(OrderStatus.PAID, order.getStatus());
    }
}

