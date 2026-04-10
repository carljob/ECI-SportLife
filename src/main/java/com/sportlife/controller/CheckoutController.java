package com.sportlife.controller;

import com.sportlife.core.models.Payment;
import com.sportlife.core.services.CheckoutService;
import com.sportlife.dtos.request.CheckoutRequest;
import com.sportlife.dtos.request.ProcessPaymentRequest;
import com.sportlife.dtos.response.OrderResponse;
import com.sportlife.dtos.response.PaymentResponse;
import com.sportlife.mappers.DtoMapper;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(DtoMapper.toResponse(checkoutService.checkout(request.getUserId())));
    }

    @PostMapping("/payment")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        Payment payment = checkoutService.processPayment(request.getOrderId(), request.getMethod(), request.getAmount());
        return ResponseEntity.ok(DtoMapper.toResponse(payment, payment.isApproved() ? "PAID" : "FAILED"));
    }
}

