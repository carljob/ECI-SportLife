package com.sportlife.persistence.entities;

import java.math.BigDecimal;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    /** Estado del producto: true = activo, false = inactivo */
    @Column(nullable = false)
    private boolean active = true;
}
