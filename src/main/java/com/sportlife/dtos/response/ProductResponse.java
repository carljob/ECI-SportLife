package com.sportlife.dtos.response;

import java.math.BigDecimal;
import lombok.*;

@Data @Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private boolean active;
}
