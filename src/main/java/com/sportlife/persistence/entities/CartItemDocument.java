package com.sportlife.persistence.entities;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CartItemDocument {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}

