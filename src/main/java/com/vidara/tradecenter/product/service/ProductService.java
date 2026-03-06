package com.vidara.tradecenter.product.service;

import com.vidara.tradecenter.common.dto.PagedResponse;
import com.vidara.tradecenter.product.dto.request.ProductRequest;
import com.vidara.tradecenter.product.dto.response.ProductDetailResponse;
import com.vidara.tradecenter.product.dto.response.ProductResponse;

public interface ProductService {

    // Create a new product
    ProductDetailResponse create(ProductRequest request);

    // Get all products with pagination
    PagedResponse<ProductResponse> getAll(int page, int size, String sortBy, String sortDir);

    // Get product by ID (detail view)
    ProductDetailResponse getById(Long id);

    // Get product by slug (detail view)
    ProductDetailResponse getBySlug(String slug);

    // Update an existing product
    ProductDetailResponse update(Long id, ProductRequest request);

    // Delete a product
    void delete(Long id);

    // Filter products with dynamic criteria
    PagedResponse<ProductResponse> filter(Long categoryId, Long brandId,
                                          Double minPrice, Double maxPrice,
                                          String search,
                                          int page, int size,
                                          String sortBy, String sortDir);
}
