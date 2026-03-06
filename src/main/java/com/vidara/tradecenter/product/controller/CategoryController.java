package com.vidara.tradecenter.product.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.product.dto.request.CategoryRequest;
import com.vidara.tradecenter.product.dto.response.CategoryResponse;
import com.vidara.tradecenter.product.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    // CREATE CATEGORY (ADMIN only - secured in SecurityConfig)
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse categoryResponse = categoryService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", categoryResponse));
    }


    // GET ALL CATEGORIES (public)
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAll();
        return ResponseEntity
                .ok(ApiResponse.success("Categories retrieved successfully", categories));
    }


    // GET CATEGORY BY ID (public)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        CategoryResponse categoryResponse = categoryService.getById(id);
        return ResponseEntity
                .ok(ApiResponse.success("Category retrieved successfully", categoryResponse));
    }


    // UPDATE CATEGORY (ADMIN only - secured in SecurityConfig)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse categoryResponse = categoryService.update(id, request);
        return ResponseEntity
                .ok(ApiResponse.success("Category updated successfully", categoryResponse));
    }


    // DELETE CATEGORY (ADMIN only - secured in SecurityConfig)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity
                .ok(ApiResponse.success("Category deleted successfully"));
    }
}
