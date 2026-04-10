package com.sportlife.persistence.entities;

import java.math.BigDecimal;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "payments")
public class PaymentDocument {
    @Id
    private String id;
    private Long orderId;
    private String method;
    private BigDecimal amount;
    private boolean approved;
}

