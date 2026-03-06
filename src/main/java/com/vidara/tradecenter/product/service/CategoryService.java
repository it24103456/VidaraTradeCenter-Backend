package com.vidara.tradecenter.product.service;

import com.vidara.tradecenter.product.dto.request.CategoryRequest;
import com.vidara.tradecenter.product.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    // Create a new category
    CategoryResponse create(CategoryRequest request);

    // Get all categories
    List<CategoryResponse> getAll();

    // Get category by ID
    CategoryResponse getById(Long id);

    // Update an existing category
    CategoryResponse update(Long id, CategoryRequest request);

    // Delete a category
    void delete(Long id);
}
