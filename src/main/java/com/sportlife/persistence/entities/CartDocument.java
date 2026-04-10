package com.sportlife.persistence.entities;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "carts")
public class CartDocument {
    @Id
    private String id;
    private Long userId;
    private List<CartItemDocument> items = new ArrayList<>();
}

