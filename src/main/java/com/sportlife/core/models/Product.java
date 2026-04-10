package com.sportlife.core.models;

import java.math.BigDecimal;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private Integer stock;
    @Builder.Default
    private boolean active = true;
}
