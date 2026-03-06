package com.vidara.tradecenter.product.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.product.dto.request.BrandRequest;
import com.vidara.tradecenter.product.dto.response.BrandResponse;
import com.vidara.tradecenter.product.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }


    // CREATE BRAND (ADMIN only - secured in SecurityConfig)
    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
            @Valid @RequestBody BrandRequest request) {
        BrandResponse brandResponse = brandService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Brand created successfully", brandResponse));
    }


    // GET ALL BRANDS (public)
    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getAllBrands() {
        List<BrandResponse> brands = brandService.getAll();
        return ResponseEntity
                .ok(ApiResponse.success("Brands retrieved successfully", brands));
    }


    // GET BRAND BY ID (public)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandById(@PathVariable Long id) {
        BrandResponse brandResponse = brandService.getById(id);
        return ResponseEntity
                .ok(ApiResponse.success("Brand retrieved successfully", brandResponse));
    }


    // UPDATE BRAND (ADMIN only - secured in SecurityConfig)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request) {
        BrandResponse brandResponse = brandService.update(id, request);
        return ResponseEntity
                .ok(ApiResponse.success("Brand updated successfully", brandResponse));
    }


    // DELETE BRAND (ADMIN only - secured in SecurityConfig)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity
                .ok(ApiResponse.success("Brand deleted successfully"));
    }
}
