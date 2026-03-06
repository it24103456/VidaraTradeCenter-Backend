package com.vidara.tradecenter.product.mapper;

import com.vidara.tradecenter.product.dto.request.CategoryRequest;
import com.vidara.tradecenter.product.dto.response.CategoryResponse;
import com.vidara.tradecenter.product.model.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    // Map CategoryRequest DTO → Category entity
    public Category toCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        return category;
    }

    // Map Category entity → CategoryResponse DTO
    public CategoryResponse toCategoryResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setSlug(category.getSlug());
        response.setImageUrl(category.getImageUrl());

        // Set parent ID if parent exists
        if (category.getParentCategory() != null) {
            response.setParentId(category.getParentCategory().getId());
        }

        // Map subcategories recursively
        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            List<CategoryResponse> subcategoryResponses = category.getSubcategories().stream()
                    .map(this::toCategoryResponse)
                    .collect(Collectors.toList());
            response.setSubcategories(subcategoryResponses);
        }

        // Product count will be set by the service layer
        response.setProductCount(0);

        return response;
    }
}
