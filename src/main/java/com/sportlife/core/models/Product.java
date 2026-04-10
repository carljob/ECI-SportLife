package com.sportlife.core.models;

import java.math.BigDecimal;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Product {
    // Campos del catálogo: identificador, nombre, descripción, categoría, precio, stock y estado.
    private Long id;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private Integer stock;
    // Estado activo/inactivo usado para publicar/ocultar productos del catálogo.
    @Builder.Default
    private boolean active = true;

    // Nota MVP: el requisito de imágenes no está modelado todavía en esta versión.
}
