package com.vidara.tradecenter.product.service;

import com.vidara.tradecenter.product.dto.request.BrandRequest;
import com.vidara.tradecenter.product.dto.response.BrandResponse;

import java.util.List;

public interface BrandService {

    // Create a new brand
    BrandResponse create(BrandRequest request);

    // Get all brands
    List<BrandResponse> getAll();

    // Get brand by ID
    BrandResponse getById(Long id);

    // Update an existing brand
    BrandResponse update(Long id, BrandRequest request);

    // Delete a brand
    void delete(Long id);
}
