package com.sportlife.dtos.response;

import java.math.BigDecimal;
import lombok.*;

@Data @Builder
public class PaymentResponse {
    private Long orderId;
    private String paymentId;
    private String method;
    private BigDecimal amount;
    private boolean approved;
    private String status;
}
