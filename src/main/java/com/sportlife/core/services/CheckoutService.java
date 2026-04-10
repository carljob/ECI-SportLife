package com.sportlife.core.services;

import com.sportlife.core.models.Order;
import com.sportlife.core.models.Payment;

public interface CheckoutService {
    Order checkout(Long userId);
    Payment processPayment(Long orderId, String method, java.math.BigDecimal amount);
}

