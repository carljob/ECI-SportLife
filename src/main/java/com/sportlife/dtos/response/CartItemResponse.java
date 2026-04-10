package com.sportlife.dtos.response;

import java.math.BigDecimal;
import lombok.*;

@Data @Builder
public class CartItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
