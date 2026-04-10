package com.sportlife.persistence.entities;

import java.math.BigDecimal;
import javax.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class OrderItemEmbeddable {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}

