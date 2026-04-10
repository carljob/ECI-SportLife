package com.sportlife.dtos.request;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProcessPaymentRequest {
    @NotNull
    private Long orderId;
    @NotBlank
    private String method;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}

