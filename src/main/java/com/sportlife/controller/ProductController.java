package com.sportlife.controller;

import com.sportlife.core.services.ProductService;
import com.sportlife.dtos.response.ProductResponse;
import com.sportlife.mappers.DtoMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** Requisito del enunciado: listar productos disponibles del catálogo. */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> listProducts() {
        return ResponseEntity.ok(productService.listProducts().stream().map(DtoMapper::toResponse).collect(Collectors.toList()));
    }

    /** Requisito del enunciado: buscar productos por nombre. */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchByName(name).stream().map(DtoMapper::toResponse).collect(Collectors.toList()));
    }

    /** Requisito del enunciado: filtrar productos por categoría. */
    @GetMapping("/category")
    public ResponseEntity<List<ProductResponse>> filterByCategory(@RequestParam String category) {
        return ResponseEntity.ok(productService.filterByCategory(category).stream().map(DtoMapper::toResponse).collect(Collectors.toList()));
    }

    /** Requisito del enunciado: ver detalle completo de un producto específico. */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(DtoMapper.toResponse(productService.getDetail(id)));
    }
}


