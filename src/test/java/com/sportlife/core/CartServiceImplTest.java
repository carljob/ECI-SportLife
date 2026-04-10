package com.sportlife.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sportlife.core.models.Cart;
import com.sportlife.core.models.Product;
import com.sportlife.core.services.CartServiceImpl;
import com.sportlife.core.services.ProductService;
import com.sportlife.core.validators.StockValidator;
import com.sportlife.handlers.BusinessException;
import com.sportlife.persistence.entities.CartDocument;
import com.sportlife.persistence.repositories.CartRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock CartRepository cartRepository;
    @Mock ProductService productService;
    @Mock StockValidator stockValidator;

    @InjectMocks CartServiceImpl cartService;

    // ── Helper ────────────────────────────────────────────────────────
    private Product product(Long id, String name, int stock, int price) {
        return Product.builder()
            .id(id).name(name).stock(stock)
            .price(new BigDecimal(price)).active(true).build();
    }

    // ── Tests ─────────────────────────────────────────────────────────

    @Test
    void addProduct_shouldCreateCartAndAddItem() {
        when(productService.getDetail(1L)).thenReturn(product(1L, "Balon", 10, 100));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenAnswer(inv -> {
            CartDocument doc = inv.getArgument(0);
            doc.setId("cart-id-1");
            return doc;
        });

        Cart cart = cartService.addProduct(5L, 1L, 2);

        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
        assertEquals("Balon", cart.getItems().get(0).getProductName());
    }

    @Test
    void addProduct_shouldAccumulateQuantityWhenProductAlreadyInCart() {
        // Carrito pre-existente con 2 unidades del producto
        CartDocument existingDoc = new CartDocument();
        existingDoc.setId("cart-1");
        existingDoc.setUserId(5L);
        com.sportlife.persistence.entities.CartItemDocument item =
            new com.sportlife.persistence.entities.CartItemDocument();
        item.setProductId(1L);
        item.setProductName("Balon");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("100"));
        existingDoc.getItems().add(item);

        when(productService.getDetail(1L)).thenReturn(product(1L, "Balon", 10, 100));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(existingDoc));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cart cart = cartService.addProduct(5L, 1L, 3); // agrega 3 más

        assertEquals(5, cart.getItems().get(0).getQuantity(), "Debe acumular 2+3=5");
    }

    @Test
    void getCart_shouldReturnEmptyCartWhenNotExists() {
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());

        Cart cart = cartService.getCart(99L);

        assertNotNull(cart);
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void stockValidator_shouldThrowWhenQuantityZero() {
        StockValidator validator = new StockValidator();
        assertThrows(BusinessException.class, () -> validator.validate(10, 0));
    }

    @Test
    void stockValidator_shouldThrowWhenStockInsufficient() {
        StockValidator validator = new StockValidator();
        assertThrows(BusinessException.class, () -> validator.validate(5, 10));
    }

    @Test
    void stockValidator_shouldPassWhenStockSufficient() {
        StockValidator validator = new StockValidator();
        assertDoesNotThrow(() -> validator.validate(10, 5));
    }
}
