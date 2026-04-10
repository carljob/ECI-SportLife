package com.sportlife.dtos.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {
    @NotNull
    private Long userId;
}

