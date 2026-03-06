package com.vidara.tradecenter.product.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.common.dto.PagedResponse;
import com.vidara.tradecenter.product.dto.request.ProductRequest;
import com.vidara.tradecenter.product.dto.response.ProductDetailResponse;
import com.vidara.tradecenter.product.dto.response.ProductResponse;
import com.vidara.tradecenter.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    // CREATE PRODUCT (ADMIN only - secured in SecurityConfig)
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        ProductDetailResponse productResponse = productService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", productResponse));
    }


    // GET ALL PRODUCTS (public, with filter params)
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Always use filter() — ensures only ACTIVE products are shown publicly
        PagedResponse<ProductResponse> products = productService.filter(
                categoryId, brandId, minPrice, maxPrice, search,
                page, size, sortBy, sortDir);
        return ResponseEntity
                .ok(ApiResponse.success("Products retrieved successfully", products));
    }


    // GET PRODUCT BY ID (public)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable Long id) {
        ProductDetailResponse productResponse = productService.getById(id);
        return ResponseEntity
                .ok(ApiResponse.success("Product retrieved successfully", productResponse));
    }


    // GET PRODUCT BY SLUG (public)
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductBySlug(@PathVariable String slug) {
        ProductDetailResponse productResponse = productService.getBySlug(slug);
        return ResponseEntity
                .ok(ApiResponse.success("Product retrieved successfully", productResponse));
    }


    // UPDATE PRODUCT (ADMIN only - secured in SecurityConfig)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductDetailResponse productResponse = productService.update(id, request);
        return ResponseEntity
                .ok(ApiResponse.success("Product updated successfully", productResponse));
    }


    // DELETE PRODUCT (ADMIN only - secured in SecurityConfig)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity
                .ok(ApiResponse.success("Product deleted successfully"));
    }
}
