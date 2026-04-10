package com.sportlife.core.services;

import com.sportlife.core.models.Product;
import java.util.List;

public interface ProductService {
    List<Product> listProducts();
    List<Product> searchByName(String name);
    List<Product> filterByCategory(String category);
    Product getDetail(Long id);
}

