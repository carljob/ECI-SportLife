package com.sportlife.core.services;

import com.sportlife.core.models.Product;
import com.sportlife.handlers.ResourceNotFoundException;
import com.sportlife.persistence.mappers.ProductPersistenceMapper;
import com.sportlife.persistence.repositories.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> listProducts() {
        return productRepository.findAll().stream().map(ProductPersistenceMapper::toModel).collect(Collectors.toList());
    }

    @Override
    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
            .map(ProductPersistenceMapper::toModel).collect(Collectors.toList());
    }

    @Override
    public List<Product> filterByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category).stream()
            .map(ProductPersistenceMapper::toModel).collect(Collectors.toList());
    }

    @Override
    public Product getDetail(Long id) {
        return productRepository.findById(id)
            .map(ProductPersistenceMapper::toModel)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }
}

