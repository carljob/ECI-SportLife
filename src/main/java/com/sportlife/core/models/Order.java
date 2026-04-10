package com.sportlife.core.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private Long userId;
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime createdAt;
}

