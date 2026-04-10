package com.sportlife.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private Long orderId;
    private String paymentId;
    private boolean approved;
    private String status;
}

