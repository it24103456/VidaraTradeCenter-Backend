package com.vidara.tradecenter.admin.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.common.dto.PagedResponse;
import com.vidara.tradecenter.product.dto.request.ProductRequest;
import com.vidara.tradecenter.product.dto.response.BrandResponse;
import com.vidara.tradecenter.product.dto.response.CategoryResponse;
import com.vidara.tradecenter.product.dto.response.ProductDetailResponse;
import com.vidara.tradecenter.product.dto.response.ProductResponse;
import com.vidara.tradecenter.product.service.BrandService;
import com.vidara.tradecenter.product.service.CategoryService;
import com.vidara.tradecenter.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public AdminProductController(ProductService productService,
                                  CategoryService categoryService,
                                  BrandService brandService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }


    // ==================== LIST PRODUCTS (PAGINATED) ====================

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String search) {

        PagedResponse<ProductResponse> products;

        if (categoryId != null || brandId != null || (search != null && !search.isEmpty())) {
            products = productService.filter(categoryId, brandId, null, null, search,
                    page, size, sortBy, sortDir);
        } else {
            products = productService.getAll(page, size, sortBy, sortDir);
        }

        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }


    // ==================== GET PRODUCT DETAIL ====================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable Long id) {
        ProductDetailResponse product = productService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }


    // ==================== GET FORM DATA (categories + brands) ====================

    @GetMapping("/form-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFormData() {
        Map<String, Object> formData = new HashMap<>();
        formData.put("categories", categoryService.getAll());
        formData.put("brands", brandService.getAll());
        return ResponseEntity.ok(ApiResponse.success("Form data retrieved successfully", formData));
    }


    // ==================== CREATE PRODUCT ====================

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        ProductDetailResponse product = productService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", product));
    }


    // ==================== UPDATE PRODUCT ====================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductDetailResponse product = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }


    // ==================== DELETE PRODUCT ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }
}
